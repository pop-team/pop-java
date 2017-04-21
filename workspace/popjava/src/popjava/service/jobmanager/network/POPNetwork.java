package popjava.service.jobmanager.network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import popjava.service.jobmanager.POPJavaJobManager;
import popjava.service.jobmanager.protocol.POPProtocolBase;
import popjava.service.jobmanager.protocol.POPProtocolFactory;

/**
 *
 * @author Davide Mazzoleni
 */
public class POPNetwork {
	private final String name;
	private final POPProtocolBase protocol;
	private final List<POPNetworkNode> members;
	private final POPJavaJobManager jobManager;
	private final String[] otherParams;

	public POPNetwork(String name, POPProtocolBase protocol, POPJavaJobManager jobManager, String... other) {
		this.name = name;
		this.protocol = protocol;
		this.members = new ArrayList<>();
		this.jobManager = jobManager;
		this.otherParams = other;
		
		setupProtocol();
	}

	private void setupProtocol() {
		this.protocol.setNetwork(this);
		this.protocol.setJobManager(jobManager);
	}
	
	/**
	 * Create a network node for this network by supplying the right parameters
	 * If the parameters are wrong the node won't be able to be added to the network
	 * @param params
	 * @return 
	 */
	public POPNetworkNode makeNode(String... params) {
		return POPNetworkNodeFactory.makeNode(protocol.getClass(), params);
	}
	
	public POPNetwork(String name, String protocol, POPJavaJobManager jobManager, String... other) {
		this(name, POPProtocolFactory.makeProtocol(protocol), jobManager, other);
	}

	public String getName() {
		return name;
	}

	public POPProtocolBase getProtocol() {
		return protocol;
	}

	public int size() {
		return members.size();
	}

	/**
	 * Get NetworkNode already casted to correct type
	 * @param <T> The type we want the set of NetworkNode
	 * @return An immutable set we can loop through
	 */
	@SuppressWarnings("unchecked")
	public<T extends POPNetworkNode> List<T> getMembers() {
		return (List<T>)Collections.unmodifiableList(members);
	}

	/**
	 * Add a NetworkNode to this network
	 * @param e The node
	 * @return true if the Node is added, false if not or not compatible
	 */
	public boolean add(POPNetworkNode e) {
		if (protocol.isValidNode(e))
			return members.add(e);
		return false;
	}

	/**
	 * Remove a node from this Network
	 * @param o The node
	 * @return true if the Node is remove, false otherwise
	 */
	public boolean remove(POPNetworkNode o) {
		return members.remove(o);
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
