package popjava.interfacebase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import popjava.PopJava;
import popjava.annotation.POPObjectDescription;
import popjava.base.BindStatus;
import popjava.base.MessageHeader;
import popjava.base.POPErrorCode;
import popjava.base.POPException;
import popjava.base.Semantic;
import popjava.baseobject.ObjectDescription;
import popjava.baseobject.POPAccessPoint;
import popjava.broker.Broker;
import popjava.buffer.BufferFactory;
import popjava.buffer.BufferFactoryFinder;
import popjava.buffer.BufferXDR;
import popjava.buffer.POPBuffer;
import popjava.codemanager.AppService;
import popjava.combox.Combox;
import popjava.combox.ComboxAllocate;
import popjava.combox.ComboxFactory;
import popjava.combox.ComboxFactoryFinder;
import popjava.util.ssl.SSLUtils;
import popjava.dataswaper.POPString;
import popjava.service.deamon.POPJavaDeamonConnector;
import popjava.service.jobmanager.POPJavaAppService;
import popjava.service.jobmanager.POPJavaJobManager;
import popjava.service.jobmanager.network.POPConnectorDirect;
import popjava.serviceadapter.POPAppService;
import popjava.serviceadapter.POPJobManager;
import popjava.serviceadapter.POPJobService;
import popjava.system.POPJavaConfiguration;
import popjava.system.POPSystem;
import popjava.util.Configuration;
import popjava.util.LogWriter;
import popjava.util.POPRemoteCaller;
import popjava.util.SystemUtil;
import popjava.util.Util;


/**
 * Interface side of a POP-Java parallel object. This object is the local representative of the parallel object
 *	
 */
public class Interface {

	protected Combox combox;
	//protected Buffer popBuffer;
	protected POPAccessPoint popAccessPoint = new POPAccessPoint();
	protected ObjectDescription od = new ObjectDescription();

	private final AtomicInteger requestID = new AtomicInteger(10000);
	
	private static final Configuration conf = Configuration.getInstance();
	
	/**
	 * Default Interface constructor
	 */
	public Interface() {
		od = new ObjectDescription();
	}

	/**
	 * Create an Interface by giving the access point of the parallel object
	 * @param accessPoint	Access point of the parallel object
	 * @throws POPException	thrown of the interface cannot be bind with the parallel object
	 */
	public Interface(POPAccessPoint accessPoint) throws POPException {
		popAccessPoint = accessPoint;
		bind(accessPoint);
	}

	/**
	 * Create an Interface by giving the access point of the parallel object
	 * @param accessPoint	Access point of the parallel object
	 * @param od A custom OD for specifying possible connection parameters
	 * @throws POPException	thrown of the interface cannot be bind with the parallel object
	 */
	public Interface(POPAccessPoint accessPoint, ObjectDescription od) throws POPException {
		popAccessPoint = accessPoint;
		this.od.merge(od);
		bind(accessPoint);
	}

	/**
	 * Serialization of the Interface into the buffer
	 * @param buffer	Buffer to serialize in
	 * @return	true if the serialization is finished without any problems
	 */
	public boolean serialize(POPBuffer buffer) {
		od.serialize(buffer);
		popAccessPoint.serialize(buffer);
		int ref = addRef();
		buffer.putInt(ref);
		return true;
	}

	/**
	 * Deserialize an Interface from a buffer
	 * @param buffer	Buffer to deserialize from
	 * @return	True if the deserialization has finished without any problems
	 */
	public boolean deserialize(POPBuffer buffer) {
		boolean result = true;
		od.deserialize(buffer);
		popAccessPoint.deserialize(buffer);
		
		// if a certifate was sent with the request, save it
		byte[] certificate = popAccessPoint.getX509certificate();
		if (certificate != null && certificate.length > 0) {
			SSLUtils.addCertToTempStore(certificate, true);
		}
		
		int ref = buffer.getInt(); //related to the addRef called in serialize()
		if (ref > 0) {
			try {
				bind(popAccessPoint);
			} catch (POPException e) {
				result = false;
				LogWriter.writeDebugInfo("[Interface] Deserialize. Cannot bind to " + popAccessPoint.toString());
				e.printStackTrace();
			}
			if (result){
				decRef();
			}
		}
		return result;
	}
	
