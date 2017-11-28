package popjava.service.jobmanager.search;

import java.util.HashMap;
import java.util.Map;
import java.security.cert.Certificate;
import popjava.buffer.POPBuffer;
import popjava.util.ssl.SSLUtils;
import popjava.dataswaper.IPOPBase;
import popjava.service.jobmanager.Resource;
import popjava.system.POPSystem;

/**
 * A request for the Search Node, is also used to handle application death.
 *
 * @author Davide Mazzoleni
 */
public class SNRequest implements IPOPBase {

	private String requestId;
	private String os;
	private Resource minResource;
	private Resource reqResource;

	private SNExploration explorationNodes;
	private SNWayback wayback;

	private String networkUUID;
	private String connector;
	
	private boolean endRequest = false;
	private int hops = Integer.MAX_VALUE;
	private int popAppId;
	
	private byte[] publicCertificate = new byte[0];
	private byte[] appServiceCertificate = new byte[0];
	
	private Map<String,String> customParams = new HashMap<>();

	public SNRequest() {
	}
	
	// TODO get appservice certificate and fill appServiceCertificate
	public SNRequest(String nodeId, Resource reqResource, Resource minResource, String networkUUID, String connector, String appServiceFingerprint) {
		this.requestId = nodeId;
		this.os = POPSystem.getPlatform();
		this.minResource = minResource;
		this.reqResource = reqResource;
		this.explorationNodes = new SNExploration();
		this.wayback = new SNWayback();
		this.networkUUID = networkUUID;
		this.connector = connector;
		
		// this node certificate
		Certificate localPublicCertificate = SSLUtils.getCertificateFromAlias(networkUUID);
		if (localPublicCertificate != null) {
			publicCertificate = SSLUtils.certificateBytes(localPublicCertificate);
		}
		
		// app service certificate
		if (appServiceFingerprint != null) {
			Certificate appServiceCert = SSLUtils.getCertificate(appServiceFingerprint);
			appServiceCertificate = SSLUtils.certificateBytes(appServiceCert);
		}
	}

	public boolean isEndRequest() {
		return endRequest;
	}

	public void setAsEndRequest() {
		endRequest = true;
	}

	public String getUID() {
		return requestId;
	}

	public SNExploration getExplorationList() {
		return explorationNodes;
	}

	public String getNetworkUUID() {
		return networkUUID;
	}

	public void setHopLimit(int hops) {
		this.hops = hops;
	}

	public void decreaseHopLimit() {
		hops--;
	}

	public int getRemainingHops() {
		return hops;
	}

	public void setPOPAppId(int popAppId) {
		this.popAppId = popAppId;
	}

	public int getPOPAppId() {
		return popAppId;
	}

	public Resource getResourceNeeded() {
		return reqResource;
	}

	public Resource getMinResourceNeeded() {
		return minResource;
	}

	public SNWayback getWayback() {
		return wayback;
	}

	public void setOS(String platform) {
		this.os = platform;
	}

	public byte[] getPublicCertificate() {
		return publicCertificate;
	}

	public byte[] getAppServiceCertificate() {
		return appServiceCertificate;
	}

	public String getConnector() {
		return connector;
	}
	
	public void setValue(String key, String value) {
		customParams.put(key, value);
	}
	
	public String getValue(String key) {
		return customParams.get(key);
	}
	
	@Override
	public boolean serialize(POPBuffer buffer) {
		buffer.putString(requestId);
		buffer.putString(os);
		buffer.putValue(minResource, Resource.class);
		buffer.putValue(reqResource, Resource.class);
		buffer.putValue(explorationNodes, SNExploration.class);
		buffer.putValue(wayback, SNWayback.class);
		buffer.putString(networkUUID);
		buffer.putBoolean(endRequest);
		buffer.putInt(hops);
		buffer.putInt(popAppId);
		buffer.putByteArray(publicCertificate);
		buffer.putString(connector);
		buffer.putInt(customParams.size());
		for (Map.Entry<String, String> entry : customParams.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			buffer.putString(key);
			buffer.putString(value);
		}
		return true;
	}

	@Override
	public boolean deserialize(POPBuffer buffer) {
		requestId = buffer.getString();
		os = buffer.getString();
		minResource = (Resource) buffer.getValue(Resource.class);
		reqResource = (Resource) buffer.getValue(Resource.class);
		explorationNodes = (SNExploration) buffer.getValue(SNExploration.class);
		wayback = (SNWayback) buffer.getValue(SNWayback.class);
		networkUUID = buffer.getString();
		endRequest = buffer.getBoolean();
		hops = buffer.getInt();
		popAppId = buffer.getInt();
		int buffSize = buffer.getInt();
		publicCertificate = buffer.getByteArray(buffSize);
		connector = buffer.getString();
		int mapSize = buffer.getInt();
		for (int i = 0; i < mapSize; i++) {
			customParams.put(buffer.getString(), buffer.getString());
		}
		return true;
	}

}
