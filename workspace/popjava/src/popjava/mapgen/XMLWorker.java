package popjava.mapgen;


import java.io.File;
import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.*;
import javax.xml.validation.*;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;


public class XMLWorker {
	public XMLWorker(){
		
	}
	/**
	 * Validate an XML file with an XML schema
	 * @param xmlFile	location of the XML file
	 * @param xmlSchema	location of the XML schema
	 * @return true if the XML file is valid
	 */
	public boolean isValid(String xmlFile, String xmlSchema) {
		// parse an XML document into a DOM tree
	    DocumentBuilder parser = null;
		try {
			parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
			return false;
		}
	    Document document = null;
		try {
			document = parser.parse(new File(xmlFile));
		} catch (SAXException e1) {
			e1.printStackTrace();
			return false;
		} catch (IOException e1) {
			e1.printStackTrace();
			return false;
		}

	    // create a SchemaFactory capable of understanding WXS schemas
	    SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

	    // load a WXS schema, represented by a Schema instance
	    Source schemaFile = new StreamSource(new File(xmlSchema));
	    Schema schema = null;
		try {
			schema = factory.newSchema(schemaFile);
		} catch (SAXException e1) {
			e1.printStackTrace();
			return false;
		}

	    // create a Validator instance, which can be used to validate an instance document
	    Validator validator = schema.newValidator();

	    // validate the DOM tree
	    try {
	        validator.validate(new DOMSource(document));
	    } catch (SAXException e) {
	    	e.printStackTrace();
	        return false;
	    } catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Load the XML file into a DOM document
	 * @param file	Path of the XML file
	 * @return	The DOM document loaded with the XML file
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	protected Document load(String file) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.parse(new File(file));
		return doc;
	}

	/**
	 * Create a new empty DOM document
	 * @return The new DOM document
	 * @throws ParserConfigurationException
	 */
	protected Document create() throws ParserConfigurationException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.newDocument();
		return doc;
	}
}
