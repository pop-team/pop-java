package popjava.system;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Random;

import javassist.util.proxy.ProxyFactory;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import popjava.PopJava;
import popjava.base.*;
import popjava.baseobject.ObjectDescription;
import popjava.baseobject.POPAccessPoint;
import popjava.codemanager.AppService;
import popjava.codemanager.POPJavaAppService;
import popjava.combox.ComboxSocketFactory;

import popjava.serviceadapter.POPAppService;
import popjava.serviceadapter.POPJobManager;
import popjava.util.LogWriter;
import popjava.util.Util;

/**
 * This class is responsible for the initialization of a POP-Java application. It has also the responsibility to retrieve the configuration parameters.
 */
public class POPSystem {
	private static POPRemoteLogThread prlt;
	private static String Platform = "linux";
	
	/**
	 * POP-Java location environement variable name
	 */
	public static final String PopLocationEnviromentName = "POP_LOCATION";
//	public static final String PopPluginLocationEnviromentName = "POP_PLUGIN_LOCATION";
//	public static final String PopAppCoreServiceEnviromentName = "POP_APP_CORE_SERVICE_CONFIG";
//	public static final String DefaultPopLocation = "/home/clementval/popj";
//	public static final String DefaultPopPluginLocation = "/home/clementval/popj/plugin";
//	public static final String DefaultPopAppCoreService = "/home/clementval/popc/services/appservice";
//	public static String DefaultAppServiceCodeFile = "";
	
	/**
	 * POP-Java Job service access point
	 */
	public static POPAccessPoint JobService = new POPAccessPoint();
	
	/**
	 * POP-Java application service access point 
	 */
	public static POPAccessPoint AppServiceAccessPoint = new POPAccessPoint();
	
	public static void writeLog(String log){
		try {
			POPAppService app = (POPAppService)PopJava.newActive(POPAppService.class, POPSystem.AppServiceAccessPoint);
			app.logPJ(app.getPOPCAppID(), log);
		} catch (POPException e) {
			/*try{
				POPAppService app = (POPAppService)PopJava.newActive(POPJavaAppService.class, POPSystem.AppServiceAccessPoint);
				app.logPJ(app.getPOPCAppID(), log);
			} catch (POPException e2) {
				e2.printStackTrace();
			}*/
		}
	}

	static {
		// Trick :(( I don't know why the system i386 doesn't work
		String osName = System.getProperty("os.name");
		// String osArchitect = System.getProperty("os.arch");
		String osArchitect = "i686";
		Platform = String.format("%s-pc-%s", osArchitect, osName);
//		String popLocation = POPSystem.getPopLocation();
//		POPJavaObjectExecuteCommand = String.format(
//				"/usr/bin/java -cp %s popjava.broker.Broker -codelocation=",
//				popLocation);
//		DefaultAppServiceCodeFile = POPSystem.getPopAppCoreService();
	}

	/**
	 * Creates a new instance of POPSystem
	 */
	public POPSystem() {
	}

	/**
	 * Retrieve the local IP address and format it as an int
	 * @return int value of the local IP address
	 */
	public static int getIPAsInt() {
		try {
			java.net.InetAddress localAddress = java.net.InetAddress
					.getLocalHost();
			byte[] ip = localAddress.getAddress();
			return ip[0] * 256 * 256 * 256 + ip[1] * 256 * 256 + ip[2] * 256
					+ ip[3];
		} catch (java.net.UnknownHostException e) {

		}
		return 127 * 256 * 256 * 256;
	}

	/**
	 * Retrieve the local IP address and format it as a String
	 * @return String value of the local IP address
	 */
	public static String getIP() {
		String result = "";
		try {
			java.net.InetAddress localAddress = java.net.InetAddress
					.getLocalHost();
			result = localAddress.getHostAddress();
		} catch (java.net.UnknownHostException e) {
			result = "127.0.0.1";
		}
		return result;
	}

	/**
	 * Retrieve the local hostname and format it as a String
	 * @return String value of the local hostname
	 */
	public static String getHostName() {
		String result = "";
		try {
			java.net.InetAddress localAddress = java.net.InetAddress
					.getLocalHost();
			result = localAddress.getHostName();

		} catch (java.net.UnknownHostException e) {
			result = "localhost";
		}
		return result;
	}

	/**
	 * Get the host of the local node
	 * @return	Host name as a string value
	 */
	public static String getHost() {
		String result = "";
		Enumeration<NetworkInterface> en;
		try {
			en = NetworkInterface.getNetworkInterfaces();
			while(en.hasMoreElements()){
				NetworkInterface ni = en.nextElement();
				Enumeration<InetAddress> enina = ni.getInetAddresses();
				while(enina.hasMoreElements()){
					InetAddress ina = enina.nextElement();
					if(!ina.getHostAddress().contains(":") 
							&& !ina.getHostAddress().equals("127.0.0.1")
							&& !ina.getHostAddress().equals("127.0.1.1")){
						result = ina.getHostAddress();
						break;
					}
				}
			}
		} catch (SocketException e) {
			result = "localhost";
		}
		
		return result;
	}

