package popjava;

import java.io.IOException;
import java.security.cert.Certificate;
import popjava.base.POPObject;
import popjava.baseobject.POPAccessPoint;
import popjava.combox.ssl.SSLUtils;
import popjava.service.jobmanager.POPJavaJobManager;
import popjava.service.jobmanager.network.POPNetworkNode;
import popjava.serviceadapter.POPJobManager;
import popjava.system.POPSystem;
import popjava.util.Configuration;
import popjava.util.LogWriter;

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
	 * @param network The name of the network
	 * @param node A network node implementation
	 */
	public void registerNode(String network, POPNetworkNode node) {
		jobManager.registerPermanentNode(network, node.getCreationParams());
	}
	
	/**
	 * Register a new node with a certificate associated to it
	 * 
	 * @param network
	 * @param node
	 * @param certificate
	 * @return 
	 */
	public boolean registerNode(String network, POPNetworkNode node, Certificate certificate) {
		try {
			SSLUtils.addConfidenceLink(node, certificate);
			jobManager.registerPermanentNode(network, node.getCreationParams());
			return true;
		} catch(IOException e) {
			LogWriter.writeExceptionLog(e);
			return false;
		}
	}
	
	/**
	 * Add a confidence link to a previously added node
	 * 
	 * @param node
	 * @param certificate
	 * @return 
	 */
	public boolean assignCertificate(POPNetworkNode node, Certificate certificate) {
		try {
			SSLUtils.addConfidenceLink(node, certificate);
			return true;
		} catch(IOException e) {
			LogWriter.writeExceptionLog(e);
			return false;
		}
	}
	
	/**
	 * Add a confidence link to a previously added node
	 * 
	 * @param node
	 * @param certificate
	 * @return 
	 */
	public boolean replaceCertificate(POPNetworkNode node, Certificate certificate) {
		try {
			SSLUtils.replaceConfidenceLink(node, certificate);
			return true;
		} catch(IOException e) {
			LogWriter.writeExceptionLog(e);
			return false;
		}
	}
	
	/**
	 * Remove a Node/Friend from a network
	 *
	 * @param network The name of the network
	 * @param node A network node implementation
	 */
	public void unregisterNode(String network, POPNetworkNode node) {
		jobManager.unregisterPermanentNode(network, node.getCreationParams());
		// try remove
		try {
			SSLUtils.removeConfidenceLink(node);
		} catch(IOException e) {
			// too bad
		}
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