	/**
	 * Get the remote caller of the host the object is connected to.
	 * @return the remote object connection
	 */
	public POPRemoteCaller getRemote() {
		return combox.getRemoteCaller();
	}

	/**
	 * Return the access point of the parallel object associated with this interface
	 * @return	Access point of the associated parallel object
	 */
	public POPAccessPoint getAccessPoint() {
		return popAccessPoint;
	}

	/**
	 * Set the access point associated with this interface
	 * @param accessPoint	Access point to associate
	 */
	public void setAccessPoint(POPAccessPoint accessPoint) {
		this.popAccessPoint = accessPoint;
	}

	/**
	 * Return the object description associated with this interface
	 * @return ObjectDescription of this interface
	 */
	public ObjectDescription getOD() {
		return od;
	}

	/**
	 * Associate an object description with this interface
	 * @param od	Object descritption to associate
	 */
	public void setOd(ObjectDescription od) {
		this.od = od;
	}

	
	public void release() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Allocate resource for the associated parallel object
	 * @param objectName	Name of the object
	 * @return	True if the interface can allocate some resources
	 * @throws POPException	thrown if any exception occurred during the allocating process
	 */
	public boolean allocate(String objectName) throws POPException {

		//Init the AP array for object contact
		popAccessPoint = new POPAccessPoint();
		POPAccessPoint[] allocatedAccessPoint = new POPAccessPoint[1];
		allocatedAccessPoint[0] = popAccessPoint;
		
		//Init the AP array for remote job contacts
		POPAccessPoint[] remotejobscontact = new POPAccessPoint[1];
		POPAccessPoint remotjob = new POPAccessPoint();
		remotejobscontact[0] = remotjob;
		
		boolean canExecLocal = false;
		canExecLocal = tryLocal(objectName, popAccessPoint, od);
		
		if (!canExecLocal) {
			if(!od.getHostName().isEmpty()){
				if(Thread.currentThread().isInterrupted()){
					return false;
				}
				throw new POPException(-1, "Could not create "+objectName+" on "+od.getHostName());
			}
			
			boolean allocated = allocateThroughJobmanager(objectName, allocatedAccessPoint,
                    remotejobscontact);
			
			if(!allocated && od.getHostName().isEmpty()){
			    LogWriter.printDebug("No url specified for "+objectName+", fallback to localhost");
			    od.setHostname("localhost");
			    tryLocal(objectName, popAccessPoint, od);
			}
		}
		
		return bind(popAccessPoint);
	}

