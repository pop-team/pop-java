package ch.icosys.popjava.core.combox;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ch.icosys.popjava.core.combox.socket.raw.ComboxSocketFactory;
import ch.icosys.popjava.core.combox.socket.ssl.ComboxSecureSocketFactory;
import ch.icosys.popjava.core.system.POPJavaConfiguration;
import ch.icosys.popjava.core.system.XMLWorker;
import ch.icosys.popjava.core.util.Configuration;
import ch.icosys.popjava.core.util.LogWriter;

/**
 * This class is responsible to find the different combox available in POP-Java
 */

public class ComboxFactoryFinder {
	private final Map<String, ComboxFactory> comboxFactoryList = new HashMap<>();

	private URLClassLoader urlClassLoader = null;

	private static ComboxFactoryFinder currentInstance = null;

	private final String PackageNodeName = "Package";

	private final String JarAttributeName = "JarFile";

	private final String ComboxFactoryNodeName = "ComboxFactory";

	private final Configuration conf = Configuration.getInstance();

	/**
	 * Default constructor
	 */
	protected ComboxFactoryFinder() {
		// Load default combox factory
		ComboxSocketFactory comboxSocketFactory = new ComboxSocketFactory();
		ComboxSecureSocketFactory comboxSecureSocketFactory = new ComboxSecureSocketFactory();

		comboxFactoryList.put(comboxSocketFactory.getComboxName(), comboxSocketFactory);
		comboxFactoryList.put(comboxSecureSocketFactory.getComboxName(), comboxSecureSocketFactory);

		String pluginLocation = POPJavaConfiguration.getPopPluginLocation();
		if (pluginLocation.length() > 0) {
			loadComboxMap(pluginLocation);
		}
	}

	/**
	 * Get the unique instance of the factory finder
	 * 
	 * @return The unique ComboxFactoryFinder instance
	 */
	public static ComboxFactoryFinder getInstance() {
		if (currentInstance == null)
			currentInstance = new ComboxFactoryFinder();
		return currentInstance;
	}

	/**
	 * Load all the combox in the pop_combox.xml file
	 * 
	 * @param pluginLocation
	 *            Location of the plugin file
	 */
	public void loadComboxMap(String pluginLocation) {
		DocumentBuilder builder;
		String comboxMapLocation = pluginLocation + File.separator + "pop_combox.xml";
		String schemaLocation = pluginLocation + File.separator + "pop_combox.xsd";

		XMLWorker xw = new XMLWorker();
		if (!xw.isValid(comboxMapLocation, schemaLocation)) {
			LogWriter.printDebug("The combox plugin map is not valid");
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
				if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals(PackageNodeName)) {
					Element packageElement = (Element) node;
					// Get "package" attribute
					String jarFileName = packageElement.getAttribute(JarAttributeName);
					String jarFileLocation = pluginLocation + File.separator + jarFileName;
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
								&& childNode.getNodeName().equals(ComboxFactoryNodeName)) {
							String factoryName = childNode.getTextContent();
							if (factoryName != null && factoryName.length() > 0)
								loadPlugin(factoryName, urlClassLoader);
						}
						childNode = childNode.getNextSibling();
					}
				}
			}
		} catch (ParserConfigurationException | IOException | SAXException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Find a specific factory with the given name
	 * 
	 * @param factoryName
	 *            Name of the factory
	 * @return The combox factory or null if not found, if empty or null the default
	 *         protocol factory
	 */
	public ComboxFactory findFactory(String factoryName) {
		if (factoryName == null || factoryName.isEmpty()) {
			factoryName = conf.getDefaultProtocol();
		}
		factoryName = factoryName.toLowerCase();

		ComboxFactory factory = comboxFactoryList.get(factoryName);
		if (factory != null && factory.isAvailable()) {
			return factory;
		}
		return null;
	}

	/**
	 * Check if a factory protocol is secure or not.
	 * 
	 * @param factoryName
	 *            The name of the factory
	 * @return true if secure, false if not found or not secure
	 */
	public boolean isFactorySecure(String factoryName) {
		if (factoryName == null || factoryName.isEmpty()) {
			return false;
		}
		factoryName = factoryName.toLowerCase();

		ComboxFactory factory = comboxFactoryList.get(factoryName);

		return factory != null && factory.isSecure();
	}

	/**
	 * Get the number of factory
	 * 
	 * @return Number of factory
	 */
	public int getFactoryCount() {
		return comboxFactoryList.size();
	}

	/**
	 * Get all available factories at a given instant
	 * 
	 * @return An array containing all available factories
	 */
	public ComboxFactory[] getAvailableFactories() {
		List<ComboxFactory> factories = new ArrayList<>();
		for (ComboxFactory factory : comboxFactoryList.values()) {
			if (factory.isAvailable()) {
				factories.add(factory);
			}
		}
		return factories.toArray(new ComboxFactory[factories.size()]);
	}

	/**
	 * Get the factory at the specified index
	 * 
	 * @param index
	 *            Index of the factory
	 * @return The factory at the specified index or null if out of bound index
	 */
	public ComboxFactory get(int index) {
		if (index < 0 || index >= getFactoryCount())
			return null;
		Set<String> keys = comboxFactoryList.keySet();
		String key = "";
		int currentIndex = 0;
		for (Iterator<String> iterator = keys.iterator(); iterator.hasNext() && currentIndex <= index; currentIndex++) {
			key = iterator.next();
		}
		return comboxFactoryList.get(key);
	}

	/**
	 * Load a specific combox plug-in
	 * 
	 * @param comboxFactoryName
	 *            Name of the combox plug-in
	 * @param urlClassLoader
	 *            URL of the combox plug-in
	 * @return The combox factory loaded or null if the plug-in is not found
	 */
	private ComboxFactory loadPlugin(String comboxFactoryName, URLClassLoader urlClassLoader) {
		comboxFactoryName = comboxFactoryName.trim();
		if (urlClassLoader == null || comboxFactoryName.length() == 0)
			return null;
		ComboxFactory comboxFactory = null;
		try {
			Class<?> comboxClass = Class.forName(comboxFactoryName, true, urlClassLoader);
			Constructor<?> constructor = comboxClass.getConstructor();
			comboxFactory = (ComboxFactory) constructor.newInstance();
			if (!comboxFactoryList.containsKey(comboxFactory.getComboxName())) {
				comboxFactoryList.put(comboxFactory.getComboxName(), comboxFactory);
			}

		} catch (ClassNotFoundException | IllegalArgumentException | InstantiationException | IllegalAccessException
				| InvocationTargetException | SecurityException | NoSuchMethodException e) {
			LogWriter.writeExceptionLog(e);
		}

		return comboxFactory;
	}
}
