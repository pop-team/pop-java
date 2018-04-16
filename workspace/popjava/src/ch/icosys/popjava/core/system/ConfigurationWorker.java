package popjava.system;

import java.io.File;
import java.security.InvalidParameterException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
/**
 * POP-Java configuration class. Provide access trough the different configuration parameters stored in the XML configuration file.
 * @author clementval
 */
public class ConfigurationWorker extends XMLWorker {

	/**
	 * Private constants declaration. This constant should not be modified
	 */
	private static final String CONFIG_SUFFIX = "popj_config";
	private static final String ENV_POPJ_LOCATION = "POPJAVA_LOCATION";
	private static final String CONFIG_DIRECTORY = "etc";
	/**
	 *private static final String DEFAULT_POPJ_LOCATION = "/usr/local/popj";
	 */
	private static final String DEFAULT_POPJ_LOCATION = "/home/clementval/popj";
	private static final String ATTRIBUTE_ITEM_NAME = "item";
	
	/**
	 * POP-Java location configuration item name
	 */
	public static final String POPJ_LOCATION_ITEM = "popj_location";
	/**
	 * POP-Java plug-in location configuration item name
	 */
	public static final String POPJ_PLUGIN_ITEM = "popj_plugin_location";
	/**
	 * POP-Java broker command configuration item name
	 */
	public static final String POPJ_BROKER_COMMAND_ITEM = "popj_broker_command";
	/**
	 * POP-Java application core service location configuration name
	 */
	public static final String POPC_APPCORESERVICE_ITEM = "popc_appcoreservice_location";

	/**
	 * Variables declaration
	 */
	private String configFileLocation;
	private String configSchemaLocation;
	private String popjLocation;

	/**
	 * Constructs a new ConfigurationWorker and retrieve POP-Java base location
	 * @throws Exception thrown if the configuration file is not valid with its XML schema
	 */
	public ConfigurationWorker() throws Exception {
		String baseLocation = System.getenv(ENV_POPJ_LOCATION);
		if (baseLocation == null) {
			baseLocation = DEFAULT_POPJ_LOCATION;
		}
		popjLocation = baseLocation;
		if (!popjLocation.endsWith("/"))
			popjLocation += "/";
		configFileLocation = popjLocation + CONFIG_DIRECTORY + "/"
				+ CONFIG_SUFFIX + XML_FILE_EXTENSION;
		configSchemaLocation = popjLocation + CONFIG_DIRECTORY + "/"
				+ CONFIG_SUFFIX + XSD_FILE_EXTENSION;

		if (!isValid(configFileLocation, configSchemaLocation)) {
			throw new InvalidParameterException("Configuration file is not valid");
		}
	}

	/**
	 * Retrieve a configuration item in the configuration file by its name
	 * @param name	name of the item to retrieve the value
	 * @return		Value of the item or null if not found
	 */
	public String getValue(String name){
		DocumentBuilder builder;
		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = builder.parse(new File(configFileLocation));
			Element popjConfigElement = document.getDocumentElement();
			NodeList list = popjConfigElement.getChildNodes();
			for (int index = 0; index < list.getLength(); ++index) {
				Node node = list.item(index);
				if (node.getNodeType() == Node.ELEMENT_NODE	&& node.getNodeName().equals(ATTRIBUTE_ITEM_NAME)) {
					NamedNodeMap attributes = node.getAttributes();
					if(attributes != null){
						String itemName = attributes.item(0).getTextContent();
						if(itemName.equals(name))
							return node.getTextContent();
					}
				}
				
			}
		} catch (Exception e){
			e.printStackTrace();
			return null;
		}
		return null;
	}
}