    public boolean allocateThroughJobmanager(String objectName,
            POPAccessPoint[] allocatedAccessPoint,
            POPAccessPoint[] remotejobscontact) {
        // ask the job manager to allocate the broker
        String platforms = od.getPlatform();
			
        if (platforms.isEmpty()) {
        	AppService appCoreService = null;
        	appCoreService = PopJava.newActive(POPAppService.class, POPSystem.appServiceAccessPoint);
        	POPString popStringPlatorm = new POPString();
        	appCoreService.getPlatform(objectName, popStringPlatorm);
        	platforms = popStringPlatorm.getValue();
        	if (platforms.length() <= 0) {
        		throw new POPException(
        				POPErrorCode.OBJECT_EXECUTABLE_NOTFOUND,
        				"OBJECT_EXECUTABLE_NOTFOUND");
        	}
        	od.setPlatform(platforms);
        	//appCoreService.exit();
        }
        
        // Global Resource management system --> Find a resource.
        String jobUrl = od.getJobUrl();
        POPAccessPoint jobContact = new POPAccessPoint();
        if (jobUrl.length() > 0) {
        	jobContact.setAccessString(jobUrl);
        } else {
        	jobContact = POPSystem.jobService;
        }

        if (jobContact.isEmpty()) {
			ComboxFactoryFinder finder = ComboxFactoryFinder.getInstance();
			String[] jmProtocols = conf.getJobManagerProtocols();
			for (int i = 0; i < jmProtocols.length; i++) {
				String protocol = jmProtocols[i];
				ComboxFactory factory = finder.findFactory(protocol);
				if (factory != null) {
					jobContact.setAccessString(String.format("%s://%s:%d", 
						factory.getComboxName(), 
						POPSystem.getHostIP(),
						conf.getJobManagerPorts()[i]));
					break;
				}
			}
        }

        POPJobService jobManager = null;
        try{
			// XXX There are some method not found error if we use the generic
        	//if(conf.CONNECT_TO_POPCPP || conf.START_JOBMANAGER){
        	//	jobManager = PopJava.newActive(POPJobService.class, jobContact);
        	//}
			if (conf.isConnectToPOPcpp())
        		jobManager = PopJava.newActive(POPJobManager.class, jobContact);
			else if (conf.isConnectToJavaJobmanager())
        		jobManager = PopJava.newActive(POPJavaJobManager.class, jobContact);
			else
        		jobManager = PopJava.newActive(POPJobService.class, jobContact);
        }catch(Exception e){
        	e.printStackTrace();
        }
		
        if(jobManager == null){
            return false;
        }
		
		int createdCode;
		try {
			createdCode = jobManager.createObject(POPSystem.appServiceAccessPoint, objectName, od, allocatedAccessPoint.length, 
					allocatedAccessPoint, remotejobscontact.length, remotejobscontact);
		} catch (Exception e) {
			createdCode = POPErrorCode.POP_EXEC_FAIL;
			LogWriter.writeDebugInfo("[Interface] Exception while calling job manager: %s", e.getCause());
		} finally {
			jobManager.exit();
		}
        if (createdCode != 0) {
        	switch (createdCode) {
        		case POPErrorCode.POP_EXEC_FAIL:
        			throw new POPException(createdCode,	"OBJECT_EXECUTABLE_NOTFOUND");
        		case POPErrorCode.POP_JOBSERVICE_FAIL:
        			throw new POPException(createdCode, "NO_RESOURCE_MATCH "+objectName);
        		default:
        			throw new POPException(createdCode, "UNABLE_TO_CREATED_THE_PARALLEL_OBJECT");
        	}
        }

		// XXX What does this really do?
        for (int i = 0; i < allocatedAccessPoint.length; i++) {
        	if(allocatedAccessPoint[0].size() >= 1 && (
        			allocatedAccessPoint[0].get(0).getHost().equals("127.0.0.1") ||
        			allocatedAccessPoint[0].get(0).getHost().equals("127.0.1.1"))){

        		allocatedAccessPoint[0].get(0).setHost(remotejobscontact[0].get(0).getHost());
        	}
        }
        popAccessPoint.setAccessString(allocatedAccessPoint[0].toString());
        
        return true;
    }

	/**
	 * Bind the interface with a parallel object (Broker-side)
	 * @param accesspoint	Access point of the parallel object (Broker-side)s
	 * @return	true if the interface is binded to the broker-side
	 * @throws POPException	thrown if any exception occurred during the binding process
	 */
	protected boolean bind(POPAccessPoint accesspoint) throws POPException {

		if (accesspoint == null || accesspoint.isEmpty()){
			throw POPException.throwAccessPointNotAvailableException(accesspoint);
		}
		
		ComboxFactoryFinder finder = ComboxFactoryFinder.getInstance();
		
		if (combox != null){
			combox.close();
		}
		
		for (int i = 0; i < accesspoint.size(); i++) {
			String protocol = accesspoint.get(i).getProtocol();
			ComboxFactory factory = finder.findFactory(protocol);
			// choose the first available protocol
			if (factory != null) {
				try {
					String networkUUID = od.getNetwork();
					if (networkUUID == null || networkUUID.isEmpty()) {
						networkUUID = conf.getDefaultNetwork();
					}
					combox = factory.createClientCombox(networkUUID);
				} catch(IOException e) {
					LogWriter.writeExceptionLog(e);
					continue;
				}
				break;
			}
		}
		
		if (combox != null && combox.connectToServer(accesspoint, conf.getConnectionTimeout())) {

			BindStatus bindStatus = new BindStatus();
			bindStatus(bindStatus);
			switch (bindStatus.getCode()) {
			case BindStatus.BIND_OK:
				this.getOD().setPlatform(bindStatus.getPlatform());
				negotiateEncoding(conf.getSelectedEncoding(), bindStatus.getPlatform());
				return true;
			case BindStatus.BIND_FORWARD_SESSION:
			case BindStatus.BIND_FORWARD_PERMANENT:
				break;
			default:
				break;
			}
		} else {
			POPException.throwObjectBindException(accesspoint);
		}
		
		return true;
	}

