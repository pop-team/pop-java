package ch.icosys.popjava.core;

import java.io.File;
import java.io.IOException;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.icosys.popjava.core.base.POPObject;
import ch.icosys.popjava.core.baseobject.POPAccessPoint;
import ch.icosys.popjava.core.service.jobmanager.POPJavaJobManager;
import ch.icosys.popjava.core.service.jobmanager.Resource;
import ch.icosys.popjava.core.service.jobmanager.external.POPNetworkDetails;
import ch.icosys.popjava.core.service.jobmanager.network.POPNetworkDescriptor;
import ch.icosys.popjava.core.service.jobmanager.network.POPNode;
import ch.icosys.popjava.core.system.POPSystem;
import ch.icosys.popjava.core.util.Configuration;
import ch.icosys.popjava.core.util.Util;
import ch.icosys.popjava.core.util.ssl.KeyPairDetails;
import ch.icosys.popjava.core.util.ssl.KeyStoreDetails;
import ch.icosys.popjava.core.util.ssl.SSLUtils;

/**
 * Proxy to localhost Job Manager
 *
 * @author Davide Mazzoleni
 */
public class JobManagerConfig {

	private final POPJavaJobManager jobManager;

	public JobManagerConfig() {
		Configuration conf = Configuration.getInstance();
		String protocol = conf.getJobManagerProtocols()[0];
		int port = conf.getJobManagerPorts()[0];
		String accessString = String.format("%s://%s:%d", protocol, POPSystem.getHostIP(), port);
		POPAccessPoint jma = new POPAccessPoint(accessString);
		jobManager = PopJava.connect(null, POPJavaJobManager.class, conf.getDefaultNetwork(), jma);
	}

	/**
	 * Register a POPObject and make it available for discovery in a network
	 *
	 * @param object the object to publish
	 * @param tfcNetworkUUID in which network should the object be visible in
	 * @param secret a secret to un-register the object manually, if the object dies it will un-register itself
	 * @return true if registered, false otherwise
	 */
	public boolean publishTFCObject(Object object, String tfcNetworkUUID, String secret) {
		if (object instanceof POPObject) {
			POPObject temp = (POPObject) object;
			return jobManager.registerTFCObject(tfcNetworkUUID, temp.getClassName(), temp.getAccessPoint(), secret);
		}
		return false;
	}

	/**
	 * Unregister a POPObject from the local JobManager
	 *
	 * @param object the object to publish
	 * @param tfcNetworkUUID the network the object is registered in
	 * @param secret the secret used when the object was published
	 */
	public void withdrawnTFCObject(Object object, String tfcNetworkUUID, String secret) {
		if (object instanceof POPObject) {
			POPObject temp = (POPObject) object;
			jobManager.unregisterTFCObject(tfcNetworkUUID, temp.getClassName(), temp.getAccessPoint(), secret);
		}
	}

	/**
	 * Add a new Node/Friend to a network
	 *
	 * @param networksUUID The name of the network
	 * @param node A network node implementation
	 */
	public void registerNode(String networksUUID, POPNode node) {
		jobManager.registerPermanentNode(networksUUID, node.getCreationParams());
	}
	
	/**
	 * Register a new node with a certificate associated to it
	 * 
	 * @param networkUUID Name of the network
	 * @param node The node to add
	 * @param certificate The certificate to use
	 */
	public void registerNode(String networkUUID, POPNode node, Certificate certificate) {
		jobManager.registerPermanentNode(networkUUID, SSLUtils.certificateBytes(certificate), node.getCreationParams());
	}
	
	/**
	 * Remove a Node/Friend from a network
	 *
	 * @param networkUUID The name of the network
	 * @param node A network node implementation
	 */
	public void unregisterNode(String networkUUID, POPNode node) {
		jobManager.unregisterPermanentNode(networkUUID, node.getCreationParams());
	}

	/**
	 * Create a new network of interest, return the details with UUID.
	 * 
	 * @param friendlyName A friendly name to identify the network locally.
	 * @return An object containing a generated UUID and the friendly name.
	 */
	public POPNetworkDetails createNetwork(String friendlyName) {
		return jobManager.createNetwork(friendlyName);
	}

	/**
	 * Create a new network of interest, return the details with UUID.
	 * 
	 * @param networkUUID The UUID the network will use
	 * @param friendlyName A friendly name to identify the network locally.
	 * @return An object containing the UUID and the friendly name.
	 */
	public POPNetworkDetails createNetwork(String networkUUID, String friendlyName) {
		return jobManager.createNetwork(networkUUID, friendlyName);
	}

	/**
	 * Remove a network of interest with all its members
	 * 
	 * @param networkUUID the network to remove
	 */
	public void removeNetwork(String networkUUID) {
		jobManager.removeNetwork(networkUUID);
	}

