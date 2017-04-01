package popjava.service.jobmanager.network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import popjava.service.jobmanager.POPJavaJobManager;
import popjava.service.jobmanager.protocol.CreateObjectProtocolBase;
import popjava.service.jobmanager.protocol.ProtocolFactory;

/**
 *
 * @author Davide Mazzoleni
 */
public class Network {
	private final String name;
	private final CreateObjectProtocolBase protocol;
	private final List<NetworkNode> members;
	private final POPJavaJobManager jobManager;
	private final String[] otherParams;

	public Network(String name, CreateObjectProtocolBase protocol, POPJavaJobManager jobManager, String... other) {
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
	public NetworkNode makeNode(String... params) {
		return NetworkNodeFactory.makeNode(protocol.getClass(), params);
	}
	
	public Network(String name, String protocol, POPJavaJobManager jobManager, String... other) {
		this(name, ProtocolFactory.makeProtocol(protocol), jobManager, other);
	}

	public String getName() {
		return name;
	}

	public CreateObjectProtocolBase getProtocol() {
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
	public<T extends NetworkNode> List<T> getMembers() {
		return (List<T>)Collections.unmodifiableList(members);
	}

	/**
	 * Add a NetworkNode to this network
	 * @param e The node
	 * @return true if the Node is added, false if not or not compatible
	 */
	public boolean add(NetworkNode e) {
		if (protocol.isValidNode(e))
			return members.add(e);
		return false;
	}

	/**
	 * Remove a node from this Network
	 * @param o The node
	 * @return true if the Node is remove, false otherwise
	 */
	public boolean remove(NetworkNode o) {
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
		final Network other = (Network) obj;
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