	/**
	 * Get the current binding status 
	 * @param bindStatus	status
	 * @throws POPException	remote exception check, caused by
	 */
	private void bindStatus(BindStatus bindStatus) throws POPException {
		if (combox == null){
			throw POPException.throwComboxNotAvailableException();
		}

		POPBuffer popBuffer = combox.getBufferFactory().createBuffer();
		MessageHeader messageHeader = new MessageHeader(0,
				MessageHeader.BIND_STATUS_CALL, Semantic.SYNCHRONOUS);
		messageHeader.setRequestID(requestID.incrementAndGet());
		
		popBuffer.setHeader(messageHeader);
		popDispatch(popBuffer);
		int errorcode = 0;
		POPBuffer responseBuffer = combox.getBufferFactory().createBuffer();
		popResponse(responseBuffer, messageHeader.getRequestID());
		errorcode = responseBuffer.getInt();
		bindStatus.setCode(errorcode);
		String platform = responseBuffer.getString();
		String info = responseBuffer.getString();
		bindStatus.setPlatform(platform);
		bindStatus.setInfo(info);

	}

	/**
	 * Negotiate the encoding between the interface-side and the broker-side
	 * @param info		Encoding information
	 * @param platform	Platform of the interface
	 * @throws POPException	thrown if any exception occurred during the negotiating process
	 */
	private void negotiateEncoding(String info, String platform)
			throws POPException {
		if (combox == null){
			throw POPException.throwComboxNotAvailableException();
		}
		POPBuffer popBuffer = combox.getBufferFactory().createBuffer();
		MessageHeader messageHeader = new MessageHeader(0, MessageHeader.GET_ENCODING_CALL, Semantic.SYNCHRONOUS);
		messageHeader.setRequestID(requestID.incrementAndGet());
		popBuffer.setHeader(messageHeader);
		popBuffer.putString(conf.getSelectedEncoding());

		popDispatch(popBuffer);

		boolean result = false;
		POPBuffer responseBuffer = combox.getBufferFactory().createBuffer();
		
		popResponse(responseBuffer, messageHeader.getRequestID());
		result = responseBuffer.getBoolean();
		if (result) {
			BufferFactory bufferFactory = BufferFactoryFinder.getInstance().findFactory(conf.getSelectedEncoding());
			combox.setBufferFactory(bufferFactory);
			
			//TODO: Check out why this was done
			//popBuffer = bufferFactory.createBuffer();
		}
	}

	public int addRef() {
		if (combox == null){
			return -1;
		}
		POPBuffer popBuffer = combox.getBufferFactory().createBuffer();
		MessageHeader messageHeader = new MessageHeader(0, MessageHeader.ADD_REF_CALL, Semantic.SYNCHRONOUS);
		messageHeader.setRequestID(requestID.incrementAndGet());
		popBuffer.setHeader(messageHeader);

		int result = 0;
		try {
			popDispatch(popBuffer);
			POPBuffer responseBuffer = combox.getBufferFactory().createBuffer();
			popResponse(responseBuffer, messageHeader.getRequestID());
			result = responseBuffer.getInt();
		} catch (POPException e) {
			return -1;
		}
		return result;
	}

	public int decRef() {
		if (combox == null){
			return -1;
		}

		POPBuffer popBuffer = combox.getBufferFactory().createBuffer();
		MessageHeader messageHeader = new MessageHeader(0,
				MessageHeader.DEC_REF_CALL, Semantic.SYNCHRONOUS);
		messageHeader.setRequestID(requestID.incrementAndGet());
		popBuffer.setHeader(messageHeader);

		int result = 0;
		try {
			popDispatch(popBuffer);
			POPBuffer responseBuffer = combox.getBufferFactory().createBuffer();
			popResponse(responseBuffer, messageHeader.getRequestID());
			result = responseBuffer.getInt();
		} catch (POPException e) {
			return -1;
		}
		return result;
	}

