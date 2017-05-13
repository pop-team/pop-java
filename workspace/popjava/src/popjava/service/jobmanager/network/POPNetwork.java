package popjava.service.jobmanager.network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import popjava.service.jobmanager.POPJavaJobManager;
import popjava.service.jobmanager.connector.POPConnectorBase;
import popjava.service.jobmanager.connector.POPConnectorFactory;

/**
 *
 * @author Davide Mazzoleni
 */
public class POPNetwork {
	private final String name;
	private final Map<Class<? extends POPConnectorBase>, POPConnectorBase> connectors;
	private final Map<Class<? extends POPConnectorBase>, List<POPNetworkNode>> members;
	private final POPJavaJobManager jobManager;
	private final String[] otherParams;

	public POPNetwork(String name, POPJavaJobManager jobManager, String... other) {
		this.name = name;
		this.connectors = new HashMap<>();
		this.members = new HashMap<>();
		this.jobManager = jobManager;
		this.otherParams = other;
	}
	
	public String getName() {
		return name;
	}

	public POPConnectorBase[] getConnectors() {
		return connectors.values().toArray(new POPConnectorBase[0]);
	}
	
	public POPConnectorBase getConnector(Class connector){
		return connectors.get(connector);
	}

	public int size() {
		int size = 0;
		for (List<POPNetworkNode> l : members.values())
			size += l.size();
		return size;
	}

	/**
	 * Get NetworkNode already casted to correct type
	 * @param <T> The type we want the set of NetworkNode
	 * @param connector Which connector we are using
	 * @return An immutable set we can loop through
	 */
	@SuppressWarnings("unchecked")
	public<T extends POPNetworkNode> List<T> getMembers(Class<? extends POPConnectorBase> connector) {
		List<POPNetworkNode> nodes = members.get(connector);
		if (nodes == null)
			return new ArrayList<>();
		return (List<T>)Collections.unmodifiableList(nodes);
	}

	/**
	 * Add a NetworkNode to this network
	 * @param e The node
	 * @return true if the Node is added, false if not or not compatible
	 */
	@SuppressWarnings("unchecked")
	public boolean add(POPNetworkNode e) {
		// connector
		POPConnectorBase c = connectors.get(e.getConnectorClass());
		if (c == null) {
			c = POPConnectorFactory.makeConnector(e.getConnectorName());
			c.setJobManager(jobManager);
			c.setNetwork(this);
			connectors.put(e.getConnectorClass(), c);
		}
		// members
		List<POPNetworkNode> m = members.get(c.getClass());
		if (m == null) {
			m = new ArrayList<>();
			members.put(e.getConnectorClass(), m);
		}
		if (c.isValidNode(e))
			return m.add(e);
		return false;
	}

	/**
	 * Remove a node from this Network
	 * @param o The node
	 * @return true if the Node is remove, false otherwise
	 */
	public boolean remove(POPNetworkNode o) {
		// connector
		POPConnectorBase c = connectors.get(o.getConnectorClass());
		if (c == null)
			return false;
		// members
		List<POPNetworkNode> mem = members.get(c.getClass());
		if (mem == null)
			return false;
		return mem.remove(o);
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 89 * hash + Objects.hashCode(this.name);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final POPNetwork other = (POPNetwork) obj;
		if (!Objects.equals(this.name, other.name)) {
			return false;
		}
		if (!Objects.equals(this.members, other.members)) {
			return false;
		}
		if (!Objects.equals(this.jobManager, other.jobManager)) {
			return false;
		}
		return true;
	}
	
}
