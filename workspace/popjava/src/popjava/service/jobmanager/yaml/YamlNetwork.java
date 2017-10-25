package popjava.service.jobmanager.yaml;

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
	 * @return 
	 */
	public String getUuid() {
		return uuid;
	}
	
	/**
	 * The unique identifier for a network.
	 * 
	 * @param uuid 
	 */
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	/**
	 * A friendly name to visualize and recognize the network. 
	 * 
	 * @return 
	 */
	public String getFriendlyName() {
		return friendlyName;
	}

	/**
	 * A friendly name to visualize and recognize the network.
	 * 
	 * @param friendlyName 
	 */
	public void setFriendlyName(String friendlyName) {
		this.friendlyName = friendlyName;
	}

	/**
	 * The connector in this network.
	 * 
	 * @return 
	 */
	public List<YamlConnector> getConnectors() {
		return connectors;
	}

	/**
	 * The connector in this network.
	 * 
	 * @param connectors 
	 */
	public void setConnectors(List<YamlConnector> connectors) {
		this.connectors = connectors;
	}
}