	/**
	 * Check if the parallel object associated with this interface is still alive
	 * @return	true if the parallel object is alive
	 */
	public boolean isAlive() {
		if (combox == null){
			return false;
		}
		POPBuffer popBuffer = combox.getBufferFactory().createBuffer();
		MessageHeader messageHeader = new MessageHeader(0,
				MessageHeader.OBJECT_ALIVE_CALL, Semantic.SYNCHRONOUS);
		messageHeader.setRequestID(requestID.incrementAndGet());
		popBuffer.setHeader(messageHeader);

		popDispatch(popBuffer);
		boolean result = false;
		try {
			POPBuffer responseBuffer = combox.getBufferFactory().createBuffer();
			popResponse(responseBuffer, messageHeader.getRequestID());
			result = responseBuffer.getBoolean();
		} catch (POPException e) {
			return false;
		}
		return result;
	}

	/**
	 * Kill the associated parallel object
	 */
	public void kill() {
		if (combox == null){
			return;
		}
		POPBuffer popBuffer = combox.getBufferFactory().createBuffer();
		MessageHeader messageHeader = new MessageHeader(0,
				MessageHeader.KILL_ALL, Semantic.SYNCHRONOUS);
		messageHeader.setRequestID(requestID.incrementAndGet());
		popBuffer.setHeader(messageHeader);

		popDispatch(popBuffer);
		try {
			POPBuffer responseBuffer = combox.getBufferFactory().createBuffer();
			popResponse(responseBuffer, messageHeader.getRequestID());
		} catch (POPException e) {
		}
	}

	/**
	 * Try a local execution for the associated parallel object
	 * @param objectName	Name of the object
	 * @param accesspoint	Output parameter - Access point of the object
	 * @param od the object description
	 * @return true if the local execution succeed
	 * @throws POPException thrown if any exception occurred during the creation process
	 */
	public static boolean tryLocal(String objectName, POPAccessPoint accesspoint, ObjectDescription od)
			throws POPException {
		//String hostname = "";
		String joburl ="";
		String codeFile = "";
		// Get ClassName

		// Check if Od is empty
		boolean odEmpty = od.isEmpty();
		if (odEmpty){
			return false;
		}

		joburl = od.getHostName();
		/*if (joburl == null || joburl.length() == 0 || !Util.sameContact(joburl, POPSystem.getHost())){
			return false;
		}*/
		
		if(joburl == null || joburl.isEmpty()){
			return false;
		}
		LogWriter.writeDebugInfo("[Interface] Creating %s on %s with %s", objectName, joburl, Arrays.toString(od.getProtocols()) );

		codeFile = od.getCodeFile();
		
		// Host name existed
		if (codeFile == null || codeFile.length() == 0) {
			
			codeFile = getRemoteCodeFile(objectName);
			if (codeFile == null || codeFile.length() == 0){
				return false;
			}
		}

		// parameters for localExec
		String[] rports = null;
		String[] protocols = null;	
		
		// object port in od.url
		int urlPortIndex = joburl.lastIndexOf(":");
		if (urlPortIndex > 0) {
			rports = new String[] { joburl.substring(urlPortIndex + 1) };
			joburl = joburl.substring(0, urlPortIndex);
		}
		
		// object protocol(s) with port(s), may be empty
		int nbProtocols = od.getProtocols().length;
		
		
		// empty protocol in od, all or default
		if (nbProtocols == 1 && od.getProtocols()[0].isEmpty()) {			
			ComboxFactoryFinder finder = ComboxFactoryFinder.getInstance();
			ComboxFactory[] protocolsFactories = finder.getAvailableFactories();
			int protocolsCount = protocolsFactories.length;
			
			// use default protocol if a port is set in url
			if (rports != null) {
				protocols = new String[] { conf.getDefaultProtocol() };
			}
			// use all other protocols otherwise
			else {
				protocols = new String[protocolsCount];
				rports = new String[protocolsCount];
				for (int i = 0; i < protocolsCount; i++) {
					protocols[i] = protocolsFactories[i].getComboxName();
					rports[i] = "0";
				}
			}
		}
		// multiple protocols
		else if (nbProtocols >= 1) {
			// missmatch between number of choosen protocols and previously set ports (1 from od.host)
			if (rports != null && nbProtocols != rports.length) {
				throw new POPException(POPErrorCode.METHOD_ANNOTATION_EXCEPTION, "You can't specify a port in the url and have multiple protocols.");
			}
			
			// if we have to set ports or check that we don't set them multiuple times
			boolean setPorts = rports == null;
			if (setPorts) {
				rports = new String[nbProtocols];
			}
			protocols = new String[nbProtocols];
			
			for (int i = 0; i < nbProtocols; i++) {
				String protocol = od.getProtocols()[i];
				String port = null;
				
				// look for port in protocol
				int portIdx = protocol.lastIndexOf(":");
				if (portIdx > 0) {
					port = protocol.substring(portIdx + 1);
					protocol = protocol.substring(0, portIdx);
				}
				
				// can't set ports multiple times
				if (!setPorts && port != null) {
					throw new POPException(POPErrorCode.METHOD_ANNOTATION_EXCEPTION, "You can't specify ports multiple times");
				}
				// set port array
				if (setPorts) {
					if (port == null) {
						rports[i] = "0";
					} else {
						rports[i] = port;
					}
				}
				
				protocols[i] = protocol;
			}
		}
		// no protocols, error
		else {
			throw new POPException(POPErrorCode.METHOD_ANNOTATION_EXCEPTION, "At least one protocol should be specified");
		}
		
		// has custom app service
		POPAccessPoint appService = POPSystem.appServiceAccessPoint;
		if (od.getOriginAppService() != null) {
			appService = od.getOriginAppService();
		}
		
		int status = localExec(joburl, codeFile, objectName, protocols, rports,
				POPSystem.jobService, appService, accesspoint,
				od);

		if (status != 0) {
			// Throw exception
			LogWriter.writeDebugInfo("[Interface] Could not create "+objectName+" on "+joburl);
		}
		
		return (status == 0);
	}
	
