package popjava.service.jobmanager.connector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import popjava.baseobject.ObjectDescription;
import popjava.baseobject.POPAccessPoint;
import popjava.service.jobmanager.POPJavaJobManager;
import popjava.service.jobmanager.network.POPNetwork;
import popjava.service.jobmanager.network.POPNetworkNode;

/**
 *
 * @author Davide Mazzoleni
 */
public abstract class POPConnector {
	
	public enum Name {
		JOBMANAGER("jobmanager"),
		TFC("tfc"),
		DIRECT("direct");
		
		private final String globalName;

		private Name(String globalName) {
			this.globalName = globalName;
		}

		public String getGlobalName() {
			return globalName;
		}
		
		public static Name from(String global) {
			for (Name name : values()) {
				if (name.getGlobalName().toLowerCase().equals(global.toLowerCase())) {
					return name;
				}
			}
			throw new IllegalArgumentException("unknown enum for enum");
		}
	}

	protected POPNetwork network;
	protected POPJavaJobManager jobManager;
	
	protected final Name name;
	protected final List<POPNetworkNode> nodes = new ArrayList<>();

	/**
	 * The constructor define the name of the connector.
	 * 
	 * @param name 
	 */
	public POPConnector(Name name) {
		this.name = name;
	}
	
	/**
	 * Protocol specific createObject
	 *
	 * @see POPJavaJobManager
	 * @param localservice
	 * @param objname
	 * @param od
	 * @param howmany
	 * @param objcontacts
	 * @param howmany2
	 * @param remotejobcontacts
	 * @return
	 */
	public abstract int createObject(POPAccessPoint localservice,
			String objname,
			ObjectDescription od,
			int howmany, POPAccessPoint[] objcontacts,
			int howmany2, POPAccessPoint[] remotejobcontacts);

	/**
	 * The enum identifying this class.
	 * 
	 * @return 
	 */
	public Name getName() {
		return name;
	}

	/**
	 * Is the connector empty or not.
	 * 
	 * @return 
	 */
	public boolean isEmpty() {
		return nodes.isEmpty();
	}	

	/**
	 * Add a new network node to this connector
	 *
	 * @param node The node to add
	 * @return true if it's added, false otherwise
	 */
	public boolean add(POPNetworkNode node) {
		return nodes.contains(node) || nodes.add(node);
	}

	/**
	 * Remove a network node from this connector
	 *
	 * @param node The node to remove
	 * @return true if it's added, false otherwise
	 */
	public boolean remove(POPNetworkNode node) {	
		return nodes.remove(node);
	}

	/**
	 * The number of nodes contained in this connector
	 *
	 * @return how many nodes are contained in this node
	 */
	public int size() {
		return nodes.size();
	}
	
	/**
	 * Get an unmodifiable list with all the nodes in the connector.
	 * 
	 * @return 
	 */
	public List<POPNetworkNode> getNodes() {
		return Collections.unmodifiableList(nodes);
	}
	
	/**
	 * Set this protocol network of influence, will be used by its children
	 *
	 * @param network
	 */
	public void setNetwork(POPNetwork network) {
		this.network = network;
	}

	/**
	 * Reference to the JobManger, needed for potential call to Reserve, CancelReservation, etc.
	 *
	 * @param jobManager
	 */
	public void setJobManager(POPJavaJobManager jobManager) {
		this.jobManager = jobManager;
	}
}
