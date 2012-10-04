package popjava.buffer;

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

import popjava.system.POPJavaConfiguration;
import popjava.system.POPSystem;
import popjava.system.XMLWorker;

/**
 * This class is responsible to discover the buffer
 */
public class BufferFactoryFinder {
	/**
	 * List of different buffer name and their class
	 */
	private ConcurrentHashMap<String, BufferFactory> bufferFactoryList = new ConcurrentHashMap<String, BufferFactory>();
	/**
	 * Class loader to retrive a plugin
	 */
	private URLClassLoader urlClassLoader = null;
	/**
	 * Singleton reference of the unique instance of this class
	 */
	private static BufferFactoryFinder currentInstance = null;
	/**
	 * Plugin file XML element name
	 */
	private final String PackageNodeName = "Package";
	/**
	 * Plugin file XML element name
	 */
	private final String JarAttributeName = "JarFile";
	/**
	 * Plugin file XML element name
	 */
	private final String BufferFactoryNodeName = "BufferFactory";

	/**
	 * Default constructor
	 */
	protected BufferFactoryFinder() {
		// Load default buffer factory
		BufferRawFactory rawBufferFactory = new BufferRawFactory();
		bufferFactoryList.put(rawBufferFactory.getBufferName(),
				rawBufferFactory);
		BufferXDRFactory xdrBufferFactory = new BufferXDRFactory();
		bufferFactoryList.put(xdrBufferFactory.getBufferName(),
				xdrBufferFactory);
		// Load the plug-in factory
		// It will read the file at POP_PLUGIN_LOCATION
		// Each line in this file follows the format:
		// jarFile|BufferFactory0|BufferFactory1|...
		String pluginLocation = POPJavaConfiguration.getPopPluginLocation();
		if (pluginLocation.length() > 0) {
			loadBufferMap(pluginLocation);
		}
	}

	/**
	 * Get the unique instance of the BufferFactoryFinder
	 * 
	 * @return the unique instance of the BufferFactoryFinder
	 */
	public static BufferFactoryFinder getInstance() {
		if (currentInstance == null)
			currentInstance = new BufferFactoryFinder();
		return currentInstance;
	}

	/**
	 * Read the plugin file
	 * 
	 * @param pluginLocation
	 *            Location of the plugin file
	 */
	public void loadBufferMap(String pluginLocation) {
		DocumentBuilder builder;
		String bufferMapLocation =  pluginLocation
				+ File.separator + "pop_buffer.xml";
		String schemaLocation =  pluginLocation
		+ File.separator + "pop_buffer.xsd";
		
		XMLWorker xw = new XMLWorker();
		if(!xw.isValid(bufferMapLocation, schemaLocation)){
			System.out.println("The buffer plugin map is not valid");
			return;
		}
		
		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = builder.parse(new File(bufferMapLocation));
			// mainNode: <BufferFactoryList>
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
							+ File.separator + jarFileName;
					File jarFile = new File(jarFileLocation);
					try {
						URL[] urls = new URL[1];
						urls[0] = jarFile.toURI().toURL();
						urlClassLoader = new URLClassLoader(urls);
					} catch (MalformedURLException ex) {
						continue;
					}
					// Get children of <Package>: text nodes & BufferFactory
					// nodes
					// Each child is a buffer
					Node childNode = node.getFirstChild();
					while (childNode != null) {
						if (childNode.getNodeType() == Node.ELEMENT_NODE
								&& childNode.getNodeName().equals(
										BufferFactoryNodeName)) {
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
	 * Find a specific factory
	 * 
	 * @param factoryName
	 *            Name of the factory
	 * @return The factory found or null
	 */
	public BufferFactory findFactory(String factoryName) {
		factoryName = factoryName.toLowerCase();
		if (bufferFactoryList.containsKey(factoryName)) {
			return bufferFactoryList.get(factoryName);
		}
		return null;
	}

	/**
	 * Load a specific buffer plugin from its location
	 * 
	 * @param bufferFactoryName
	 *            Name of the buffer factory
	 * @param urlClassLoader
	 *            URL to load the class
	 * @return The buffer factory found or null
	 */
	private BufferFactory loadPlugin(String bufferFactoryName,
			URLClassLoader urlClassLoader) {
		bufferFactoryName = bufferFactoryName.trim();
		if (urlClassLoader == null || bufferFactoryName.length() == 0)
			return null;
		BufferFactory bufferFactory = null;
		try {
			Class<?> bufferClass = Class.forName(bufferFactoryName, true,
					urlClassLoader);
			Constructor<?> constructor = bufferClass.getConstructor();
			bufferFactory = (BufferFactory) constructor.newInstance();
			if (!bufferFactoryList.containsKey(bufferFactory.getBufferName())) {
				bufferFactoryList.put(bufferFactory.getBufferName(),
						bufferFactory);
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
		return bufferFactory;
	}

	/**
	 * Get a formatted string of supporting buffer
	 * 
	 * @return formatted string of supporting buffer
	 */
	public String getSupportingBuffer() {
		String supportingBuffer = "";
		Enumeration<String> keys = bufferFactoryList.keys();
		String key = "";
		while (keys.hasMoreElements()) {
			key = keys.nextElement();
			supportingBuffer += key + " ";
		}
		supportingBuffer = supportingBuffer.trim();
		return supportingBuffer;
	}
}
