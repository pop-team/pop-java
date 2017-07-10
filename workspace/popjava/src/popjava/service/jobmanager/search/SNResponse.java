package popjava.service.jobmanager.search;

import popjava.buffer.POPBuffer;
import popjava.dataswaper.IPOPBase;

/**
 * Response of a network request, create and send one for itself.
 *
 * @author Davide Mazzoleni
 */
public class SNResponse implements IPOPBase {

	private String uid;
	private SNExploration explorationList;
	private SNNodesInfo.Node nodeinfo;

	public SNResponse() {
	}

	public SNResponse(String uid, SNExploration explorationList, SNNodesInfo.Node nodeinfo) {
		this.uid = uid;
		this.explorationList = explorationList;
		this.nodeinfo = nodeinfo;
	}

	public String getUID() {
		return uid;
	}

	public SNNodesInfo.Node getResultNode() {
		return nodeinfo;
	}

	@Override
	public boolean serialize(POPBuffer buffer) {
		buffer.putString(uid);
		buffer.putValue(explorationList, SNExploration.class);
		buffer.putValue(nodeinfo, SNNodesInfo.Node.class);
		return true;
	}

	@Override
	public boolean deserialize(POPBuffer buffer) {
		uid = buffer.getString();
		explorationList = (SNExploration) buffer.getValue(SNExploration.class);
		nodeinfo = (SNNodesInfo.Node) buffer.getValue(SNNodesInfo.Node.class);
		return true;
	}
}