	/**
	 * Change the value of available power on the job manager
	 * 
	 * @param limit the new power limit
	 */
	public void changeAvailablePower(float limit) {
		jobManager.changeAvailablePower(limit);
	}

	/**
	 * Change the value of available memory on the job manager
	 * 
	 * @param limit the new memory limit
	 */
	public void changeAvailableMemory(float limit) {
		jobManager.changeAvailableMemory(limit);
	}

	/**
	 * Change the value of available bandwidth on the job manager
	 * 
	 * @param limit the new bandwidth limit
	 */
	public void changeAvailableBandwidth(float limit) {
		jobManager.changeAvailableBandwidth(limit);
	}

	/**
	 * Change the maximal number of object that can be create with this job manager
	 * 
	 * @param limit the new limit of spawned objects
	 */
	public void changeMaxJobLimit(int limit) {
		jobManager.changeMaxJobLimit(limit);
	}

	/**
	 * Change the value maximal power an object can request
	 * 
	 * @param limit the upper power an object can have allocated
	 */
	public void changeMaxJobPower(float limit) {
		jobManager.changeMaxJobPower(limit);
	}

	/**
	 * Change the value maximal memory an object can request
	 * 
	 * @param limit the upper memory an object can have allocated
	 */
	public void changeMaxJobMemory(float limit) {
		jobManager.changeMaxJobMemory(limit);
	}

	/**
	 * Change the value maximal bandwidth an object can request
	 * 
	 * @param limit the upper bandwidth an object can have allocated
	 */
	public void changeMaxJobBandwidth(float limit) {
		jobManager.changeMaxJobBandwidth(limit);
	}
	
	/**
	 * Array of networks available locally
	 * 
	 * @return all available networks on the local Job Manager
	 */
	public POPNetworkDetails[] availableNetworks() {
		return jobManager.getAvailableNetworks();
	}
	
	/**
	 * All the node available in a network
	 * Use {@link POPNode#getConnectorDescriptor()} to know which type you are working with.
	 * 
	 * @param networkUUID the network we want to know the nodes
	 * @return an array of generic nodes
	 */
	public POPNode[] networkNodes(String networkUUID) {
		// get nodes
		String[][] networkNodes = jobManager.getNetworkNodes(networkUUID);
		
		// no results
		if (networkNodes == null) {
			return new POPNode[0];
		}
		
		POPNode[] nodes = new POPNode[networkNodes.length];
		// make them real
		for (int i = 0; i < nodes.length; i++) {
			List<String> nodeParams = new ArrayList<>(Arrays.asList(networkNodes[i]));
			String connector = Util.removeStringFromList(nodeParams, "connector=");
			POPNetworkDescriptor descriptor = POPNetworkDescriptor.from(connector);
			if (descriptor != null) {
				nodes[i] = descriptor.createNode(nodeParams);
			}
		}
		
		return nodes;
	}
	
	/**
	 * Generate a KeyStore with private key and certificate.
	 * Proxy for {@link SSLUtils#generateKeyStore(ch.icosys.popjava.core.util.ssl.KeyStoreDetails, ch.icosys.popjava.core.util.ssl.KeyPairDetails)}
	 *
     * @param ksDetails the details of the keystore
     * @param keyDetails the details about the private key
	 * @return true if the keystore was generated correctly
	 */
	public boolean generateKeyStore(KeyStoreDetails ksDetails, KeyPairDetails keyDetails) {
		return SSLUtils.generateKeyStore(ksDetails, keyDetails);
	}
	
	/**
	 * Change configuration file location.
	 * This method will only change the location and try to write in it, it will not delete the old file.
	 * This method is NOT meant to be used to load a new configuration file.
	 * 
	 * @param location The new location of the configuration file.
	 * @throws java.io.IOException If you can't write in the location specified.
	 */
	public void setConfigurationFileLocation(File location) throws IOException {
		if (!location.canWrite()) {
			throw new IOException("Can't write in this location");
		}
		jobManager.setConfigurationFile(location.getAbsolutePath());
	}
	
	
	/**
	 * The initial capacity of the node
	 * 
	 * @return how many resources were allocated on the job manager originally
	 */
	public Resource getInitialAvailableResources() {
		return jobManager.getInitialAvailableResources();
	}

	/**
	 * The upper limit for each job
	 * 
	 * @return how many resources can a single job allocate
	 */
	public Resource getJobResourcesLimit() {
		return jobManager.getInitialAvailableResources();
	}

	/**
	 * The maximum number of simultaneous object available on the JM machine
	 * 
	 * @return how many jobs can the job manager spawn
	 */
	public int getMaxJobs() {
		return jobManager.getMaxJobs();
	}
	
	/**
	 * Don't use this
	 * 
	 * @deprecated Should removed in production
	 */
	@Deprecated
	public void dump() {
		jobManager.dump();
	}
}