	/**
	 * Lookup local code manager for the binary source....
	 * @param objectName the name of the object {@link Class#getName()}
	 * @return the remote code location
	 */
	private static String getRemoteCodeFile(String objectName){
		if (objectName.equals(POPAppService.class.getName())
			|| objectName.equals(POPJavaAppService.class.getName())) {
			return getPOPCodeFile();
		}

		AppService appCoreService = getAppcoreService();
		
		if(appCoreService != null){
			return getCodeFile(appCoreService, objectName);
		}
		
		return getPOPCodeFile();
	}
	
	public static AppService getAppcoreService(){
	    AppService appCoreService = null;
	    if(!POPSystem.appServiceAccessPoint.isEmpty()){
            if(conf.isConnectToPOPcpp()){
                try{
                	POPAppService tempService = PopJava.newActive(POPAppService.class, POPSystem.appServiceAccessPoint);
                	tempService.unregisterService("");
                    appCoreService = tempService;
                }catch(Exception e){
                	LogWriter.writeDebugInfo("[Interface] Running app service is not from POP-C++, fall back to POP-Java");
                    appCoreService = null;
                }
            }
            
            if(appCoreService == null){
                try{
                    appCoreService = PopJava.newActive(
                    		POPJavaAppService.class, POPSystem.appServiceAccessPoint);
                }catch(POPException e){
                    LogWriter.writeDebugInfo("[Interface] Could not contact Appservice to recover code file");
                }
            }
        }else{
    		System.err.println("POPSystem.appServiceAccessPoint was empty");
    	}
	    
	    return appCoreService;
	}
	
	private static String getPOPCodeFile(){
	    
		String popPath = POPJavaConfiguration.getClassPath();
		String popJar = POPJavaConfiguration.getPopJavaJar();
		
		return String.format(
				POPJavaConfiguration.getBrokerCommand(),
				popJar,
				popPath);
	}
	
