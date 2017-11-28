package popjava.service.jobmanager.network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import popjava.baseobject.ObjectDescription;
import popjava.baseobject.POPAccessPoint;
import popjava.service.jobmanager.POPJavaJobManager;
import popjava.service.jobmanager.yaml.YamlConnector;

/**
 *
 * @author Davide Mazzoleni
 */
public abstract class POPConnector {

	protected POPNetwork network;
	protected POPJavaJobManager jobManager;
	
	protected final POPNetworkDescriptor descriptor;
	protected final List<POPNode> nodes = new ArrayList<>();

	/**
	 * The constructor define the name of the connector.
	 * 
	 * @param descriptor 
	 */
	public POPConnector(POPNetworkDescriptor descriptor) {
		this.descriptor = descriptor;
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
	 * The descriptor identifying this class.
	 * 
	 * @return 
	 */
	public POPNetworkDescriptor getDescriptor() {
		return descriptor;
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
	boolean addNode(POPNode node) {
		return node.getConnectorDescriptor() == descriptor && nodes.contains(node) || nodes.add(node);
	}

	/**
	 * Remove a network node from this connector
	 *
	 * @param node The node to remove
	 * @return true if it's added, false otherwise
	 */
	boolean removeNode(POPNode node) {	
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
	List<POPNode> getNodes() {
		return Collections.unmodifiableList(nodes);
	}
	
	/**
	 * Set this protocol network of influence, will be used by its children
	 *
	 * @param network
	 */
	void setNetwork(POPNetwork network) {
		this.network = network;
	}

	/**
	 * Reference to the JobManger, needed for potential call to Reserve, CancelReservation, etc.
	 *
	 * @param jobManager
	 */
	void setJobManager(POPJavaJobManager jobManager) {
		this.jobManager = jobManager;
	}

	YamlConnector toYamlResource() {
		YamlConnector yamlConnector = new YamlConnector();
		yamlConnector.setType(descriptor.getGlobalName());
		
		List<Map<String, Object>> nodesParams = new ArrayList<>(nodes.size());
		yamlConnector.setNodes(nodesParams);
		
		for (POPNode node : nodes) {
			nodesParams.add(node.toYamlResource());
		}
		
		return yamlConnector;
	}
}
