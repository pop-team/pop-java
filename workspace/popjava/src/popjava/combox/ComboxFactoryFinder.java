package popjava.combox;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import popjava.system.POPSystem;
import popjava.system.XMLWorker;

/**
 * This class is responsible to find the different combox available in POP-Java
 */

public class ComboxFactoryFinder {
	private ConcurrentHashMap<String, ComboxFactory> comboxFactoryList = new ConcurrentHashMap<String, ComboxFactory>();
	private URLClassLoader urlClassLoader = null;
	private static ComboxFactoryFinder currentInstance = null;
	private final String PackageNodeName = "Package";
	private final String JarAttributeName = "JarFile";
	private final String ComboxFactoryNodeName = "ComboxFactory";

	/**
	 * Default constructor
	 */
	protected ComboxFactoryFinder() {
		// Load default combox factory
		ComboxSocketFactory comboxSocketFactory = new ComboxSocketFactory();
		comboxFactoryList.put(comboxSocketFactory.getComboxName(),
				comboxSocketFactory);
		String pluginLocation = POPSystem.getPopPluginLocation();
		if (pluginLocation.length() > 0) {
			loadComboxMap(pluginLocation);
		}
	}

	/**
	 * Get the unique instance of the factory finder
	 * @return	The unique ComboxFactoryFinder instance
	 */
	public static ComboxFactoryFinder getInstance() {
		if (currentInstance == null)
			currentInstance = new ComboxFactoryFinder();
		return currentInstance;
	}

	/**
	 * Load all the combox in the pop_combox.xml file
	 * @param pluginLocation	Location of the plugin file
	 */
	public void loadComboxMap(String pluginLocation) {
		DocumentBuilder builder;
		String comboxMapLocation = pluginLocation
				+ POPSystem.getSeparatorString() + "pop_combox.xml";
		String schemaLocation = pluginLocation
		+ POPSystem.getSeparatorString() + "pop_combox.xsd";
		
		XMLWorker xw = new XMLWorker();
		if(!xw.isValid(comboxMapLocation, schemaLocation)){
			System.out.println("The combox plugin map is not valid");
			return;
		}
		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = builder.parse(new File(comboxMapLocation));
			// mainNode: <ComboxFactoryList>
			Element factoryListElement = document.getDocumentElement();
			// list: list of <Package> & text node
			NodeList list = factoryListElement.getChildNodes();
			for (int index = 0; index < list.getLength(); ++index) {
				// node: a <Package> node
				Node node = list.item(index);
				// Handle <Package> node only
				if (node.getNodeType() == Node.ELEMENT_NODE
						&& node.getNodeName().equals(PackageNodeName)) {
					Element packageElement = (Element) node;
					// Get "package" attribute
					String jarFileName = packageElement
							.getAttribute(JarAttributeName);
					String jarFileLocation = pluginLocation
							+ POPSystem.getSeparatorString() + jarFileName;
					File jarFile = new File(jarFileLocation);
					try {
						URL[] urls = new URL[1];
						urls[0] = jarFile.toURI().toURL();
						urlClassLoader = new URLClassLoader(urls);
					} catch (MalformedURLException ex) {
						continue;
					}
					// Get children of <Package>: text nodes & ComboxFactory
					// nodes
					// Each child is a combox
					Node childNode = node.getFirstChild();
					while (childNode != null) {
						if (childNode.getNodeType() == Node.ELEMENT_NODE
								&& childNode.getNodeName().equals(
										ComboxFactoryNodeName)) {
							String factoryName = childNode.getTextContent();
							if (factoryName != null && factoryName.length() > 0)
								loadPlugin(factoryName, urlClassLoader);
						}
						childNode = childNode.getNextSibling();
					}
				}
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Find a specific factory with the given name
	 * @param factoryName	Name of the factory
	 * @return	The combox factory or null if not found
	 */
	public ComboxFactory findFactory(String factoryName) {
		factoryName = factoryName.toLowerCase();
		if (comboxFactoryList.containsKey(factoryName)) {
			return comboxFactoryList.get(factoryName);
		}
		return null;
	}

	/**
	 * Get the number of factory
	 * @return Number of factory
	 */
	public int getFactoryCount() {
		return comboxFactoryList.size();
	}

	/**
	 * Get the factory at the specified index
	 * @param index	Index of the factory
	 * @return	The factory at the specified index or null if out of bound index
	 */
	public ComboxFactory get(int index) {
		if (index < 0 || index >= getFactoryCount())
			return null;
		Enumeration<String> keys = comboxFactoryList.keys();
		String key = "";
		int currentIndex = 0;
		while (keys.hasMoreElements() && currentIndex <= index) {
			currentIndex++;
			key = keys.nextElement();
		}
		return comboxFactoryList.get(key);
	}

	/**
	 * Load a specific combox plug-in
	 * @param comboxFactoryName	Name of the combox plug-in
	 * @param urlClassLoader	URL of the combox plug-in
	 * @return	The combox factory loaded or null if the plug-in is not found
	 */
	private ComboxFactory loadPlugin(String comboxFactoryName,
			URLClassLoader urlClassLoader) {
		comboxFactoryName = comboxFactoryName.trim();
		if (urlClassLoader == null || comboxFactoryName.length() == 0)
			return null;
		ComboxFactory comboxFactory = null;
		try {
			Class<?> comboxClass = Class.forName(comboxFactoryName, true,
					urlClassLoader);
			Constructor<?> constructor = comboxClass.getConstructor();
			comboxFactory = (ComboxFactory) constructor.newInstance();
			if (!comboxFactoryList.containsKey(comboxFactory.getComboxName())) {
				comboxFactoryList.put(comboxFactory.getComboxName(),
						comboxFactory);
			} else {

			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}

		return comboxFactory;
	}
}
