package popjava.service.jobmanager.connector;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import popjava.base.POPErrorCode;
import popjava.base.POPException;
import popjava.baseobject.ObjectDescription;
import popjava.baseobject.POPAccessPoint;
import popjava.util.ssl.SSLUtils;
import popjava.interfacebase.Interface;
import popjava.service.jobmanager.Resource;
import popjava.service.jobmanager.network.NodeTFC;
import popjava.service.jobmanager.network.POPNetworkNode;
import popjava.service.jobmanager.search.SNExploration;
import popjava.service.jobmanager.search.SNNodesInfo;
import popjava.service.jobmanager.search.SNRequest;
import popjava.service.jobmanager.search.SNResponse;
import popjava.service.jobmanager.search.SNWayback;
import popjava.service.jobmanager.tfc.TFCResource;
import popjava.system.POPSystem;
import popjava.util.Configuration;
import popjava.util.LogWriter;
import popjava.util.Util;

/**
 *
 * @author Dosky
 */
public class POPConnectorTFC extends POPConnectorBase implements POPConnectorSearchNodeInterface {
	
	public static final String IDENTITY = "tfc";
	
	private static final String TFC_REQ_OBJECT = "_tfc_object";
	private static final String TFC_RES_ACCESS_POINT = "_tfc_access_point";
	
	private final Map<String, List<TFCResource>> tfcObjects = new HashMap<>();

	@Override
	public int createObject(POPAccessPoint localservice, String objname, ObjectDescription od, int howmany, POPAccessPoint[] objcontacts, int howmany2, POPAccessPoint[] remotejobcontacts) {
		// use search node to find a suitable node
		SNRequest request = new SNRequest(Util.generateUUID(), new Resource(), new Resource(), network.getName(), IDENTITY, null);
		// setup request
		// distance between nodes
		if (od.getSearchMaxDepth() > 0) {
			request.setHopLimit(od.getSearchMaxDepth());
		}
		// size? not implemented
		if (od.getSearchMaxSize() > 0) {
			;
		}
		int timeout = Configuration.TFC_SEARCH_TIMEOUT;
		if (od.getSearchWaitTime() >= 0) {
			timeout = od.getSearchWaitTime();
		}
		if (!od.getPlatform().isEmpty()) {
			request.setOS(od.getPlatform());
		}
		String appId = "", reqId = "";
		
		// TFC specific requests parameters
		request.setValue(TFC_REQ_OBJECT, objname);

		// send request
		SNNodesInfo remoteJobMngs = jobManager.launchDiscovery(request, timeout);
		if (remoteJobMngs.isEmpty()) {
			throw new POPException(POPErrorCode.ALLOCATION_EXCEPTION, "No answer from the network while looking for TFC resource " + objname);
		}
		
		// set return access points
		for (int i = 0; i < howmany && i < remoteJobMngs.size(); i++) {
			if (objcontacts[i] != null) {
				String objAP = remoteJobMngs.get(i).getValue(TFC_RES_ACCESS_POINT);
				if (objAP != null && !objAP.isEmpty()) {
					objcontacts[i].setAccessString(objAP);
				}
			}
		}
		
		return 0;
	}

	@Override
	public boolean isValidNode(POPNetworkNode node) {
		return node instanceof NodeTFC && ((NodeTFC)node).isInitialized();
	}

	public void unregisterObject(TFCResource resource) {
		// load list of TFC resources
		List<TFCResource> localTFCObjects = tfcObjects.get(resource.getObjectName());
		if (localTFCObjects == null) {
			return;
		}
		
		// find resource in list
		TFCResource liveResource = null;
		for (TFCResource localTFCObject : localTFCObjects) {
			if (localTFCObject.equals(resource)) {
				liveResource = localTFCObject;
				break;
			}
		}
		if (liveResource == null) {
			return;
		}
		
		// check if secret match
		if (!liveResource.getSecret().equals(resource.getSecret())) {
			return;
		}
		
		LogWriter.writeDebugInfo("[TFC] unregistering " + liveResource);
		
		// finally remove the resource
		localTFCObjects.remove(liveResource);
	}

	public boolean registerObject(TFCResource resource) {
		// load list of TFC resources
		List<TFCResource> localTFCObjects = tfcObjects.get(resource.getObjectName());
		if (localTFCObjects == null) {
			localTFCObjects = new LinkedList<>();
			tfcObjects.put(resource.getObjectName(), localTFCObjects);
		}
		
		// look if it already exists
		for (TFCResource localTFCObject : localTFCObjects) {
			if (localTFCObject.equals(resource)) {
				return true;
			}
		}
		
		LogWriter.writeDebugInfo("[TFC] registering " + resource);
		
		// add to list
		return localTFCObjects.add(resource);
	}

	@Override
	public void askResourcesDiscoveryAction(SNRequest request, POPAccessPoint sender, SNExploration oldExplorationList) {
		// we get the name of tfc object requested
		String tfcObject = request.getValue(TFC_REQ_OBJECT);
		if (tfcObject == null) {
			return;
		}
		
		LogWriter.writeDebugInfo("[TFC] handling request");
		
		// we look in the network if we have the requested object
		List<TFCResource> requestObjects = tfcObjects.get(tfcObject);
		
		// we answer the origin jobManager with all the discovered objects
		if (requestObjects != null && requestObjects.size() > 0) {
			LogWriter.writeDebugInfo(String.format("[TFC] found %d object(s)", requestObjects.size()));
		
			for (Iterator<TFCResource> iterator = requestObjects.iterator(); iterator.hasNext();) {
				TFCResource tfcResource = iterator.next();
				try {
					// test if object is actually alive
					Interface aliveTest = new Interface(tfcResource.getAccessPoint());
					aliveTest.close();
				} catch(Exception e) {
					// failed to connect, dead object, remove from list
					iterator.remove();
					continue;
				}
				
				// send an answer to the origin
				SNNodesInfo.Node nodeinfo = new SNNodesInfo.Node(jobManager.getNodeId(), jobManager.getAccessPoint(), POPSystem.getPlatform(), new Resource());
				// add custom TFC parameter
				nodeinfo.setValue(TFC_RES_ACCESS_POINT, tfcResource.getAccessPoint().toString());
				SNResponse response = new SNResponse(request.getUID(), request.getExplorationList(), nodeinfo);

				// if we want to answer we save the certificate if there is any
				if (request.getPublicCertificate().length > 0) {
					SSLUtils.addCertToTempStore(request.getPublicCertificate());
				}

				// route response to the original JM
				jobManager.rerouteResponse(response, new SNWayback(request.getWayback()));
			}
		}
	}
}