	public static String getCodeFile(AppService manager, String objectName){
		if(manager == null){
			throw new NullPointerException("AppService can not be null");
		}
	    
		POPString popStringCodeFile = new POPString();
		
		manager.queryCode(objectName, POPSystem.getPlatform(), popStringCodeFile);
		String codeFile = popStringCodeFile.getValue();
		
		//Get wildcard code first
		if(codeFile == null || codeFile.isEmpty()){
		    popStringCodeFile = new POPString();
	        
	        manager.queryCode("*", POPAppService.ALL_PLATFORMS, popStringCodeFile);
	        codeFile = popStringCodeFile.getValue();
		}
		
		//Fall back to local popjava install path
		if(codeFile == null || codeFile.isEmpty()){
			Util util = new Util();
			codeFile = util.getLocalJavaFileLocation(objectName);
		}
		
		return codeFile;
	}

	/**
	 * Launch a parallel object with a command
	 * @param hostname	Hostname to create the object
	 * @param codeFile	Path of the executable code file
	 * @param classname	Name of the Class of the parallel object
	 * @param rports		port
	 * @param jobserv	jobMgr service access point
	 * @param appserv	Application service access point
	 * @param objaccess	Output arguments - Access point to the object
	 * @return -1 if the local execution failed 
	 */
	private static int localExec(String hostname, String codeFile,
			String classname, String[] protocols, String[] rports, POPAccessPoint jobserv,
			POPAccessPoint appserv, POPAccessPoint objaccess,
			ObjectDescription od) {
		
		assert protocols.length == rports.length;
		
		boolean isLocal = Util.isLocal(hostname);
		/*if (!isLocal) {
			return -1;
		}*/
		if (codeFile == null || codeFile.length() == 0){
			return -1;
		}
		codeFile = codeFile.trim();

		ArrayList<String> codeList = Util.splitTheCommand(codeFile);
		ArrayList<String> argvList = new ArrayList<>(codeList);
		
		/*if(od.getMemoryMin() >  0){
			argvList.add(1, "-Xms"+od.getMemoryMin()+"m");
		}
		if(od.getMemoryReq() >  0){
			argvList.add(1, "-Xmx"+od.getMemoryReq()+"m");
		}*/
		
		if(codeFile.startsWith("java")){

			argvList.add(1, "--add-opens=java.base/java.lang=ALL-UNNAMED");
			argvList.add(1, "-XX:+IgnoreUnrecognizedVMOptions");
			
			if(conf.isActivateJmx()) {
				argvList.add(1, "-Dcom.sun.management.jmxremote.port="+(int)(Math.random() * 1000+3000));
				argvList.add(1, "-Dcom.sun.management.jmxremote.ssl=false");
				argvList.add(1, "-Dcom.sun.management.jmxremote.authenticate=false");
			}
			
		}
		
		if(od.getJVMParameters() != null && !od.getJVMParameters().isEmpty()){
			String [] jvmParameters = od.getJVMParameters().split(" ");
			for(String parameter: jvmParameters){
				argvList.add(1, parameter);
			}
		}
		
		ComboxAllocate allocateCombox = null;
		for (String protocol : protocols) {
			ComboxFactory factory = ComboxFactoryFinder.getInstance().findFactory(protocol);
			if (factory != null && factory.isAvailable()) {
				try {
					allocateCombox = factory.createAllocateCombox();
				} catch(IOException e) {
					LogWriter.writeExceptionLog(e);
					continue;
				}
				break;
			}
		}
		if (allocateCombox == null) {
			return -1;
		}
		
		String callbackString = Broker.CALLBACK_PREFIX+allocateCombox.getUrl();
		argvList.add(callbackString);
		if (classname != null && classname.length() > 0) {
			String objectString = Broker.OBJECT_NAME_PREFIX+classname;
			argvList.add(objectString);
		}
		if (appserv != null && !appserv.isEmpty()) {
			String appString = Broker.APPSERVICE_PREFIX+appserv.toString();
			argvList.add(appString);
		}
		if (jobserv != null && !jobserv.isEmpty()) {
			String jobString = Broker.JOB_SERVICE + jobserv.toString();
			argvList.add(jobString);
		}
		
		if (od.isTracking()) {
			argvList.add(Broker.TRACKING);
		}
		
		String networkUUID = od.getNetwork();
		if (networkUUID == null || networkUUID.isEmpty()) {
			networkUUID = conf.getDefaultNetwork();
		}
		String networkArg = Broker.NETWORK_UUID + networkUUID;
		argvList.add(networkArg);
		
		for (int i = 0; i < protocols.length; i++) {
			String protocol = protocols[i];
			String port = rports[i];
			ComboxFactory wantedProtocol = ComboxFactoryFinder.getInstance().findFactory(protocol);
			if (wantedProtocol != null) {
				String portString = String.format("-%s_port=%s", wantedProtocol.getComboxName(), port);
				argvList.add(portString);
			} else {
				LogWriter.writeDebugInfo("[Interface] specified protocol '%s' can't be found.", protocol);
			}
		}
		
		int ret = -1;
		
		//Allow local objects to be declared as remote to test remote object creation locally
		if(hostname.equals(POPObjectDescription.LOCAL_DEBUG_URL)){
			hostname = "localhost";
		}
		
		// XXX Move this to regular OD?
		String potentialServicePort = od.getValue(POPConnectorDirect.OD_SERVICE_PORT);
		// We want to use SSH locally if we force a port
		if(isLocal && potentialServicePort.isEmpty()){
			if (conf.isUsingUserConfig()) {
				// add config file, system or local
				argvList.add(Broker.POPJAVA_CONFIG_PREFIX + conf.getUserConfig().toString());
			}
			ret = SystemUtil.runCmd(argvList, od.getDirectory(), od.getHostuser());
		}else{
			//String potentialPort = od.getValue(POPConnectorDirect.OD_SERVICE_PORT);
			switch(od.getConnectionType()){
			case ANY:
			case SSH:
				// add port to host if specified
				if (!potentialServicePort.isEmpty()) {
					ret = SystemUtil.runRemoteCmd(hostname, potentialServicePort, argvList);
				} else {
					ret = SystemUtil.runRemoteCmd(hostname, argvList);
				}
				break;
			case DAEMON:
				POPJavaDeamonConnector connector;
				try {
					// if manually specified port
					if (potentialServicePort.equals(""))
						connector = new POPJavaDeamonConnector(hostname);
					else
						connector = new POPJavaDeamonConnector(hostname, Integer.parseInt(potentialServicePort));
					if(connector.sendCommand(od.getConnectionSecret(), argvList)){
						ret = 0;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			}
		}

		if (ret == -1) {
			return ret;
		}
		
		allocateCombox.startToAcceptOneConnection();
		
		if(!allocateCombox.isComboxConnected()){
			LogWriter.writeDebugInfo("[Interface] Could not connect broker");
			return -1;
		}
		
		BufferXDR buffer = new BufferXDR();
		int result = 0;

		if (allocateCombox.receive(buffer) > 0) {
			int status = buffer.getInt();
			String str = buffer.getString();
			
			if (status == 0){
				objaccess.setAccessString(str);
			}else{
				result = status;
			}
		} else {
			result = -1;
		}

		allocateCombox.close();
		
		return result;
	}

	/**
	 * Send the buffer content to the broker-side
	 * @param buffer	Buffer to send
	 */
	protected void popDispatch(POPBuffer buffer) {
		int length = combox.send(buffer);
		if (length < 0) {
			throw new POPException(POPErrorCode.POP_COMBOX_NOT_AVAILABLE, "Connection closed remotely while sending");
		}
	}

	/**
	 * Receive response from the broker-side
	 * @param buffer the buffer
	 * @return 0 or an exception
	 * @throws POPException connection failed
	 */
	protected int popResponse(POPBuffer buffer, int requestId) throws POPException {
		
		if (combox.receive(buffer, requestId) > 0) {
			
			MessageHeader messageHeader = buffer.getHeader();
			if (messageHeader.getRequestType() == MessageHeader.EXCEPTION) {
				int errorCode = messageHeader.getExceptionCode();
				POPBuffer.checkAndThrow(errorCode, buffer);
			}
		} else {
		    throw new POPException(POPErrorCode.POP_COMBOX_NOT_AVAILABLE, "Connection closed remotely while receiving");
        }
		return 0;
	}

	/**
	 * Close the combox associated with this interface
	 */
	public void close() {
		if(combox != null){
			combox.close();
		}
		
		combox = null;
	}

	/**
	 * Close everything
	 */
	@Override
    protected void finalize() throws Throwable {
		decRef();
		try {
			close(); // close open files
		} finally {
			super.finalize();
		}

	}
}
