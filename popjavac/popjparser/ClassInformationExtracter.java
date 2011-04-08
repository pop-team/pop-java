import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
/*
 * This class is used to read the POP-C++ additional informations file
 */
public class ClassInformationExtracter {
	private static final String ELEMENT_POPJPARSER_INFOS = "popjparser-infos";
	private static final String ELEMENT_POPC_PARCLASS = "popc-parclass";
	private static final String ITEM_FILENAME = "file";
	private static final String ITEM_CLASSUID = "classuid";
	private static final String ITEM_HASDESTRUCTOR = "hasDestructor";
	private static final String ITEM_NAME = "name";
	
	
	private String infoFile;
	private ArrayList<ClassInformation> infos;
	
	public ClassInformationExtracter(String infoFile){
		this.infoFile = infoFile;
		infos = new ArrayList<ClassInformation>();
	}
	
	/*
   * Load the XML file into a DOM document
   */
	public void loadFile(){
		DocumentBuilder builder;
		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = builder.parse(new File(infoFile));
			Element popjparserInformation = document.getDocumentElement();
			NodeList list = popjparserInformation.getChildNodes();
			for (int index = 0; index < list.getLength(); ++index) {
				Node node = list.item(index);
				if (node.getNodeType() == Node.ELEMENT_NODE	&& node.getNodeName().equals(ELEMENT_POPC_PARCLASS)) {
					NamedNodeMap attributes = node.getAttributes();
					ClassInformation ci = new ClassInformation(attributes.getNamedItem(ITEM_FILENAME).getTextContent());
					ci.setClassName(attributes.getNamedItem(ITEM_NAME).getTextContent());
					ci.setClassUID(Integer.parseInt(attributes.getNamedItem(ITEM_CLASSUID).getTextContent()));
					ci.setDestructor(Boolean.parseBoolean(attributes.getNamedItem(ITEM_HASDESTRUCTOR).getTextContent()));
					infos.add(ci);
				}
			}
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	/*
   * Get all ClassInformation in the files
   */
	public ArrayList<ClassInformation> getInfos(){
		return infos;
	}
	
	/*
   * Get informations about a specific parclass
   */
	public ClassInformation getInfo(String filename) {
		for (int i = 0; i < infos.size(); i++) {
			ClassInformation ci = infos.get(i);
			if(ci.getFilename().equals(filename))
				return ci;
		}
		return null;
	}
	
}
