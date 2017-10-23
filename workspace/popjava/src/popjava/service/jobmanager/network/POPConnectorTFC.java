package popjava.service.jobmanager.network;

import java.util.Collections;
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
public class POPConnectorTFC extends POPConnector implements POPConnectorSearchNodeInterface {
	
	private static class DescriptorMethodImpl implements POPNetworkDescriptorMethod {
		@Override
		public POPConnector createConnector() {
			return new POPConnectorTFC();
		}

		@Override
		public POPNode createNode(List<String> params) {
			return new POPNodeTFC(params);
		}
	}
	static final POPNetworkDescriptor DESCRIPTOR = new POPNetworkDescriptor("tfc", new DescriptorMethodImpl());
	
	private final Configuration conf = Configuration.getInstance();
	
	private static final String TFC_REQ_OBJECT = "_tfc_object";
	private static final String TFC_RES_ACCESS_POINT = "_tfc_access_point";
	
	private final Map<String, List<TFCResource>> tfcObjects = new HashMap<>();

	public POPConnectorTFC() {
		super(DESCRIPTOR);
	}
	
	@Override
	public int createObject(POPAccessPoint localservice, String objname, ObjectDescription od, int howmany, POPAccessPoint[] objcontacts, int howmany2, POPAccessPoint[] remotejobcontacts) {
		// use search node to find a suitable node
		SNRequest request = new SNRequest(Util.generateUUID(), new Resource(), new Resource(), network.getUUID(), getDescriptor().getGlobalName(), null);
		// setup request
		// distance between nodes
		if (od.getSearchMaxDepth() > 0) {
			request.setHopLimit(od.getSearchMaxDepth());
		}
		// size? not implemented
		if (od.getSearchMaxSize() > 0) {

		}
		int timeout = conf.getTFCSearchTimeout();
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
	
	private List<TFCResource> getAliveTFCResources(String tfcObject) {
		List<TFCResource> resources = tfcObjects.get(tfcObject);
		if (resources == null) {
			return null;
		}
		
		// test liviness
		for (Iterator<TFCResource> iterator = resources.iterator(); iterator.hasNext();) {
			TFCResource tfcResource = iterator.next();
			try {
				// test if object is actually alive
				Interface aliveTest = new Interface(tfcResource.getAccessPoint());
				aliveTest.close();
			} catch(Exception e) {
				// failed to connect, dead object, remove from list
				iterator.remove();
				LogWriter.writeDebugInfo("[TFC] unavailable %s removed ", tfcResource);
			}
		}
		return Collections.unmodifiableList(resources);
	}
	
	public List<TFCResource> getObjects(String tfcObject) {
		List<TFCResource> resources = getAliveTFCResources(tfcObject);
		if (resources == null) {
			return null;
		}
		return resources;
	}

	@Override
	public void askResourcesDiscoveryAction(SNRequest request, POPAccessPoint sender, SNExploration oldExplorationList) {
		// we get the name of tfc object requested
		String tfcObject = request.getValue(TFC_REQ_OBJECT);
		if (tfcObject == null) {
			return;
		}
		
		LogWriter.writeDebugInfo("[TFC] handling;%s;%s", request.getUID(), tfcObject);
		
		List<TFCResource> resources = getAliveTFCResources(tfcObject);
		if (resources == null) {
			LogWriter.writeDebugInfo("[TFC] no resource found for %s", tfcObject);
			return;
		}
		LogWriter.writeDebugInfo("[TFC] found %d object(s)", resources.size());

		for (TFCResource tfcResource : resources) {
			// send an answer to the origin
			SNNodesInfo.Node nodeinfo = new SNNodesInfo.Node(jobManager.getNodeId(), jobManager.getAccessPoint(), POPSystem.getPlatform(), new Resource());
			// add custom TFC parameter
			String resourceString = tfcResource.getAccessPoint().toString();
			nodeinfo.setValue(TFC_RES_ACCESS_POINT, resourceString);
			SNResponse response = new SNResponse(request.getUID(), request.getExplorationList(), nodeinfo);

			// if we want to answer we save the certificate if there is any
			if (request.getPublicCertificate().length > 0) {
				SSLUtils.addCertToTempStore(request.getPublicCertificate());
			}

			LogWriter.writeDebugInfo("[TFC] aswering request %s of %s with %s.", request.getUID(), tfcObject, resourceString);
			// route response to the original JM
			jobManager.rerouteResponse(response, new SNWayback(request.getWayback()));				
		}
	}

	@Override
	public boolean broadcastPresence() {
		return false;
	}
}
