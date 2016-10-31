/**
 * Interface.java 
 * 
 */

package popjava.interfacebase;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
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
import popjava.codemanager.POPJavaAppService;
import popjava.combox.Combox;
import popjava.combox.ComboxAllocateSocket;
import popjava.combox.ComboxFactoryFinder;
import popjava.dataswaper.POPString;
import popjava.service.deamon.POPJavaDeamonConnector;
import popjava.serviceadapter.POPAppService;
import popjava.serviceadapter.POPJobManager;
import popjava.serviceadapter.POPJobService;
import popjava.system.POPJavaConfiguration;
import popjava.system.POPSystem;
import popjava.util.Configuration;
import popjava.util.LogWriter;
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

	private AtomicInteger requestID = new AtomicInteger(10000);
	
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
		int ref = buffer.getInt(); //related to the addRef called in serialize()
		if (ref > 0) {
			try {
				bind(popAccessPoint);
			} catch (POPException e) {
				result = false;
				LogWriter.writeDebugInfo("Deserialize. Cannot bind to " + popAccessPoint.toString());
				e.printStackTrace();
			}
			if (result){
				decRef();
			}
		}
		return result;
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
		canExecLocal = tryLocal(objectName, popAccessPoint);
		
		if (!canExecLocal) {
			if(!od.getHostName().isEmpty()){
				throw new POPException(-1, "Could not create "+objectName+" on "+od.getHostName());
			}
			
			boolean allocated = allocateThroughJobmanager(objectName, allocatedAccessPoint,
                    remotejobscontact);
			
			if(!allocated && od.getHostName().isEmpty()){
			    LogWriter.printDebug("No url specified for "+objectName+", fallback to localhost");
			    od.setHostname("localhost");
			    tryLocal(objectName, popAccessPoint);
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
        	jobContact.setAccessString(String.format("socket://%s:%d", POPSystem
        			.getHostIP(), POPJobManager.DEFAULT_PORT));
        }

        POPJobService jobManager = null;
        try{
        	if(Configuration.CONNECT_TO_POPCPP){
        		jobManager = PopJava.newActive(POPJobService.class, jobContact);
        	}
        }catch(Exception e){
        	e.printStackTrace();
        }
        
        if(jobManager == null){
            return false;
        }
        
        ObjectDescription constOd = new ObjectDescription(od);
        
        int createdCode = jobManager.createObject(POPSystem.appServiceAccessPoint, objectName, constOd, allocatedAccessPoint.length, 
        		allocatedAccessPoint, remotejobscontact.length, remotejobscontact);
        jobManager.exit();
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
			POPException.throwAccessPointNotAvailableException(accesspoint);
		}
		
		ComboxFactoryFinder finder = ComboxFactoryFinder.getInstance();
		
		if (combox != null){
			combox.close();
		}
		combox = finder.findFactory(Configuration.DEFAULT_PROTOCOL)
				.createClientCombox(accesspoint);
		
		if (combox.connect(accesspoint, Configuration.CONNECTION_TIMEOUT)) {

			BindStatus bindStatus = new BindStatus();
			bindStatus(bindStatus);
			switch (bindStatus.getCode()) {
			case BindStatus.BIND_OK:
				this.getOD().setPlatform(bindStatus.getPlatform());
				negotiateEncoding(Configuration.SELECTED_ENCODING, bindStatus
						.getPlatform());
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
	 * @param bindStatus	
	 * @throws POPException
	 */
	private void bindStatus(BindStatus bindStatus) throws POPException {
		if (combox == null){
			POPException.throwComboxNotAvailableException();
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
			POPException.throwComboxNotAvailableException();
		}
		POPBuffer popBuffer = combox.getBufferFactory().createBuffer();
		MessageHeader messageHeader = new MessageHeader(0, MessageHeader.GET_ENCODING_CALL, Semantic.SYNCHRONOUS);
		messageHeader.setRequestID(requestID.incrementAndGet());
		popBuffer.setHeader(messageHeader);
		popBuffer.putString(Configuration.SELECTED_ENCODING);

		popDispatch(popBuffer);

		boolean result = false;
		POPBuffer responseBuffer = combox.getBufferFactory().createBuffer();
		
		popResponse(responseBuffer, messageHeader.getRequestID());
		result = responseBuffer.getBoolean();
		if (result) {
			BufferFactory bufferFactory = BufferFactoryFinder.getInstance().findFactory(Configuration.SELECTED_ENCODING);
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
		
		popDispatch(popBuffer);
		int result = 0;
		try {
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

		popDispatch(popBuffer);
		int result = 0;
		try {
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
			return;
		}
	}

	/**
	 * Try a local execution for the associated parallel object
	 * @param objectName	Name of the object
	 * @param accesspoint	Output parameter - Access point of the object
	 * @return true if the local execution succeed
	 * @throws POPException thrown if any exception occurred during the creation process
	 */
	private boolean tryLocal(String objectName, POPAccessPoint accesspoint)
			throws POPException {
	//	String hostname = "";
		String joburl ="";
		String codeFile = "";
		// Get ClassName

		// Check if Od is empty
		boolean odEmpty = od.isEmpty();
		if (odEmpty){
			return false;
		}

		joburl = od.getHostName();
		LogWriter.writeDebugInfo("Joburl "+joburl+" "+objectName);
		/*if (joburl == null || joburl.length() == 0 || !Util.sameContact(joburl, POPSystem.getHost())){
			return false;
		}*/
		
		if(joburl == null || joburl.length() == 0){
			return false;
		}

		codeFile = od.getCodeFile();
		
		// Host name existed
		if (codeFile == null || codeFile.length() == 0) {
			
			codeFile = getRemoteCodeFile(objectName);
			if (codeFile == null || codeFile.length() == 0){
				return false;
			}
		}

		String rport = "";
		int index = joburl.lastIndexOf(":");
		if (index > 0) {
			rport = joburl.substring(index + 1);
			joburl = joburl.substring(0, index);
		}

		int status = localExec(joburl, codeFile, objectName, rport,
				POPSystem.jobService, POPSystem.appServiceAccessPoint, accesspoint);
		
		if (status != 0) {
			// Throw exception
			LogWriter.writeDebugInfo("Could not create "+objectName+" on "+joburl);
		}
		return (status == 0);
	}
	
	/**
	 * Lookup local code manager for the binary source....
	 * @param objectName
	 * @return
	 */
	private static String getRemoteCodeFile(String objectName){
		if(objectName.equals(POPAppService.class.getName()) ||
				objectName.equals(POPJavaAppService.class.getName())
				){
			return getPOPCodeFile();
		}
		
		AppService appCoreService = getAppcoreService();
		
		if(appCoreService != null){
			String codeFile = getCodeFile(appCoreService, objectName);
			return codeFile;
		}
		
		return getPOPCodeFile();
	}
	
	public static AppService getAppcoreService(){
	    AppService appCoreService = null;
	    if(!POPSystem.appServiceAccessPoint.isEmpty()){
            if(Configuration.CONNECT_TO_POPCPP){
                try{
                	POPAppService tempService = PopJava.newActive(POPAppService.class, POPSystem.appServiceAccessPoint);
                	tempService.unregisterService("");
                    appCoreService = tempService;
                }catch(Exception e){
                	LogWriter.writeDebugInfo("Running app service is not from POP-C++, fall back to POP-Java");
                    appCoreService = null;
                }
            }
            
            if(appCoreService == null){
                try{
                    appCoreService = PopJava.newActive(
                    		POPJavaAppService.class, POPSystem.appServiceAccessPoint);
                }catch(POPException e){
                    LogWriter.writeDebugInfo("Could not contact Appservice to recover code file");
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
	 * @param rport		port
	 * @param jobserv	jobMgr service access point
	 * @param appserv	Application service access point
	 * @param objaccess	Output arguments - Access point to the object
	 * @return -1 if the local execution failed 
	 */
	private int localExec(String hostname, String codeFile,
			String classname, String rport, POPAccessPoint jobserv,
			POPAccessPoint appserv, POPAccessPoint objaccess) {
		
		boolean isLocal = Util.isLocal(hostname);
		/*if (!isLocal) {
			return -1;
		}*/
		if (codeFile == null || codeFile.length() == 0){
			return -1;
		}
		codeFile = codeFile.trim();

		ArrayList<String> argvList = new ArrayList<String>();

		ArrayList<String> codeList = Util.splitTheCommand(codeFile);
		argvList.addAll(codeList);
		
		/*if(od.getMemoryMin() >  0){
			argvList.add(1, "-Xms"+od.getMemoryMin()+"m");
		}
		if(od.getMemoryReq() >  0){
			argvList.add(1, "-Xmx"+od.getMemoryReq()+"m");
		}*/
		
		if(codeFile.startsWith("java") && Configuration.ACTIVATE_JMX){
			argvList.add(1, "-Dcom.sun.management.jmxremote.port="+(int)(Math.random() * 1000+3000));
			argvList.add(1, "-Dcom.sun.management.jmxremote.ssl=false");
			argvList.add(1, "-Dcom.sun.management.jmxremote.authenticate=false");
		}
		
		if(od.getJVMParameters() != null && !od.getJVMParameters().isEmpty()){
			String [] jvmParameters = od.getJVMParameters().split(" ");
			for(String parameter: jvmParameters){
				argvList.add(1, parameter);
			}
		}
		
		ComboxAllocateSocket allocateCombox = new ComboxAllocateSocket();
		String callbackString = String.format(Broker.CALLBACK_PREFIX+"%s", allocateCombox.getUrl());
		argvList.add(callbackString);
		if (classname != null && classname.length() > 0) {
			String objectString = String.format(Broker.OBJECT_NAME_PREFIX+"%s", classname);
			argvList.add(objectString);
		}
		if (appserv != null && !appserv.isEmpty()) {
			String appString = String.format(Broker.APPSERVICE_PREFIX+"%s", appserv.toString());
			argvList.add(appString);
		}
		if (jobserv != null && !jobserv.isEmpty()) {
			String jobString = String.format("-jobservice=%s", jobserv.toString());
			argvList.add(jobString);
		}

		if (rport != null && rport.length() > 0) {
			String portString = String.format("-socket_port=%s", rport);
			argvList.add(portString);
		}
		
		int ret = -1;
		
		//Allow local objects to be declared as remote to test remote object creation locally
		if(hostname.equals(POPObjectDescription.LOCAL_DEBUG_URL)){
			hostname = "localhost";
		}
		
		if(isLocal){
			ret = SystemUtil.runCmd(argvList);
		}else{
			switch(od.getConnectionType()){
			case ANY:
			case SSH:
				ret = SystemUtil.runRemoteCmd(hostname, argvList);
				break;
			case DEAMON:
				POPJavaDeamonConnector connector;
				try {
					connector = new POPJavaDeamonConnector(hostname);
					if(connector.sendCommand(od.getConnectionSecret(), argvList)){
						ret = 0;
					}
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		if (ret == -1) {
			return ret;
		}
		
		allocateCombox.startToAcceptOneConnection();
		
		if(!allocateCombox.isComboxConnected()){
			LogWriter.writeDebugInfo("Could not connect broker");
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
		combox.send(buffer);
	}

	/**
	 * Receive response from the broker-side
	 * @param buffer
	 * @return
	 * @throws POPException
	 */
	protected int popResponse(POPBuffer buffer, int requestId) throws POPException {
		
		if (combox.receive(buffer, requestId) > 0) {
			
			MessageHeader messageHeader = buffer.getHeader();
			if (messageHeader.getRequestType() == MessageHeader.EXCEPTION) {
				int errorCode = messageHeader.getExceptionCode();
				POPBuffer.checkAndThrow(errorCode, buffer);
			}
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
