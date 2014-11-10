package mapgen;
import java.io.File;
import java.util.ArrayList;

import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;

/**
 * This class is used to write new informations in the object map file or to
 * create a new one with some informations
 * 
 * @author clementval
 * 
 */
public class ObjectMapWriter extends XMLWorker {
	private String xmlFile;
	private boolean append;
	private Document objmap;

	/**
	 * Create a new instance of the ObjectMapWriter. This object is used to
	 * write the object map file
	 * 
	 * @param xmlFile
	 *            location of the XML file
	 * @param append
	 *            Set to true if the new entries must be added to the current
	 *            one
	 * @throws Exception
	 *             Thrown if anything go wrong in the process
	 */
	public ObjectMapWriter(String xmlFile, boolean append) throws Exception {
		this.xmlFile = xmlFile;
		this.append = append;
		File f = null; 
		if(xmlFile != null)
			f = new File(xmlFile);
		if (f == null || !f.exists()) {
			append = false;
			objmap = create();
			Element codeInfoList = objmap
					.createElement(Constants.NODE_CODE_INFO_LIST);
			objmap.appendChild(codeInfoList);
		} else {
			ConfigurationWorker cw = new ConfigurationWorker();
			String popjLocation = cw
					.getValue(ConfigurationWorker.POPJ_LOCATION_ITEM);
			if (isValid(xmlFile, popjLocation + "/etc/objectmap.xsd"))
				objmap = load(xmlFile);
			else
				throw new Exception("The input object map is not valid");
		}
		
	}

	/**
	 * Add entries into the XML file
	 * 
	 * @param parclasses
	 *            Array of the parallel classes name
	 * @param path
	 *            Path of the .jar file
	 * @throws Exception
	 *             Thrown if the process go wrong
	 */
	public void writePOPJavaEntries(ArrayList<String> parclasses, String path)
			throws Exception {
		// Create the new entries
		for (int i = 0; i < parclasses.size(); i++) {
			writePOPJavaEntry(parclasses.get(i), path);
		}
	}

	/**
	 * Write a single entry of POP-Java type
	 * 
	 * @param parclass
	 *            Name of the parallel class
	 * @param path
	 *            Path of the .class or .jar file
	 */
	public void writePOPJavaEntry(String parclass, String path) {
		// Delete the previous entry if present in the file
		deleteEntry(parclass);

		// Get the root element of the XML file
		Node codeInfoList = objmap.getFirstChild();

		// Creating the object name element
		Element objectName = objmap.createElement(Constants.NODE_OBJECT_NAME);
		objectName.setTextContent(parclass);

		// Creating the code file element
		Element codeFile = objmap.createElement(Constants.NODE_CODE_FILE);
		codeFile.setTextContent(path);
		// Setting the Type attribute to the element
		codeFile.setAttribute(Constants.ATTR_TYPE, Constants.TYPE_POPJAVA);

		// Creating the platform element
		Element platform = objmap.createElement(Constants.NODE_PLATFORM);
		platform.setTextContent(Constants.ALL_PLATFORM);

		// Creating the code info element
		Element codeInfo = objmap.createElement(Constants.NODE_CODE_INFO);
		// Adding the three others elements to it
		codeInfo.appendChild(objectName);
		codeInfo.appendChild(codeFile);
		codeInfo.appendChild(platform);

		// Adding the code info element to the root element
		codeInfoList.appendChild(codeInfo);
	}

	/**
	 * Add a entry in the XML file for a POP-C++ executable file
	 * 
	 * @param parclass
	 *            Name of the parallel class
	 * @param path
	 *            Path of the executable file
	 * @param arch
	 *            Architecture of the executable
	 */
	public void writePOPCPPEntry(String parclass, String path, String arch) {
		// Delete the previous entry if present in the file
		deleteEntry(parclass);

		// Get the root element of the XML file
		Node codeInfoList = objmap.getFirstChild();

		// Creating the object name element
		Element objectName = objmap.createElement(Constants.NODE_OBJECT_NAME);
		objectName.setTextContent(parclass);

		// Creating the code file element
		Element codeFile = objmap.createElement(Constants.NODE_CODE_FILE);
		codeFile.setTextContent(path);

		// Creating the platform element
		Element platform = objmap.createElement(Constants.NODE_PLATFORM);
		platform.setTextContent(arch);

		// Creating the code info element
		Element codeInfo = objmap.createElement(Constants.NODE_CODE_INFO);
		// Adding the three others elements to it
		codeInfo.appendChild(objectName);
		codeInfo.appendChild(codeFile);
		codeInfo.appendChild(platform);

		// Adding the code info element to the root element
		codeInfoList.appendChild(codeInfo);
	}

	/**
	 * Write all the changes to the file
	 * 
	 * @throws TransformerException
	 *             Thrown if the tranformation from the DOM document to the
	 *             string file go wrong
	 */
	public void writeToFile() throws TransformerException {
		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory
				.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		DOMSource source = new DOMSource(objmap);
		StreamResult result = new StreamResult(new File(xmlFile));
		transformer.transform(source, result);
	}
	
	public void writeToConsole() throws TransformerException{
		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory
				.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		DOMSource source = new DOMSource(objmap);
		StreamResult result = new StreamResult(System.out);
		transformer.transform(source, result);
	}

	/**
	 * Delete the entry if it already exists in the XML file
	 * 
	 * @param parclass
	 *            Name of the parclass
	 */
	private void deleteEntry(String parclass) {
		// Get the root element of the XML file
		Node codeInfoList = objmap.getFirstChild();

		// Get the children of the root element
		NodeList codeInfos = codeInfoList.getChildNodes();
		for (int i = 0; i < codeInfos.getLength(); i++) {
			// Get the current child
			Node codeInfo = codeInfos.item(i);
			// Get the elements of the current node
			NodeList elements = codeInfo.getChildNodes();
			for (int j = 0; j < elements.getLength(); j++) {
				// Get the current element
				Node element = elements.item(j);
				// Check if it's an ObjectName node and check if the name is
				// equal
				if (element.getNodeName().equals(Constants.NODE_OBJECT_NAME)
						&& element.getTextContent().equals(parclass)) {
					while(codeInfo.hasChildNodes())
						codeInfo.removeChild(codeInfo.getFirstChild());
				
					// Remove the entry
					codeInfo.getParentNode().removeChild(codeInfo);
					objmap.normalize();
					break;
				}
			}

		}
	}

}
