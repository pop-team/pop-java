package popjava.service.jobmanager.search;

import popjava.buffer.POPBuffer;
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

	private String network;

	private boolean endRequest = false;
	private int hops = Integer.MAX_VALUE;
	private int popAppId;

	public SNRequest() {
	}

	public SNRequest(String nodeId, Resource reqResource, Resource minResource, String network) {
		this.requestId = nodeId;
		this.os = POPSystem.getPlatform();
		this.minResource = minResource;
		this.reqResource = reqResource;
		this.explorationNodes = new SNExploration();
		this.wayback = new SNWayback();
		this.network = network;
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

	public String getNetworkName() {
		return network;
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

	@Override
	public boolean serialize(POPBuffer buffer) {
		buffer.putString(requestId);
		buffer.putString(os);
		buffer.putValue(minResource, Resource.class);
		buffer.putValue(reqResource, Resource.class);
		buffer.putValue(explorationNodes, SNExploration.class);
		buffer.putValue(wayback, SNWayback.class);
		buffer.putString(network);
		buffer.putBoolean(endRequest);
		buffer.putInt(hops);
		buffer.putInt(popAppId);
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
		network = buffer.getString();
		endRequest = buffer.getBoolean();
		hops = buffer.getInt();
		popAppId = buffer.getInt();
		return true;
	}

}
