package popjava.service.jobmanager.search;

import popjava.baseobject.POPAccessPoint;
import popjava.buffer.POPBuffer;
import popjava.dataswaper.IPOPBase;

/**
 * Response of a network request, create and send one for itself.
 * @author Davide Mazzoleni
 */
public class SNResponse implements IPOPBase {
	
	private String uid;
	private SNExploration explorationList;
	private POPAccessPoint originJobManager;
	private SNNodesInfo.Node nodeinfo;

	public SNResponse() {
	}

	public SNResponse(String uid, SNExploration explorationList, POPAccessPoint originJobManager, SNNodesInfo.Node nodeinfo) {
		this.uid = uid;
		this.explorationList = explorationList;
		this.originJobManager = originJobManager;
		this.nodeinfo = nodeinfo;
	}

	public String getUID() {
		return uid;
	}
	
	public POPAccessPoint getOriginJM() {
		return originJobManager;
	}

	public SNNodesInfo.Node getResultNode() {
		return nodeinfo;
	}

	@Override
	public boolean serialize(POPBuffer buffer) {
		buffer.putString(uid);
		buffer.putValue(explorationList, SNExploration.class);
		buffer.putValue(originJobManager, POPAccessPoint.class);
		buffer.putValue(nodeinfo, SNNodesInfo.Node.class);
		return true;
	}

	@Override
	public boolean deserialize(POPBuffer buffer) {
		uid = buffer.getString();
		explorationList = (SNExploration) buffer.getValue(SNExploration.class);
		originJobManager = (POPAccessPoint) buffer.getValue(POPAccessPoint.class);
		nodeinfo = (SNNodesInfo.Node) buffer.getValue(SNNodesInfo.Node.class);
		return true;
	}
}
