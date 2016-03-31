package popjava.broker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javassist.NotFoundException;
import javassist.util.proxy.ProxyObject;
import popjava.PopJava;
import popjava.annotation.POPClass;
import popjava.annotation.POPParameter;
import popjava.base.MessageHeader;
import popjava.base.MethodInfo;
import popjava.base.POPErrorCode;
import popjava.base.POPException;
import popjava.base.POPObject;
import popjava.base.POPSystemErrorCode;
import popjava.base.Semantic;
import popjava.baseobject.AccessPoint;
import popjava.baseobject.POPAccessPoint;
import popjava.buffer.BufferFactory;
import popjava.buffer.BufferFactoryFinder;
import popjava.buffer.BufferXDR;
import popjava.buffer.POPBuffer;
import popjava.combox.Combox;
import popjava.combox.ComboxFactory;
import popjava.combox.ComboxFactoryFinder;
import popjava.combox.ComboxServer;
import popjava.combox.ComboxSocket;
import popjava.javaagent.POPJavaAgent;
import popjava.system.POPSystem;
import popjava.util.LogWriter;
import popjava.util.Util;

/**
 * This class is the base class of all broker-side parallel object. The broker
 * is responsible to receive the requests from the interface-side and to execute
 * them on the real object
 */
public final class Broker {
	static public final int RUNNING = 0;
	static public final int EXIT = 1;
	static public final int ABORT = 2;
	
	static public final int REQUEST_QUEUE_TIMEOUT_MS = 600;
	static public final int BASIC_CALL_MAX_RANGE = 10;
	static public final int CONSTRUCTOR_SEMANTIC_ID = 21;
	static public final String CALLBACK_PREFIX = "-callback=";
	static public final String CODELOCATION_PREFIX = "-codelocation=";
	static public final String OBJECT_NAME_PREFIX = "-object=";
	static public final String ACTUAL_OBJECT_NAME_PREFIX = "-actualobject=";
	static public final String APPSERVICE_PREFIX = "-appservice=";

	protected int state;
	protected ComboxServer comboxServer;
	protected POPBuffer buffer;
	protected static POPAccessPoint accessPoint = new POPAccessPoint();
	protected POPObject popObject = null;
	protected POPObject popInfo = null;
	protected int connectionCount = 0;
	protected Semaphore sequentialSemaphore = new Semaphore(1, true);
	
	private ExecutorService threadPoolSequential = Executors.newSingleThreadExecutor(new ThreadFactory() {
		
		@Override
		public Thread newThread(Runnable arg0) {
			Thread thread = Executors.defaultThreadFactory().newThread(arg0);
			thread.setName("Sequential request thread");
			return thread;
		}
	});
	
