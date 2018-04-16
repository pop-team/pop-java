package ch.icosys.popjava.core.service.jobmanager.network;

import java.security.cert.Certificate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ch.icosys.popjava.core.PopJava;
import ch.icosys.popjava.core.base.POPErrorCode;
import ch.icosys.popjava.core.base.POPException;
import ch.icosys.popjava.core.base.POPObject;
import ch.icosys.popjava.core.baseobject.ObjectDescription;
import ch.icosys.popjava.core.baseobject.POPAccessPoint;
import ch.icosys.popjava.core.service.jobmanager.Resource;
import ch.icosys.popjava.core.service.jobmanager.search.SNExploration;
import ch.icosys.popjava.core.service.jobmanager.search.SNNodesInfo;
import ch.icosys.popjava.core.service.jobmanager.search.SNRequest;
import ch.icosys.popjava.core.service.jobmanager.search.SNResponse;
import ch.icosys.popjava.core.service.jobmanager.search.SNWayback;
import ch.icosys.popjava.core.service.jobmanager.tfc.TFCResource;
import ch.icosys.popjava.core.system.POPSystem;
import ch.icosys.popjava.core.util.Configuration;
import ch.icosys.popjava.core.util.LogWriter;
import ch.icosys.popjava.core.util.Util;
import ch.icosys.popjava.core.util.ssl.SSLUtils;

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
		if (od.getSearchHosts().length > 0) {
			request.setHosts(od.getSearchHosts());
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
					byte[] cert = remoteJobMngs.get(i).getCertificate();
					if (cert != null && cert.length > 0) {
						objcontacts[i].setFingerprint(SSLUtils.certificateFingerprint(cert));
						objcontacts[i].setX509certificate(cert);
					}
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
	
	private List<TFCResource> getAliveTFCResources(String tfcObject, byte[] cert) {
		List<TFCResource> resources = tfcObjects.get(tfcObject);
		if (resources == null) {
			return null;
		}
		
		// test liviness
		for (Iterator<TFCResource> iterator = resources.iterator(); iterator.hasNext();) {
			TFCResource tfcResource = iterator.next();
			try {
				// test if object is actually alive
				POPObject aliveTest = PopJava.connect(null, POPObject.class, network.getUUID(), tfcResource.getAccessPoint());
				// add certificate if provided
				if (cert != null && cert.length != 0) {
					aliveTest.PopRegisterFutureConnectorCertificate(cert);
				}
				aliveTest.exit();
			} catch(Exception e) {
				// failed to connect, dead object, remove from list
				iterator.remove();
				LogWriter.writeDebugInfo("[TFC] unavailable %s removed ", tfcResource);
			}
		}
		return Collections.unmodifiableList(resources);
	}
	
	public List<TFCResource> getObjects(String tfcObject, Certificate cert) {
		byte[] bytes = null;
		if (cert != null) {
			bytes = SSLUtils.certificateBytes(cert);
		}
		List<TFCResource> resources = getAliveTFCResources(tfcObject, bytes);
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
		
		List<TFCResource> resources = getAliveTFCResources(tfcObject, request.getPublicCertificate());
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
			SNResponse response = new SNResponse(request.getUID(), request.getNetworkUUID(), request.getExplorationList(), nodeinfo);

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
