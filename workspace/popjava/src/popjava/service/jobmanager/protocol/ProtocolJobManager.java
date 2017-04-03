package popjava.service.jobmanager.protocol;

import java.util.List;
import popjava.PopJava;
import popjava.base.POPErrorCode;
import popjava.base.POPException;
import popjava.baseobject.ObjectDescription;
import popjava.baseobject.POPAccessPoint;
import popjava.dataswaper.POPFloat;
import popjava.dataswaper.POPString;
import popjava.interfacebase.Interface;
import popjava.service.jobmanager.POPJavaJobManager;
import popjava.service.jobmanager.Resource;
import popjava.service.jobmanager.network.NetworkNode;
import popjava.service.jobmanager.network.NodeJobManager;
import popjava.service.jobmanager.search.NodeRequest;
import popjava.util.Configuration;
import popjava.util.LogWriter;

/**
 *
 * @author Davide Mazzoleni
 */
public class ProtocolJobManager extends CreateObjectProtocolBase {
	
	@Override
	public int createObject(POPAccessPoint localservice, String objname, ObjectDescription od, 
			int howmany, POPAccessPoint[] objcontacts, int howmany2, POPAccessPoint[] remotejobcontacts) {
		// check local resource
		Resource currAva = jobManager.getAvailableResources();
		// od request
		Resource resourceReq = new Resource(od.getPowerReq(), od.getMemoryReq(), od.getBandwidthReq());
		currAva.subtract(resourceReq);
		
		// check if we have enough resources locally
		if (currAva.getFlops() > 0 && currAva.getMemory() > 0 && currAva.getBandwidth() > 0) {
			POPFloat fitness = new POPFloat();
			int[] resIDs = new int[howmany];
			for (int i = 0; i < howmany; i++)
				resIDs[i] = jobManager.reserve(od, fitness, "", "");
			POPString pobjname = new POPString(objname);
			return jobManager.execObj(pobjname, howmany, resIDs, localservice.toString(), objcontacts);
		}
		
		// use search node to find a suitable node
		NodeRequest request = new NodeRequest();
		// setup request
		// TODO setup request

		String appId = "", reqId = "";

		// send request
		List<POPAccessPoint> remoteJobM = jobManager.launchDiscovery(request, Configuration.ALLOC_TIMEOUT);
		POPAccessPoint[] chosenRemoteJobM = new POPAccessPoint[howmany];
		if (remoteJobM.isEmpty()) {
			throw new POPException(POPErrorCode.ALLOCATION_EXCEPTION, objname);
		}

		int[] resIDs = new int[howmany];
		// make requests
		for (int jobIdx = 0, jmIdx = 0, failed = 0; jobIdx < howmany; jobIdx++, jmIdx = (jmIdx + 1) % remoteJobM.size()) {
			// connect to remote JM
			POPJavaJobManager jm = PopJava.newActive(POPJavaJobManager.class, remoteJobM.get(jmIdx));
			POPFloat fitness = new POPFloat();
			resIDs[jobIdx] = jm.reserve(od, fitness, appId, reqId);

			// failed requests
			if (resIDs[jobIdx] == 0) {
				LogWriter.writeDebugInfo(String.format("[JM] Usable to reserve on %s", jm.getAccessPoint()));
				// failed creation
				failed++;
				jobIdx--;
				if (failed == remoteJobM.size()) {
					// cancel previous registrations on remote jms
					for (int k = 0; k < jobIdx; k++) {
						jm = PopJava.newActive(POPJavaJobManager.class, chosenRemoteJobM[k]);
						jm.cancelReservation(new int[] { resIDs[k] }, 1);
					}
					return 1;
				}
			}
			// successful reservation
			else {
				chosenRemoteJobM[jobIdx] = jm.getAccessPoint();
			}
		}
		
		// execute objects
		int started = 0;
		for (int i = 0; i < howmany; i++) {
			if (!chosenRemoteJobM[i].isEmpty()) {
				POPJavaJobManager jm = PopJava.newActive(POPJavaJobManager.class, chosenRemoteJobM[i]);
				try {
					// execution
					POPString pobjname = new POPString(objname);
					int[] localRIDs = { resIDs[i] };
					POPAccessPoint[] localObjContact = { objcontacts[i] };
					int status = jm.execObj(pobjname, 1, localRIDs, localservice.toString(), localObjContact);
					// forse set return
					objcontacts[i] = localObjContact[0];
					started++;
					// failed, free resources
					if (status != 0) {
						started--;
						LogWriter.writeDebugInfo("[JM] execution failed");
						jm.cancelReservation(localRIDs, 1);
						return POPErrorCode.OBJECT_NO_RESOURCE;
					}
				}
				// cancel remote registration
				catch (Exception e) {
					jm.cancelReservation(new int[] { resIDs[i] }, 1);
					return POPErrorCode.POP_JOBSERVICE_FAIL;
				}
			}
		}
		
		LogWriter.writeDebugInfo(String.format("Object count=%d, require=%d", started, howmany));
		// created all objects
		if (started >= howmany)
			return 0;
		
		// failed to start all objects, kill already started objects
		for (int i = 0; i < started; i++) {
			try {
				Interface obj = new Interface(objcontacts[i]);
				obj.kill();
				POPJavaJobManager jm = PopJava.newActive(POPJavaJobManager.class, chosenRemoteJobM[i]);
			} catch (POPException e) {
				LogWriter.writeDebugInfo(String.format("Exception while killing objects: %s", e.getMessage()));
			}
		}
		
		return POPErrorCode.POP_EXEC_FAIL;
	}

	@Override
	public boolean isValidNode(NetworkNode node) {
		return node instanceof NodeJobManager && ((NodeJobManager)node).isInitialized();
	}

	
}
