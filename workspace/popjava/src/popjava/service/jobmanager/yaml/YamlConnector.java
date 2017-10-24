package popjava.service.jobmanager.yaml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Describe a connector in the YAML configuration file.
 * Nodes are a maps of variable values which should be able to be parsed by type's POPDescriptor.
 * 
 * Ideally we should create a descriptor starting from the type.
 * ```
 * POPDescriptor descriptor = POPNetworkDescriptor.from(connector.getType());
 * for (List<String> nodeParams : connector.asPOPNodeParams()) {
 *     handlePOPNode(descriptor.createNode(nodeParams));
 * }
 * ```
 * 
 * @author Davide Mazzoleni
 */
public class YamlConnector {

	private String type;
	private List<Map<String, Object>> nodes = Collections.EMPTY_LIST;

	/**
	 * The type of connector we are working with.
	 * Transformable with {@link popjava.service.jobmanager.network.POPNetworkDescriptor#from(java.lang.String) }.
	 * 
	 * @return 
	 */
	public String getType() {
		return type;
	}

	/**
	 * The type of connector we are working with.
	 * 
	 * @param name 
	 */
	public void setType(String name) {
		this.type = name;
	}

	/**
	 * A list with each element being a node in the connector.
	 * A node is a map of its attribute.
	 * Use {@link #asPOPNodeParams() } to have them ready for {@link popjava.service.jobmanager.network.POPNetworkDescriptor#createNode(java.util.List) }.
	 * 
	 * @return 
	 */
	public List<Map<String, Object>> getNodes() {
		return nodes;
	}

	/**
	 * A list with each element being a node in the connector.
	 * 
	 * @param nodes 
	 */
	public void setNodes(List<Map<String, Object>> nodes) {
		this.nodes = nodes;
	}

	/**
	 * Transform the nodes map into a list of String which are ready to be fed to {@link popjava.service.jobmanager.network.POPNetworkDescriptor#createNode(java.util.List) }.
	 * 
	 * @return 
	 */
	public List<List<String>> asPOPNodeParams() {
		List<List<String>> listNodes = new ArrayList<>(nodes.size());

		for (Map<String, Object> node : nodes) {
			List<String> listParams = new ArrayList<>(node.size());

			for (Map.Entry<String, Object> entry : node.entrySet()) {
				listParams.add(String.format("%s=%s", entry.getKey(), entry.getValue().toString()));
			}

			listNodes.add(listParams);
		}

		return listNodes;
	}

}
