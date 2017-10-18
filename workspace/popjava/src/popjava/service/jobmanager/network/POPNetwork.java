package popjava.service.jobmanager.network;

import java.util.Collection;
import java.util.Collections;
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
	private final String friendlyName;
	private final Map<POPConnector.Name, POPConnector> connectors;
	private final POPJavaJobManager jobManager;

	/**
	 * Create a new network from 0, it will generate a new UUID for it.
	 * 
	 * @param frendlyName A local name for the network.
	 * @param jobManager The job manager it is assigned to.
	 */
	public POPNetwork(String frendlyName, POPJavaJobManager jobManager) {
		this(Util.generateUUID(), frendlyName, jobManager);
	}
	
	/**
	 * Initialize a POP Network from a previously existing UUID.
	 * 
	 * @param uuid The previously generated UUID.
	 * @param friendlyName 
	 * @param jobManager 
	 */
	public POPNetwork(String uuid, String friendlyName, POPJavaJobManager jobManager) {
		this.uuid = uuid;
		this.friendlyName = friendlyName;
		this.connectors = new HashMap<>();
		this.jobManager = jobManager;
	}

	/**
	 * The friendly name of the network, can change.
	 * 
	 * @return 
	 */
	public String getFriendlyName() {
		return friendlyName;
	}

	/**
	 * The unique identifier across nodes of this network.
	 * 
	 * @return 
	 */
	public String getUUID() {
		return uuid;
	}

	/**
	 * All the connectors in this network
	 * 
	 * @return 
	 */
	public POPConnector[] getConnectors() {
		Collection<POPConnector> conns = connectors.values();
		return conns.toArray(new POPConnector[conns.size()]);
	}
	
	/**
	 * Get an already casted connector from its string.
	 * Warning: Responsibility on the user to use the right return.
	 * 
	 * Use ``POPConnector conector = network.getConnector(...)`` if the return is unknown.
	 * 
	 * @param <T> How to cast the connector
	 * @param connector Name of the connector
	 * @return 
	 */
	@SuppressWarnings("unchecked")
	public <T extends POPConnector> T getConnector(POPConnector.Name connector) {
		return (T) connectors.get(connector);
	}

	/**
	 * How many nodes are present in this node.
	 * 
	 * @return 
	 */
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
	 * @param connectorName Which connector we are using
	 * @return An immutable set we can loop through
	 */
	public List<POPNetworkNode> getMembers(POPConnector.Name connectorName) {
		POPConnector connector = connectors.get(connectorName);
		if (connector == null) {
			return Collections.EMPTY_LIST;
		}
		
		return connectors.get(connectorName).getNodes();
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
		POPConnector connector = connectors.get(node.getConnectorName());
		if (connector == null) {
			connector = POPConnectorFactory.makeConnector(node.getConnectorName());
			connector.setJobManager(jobManager);
			connector.setNetwork(this);
			connectors.put(connector.getName(), connector);
		}
		return connector.add(node);
	}

	/**
	 * Remove a node from this Network
	 *
	 * @param node The node
	 * @return true if the Node is remove, false otherwise
	 */
	public boolean remove(POPNetworkNode node) {
		// connector
		POPConnector connector = connectors.get(node.getConnectorName());
		if (connector == null) {
			return false;
		}
		boolean status = connector.remove(node);
		if (connector.isEmpty()) {
			connectors.remove(connector.getName());
		}
		return status;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 89 * hash + Objects.hashCode(this.friendlyName);
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
		if (!Objects.equals(this.uuid, other.uuid)) {
			return false;
		}
		if (!Objects.equals(this.jobManager, other.jobManager)) {
			return false;
		}
		return true;
	}

}
