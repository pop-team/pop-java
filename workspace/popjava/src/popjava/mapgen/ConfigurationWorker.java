package popjava.mapgen;


import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ConfigurationWorker extends XMLWorker {

	// Private constants declarations
	private static final String CONFIG_SUFFIX = "popj_config";
	private static final String XML_FILE_EXTENSION = ".xml";
	private static final String XSD_FILE_EXTENSION = ".xsd";
	private static final String ENV_POPJ_LOCATION = "POPJAVA_LOCATION";
	private static final String CONFIG_DIRECTORY = "etc";
	private static final String DEFAULT_POPJ_LOCATION = "/home/clementval/popj";
	private static final String ATTRIBUTE_ITEM_NAME = "item";
	
	// Public constants declarations
	public static final String POPJ_LOCATION_ITEM = "popj_location";
	public static final String POPJ_PLUGIN_ITEM = "popj_plugin_location";
	public static final String POPC_APPCORESERVICE_ITEM = "popc_appcoreservice_location";

	// Variable's declaration
	private final String configFileLocation;
	private final String configSchemaLocation;
	private String popjLocation;

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
			throw new Exception("Configuration file is not valid");
		}
	}

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