	/**
	 * Get the default local access point
	 * @return the default local access point
	 */
	public static POPAccessPoint getDefaultAccessPoint() {
		POPAccessPoint parrocAccessPoint = new POPAccessPoint();
		parrocAccessPoint.setAccessString(String.format("%s://127.0.0.1:0",
				ComboxSocketFactory.Protocol));
		return parrocAccessPoint;
	}

	/**
	 * Get the default object description
	 * @return	a new empty object description
	 */
	public static ObjectDescription getDefaultOD() {
		ObjectDescription od = new ObjectDescription();
		return od;
	}

	/**
	 * Get the local environment variable
	 * @param name	Name of the variable
	 * @return	Variable value or empty string
	 */
	public static String getEnviroment(String name) {
		String enviroment = System.getenv(name);
		if (enviroment == null)
			enviroment = "";
		else
			enviroment = enviroment.trim();
		return enviroment;
	}

	/**
	 * Get the system platform
	 * @return platform as a string value
	 */
	public static String getPlatform() {
		return Platform;
	}

	/**
	 * Entry point for the application scope initialization
	 * @param argvs	Any arguments to pass to the initialization
	 * @return	true if the initialization is succeed
	 * @throws POPException thrown is any problems occurred during the initialization
	 */
	public static boolean initialize(String... argvs) throws POPException {
		ArrayList<String> argvList = new ArrayList<String>();
		for (String str : argvs)
			argvList.add(str);
		return initialize(argvList);
	}
	
	private static String getNeededClasspath(){
		
		try{
			if(ProxyFactory.class.
					getProtectionDomain().getCodeSource().getLocation().
					getPath().equals(POPJavaConfiguration.getPopJavaJar())){
				return POPJavaConfiguration.getPopJavaJar();
			}
		}catch(Exception e){
		}
		
		return POPJavaConfiguration.getPOPJavaCodePath();
	}

	/**
	 * Initialize the application scope services 
	 * @param argvList	Any arguments to pass to the initialization
	 * @return	true if the initialization is succeed
	 * @throws POPException	thrown is any problems occurred during the initialization
	 */
	public static boolean initialize(ArrayList<String> argvList)
			throws POPException {		
		String POPJavaObjectExecuteCommand = String.format(
				POPJavaConfiguration.getBrokerCommand(),
				getNeededClasspath());
		
		String jobservice = Util.removeStringFromArrayList(argvList,
				"-jobservice=");
		if (jobservice == null || jobservice.length() == 0) {
			jobservice = String.format("%s:%d", POPSystem.getHostName(),
					POPJobManager.DEFAULT_PORT);
		}
		JobService.setAccessString(jobservice);
		String appservicecode = Util.removeStringFromArrayList(argvList,
				"-appservicecode=");

		
		if (appservicecode == null || appservicecode.length() == 0) {
			appservicecode = POPJavaConfiguration.getPopAppCoreService();
		}
		String proxy = Util.removeStringFromArrayList(argvList, "-proxy=");
		String appservicecontact = Util.removeStringFromArrayList(argvList,
				"-appservicecontact=");
		if ((jobservice == null || jobservice.length() == 0)
				&& (appservicecontact == null || appservicecontact.length() == 0)){
			return false;
		}
		
		AppService coreServiceManager = getCoreService(proxy, appservicecontact, appservicecode);
		
		System.out.println("coreServiceManager "+(coreServiceManager == null));
		
		if(coreServiceManager != null){
			AppServiceAccessPoint = coreServiceManager.getAccessPoint();
			System.out.println("!!! AP: "+AppServiceAccessPoint.toString());
			prlt = new POPRemoteLogThread(coreServiceManager.getPOPCAppID());
			prlt.start();
		}
		
		String codeconf = Util
				.removeStringFromArrayList(argvList, "-codeconf=");
		
		if (codeconf == null || codeconf.length() == 0) {
			codeconf = String.format("%s%setc%sdefaultobjectmap.xml",
					POPJavaConfiguration.getPopJavaLocation(),
					File.separator,
					File.separator);
		}
		
		return initCodeService(codeconf, POPJavaObjectExecuteCommand, coreServiceManager);
	}
	