	private ExecutorService threadPoolConcurrent = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*50,
			new ThreadFactory() {
		
		private int threadIndex = 0;
		
		@Override
		public Thread newThread(Runnable arg0) {
			Thread thread = Executors.defaultThreadFactory().newThread(arg0);
			thread.setName("Concurrent request thread "+(threadIndex++));
			return thread;
		}
	});
			//Executors.newCachedThreadPool());//

	private static Broker me;
	
	public static Broker getBroker(){
		return me;
	}
	
	/**
	 * Creates a new instance of POPBroker
	 * 
	 * @param codelocation
	 *            path of the real object to create with this broker
	 * @param objectName
	 *            Name of the object to create
	 */
	private Broker(String codelocation, String objectName) {
		if(me == null){
			me = this;
		}
		
		URLClassLoader urlClassLoader = null;
		
		if (codelocation != null && codelocation.length() > 0) {
			URL url = null;
			
			if(codelocation.startsWith("http:")){
				try{
					URL website = new URL(codelocation);
					ReadableByteChannel rbc = Channels.newChannel(website.openStream());
					
					File tempJar = File.createTempFile(website.getFile(), ".jar");
					
					FileOutputStream fos = new FileOutputStream(tempJar);
					fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
					
					fos.close();
					
					codelocation = tempJar.getAbsolutePath();
					
					tempJar.deleteOnExit();
				}catch(MalformedURLException e){
					e.printStackTrace();
					System.exit(0);
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(0);
				}
			}
			
			try {
				LogWriter.writeDebugInfo("Local file " + codelocation);
				url = new File(codelocation).toURI().toURL();
				POPJavaAgent.getInstance().addJar(codelocation);
			} catch (MalformedURLException e) {
				LogWriter.writeDebugInfo(this.getClass().getName()
						+ ".MalformedURLException: " + e.getMessage());
				System.exit(0);
			} catch (NotFoundException e) {
                e.printStackTrace();
                System.exit(0);
            }
			
			if (url != null) {
				LogWriter.writeDebugInfo("url construct");
				
				urlClassLoader = new URLClassLoader(new URL[]{url});
			}
		}

		Class<?> targetClass;
		try {
			targetClass = getPOPObjectClass(objectName, urlClassLoader);
			popInfo = (POPObject) targetClass.getConstructor().newInstance();
		} catch (Exception e) {
			LogWriter.writeDebugInfo(getClass().getName()
					+ " Constructor Exception: " + e.getClass().getName()
					+ " Message:" + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * This method is responsible to invoke the right constructor on the
	 * associated object
	 * 
	 * @param request
	 *            Request received from the interface-side
	 * @return true id the constructor has been called correctly
	 */
	private boolean invokeConstructor(Request request) {
		Class<?>[] parameterTypes = null;
		Object[] parameters = null;
		Constructor<?> constructor = null;
		POPException exception = null;

		try {
			MethodInfo info = new MethodInfo(request.getClassId(),
					request.getMethodId());
			constructor = popInfo.getConstructorByInfo(info);
		} catch (NoSuchMethodException e) {
			exception = POPException.createReflectMethodNotFoundException(
					popInfo.getClass().getName(), 
					request.getClassId(), request.getMethodId(),
					e.getMessage());
		}

		if (exception == null && constructor != null) {
			parameterTypes = constructor.getParameterTypes();
			try{
				POPBuffer requestBuffer = request.getBuffer();
				request.setBuffer(null); //This way the JVM can free the buffer memory
				parameters = getParameters(requestBuffer, parameterTypes, constructor.getParameterAnnotations());
			}catch(POPException e){
				exception = e;
			}
		}
		
		normalizePOPParamameters(parameters);
		
		if (exception == null && constructor != null) {
			try {
				popObject = (POPObject) constructor.newInstance(parameters);
				POPClass annotation = popObject.getClass().getAnnotation(POPClass.class);
				if(annotation != null){
					comboxServer.getRequestQueue().setMaxQueue(annotation.maxRequestQueue());
				}
				
			} catch (Exception e) {
				exception = POPException.createReflectException(
						constructor.getName(), e.getMessage());
			}
		}

		if (exception == null && constructor != null && parameterTypes != null && parameters != null) {
			if ((request.getSenmatics() & Semantic.SYNCHRONOUS) != 0) {
				// Return the value to caller
				MessageHeader messageHeader = new MessageHeader();
				messageHeader.setRequestID(request.getRequestID());
				POPBuffer responseBuffer = request.getCombox().getBufferFactory()
						.createBuffer();
				responseBuffer.setHeader(messageHeader);

				Annotation [][] annotations = constructor.getParameterAnnotations();
				for (int index = 0; index < parameterTypes.length; index++) {
					if(Util.isParameterNotOfDirection(annotations[index], POPParameter.Direction.IN) &&
					        Util.isParameterUseable(annotations[index])
							&&
							!(parameters[index] instanceof POPObject && !Util.isParameterOfAnyDirection(annotations[index]))){
						try {
							responseBuffer.serializeReferenceObject(parameterTypes[index], parameters[index]);
						} catch (POPException e) {
							exception = new POPException(e.errorCode, e.errorMessage);
							break;
						}
					}
				}
				if (exception == null) {
					sendResponse(request.getCombox(), responseBuffer);
				}
			}
			// Remove reference, remove the connection to POPObject
			for (int index = 0; index < parameterTypes.length; index++) {

				if (POPObject.class.isAssignableFrom(parameterTypes[index])
						&& parameters[index] != null) {
					POPObject obj = (POPObject) parameters[index];
					if(obj.isTemporary()){
						obj.exit();
					}
				}
			}
		}
		if (exception != null) {
			LogWriter.writeDebugInfo(this.getLogPrefix() + "sendException:"
					+ exception.getMessage());
			sendException(request.getCombox(), exception, request.getRequestID());
			System.exit(0);
		}
		return true;
	}

	private Object[] getParameters(POPBuffer requestBuffer,
			Class<?>[] parameterTypes, Annotation [][] annotations) throws POPException{
		Object[] parameters;
		parameters = new Object[parameterTypes.length];
		int index = 0;
		// Get parameters
		for (index = 0; index < parameterTypes.length; index++) {
			if(Util.isParameterNotOfDirection(annotations[index], POPParameter.Direction.OUT) &&
			        Util.isParameterUseable(annotations[index])){
				try {
					parameters[index] = requestBuffer
							.getValue(parameterTypes[index]);
				} catch (POPException e) {
					throw new POPException(e.errorCode, e.errorMessage);
				} catch (Exception e) {
					throw new POPException(
							POPErrorCode.UNKNOWN_EXCEPTION,
							"Unknown exception when get parameter "
									+ parameterTypes[index].getName());
				}
			}
		}
		return parameters;
	}
	
	/**
	 * This method is responsible to call the correct method on the associated
	 * object
	 * 
	 * @param request
	 *            Request received from the interface-side
	 * @return true if the method has been called correctly
	 * @throws InterruptedException 
	 */
	private boolean invokeMethod(Request request) throws InterruptedException {
		if(request.isSequential()){
			sequentialSemaphore.acquire();
		}
		Object result = new Object();
		POPException exception = null;
		Method method = null;
		Class<?> returnType = null;
		Class<?>[] parameterTypes = null;
		Object[] parameters = null;
		int index = 0;
		try {
			
			MethodInfo info = new MethodInfo(request.getClassId(), request.getMethodId());
			method = popInfo.getMethodByInfo(info);
		} catch (NoSuchMethodException e) {
			exception = POPException.createReflectMethodNotFoundException(
					popInfo.getClass().getName(),
					request.getClassId(), request.getMethodId(),
					e.getMessage());
		}
		// Get parameter if found the method
		if (exception == null && method != null) {

			returnType = method.getReturnType();
			parameterTypes = method.getParameterTypes();
			
			Annotation[][] annotations = method.getParameterAnnotations();
			
			try{

				POPBuffer requestBuffer = request.getBuffer();
				request.setBuffer(null);//This way the JVM can free the  buffer content
				parameters = getParameters(requestBuffer, parameterTypes, annotations);
			}catch(POPException e){
				exception = e;
			}
		}
		
		normalizePOPParamameters(parameters);
		//LogWriter.writeDebugInfo("Call method "+method.getName());
		// Invoke the method if success to get all parameter
		if (exception == null && method != null) {
			try {
				method.setAccessible(true);
				if (returnType != Void.class && returnType != void.class) {					
					result = method.invoke(popObject, parameters);
				} else {
					method.invoke(popObject, parameters);
				}
			}catch(InvocationTargetException e){
				LogWriter.writeExceptionLog(e);
				LogWriter.writeExceptionLog(e.getCause());
				LogWriter.writeDebugInfo("Cannot execute. Cause "+e.getCause().getMessage());
				exception = POPException.createReflectException(
						method.getName(), e.getCause().getMessage());
			}catch (Exception e) {
				// Cannot execute, send error
				LogWriter.writeExceptionLog(e);
				System.out.println(method.toGenericString());
				System.out.println(request.getClassId()+" "+request.getMethodId());
				LogWriter.writeDebugInfo("Cannot execute "+method.toGenericString());
				exception = POPException.createReflectException(
						method.getName(), e.getMessage());

			}
		}
		// Prepare the response buffer if success to invoke method
		if (exception == null && method != null && parameterTypes != null && parameters != null) {
			// Send response
			if (request.isSynchronous()) {

				MessageHeader messageHeader = new MessageHeader();
				messageHeader.setRequestID(request.getRequestID());
				POPBuffer responseBuffer = request.getCombox().getBufferFactory().createBuffer();
				responseBuffer.setHeader(messageHeader);
				
				Annotation[][] annotations = method.getParameterAnnotations();

				//Put all parameters back in the response, if needed
				for (index = 0; index < parameterTypes.length; index++) {
					//If parameter is not a IN variable and
					//The parameter is not a POPObject without any specified direction
					if(Util.isParameterNotOfDirection(annotations[index], POPParameter.Direction.IN) &&
					        Util.isParameterUseable(annotations[index]) &&
							!(parameters[index] instanceof POPObject && !Util.isParameterOfAnyDirection(annotations[index]))
							){
						try {
							responseBuffer.serializeReferenceObject(parameterTypes[index], parameters[index]);
						} catch (POPException e) {
							LogWriter.writeDebugInfo("Excecption serializing parameter "+parameterTypes[index].getName());
							exception = new POPException(e.errorCode, e.errorMessage);
							break;
						}
					}
				}
				
				if (exception == null) {
					if (returnType != Void.class && returnType != void.class && returnType != Void.TYPE){
						try {
						    responseBuffer.putValue(result, returnType);
						} catch (POPException e) {
							exception = e;
						}
					}
				}
				// Send response if success to put parameter to response buffer
				if (exception == null) {
					sendResponse(request.getCombox(), responseBuffer);
				}
			}
			// Remove reference, remove the connection to POPObject
			for (index = 0; index < parameterTypes.length; index++) {

				if (POPObject.class.isAssignableFrom(parameterTypes[index])
						&& parameters[index] != null) {
					POPObject object = (POPObject)parameters[index];
					//LogWriter.writeDebugInfo("POPObject parameter is temporary: "+object.isTemporary());
					if(object.isTemporary()){
						LogWriter.writeDebugInfo("Exit popobject");
						object.exit();
					}
				}
			}
		}
		// if have any error (cannot get the parameter, or cannot invoke method,
		// or cannot put the output parameter,
		// send it to the interface
		if (exception != null) {
			LogWriter.writeDebugInfo(this.getLogPrefix() + "sendException : " + exception.getMessage());
			if (request.isSynchronous()){
				sendException(request.getCombox(), exception, request.getRequestID());
			}
		}
		if(request.isSequential()){
			sequentialSemaphore.release();
		}
		return true;
	}
	
	private void normalizePOPParamameters(Object[] parameters){
		for(int i = 0; parameters != null && i < parameters.length; i++){
			if(parameters[i] instanceof POPObject){
				POPObject object = (POPObject)parameters[i];
				if(!(parameters[i] instanceof ProxyObject)){
					object = PopJava.newActive(object.getClass(), object.getAccessPoint());
				}
				object.makeTemporary();
				parameters[i] = object;
			}
		}
	}

	/**
	 * This method is responsible to dispatch the request between
	 * invokeConstructor and invokeMethod
	 * 
	 * @param request
	 *            Request received from the interface-side
	 * @return true if the request has been treated correctly
	 * @throws InterruptedException 
	 */
	public boolean invoke(Request request) throws InterruptedException {
		if ((request.getSenmatics() & Semantic.CONSTRUCTOR) != 0) {
			invokeConstructor(request);
		} else {
			invokeMethod(request);
		}
		request.setStatus(Request.SERVED);
		clearResourceAfterInvoke(request);

		return true;
	}

	/**
	 * Remove the request from the request queue after invocation
	 * 
	 * @param request
	 *            Request to be removed
	 */
	public void clearResourceAfterInvoke(Request request) {
		comboxServer.getRequestQueue().remove(request);
	}

	/**
	 * This method is responsible to handle the broker-side semantics for a
	 * request
	 * 
	 * @param request
	 *            Request received from the interface-side
	 * @throws InterruptedException 
	 */
	public void serveRequest(final Request request) throws InterruptedException {
		request.setBroker(this);
		request.setStatus(Request.SERVING);
		// Do not create new thread if method is mutex
		
		//LogWriter.writeDebugInfo("serveRequest start "+request.getClassId()+" "+request.getMethodId());
		
		if (request.isMutex()) {
			invoke(request);
		} else {
			Runnable popRequest = new Runnable() {
				
				@Override
				public void run() {
				    //LogWriter.writeDebugInfo("Start request "+request.getClassId()+" "+request.getMethodId());
			        
					try {
						invoke(request);
					} catch (InterruptedException e) {
						LogWriter.writeExceptionLog(e);
					}
					
					//LogWriter.writeDebugInfo("End request "+request.getClassId()+" "+request.getMethodId());
				}
			};
			
			if(request.isConcurrent()){
				threadPoolConcurrent.execute(popRequest);
			}else{
				threadPoolSequential.execute(popRequest);
			}
		}
		
		//LogWriter.writeDebugInfo("serveRequest end "+request.getClassId()+" "+request.getMethodId());
	}

	/**
	 * This method is responsible to handle the POP system call
	 * 
	 * @param request
	 *            Request received from the interface-side
	 * @return true if the request has been treated correctly
	 */
	public boolean popCall(Request request) {
		if (request.getMethodId() >= BASIC_CALL_MAX_RANGE){
			return false;
		}
		POPBuffer buffer = request.getBuffer();
		POPBuffer responseBuffer = request.getCombox().getBufferFactory().createBuffer();
		
		switch (request.getMethodId()) {
		case MessageHeader.BIND_STATUS_CALL:
			// BindStatus call
			if ((request.getSenmatics() & Semantic.SYNCHRONOUS) != 0) {
				MessageHeader messageHeader = new MessageHeader();
				messageHeader.setRequestID(request.getRequestID());
				responseBuffer.setHeader(messageHeader);
				responseBuffer.putInt(0);
				responseBuffer.putString(POPSystem.getPlatform());
				responseBuffer.putString(BufferFactoryFinder.getInstance().getSupportingBuffer());
				
				sendResponse(request.getCombox(), responseBuffer);
			}
			break;
		case MessageHeader.ADD_REF_CALL: {
			// AddRef call...
			if (popInfo == null) {
				return false;
			}
			int ret = 1;
			if ((request.getSenmatics() & Semantic.SYNCHRONOUS) != 0) {

				MessageHeader messageHeader = new MessageHeader();
				messageHeader.setRequestID(request.getRequestID());
				responseBuffer.setHeader(messageHeader);
				responseBuffer.putInt(ret);
				sendResponse(request.getCombox(), responseBuffer);
			}
		}
			break;
		case MessageHeader.DEC_REF_CALL: {
			// DecRef call....
			if (popInfo == null) {
				return false;
			}
			int ret = 1;

			if ((request.getSenmatics() & Semantic.SYNCHRONOUS) != 0) {
				MessageHeader messageHeader = new MessageHeader();
				messageHeader.setRequestID(request.getRequestID());
				responseBuffer.setHeader(messageHeader);
				responseBuffer.putInt(ret);
				sendResponse(request.getCombox(), responseBuffer);
			}
		}
			break;
		case MessageHeader.GET_ENCODING_CALL: {
			// GetEncoding call...
			String encoding = buffer.getString();
			boolean foundEncoding = findEndcoding(encoding);
			
			if ((request.getSenmatics() & Semantic.SYNCHRONOUS) != 0) {
				MessageHeader messageHeader = new MessageHeader();
				messageHeader.setRequestID(request.getRequestID());
				responseBuffer.setHeader(messageHeader);
				// The trick :(( I haven't implemented to right XDR buffer
				// I will try to fix this later :(
				responseBuffer.putBoolean(foundEncoding);
				sendResponse(request.getCombox(), responseBuffer);
			}
			
			if (foundEncoding) {
                request.setBufferType(encoding);
                
                BufferFactory bufferFactory = BufferFactoryFinder.getInstance().findFactory(encoding);
                request.getCombox().setBufferFactory(bufferFactory);
            }
		}
			break;
		case MessageHeader.KILL_ALL: {
			// Kill call...
			if (popInfo != null && popInfo.canKill()) {
				System.exit(1);
			}
		}
			break;
		case MessageHeader.OBJECT_ALIVE_CALL: {
			// ObjectAlive call
			if (popInfo == null)
				return false;
			if ((request.getSenmatics() & Semantic.SYNCHRONOUS) != 0) {
				MessageHeader messageHeader = new MessageHeader();
				messageHeader.setRequestID(request.getRequestID());
				responseBuffer.setHeader(messageHeader);
				boolean isAlive = true;
				responseBuffer.putBoolean(isAlive);
				sendResponse(request.getCombox(), responseBuffer);
			}
		}
			break;
		default:
			return false;
		}
		return true;
	}

	/**
	 * Kill the broker and its associated object
	 */
	public synchronized void kill() {
		setState(Broker.EXIT);
	}

	/**
	 * Return the access point of this broker
	 * 
	 * @return Access point associated with this broker
	 */
	public static POPAccessPoint getAccessPoint() {
		return accessPoint;
	}

	/**
	 * Main loop of this broker
	 * @throws InterruptedException 
	 */
	public void treatRequests() throws InterruptedException { 
		setState(Broker.RUNNING);
		while (getState() == Broker.RUNNING) {
			Request request = comboxServer.getRequestQueue().peek(REQUEST_QUEUE_TIMEOUT_MS,
					TimeUnit.MILLISECONDS);
			
			if (request != null) {
				serveRequest(request);
			}else {
				Thread.sleep(100);
			}
		}
		LogWriter.writeDebugInfo("Close broker");
	}

	/**
	 * Increment the connection counter
	 */
	public synchronized void onNewConnection() {
		connectionCount++;
		LogWriter.writeDebugInfo("Open connection "+connectionCount);
	}

	/**
	 * Decrement de connection counter and exit the broker if there is no more
	 * connection
	 */
	public synchronized void onCloseConnection() {
		connectionCount--;
		LogWriter.writeDebugInfo("Close connection, left "+connectionCount);
		if (connectionCount <= 0){
			setState(Broker.EXIT);
		}
	}

	/**
	 * Get information about the deamon mode of this broker
	 * 
	 * @return deamon mode
	 */
	public boolean isDaemon() {
		if (popInfo != null) {
			return popInfo.isDaemon();
		}
			
		return true;
	}

	/**
	 * Get information about the state of this borker
	 * 
	 * @return current state
	 */
	public synchronized int getState() {
		if (isDaemon()){
			return Broker.RUNNING;
		}
		return state;
	}

	/**
	 * Set state information of this broker
	 * 
	 * @param state
	 *            state to set to this broker
	 */
	public synchronized void setState(int state) {
		this.state = state;
	}

	/**
	 * Look for a specific encoding
	 * 
	 * @param encoding
	 *            Encoding to look for
	 * @return true if the encoding is available
	 */
	protected boolean findEndcoding(String encoding) {
	    //TODO: implement
		return true;
	}

	/**
	 * Initialization of the broker-side
	 * 
	 * @param argvs
	 *            Arguments
	 * @return true if the initialization process succeed
	 */
	public boolean initialize(ArrayList<String> argvs) {
		accessPoint = new POPAccessPoint();
		
		buffer = new BufferXDR();
		ComboxFactoryFinder finder = ComboxFactoryFinder.getInstance();
		int comboxCount = finder.getFactoryCount();
		for (int i = 0; i < comboxCount; i++) {
			ComboxFactory factory = finder.get(i);
			String prefix = String.format("-%s_port=", factory.getComboxName());

			String port = Util.removeStringFromList(argvs, prefix);
			int iPort = 0;
			if (port != null && port.length() > 0) {
				try {
					iPort = Integer.parseInt(port);
				} catch (NumberFormatException e) {

				}
			}
			
			AccessPoint ap = new AccessPoint(factory.getComboxName(),
					POPSystem.getHostIP(), iPort);
			accessPoint.addAccessPoint(ap);
			
			comboxServer = factory.createServerCombox(ap, buffer, this);
			
		}
		return true;
	}

	/**
	 * Return the class of the associated object
	 * 
	 * @param className
	 *            Name of the class
	 * @param urlClassLoader
	 *            Path of the class
	 * @return Class object or null
	 * @throws ClassNotFoundException
	 *             thrown if the class is not found
	 */
	protected Class<?> getPOPObjectClass(String className, URLClassLoader urlClassLoader) throws ClassNotFoundException {

		if (urlClassLoader != null) {
			Class<?> c = Class.forName(className, true, urlClassLoader);
			return c;
		} else {
			return Class.forName(className);
		}
	}

	/**
	 * Entry point for the Broker. This method is called when a
	 * new Broker is setup in a JVM.
	 * 
	 * @param argvs
	 *            arguments of the program
	 * @throws InterruptedException 
	 */
	public static void main(String[] argvs) throws InterruptedException {
	    POPSystem.setStarted();
	    
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            
            @Override
            public void uncaughtException(Thread t, Throwable e) {
            	LogWriter.writeDebugInfo("POP Uncatched exception");
            	LogWriter.writeExceptionLog(e);
            }
        });
		
		ArrayList<String> argvList = new ArrayList<String>();
		LogWriter.writeDebugInfo("Broker parameters");
		for (String str : argvs) {
			argvList.add(str);
			LogWriter.writeDebugInfo(str);
		}
		LogWriter.writeDebugInfo("Broker parameters end");
		
		String appservice = Util.removeStringFromList(argvList,
				APPSERVICE_PREFIX);
		String codelocation = Util.removeStringFromList(argvList,
				CODELOCATION_PREFIX);
		String objectName = Util.removeStringFromList(argvList,
				OBJECT_NAME_PREFIX);
		String actualObjectName = Util.removeStringFromList(argvList,
				ACTUAL_OBJECT_NAME_PREFIX);
		if (actualObjectName != null && actualObjectName.length() > 0) {
			objectName = actualObjectName;
		}
		String callbackString = Util.removeStringFromList(argvList, CALLBACK_PREFIX);
		if (appservice != null && appservice.length() > 0) {
			POPSystem.appServiceAccessPoint.setAccessString(appservice);
		}
		ComboxSocket callback = null;
		if (callbackString != null && callbackString.length() > 0) {
			POPAccessPoint accessPoint = new POPAccessPoint(callbackString);
			if (accessPoint.size() > 0) {
				callback = new ComboxSocket(accessPoint, 0);
				if (!callback.connect()) {
					LogWriter.writeDebugInfo(String.format(
							"-Error: fail to connect to callback:%s",
							accessPoint.toString()));
					System.exit(1);
				} else {
					LogWriter.writeDebugInfo("Connected to callback socket");
				}
			}

		} else {
			LogWriter.writeDebugInfo("-Error: callback is null");
			System.exit(1);
		}
		
		Broker broker = null;
		
		try{
			broker = new Broker(codelocation, objectName);
		}catch(Exception e){
			LogWriter.writeExceptionLog(e);
		}
		int status = 0;
		if (broker == null || !broker.initialize(argvList)) {
			status = 1;
		}
		
		//Send info back to callback
		if (callback != null) {
			MessageHeader messageHeader = new MessageHeader();
			POPBuffer buffer = new BufferXDR();
			buffer.setHeader(messageHeader);
			buffer.putInt(status);
			LogWriter.writeDebugInfo("Broker can be accessed at "+Broker.getAccessPoint().toString());
			Broker.getAccessPoint().serialize(buffer);
			callback.send(buffer);
		}

		if (status == 0 && broker != null){
			broker.treatRequests();
		}
		LogWriter.writeDebugInfo("End broker life");
		System.exit(0);
	}

	/**
	 * Send exception to the interface-side
	 * 
	 * @param combox
	 *            Combox to send the exception
	 * @param exception
	 *            Exception to send
	 * @return true if the exception has been sent
	 */
	public boolean sendException(Combox combox, POPException exception, int requestId) {
	    exception.printStackTrace();
	    
		POPBuffer buffer = combox.getBufferFactory().createBuffer();
		MessageHeader messageHeader = new MessageHeader(
				POPSystemErrorCode.EXCEPTION_PAROC_STD);
		messageHeader.setRequestID(requestId);
		
		buffer.setHeader(messageHeader);
		exception.serialize(buffer);
		combox.send(buffer);
		return true;
	}

	/**
	 * Send response to the interface-side
	 * 
	 * @param combox
	 *            Combox to send the response
	 * @param buffer
	 *            Buffer to send trough the combox
	 */
	public void sendResponse(Combox combox, POPBuffer buffer) {
		combox.send(buffer);
	}

	/**
	 * Return the prefix for log file
	 * 
	 * @return log prefix
	 */
	public String getLogPrefix() {
		if (popInfo == null){
			return this.getClass().getName() + ".Intilizing:";
		} else {
			return this.getClass().getName() + "."
					+ popInfo.getClass().getName() + ":";
		}
	}
}
