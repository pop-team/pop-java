package ch.icosys.popjava.core.service.jobmanager.network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ch.icosys.popjava.core.baseobject.ObjectDescription;
import ch.icosys.popjava.core.baseobject.POPAccessPoint;
import ch.icosys.popjava.core.service.jobmanager.POPJavaJobManager;
import ch.icosys.popjava.core.service.jobmanager.yaml.YamlConnector;

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
	 *            the descriptor for node creation
	 */
	public POPConnector(POPNetworkDescriptor descriptor) {
		this.descriptor = descriptor;
	}

	/**
	 * Protocol specific createObject
	 *
	 * @see POPJavaJobManager#createObject(POPAccessPoint, String,
	 *      ObjectDescription, int, POPAccessPoint[], int, POPAccessPoint[])
	 * @param localservice
	 *            The AppService of the application
	 * @param objname
	 *            Which object we have to create
	 * @param od
	 *            The OD of the request
	 * @param howmany
	 *            The size of objcontacts
	 * @param objcontacts
	 *            How many instances we seek
	 * @param howmany2
	 *            number of remote access points (we think)
	 * @param remotejobcontacts
	 *            actual access points (we think)
	 * @return
	 */
	public abstract int createObject(POPAccessPoint localservice, String objname, ObjectDescription od, int howmany,
			POPAccessPoint[] objcontacts, int howmany2, POPAccessPoint[] remotejobcontacts);

	/**
	 * The descriptor identifying this class.
	 * 
	 * @return the descriptor
	 */
	public POPNetworkDescriptor getDescriptor() {
		return descriptor;
	}

	/**
	 * Is the connector empty or not.
	 * 
	 * @return if there are any nodes inside the connector
	 */
	public boolean isEmpty() {
		return nodes.isEmpty();
	}

	/**
	 * Add a new network node to this connector
	 *
	 * @param node
	 *            The node to add
	 * @return true if it's added, false otherwise
	 */
	boolean addNode(POPNode node) {
		return node.getConnectorDescriptor() == descriptor && nodes.contains(node) || nodes.add(node);
	}

	/**
	 * Remove a network node from this connector
	 *
	 * @param node
	 *            The node to remove
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
	 * @return the number of nodes
	 */
	List<POPNode> getNodes() {
		return Collections.unmodifiableList(nodes);
	}

	/**
	 * Set this protocol network of influence, will be used by its children
	 *
	 * @param network
	 *            the network associated to this connector
	 */
	void setNetwork(POPNetwork network) {
		this.network = network;
	}

	/**
	 * Reference to the JobManger, needed for potential call to Reserve,
	 * CancelReservation, etc.
	 *
	 * @param jobManager
	 *            the machine job manager
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