	private static AppService getCoreService(String proxy, String appservicecontact, String appservicecode){
		if (appservicecontact == null || appservicecontact.length() == 0) {
			String url = "";
			if (proxy == null || proxy.length() == 0) {
				url = appservicecode;
			} else {
				url = String.format("%s -proxy=%s", appservicecode, proxy);
			}
			if(new File(appservicecode).exists()){
				try{
					return createAppCoreService(url);
				}catch(POPException e){
					e.printStackTrace();
				}
			}
			
		} else {
			POPAccessPoint accessPoint = new POPAccessPoint();
			accessPoint.setAccessString(appservicecontact);
			try{
				return (POPAppService) PopJava.newActive(
						POPAppService.class, accessPoint);
			}catch(POPException e){
				e.printStackTrace();
			}
		}
		
		//Create a pure java AppService as a backup (probably no popc++ present)
		try{
			LogWriter.writeDebugInfo("Create native popjava service");
			return (AppService) PopJava.newActive(POPJavaAppService.class);			
		}catch(POPException e){
			e.printStackTrace();
		}
		return null;
		
	}
	
	/**
	 * Initialize the CodeMgr by reading the object map and register all code location
	 * @param fileconf			Object map file location
	 * @param appCoreService	Reference to the AppCoreService
	 * @return	true if the initialization is well done
	 * @throws POPException 
	 */
	public static boolean initCodeService(String fileconf,
			String POPJavaObjectExecuteCommand,
			AppService appCoreService) throws POPException {
		fileconf = fileconf.trim();
		XMLWorker xw = new XMLWorker();
		
		if(!new File(POPJavaConfiguration.getPopJavaLocation()+"/etc/objectmap.xsd").exists()){
			System.out.println("Could not open objectmap.xsd at "+POPJavaConfiguration.getPopJavaLocation());
			return false;
		}
		
		if(!xw.isValid(fileconf, POPJavaConfiguration.getPopJavaLocation()+"/etc/objectmap.xsd")){
			throw new POPException(0, "Object map not valid");
		}
		
		if (appCoreService == null || fileconf == null || fileconf.length() == 0){
			return false;
		}
		
		fileconf = fileconf.replaceAll("\"", "");
		DocumentBuilder builder;
		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = builder.parse(new File(fileconf));
			// mainNode: <CodeInfoList>
			Element codeInfoListElement = document.getDocumentElement();
			// list: list of <CodeInfo> & text node
			NodeList list = codeInfoListElement.getChildNodes();
			for (int index = 0; index < list.getLength(); ++index) {
				// node: a <CodeInfo> node
				Node node = list.item(index);
				String objectname = "";
				String codefile = "";
				String platform = "";
				// Handle <CodeInfo> node only
				if (node.getNodeType() == Node.ELEMENT_NODE
						&& node.getNodeName().equals("CodeInfo")) {
					Node childNode = node.getFirstChild();
					while (childNode != null) {
						if (childNode.getNodeType() == Node.ELEMENT_NODE) {

							if (childNode.getNodeName().equals("ObjectName")) {
								objectname = childNode.getTextContent();
							} else if (childNode.getNodeName().equals(
									"CodeFile")) {
								codefile = childNode.getTextContent();
								Element codeFileElement = (Element) childNode;
								String codeFileType = codeFileElement
										.getAttribute("Type");
								if (codeFileType != null
										&& codeFileType
												.equalsIgnoreCase("popjava")) {
									codefile = POPJavaObjectExecuteCommand  + codefile;
								}
							} else if (childNode.getNodeName().equals(
									"PlatForm")) {
								platform = childNode.getTextContent();
							}
						}
						childNode = childNode.getNextSibling();
					}
					appCoreService.registerCode(objectname, platform, codefile);
				}
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * Start the application scope services. This services is a POP-C++ parallel object.
	 * @param codelocation	location of the POP-C++ AppCoreService executable file
	 * @return	Interface of AppCoreService
	 * @throws POPException
	 */
	public static AppService createAppCoreService(String codelocation)
			throws POPException {
		Random random = new Random((new Date()).getTime());
		int maxUsignedByte = 255;
		byte[] bytes = new byte[255];
		random.nextBytes(bytes);
		String randString = "";
		for (int i = 0; i < 255; i++) {
			char randChar = (char) (((double) (bytes[i] + 128) / maxUsignedByte) * 25 + 97);
			randString += Character.toString(randChar);
		}

		ObjectDescription objectDescription = POPSystem.getDefaultOD();
		objectDescription.setHostname(POPSystem.getHost());
		objectDescription.setCodeFile(codelocation);
		return (POPAppService) PopJava.newActive(POPAppService.class,
				objectDescription, randString, false, codelocation);
	}
	
	public static void end(){
		if(prlt != null){ //If initialize failed, prlt will be null
			try {
				Thread.sleep(1000); //TODO: this looks like a huge HACK
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			prlt.setRunning(false);
		}
	}
	
}
