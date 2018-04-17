package ch.icosys.popjava.core.service.jobmanager.yaml;

import java.util.Collections;
import java.util.List;

/**
 * Describe a network in the YAML configuration file.
 * 
 * @author Davide Mazzoleni
 */
public class YamlNetwork {

	private String uuid;

	private String friendlyName;

	@SuppressWarnings("unchecked")
	private List<YamlConnector> connectors = Collections.EMPTY_LIST;

	/**
	 * The unique identifier for a network.
	 * 
	 * @return the uuid of the network
	 */
	public String getUuid() {
		return uuid;
	}

	/**
	 * The unique identifier for a network.
	 * 
	 * @param uuid
	 *            the uuid of the network
	 */
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	/**
	 * A friendly name to visualize and recognize the network.
	 * 
	 * @return the friendly name locally displayed
	 */
	public String getFriendlyName() {
		return friendlyName;
	}

	/**
	 * A friendly name to visualize and recognize the network.
	 * 
	 * @param friendlyName
	 *            the friendly name locally displayed
	 */
	public void setFriendlyName(String friendlyName) {
		this.friendlyName = friendlyName;
	}

	/**
	 * The connector in this network.
	 * 
	 * @return the connectors in the network
	 */
	public List<YamlConnector> getConnectors() {
		return connectors;
	}

	/**
	 * The connector in this network.
	 * 
	 * @param connectors
	 *            the connectors in the network
	 */
	public void setConnectors(List<YamlConnector> connectors) {
		this.connectors = connectors;
	}
}
