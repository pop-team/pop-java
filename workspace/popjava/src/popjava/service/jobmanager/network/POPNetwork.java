package popjava.service.jobmanager.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import popjava.service.jobmanager.POPJavaJobManager;
import popjava.service.jobmanager.connector.POPConnector;
import popjava.service.jobmanager.connector.POPConnectorFactory;
import popjava.util.Util;

/**
 * Describe a POP Network, made of POP Connector with relative members to of a POP COnnector.
 * 
 * @author Davide Mazzoleni
 */
public class POPNetwork {

	private final String uuid;
	private final String frendlyName;
	private final Map<String, POPConnector> connectors;
	private final POPJavaJobManager jobManager;

	public POPNetwork(String frendlyName, POPJavaJobManager jobManager) {
		this(Util.generateUUID(), frendlyName, jobManager);
	}
	
	public POPNetwork(String uuid, String frendlyName, POPJavaJobManager jobManager) {
		this.uuid = uuid;
		this.frendlyName = frendlyName;
		this.connectors = new HashMap<>();
		this.jobManager = jobManager;
	}

	public String getFrendlyName() {
		return frendlyName;
	}

	public String getUUID() {
		return uuid;
	}

	public POPConnector[] getConnectors() {
		Collection<POPConnector> conns = connectors.values();
		return conns.toArray(new POPConnector[conns.size()]);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends POPConnector> T getConnector(String connector) {
		return (T) connectors.get(connector);
	}

	public int size() {
		int size = 0;
		for (POPConnector l : getConnectors()) {
			size += l.size();
		}
		return size;
	}

	/**
	 * Get NetworkNode already casted to correct type
	 *
	 * @param <T> The type we want the set of NetworkNode
	 * @param connector Which connector we are using
	 * @return An immutable set we can loop through
	 */
	@SuppressWarnings("unchecked")
	public <T extends POPNetworkNode> List<T> getMembers(Class<? extends POPConnector> connector) {
		List<POPNetworkNode> nodes = members.get(connector);
		if (nodes == null) {
			return new ArrayList<>();
		}
		return (List<T>) new ArrayList(nodes);
	}

	/**
	 * Add a NetworkNode to this network
	 *
	 * @param node The node
	 * @return true if the Node is added, false if not or not compatible
	 */
	@SuppressWarnings("unchecked")
	public boolean add(POPNetworkNode node) {
		// connector
		POPConnector connector = connectors.get(node.getConnectorClass());
		if (connector == null) {
			connector = POPConnectorFactory.makeConnector(node.getConnectorName());
			connector.setJobManager(jobManager);
			connector.setNetwork(this);
			connectors.put(node.getConnectorClass(), connector);
		}
		// members
		List<POPNetworkNode> connectorMembers = members.get(connector.getClass());
		if (connectorMembers == null) {
			connectorMembers = new ArrayList<>();
			members.put(node.getConnectorClass(), connectorMembers);
		}
		if (connectorMembers.contains(node)) {
			return true;
		}
		if (connector.isValidNode(node)) {
			return connectorMembers.add(node);
		}
		return false;
	}

	/**
	 * Remove a node from this Network
	 *
	 * @param o The node
	 * @return true if the Node is remove, false otherwise
	 */
	public boolean remove(POPNetworkNode o) {
		// connector
		POPConnector connector = connectors.get(o.getConnectorClass());
		if (connector == null) {
			return false;
		}
		// members
		List<POPNetworkNode> mem = members.get(connector.getClass());
		if (mem == null) {
			return false;
		}
		boolean status = mem.remove(o);
		if (mem.isEmpty()) {
			members.remove(connector.getClass());
			connectors.remove(connector.getClass());
		}
		return status;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 89 * hash + Objects.hashCode(this.frendlyName);
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
		if (!Objects.equals(this.frendlyName, other.frendlyName)) {
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
