package popjava;

import popjava.base.POPObject;
import popjava.baseobject.POPAccessPoint;
import popjava.service.jobmanager.POPJavaJobManager;
import popjava.service.jobmanager.network.POPNetworkNode;
import popjava.serviceadapter.POPJobManager;
import popjava.system.POPSystem;
import popjava.util.Configuration;

/**
 * Proxy to Job Manager for user use of the complexity
 *
 * @author Davide Mazzoleni
 */
public class PopJavaConfig {

	private final POPJavaJobManager jobManager;

	public PopJavaConfig() {
		String protocol = Configuration.DEFAULT_PROTOCOL;
		POPAccessPoint jma = new POPAccessPoint(String.format("%s://%s:%d",
			protocol, POPSystem.getHostIP(), POPJobManager.DEFAULT_PORT));
		jobManager = PopJava.newActive(POPJavaJobManager.class, jma);
	}

	/**
	 * Register a POPObject and make it available for discovery in a network
	 *
	 * @param object
	 * @param tfcNetwork
	 * @param secret
	 * @return
	 */
	public boolean registerTFC(Object object, String tfcNetwork, String secret) {
		if (object instanceof POPObject) {
			POPObject temp = (POPObject) object;
			boolean status = jobManager.registerTFCObject(tfcNetwork, temp.getClassName(), temp.getAccessPoint(), secret);
			return status;
		}
		return false;
	}

	/**
	 * Unregister a POPObject from the local JobManager
	 *
	 * @param object
	 * @param tfcNetwork
	 * @param secret
	 */
	public void unregisterTFC(Object object, String tfcNetwork, String secret) {
		if (object instanceof POPObject) {
			POPObject temp = (POPObject) object;
			jobManager.unregisterTFCObject(tfcNetwork, temp.getClassName(), temp.getAccessPoint(), secret);
		}
	}

	/**
	 * Add a new Node/Friend to a network
	 *
	 * @param <T>
	 * @param network The name of the network
	 * @param node A network node implementation
	 */
	public<T extends POPNetworkNode> void registerNode(String network, T node) {
		jobManager.registerPermanentNode(network, node.getCreationParams());
	}

	/**
	 * Remove a Node/Friend to a network
	 *
	 * @param <T>
	 * @param network The name of the network
	 * @param node A network node implementation
	 */
	public<T extends POPNetworkNode>  void unregisterNode(String network, T node) {
		jobManager.unregisterPermanentNode(network, node.getCreationParams());
	}

	/**
	 * Create a new network of interest
	 * 
	 * @param networkName 
	 */
	public void createNetwork(String networkName) {
		jobManager.createNetwork(networkName);
	}

	/**
	 * Remove a network of interest with all its members
	 * 
	 * @param networkName 
	 */
	public void removeNetwork(String networkName) {
		jobManager.removeNetwork(networkName);
	}

	/**
	 * Change the value of available power on the job manager
	 * 
	 * @param limit 
	 */
	public void changeAvailablePower(float limit) {
		jobManager.changeAvailablePower(limit);
	}

	/**
	 * Change the value of available memory on the job manager
	 * 
	 * @param limit 
	 */
	public void changeAvailableMemory(float limit) {
		jobManager.changeAvailableMemory(limit);
	}

	/**
	 * Change the value of available bandwidth on the job manager
	 * 
	 * @param limit 
	 */
	public void changeAvailableBandwidth(float limit) {
		jobManager.changeAvailableBandwidth(limit);
	}

	/**
	 * Change the maximal number of object that can be create with this job manager
	 * 
	 * @param limit 
	 */
	public void changeMaxJobLimit(int limit) {
		jobManager.changeMaxJobLimit(limit);
	}

	/**
	 * Change the value maximal power an object can request
	 * 
	 * @param limit 
	 */
	public void changeMaxJobPower(float limit) {
		jobManager.changeMaxJobPower(limit);
	}

	/**
	 * Change the value maximal memory an object can request
	 * 
	 * @param limit 
	 */
	public void changeMaxJobMemory(float limit) {
		jobManager.changeMaxJobMemory(limit);
	}

	/**
	 * Change the value maximal bandwidth an object can request
	 * 
	 * @param limit 
	 */
	public void changeMaxJobBandwidth(float limit) {
		jobManager.changeMaxJobBandwidth(limit);
	}
	
	/**
	 * Don't use this
	 * 
	 * @deprecated To be removed in production
	 */
	@Deprecated
	public void dump() {
		jobManager.dump();
	}
}
