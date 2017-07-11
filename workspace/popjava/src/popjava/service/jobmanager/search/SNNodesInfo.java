package popjava.service.jobmanager.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import popjava.baseobject.POPAccessPoint;
import popjava.buffer.POPBuffer;
import popjava.dataswaper.IPOPBase;
import popjava.service.jobmanager.Resource;

/**
 * Keep Node(s) references of remote network nodes who can accept a request.
 *
 * @author Davide Mazzolen
 */
public class SNNodesInfo implements IPOPBase {

	private final List<Node> nodes = new ArrayList<>();

	public boolean add(Node e) {
		return nodes.add(e);
	}

	public boolean remove(Node o) {
		return nodes.remove(o);
	}

	public void clear() {
		nodes.clear();
	}

	public Node get(int index) {
		return nodes.get(index);
	}

	public boolean isEmpty() {
		return nodes.isEmpty();
	}

	public int size() {
		return nodes.size();
	}

	/**
	 * The nodes found, the list in unmodifiable Use delegates methods if needed
	 *
	 * @return
	 */
	public List<Node> getNodes() {
		return Collections.unmodifiableList(nodes);
	}

	@Override
	public boolean serialize(POPBuffer buffer) {
		buffer.putInt(nodes.size());
		for (Node n : nodes) {
			n.serialize(buffer);
		}
		return true;
	}

	@Override
	public boolean deserialize(POPBuffer buffer) {
		int size = buffer.getInt();
		for (int i = 0; i < size; i++) {
			nodes.add((Node) buffer.getValue(Node.class));
		}
		return true;
	}

	/**
	 * Describe a JobManager
	 */
	public static class Node implements IPOPBase {

		private String nodeID;
		private POPAccessPoint jobManager;
		private String os;
		private Resource resources;
		private Map<String,String> customParams = new HashMap<>();

		public Node() {
		}

		public Node(String nodeID, POPAccessPoint jobManager, String os, Resource resources) {
			this.nodeID = nodeID;
			this.jobManager = jobManager;
			this.os = os;
			this.resources = resources;
		}

		public String getNodeID() {
			return nodeID;
		}

		public void setNodeID(String nodeID) {
			this.nodeID = nodeID;
		}

		public POPAccessPoint getJobManager() {
			return jobManager;
		}

		public void setJobManager(POPAccessPoint jobManager) {
			this.jobManager = jobManager;
		}

		public String getOs() {
			return os;
		}

		public void setOs(String os) {
			this.os = os;
		}

		public Resource getResources() {
			return resources;
		}

		public void setResources(Resource resources) {
			this.resources = resources;
		}
	
		public void setValue(String key, String value) {
			customParams.put(key, value);
		}

		public String getValue(String key) {
			return customParams.get(key);
		}

		@Override
		public boolean serialize(POPBuffer buffer) {
			buffer.putString(nodeID);
			buffer.putValue(jobManager, POPAccessPoint.class);
			buffer.putString(os);
			buffer.putValue(resources, Resource.class);
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
			nodeID = buffer.getString();
			jobManager = (POPAccessPoint) buffer.getValue(POPAccessPoint.class);
			os = buffer.getString();
			resources = (Resource) buffer.getValue(Resource.class);
			int mapSize = buffer.getInt();
			for (int i = 0; i < mapSize; i++) {
				customParams.put(buffer.getString(), buffer.getString());
			}
			return true;
		}
	}
}
