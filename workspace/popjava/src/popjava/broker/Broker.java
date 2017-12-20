package popjava.broker;

import popjava.util.RuntimeDirectoryThread;
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
import java.nio.file.Paths;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
import popjava.annotation.POPAsyncConc;
import popjava.annotation.POPAsyncMutex;
import popjava.annotation.POPAsyncSeq;
import popjava.annotation.POPSyncConc;
import popjava.annotation.POPSyncMutex;
import popjava.annotation.POPSyncSeq;
import popjava.base.MessageHeader;
import popjava.base.MethodInfo;
import popjava.base.POPErrorCode;
import popjava.base.POPException;
import popjava.base.POPObject;
import popjava.base.POPSystemErrorCode;
import popjava.base.Semantic;
import popjava.baseobject.AccessPoint;
import popjava.baseobject.POPAccessPoint;
import popjava.baseobject.POPTracking;
import popjava.buffer.BufferFactory;
import popjava.buffer.BufferFactoryFinder;
import popjava.buffer.BufferXDR;
import popjava.buffer.POPBuffer;
import popjava.combox.Combox;
import popjava.combox.ComboxFactory;
import popjava.combox.ComboxFactoryFinder;
import popjava.combox.ComboxServer;
import popjava.util.ssl.SSLUtils;
import popjava.javaagent.POPJavaAgent;
import popjava.system.POPSystem;
import popjava.util.Configuration;
import popjava.util.LogWriter;
import popjava.util.MethodUtil;
import popjava.util.POPRemoteCaller;
import popjava.util.Util;

/**
 * This class is the base class of all broker-side parallel object. The broker
 * is responsible to receive the requests from the interface-side and to execute
 * them on the real object
 */
public final class Broker {
    
    public enum State{
        Running,
        Exit,
        Abort
    }
	
	private final Configuration conf = Configuration.getInstance();
	
	public static final int REQUEST_QUEUE_TIMEOUT_MS = 600;
	public static final int BASIC_CALL_MAX_RANGE = 10;
	public static final int CONSTRUCTOR_SEMANTIC_ID = 21;
	public static final String CALLBACK_PREFIX = "-callback=";
	public static final String CODELOCATION_PREFIX = "-codelocation=";
	public static final String OBJECT_NAME_PREFIX = "-object=";
	public static final String ACTUAL_OBJECT_NAME_PREFIX = "-actualobject=";
	public static final String APPSERVICE_PREFIX = "-appservice=";
	public static final String JOB_SERVICE = "-jobservice=";
	public static final String POPJAVA_CONFIG_PREFIX = "-configfile=";
	public static final String NETWORK_UUID = "-network=";
	public static final String TRACKING = "-tracking";
	
	
	// thread unique callers
	private static ThreadLocal<POPRemoteCaller> remoteCaller = new InheritableThreadLocal<>();
	
	/**
	 * Request queue shared by all comboxes of this broker
	 */
	private final RequestQueue requestQueue = new RequestQueue();
	
	private State state;
	private ComboxServer[] comboxServers;
	private POPBuffer buffer;
	private POPAccessPoint accessPoint = new POPAccessPoint();
	private POPObject popObject = null;
	private POPObject popInfo = null;
	private int connectionCount = 0;
	private Semaphore sequentialSemaphore = new Semaphore(1, true);
	
	private boolean tracking;
	
	private Map<Method, Annotation[][]> methodParametersAnnotationCache = new HashMap<>();
	private Map<Method, Integer> methodSemanticsCache = new HashMap<>();
	
	private Map<POPRemoteCaller, POPTracking> callerTracking = new ConcurrentHashMap<>();
		
	private ExecutorService threadPoolSequential = Executors.newSingleThreadExecutor(new ThreadFactory() {
		@Override
		public Thread newThread(Runnable arg0) {
			Thread thread = Executors.defaultThreadFactory().newThread(arg0);
			thread.setName("Sequential request thread");
			thread.setDaemon(true);
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
	
	public Broker(POPObject object){
		this.popObject = object;
		popObject.setBroker(this);
		
		connectionCount++;
		
		String[] protocols = popObject.getOd().getProtocols();
		List<String> initParams = new ArrayList<>();
		if (protocols != null && protocols.length > 0) {
			ComboxFactoryFinder finder = ComboxFactoryFinder.getInstance();
				
			for (String protocol : protocols) {
				String[] split = protocol.split(":");

				ComboxFactory factory = finder.findFactory(split[0]);
				
				int port = 0;
				if (split.length == 2) {
					try {
						port = Integer.parseInt(split[1]);
					} catch (Exception e) {
					}
				}

				if (factory != null) {
					initParams.add(String.format("-%s_port=%d", factory.getComboxName(), port));
				}
			}
		}
		
		if (popObject.getOd().isTracking()) {
			initParams.add(TRACKING);
		}
		
		initParams.add(NETWORK_UUID + popObject.getOd().getNetwork());
		
		initialize(initParams);
		popInfo = object;
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					treatRequests();
					close();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}, "Local JVM Broker thread").start();
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
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(0);
				}
			}
			
			try {
				LogWriter.writeDebugInfo("[Broker] Local file '%s'", codelocation);
				url = new File(codelocation).toURI().toURL();
				POPJavaAgent.getInstance().addJar(codelocation);
			} catch (MalformedURLException e) {
				LogWriter.writeDebugInfo("[Broker] %s.MalformedURLException : %s", 
					this.getClass().getName(), e.getMessage());
				System.exit(0);
			} catch (NotFoundException e) {
                e.printStackTrace();
                System.exit(0);
            }
			
			if (url != null) {
				LogWriter.writeDebugInfo("[Broker] url construct");
				
				urlClassLoader = new URLClassLoader(new URL[]{url});
			}
		}

		Class<?> targetClass;
		try {
			targetClass = getPOPObjectClass(objectName, urlClassLoader);
			//System.out.println("@@@ " + targetClass.getName());
			popInfo = (POPObject) targetClass.getConstructor().newInstance();
		} catch (Exception e) {
			LogWriter.writeDebugInfo("[Broker] %s ; Mesage: %s",
					e.getClass().getName(), e.getMessage());
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
				popObject.setBroker(this);
				
				POPClass annotation = popObject.getClass().getAnnotation(POPClass.class);
				if(annotation != null){
					requestQueue.setMaxQueue(annotation.maxRequestQueue());
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
					        Util.isParameterUsable(annotations[index])
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
			LogWriter.writeDebugInfo("[Broker] %s sendException: %s", 
				this.getLogPrefix(), exception.getMessage());
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
			        Util.isParameterUsable(annotations[index])){
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
	 * Replace the request semantics if we are working with a PopJava Object with the corresponding local annotation.
	 * 
	 * @param request The request to be queued 
	 */
	public void finalizeRequest(Request request) {
		try {
			// skip if marked as constructor
			if ((request.getSenmatics() & Semantic.CONSTRUCTOR) != 0) {
				return;
			}
			
			MethodInfo info = new MethodInfo(request.getClassId(), request.getMethodId());
			Method method = popInfo.getMethodByInfo(info);
			
			// use previously set semantics if possible
			if(methodSemanticsCache.containsKey(method)){
				request.setSenmatics(methodSemanticsCache.get(method));
				return;
			}
			
			Annotation[] annotations = method.getAnnotations();
			int semantics = 0;
			boolean isLocalhost = false;
			
			POPSyncConc syncConc;
			POPSyncSeq syncSeq;
			POPSyncMutex syncMutex;
			POPAsyncConc asyncConc;
			POPAsyncSeq asyncSeq;
			POPAsyncMutex asyncMutex;
			
			// local semantics
			if ((syncConc = MethodUtil.getAnnotation(annotations, POPSyncConc.class)) != null) {
				semantics = Semantic.SYNCHRONOUS | Semantic.CONCURRENT;
				isLocalhost = syncConc.localhost();
			}
			else if ((syncSeq = MethodUtil.getAnnotation(annotations, POPSyncSeq.class)) != null) {
				semantics = Semantic.SYNCHRONOUS | Semantic.SEQUENCE;
				isLocalhost = syncSeq.localhost();
			}
			else if ((syncMutex = MethodUtil.getAnnotation(annotations, POPSyncMutex.class)) != null) {
				semantics = Semantic.SYNCHRONOUS | Semantic.MUTEX;
				isLocalhost = syncMutex.localhost();
			}
			else if ((asyncConc = MethodUtil.getAnnotation(annotations, POPAsyncConc.class)) != null) {
				semantics = Semantic.ASYNCHRONOUS | Semantic.CONCURRENT;
				isLocalhost = asyncConc.localhost();
			}
			else if ((asyncSeq = MethodUtil.getAnnotation(annotations, POPAsyncSeq.class)) != null) {
				semantics = Semantic.ASYNCHRONOUS | Semantic.SEQUENCE;
				isLocalhost = asyncSeq.localhost();
			}
			else if ((asyncMutex = MethodUtil.getAnnotation(annotations, POPAsyncMutex.class)) != null) {
				semantics = Semantic.ASYNCHRONOUS | Semantic.MUTEX;
				isLocalhost = asyncMutex.localhost();
			} else {
				// not a semantic match, we keep what we received
				// XXX this happen when we get the annotation from a superclass
				// FIXME get annotation from super class
				semantics = request.getSenmatics();
			}
			
			// localhost only call
			if (isLocalhost) {
				semantics |= Semantic.LOCALHOST;
			}
			
			request.setSenmatics(semantics);
			methodSemanticsCache.put(method, semantics);
		} catch (NoSuchMethodException e) {
		}

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
		
		final MethodInfo info = new MethodInfo(request.getClassId(), request.getMethodId());
		try {
			method = popInfo.getMethodByInfo(info);
			//System.out.println("((-)) " + info + " @ " + method.toGenericString());
		} catch (NoSuchMethodException e) {
			exception = POPException.createReflectMethodNotFoundException(
					popInfo.getClass().getName(),
					request.getClassId(), request.getMethodId(),
					e.getMessage());
		}
		
		if(method != null){
			// cache the method and parameters annotations since they take a while to generate
			if(!methodParametersAnnotationCache.containsKey(method)){
				methodParametersAnnotationCache.put(method, method.getParameterAnnotations());
			}
		}
		
		Annotation[][] parametersAnnotations = methodParametersAnnotationCache.get(method);
		
		// Get parameter if found the method
		if (exception == null && method != null) {

			returnType = method.getReturnType();
			parameterTypes = method.getParameterTypes();
				
			try{

				POPBuffer requestBuffer = request.getBuffer();
				request.setBuffer(null);//This way the JVM can free the  buffer content
				parameters = getParameters(requestBuffer, parameterTypes, parametersAnnotations);
			}catch(POPException e){
				exception = e;
			}
		}
		
		normalizePOPParamameters(parameters);
		//LogWriter.writeDebugInfo("Call method "+method.getName());
		// Invoke the method if success to get all parameter
		if (exception == null && method != null) {
			final long trackingStart = System.currentTimeMillis();
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
				LogWriter.writeDebugInfo("[Broker] Cannot execute. Cause %s.", e.getCause().getMessage());
				exception = POPException.createReflectException(
						method.getName(), e.getCause().getMessage());
			}catch (Exception e) {
				// Cannot execute, send error
				LogWriter.writeExceptionLog(e);
				LogWriter.writeDebugInfo("[Broker] Cannot execute %s", method.toGenericString());
				exception = POPException.createReflectException(
						method.getName(), e.getMessage());

			} finally {
				if (tracking) {
					final long trackingTime = System.currentTimeMillis() - trackingStart;
					registerTracking(request.getCombox().getRemoteCaller(), method.toGenericString(), trackingTime);
				}
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

				//Put all parameters back in the response, if needed
				for (index = 0; index < parameterTypes.length; index++) {
					//If parameter is not a IN variable and
					//The parameter is not a POPObject without any specified direction
					if(Util.isParameterNotOfDirection(parametersAnnotations[index], POPParameter.Direction.IN) &&
					        Util.isParameterUsable(parametersAnnotations[index]) &&
							!(parameters[index] instanceof POPObject && !Util.isParameterOfAnyDirection(parametersAnnotations[index]))
							){
						try {
							responseBuffer.serializeReferenceObject(parameterTypes[index], parameters[index]);
						} catch (POPException e) {
							LogWriter.writeDebugInfo("[Broker] Excecption serializing parameter %s", parameterTypes[index].getName());
							exception = new POPException(e.errorCode, e.errorMessage);
							break;
						}
					}
				}
				
				if (exception == null) {
					if (returnType != Void.class && returnType != void.class && returnType != Void.TYPE){
						try {
							
							// propagate certificates for return type
							if (result instanceof POPObject) {
								POPObject returnObject = (POPObject) result;
								POPAccessPoint objAp = returnObject.getAccessPoint();
								
								// a certificate is necessary to connect to the returned object
								String originFingerprint = objAp.getFingerprint();
								if (originFingerprint != null) {
									// add to access point for the connector
									Certificate originCert = SSLUtils.getCertificate(originFingerprint);
									objAp.setX509certificate(SSLUtils.certificateBytes(originCert));

									// send connector certificate to object's node
									String destinationFingerprint = request.getCombox().getAccessPoint().getFingerprint();
									Certificate destCert = SSLUtils.getCertificate(destinationFingerprint);
									// send caller' certificate to object origin node
									returnObject.PopRegisterFutureConnectorCertificate(SSLUtils.certificateBytes(destCert));
								}
								
								// set the od with the current connection network
								returnObject.getOd().setNetwork(request.getCombox().getNetworkUUID());
							}
							
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
						LogWriter.writeDebugInfo("[Broker] Exit popobject");
						object.exit();
					}
				}
			}
		}
		// if have any error (cannot get the parameter, or cannot invoke method,
		// or cannot put the output parameter,
		// send it to the interface
		if (exception != null) {
			LogWriter.writeDebugInfo("[Broker] %s sendException: %s.", 
				this.getLogPrefix(), exception.getMessage());
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
		POPRemoteCaller caller = request.getRemoteCaller();
		remoteCaller.set(caller);
		
		// check for localhost only execution and throw exception if we can't
		if (request.isLocalhost() && !caller.isLocalHost()) {
			if (request.isSynchronous()){
				POPException exception = new POPException(POPErrorCode.METHOD_ANNOTATION_EXCEPTION, 
					"You can't call a localhost method from a remote location.");
				sendException(request.getCombox(), exception, request.getRequestID());
			}
		}
		
		// normal case
		else {
			// normal execution
			if ((request.getSenmatics() & Semantic.CONSTRUCTOR) != 0) {
				invokeConstructor(request);
			} else {
				invokeMethod(request);
			}
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
		request.getRequestQueue().remove(request);
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
		setState(State.Exit);
	}
	
	/**
	 * Close all create servers etc
	 */
	private void close() {
		for (ComboxServer comboxServer : comboxServers) {
			comboxServer.close();
		}
	}

	/**
	 * Return the access point of this broker
	 * 
	 * @return Access point associated with this broker
	 */
	public POPAccessPoint getAccessPoint() {
		return accessPoint;
	}

	/**
	 * Main loop of this broker
	 * @throws InterruptedException 
	 */
	public void treatRequests() throws InterruptedException { 
		setState(State.Running);
		while (getState() == State.Running) {
			Request request = requestQueue.peek(REQUEST_QUEUE_TIMEOUT_MS, TimeUnit.MILLISECONDS);
			
			if (request != null && request.getClassId() != 0 && request.getMethodId() != 0) {
				serveRequest(request);
			}
		}
		LogWriter.writeDebugInfo("[Broker] Close broker "+popInfo.getClassName());
	}

	/**
	 * Increment the connection counter
	 */
	public synchronized void onNewConnection() {
		connectionCount++;
		LogWriter.writeDebugInfo("[Broker] Open connection "+connectionCount);
	}

	/**
	 * Decrement de connection counter and exit the broker if there is no more
	 * connection
	 */
	public synchronized void onCloseConnection(String source) {
		connectionCount--;
		LogWriter.writeDebugInfo("[Broker] Close connection, left "+connectionCount+" "+source);
		if (connectionCount <= 0){
			setState(State.Exit);
		}
	}

	/**
	 * Get information about the deamon mode of this broker
	 * 
	 * @return deamon mode
	 */
	public boolean isDaemon() {
		return popInfo == null || popInfo.isDaemon();

	}
	
	/**
	 * Is tracking enabled on Broker side.
	 * @return 
	 */
	public boolean isTraking() {
		return tracking;
	}

	/**
	 * Get who is calling this method.
	 * @return 
	 */
	public static POPRemoteCaller getRemoteCaller() {
		return remoteCaller.get();
	}

	/**
	 * Get information about the state of this borker
	 * 
	 * @return current state
	 */
	public synchronized State getState() {
		if (isDaemon()){
			return State.Running;
		}
		return state;
	}

	/**
	 * Set state information of this broker
	 * 
	 * @param state
	 *            state to set to this broker
	 */
	public synchronized void setState(State state) {
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
	public boolean initialize(List<String> argvs) {
		try {
			accessPoint = new POPAccessPoint();

			buffer = new BufferXDR();
			ComboxFactoryFinder finder = ComboxFactoryFinder.getInstance();
			ComboxFactory[] comboxFactories = finder.getAvailableFactories();

			// mark traking for object
			this.tracking = Util.removeStringFromList(argvs, TRACKING) != null;
			
			List<ComboxServer> liveServers = new ArrayList<>();
			for (ComboxFactory factory : comboxFactories) {
				String prefix = String.format("-%s_port=", factory.getComboxName());

				// hadle multiple times the same protocol
				String port;
				while ((port = Util.removeStringFromList(argvs, prefix)) != null) {
					// if we don't have a port, abort
					if (port == null) {
						continue;
					}

					int iPort = 0;
					if (port.length() > 0) {
						try {
							iPort = Integer.parseInt(port);
						} catch (NumberFormatException e) {

						}
					}

					AccessPoint ap = new AccessPoint(factory.getComboxName(), POPSystem.getHostIP(), iPort);
					accessPoint.addAccessPoint(ap);

					liveServers.add(factory.createServerCombox(ap, buffer, this));
				}
			}

			// If no protocol was specified, fall back to available protocols
			if(liveServers.isEmpty()){
				for (ComboxFactory factory : ComboxFactoryFinder.getInstance().getAvailableFactories()) {
					AccessPoint ap = new AccessPoint(factory.getComboxName(), POPSystem.getHostIP(), 0);
					accessPoint.addAccessPoint(ap);

					liveServers.add(factory.createServerCombox(ap, buffer, this));
				}
			}

			comboxServers = liveServers.toArray(new ComboxServer[liveServers.size()]);
			return true;
		} catch(Exception e) {
			LogWriter.writeExceptionLog(e);
			return false;
		}
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
			return Class.forName(className, true, urlClassLoader);
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
            	LogWriter.writeDebugInfo("[Broker] POP Uncatched exception");
            	LogWriter.writeExceptionLog(e);
            }
        });
		
		ArrayList<String> argvList = new ArrayList<>();
		LogWriter.writeDebugInfo("[Broker] Broker parameters");
		for (String str : argvs) {
			argvList.add(str);
			LogWriter.writeDebugInfo(" %79s", str);
		}
		LogWriter.writeDebugInfo("[Broker] Broker parameters end");
		
		String appservice = Util.removeStringFromList(argvList,
				APPSERVICE_PREFIX);
		String codelocation = Util.removeStringFromList(argvList,
				CODELOCATION_PREFIX);
		String objectName = Util.removeStringFromList(argvList,
				OBJECT_NAME_PREFIX);
		String actualObjectName = Util.removeStringFromList(argvList,
				ACTUAL_OBJECT_NAME_PREFIX);
		String userConfiguration = Util.removeStringFromList(argvList, 
				POPJAVA_CONFIG_PREFIX);
		String jobService = Util.removeStringFromList(argvList,
				JOB_SERVICE);
		String network = Util.removeStringFromList(argvList, 
				NETWORK_UUID);
		
		Configuration conf = Configuration.getInstance();
		if (userConfiguration != null) {
			try {
				File config = new File(userConfiguration);
				conf.load(config);
			} catch(IOException e) {
				LogWriter.writeDebugInfo("[Broker] Couldn't load user config %s: %s", userConfiguration, e.getMessage());
			}
		}
		
		// directories informations
		String objId = Util.generateUUID();
		// create directories and setup their cleanup
		RuntimeDirectoryThread runtimeCleanup = new RuntimeDirectoryThread(objId);
		runtimeCleanup.addCleanupHook();
		// change base dir
		System.setProperty("user.dir", Paths.get(objId).toString());
		
		if (actualObjectName != null && actualObjectName.length() > 0) {
			objectName = actualObjectName;
		}
		String callbackString = Util.removeStringFromList(argvList, CALLBACK_PREFIX);
		if (appservice != null && appservice.length() > 0) {
			POPSystem.appServiceAccessPoint.setAccessString(appservice);
		}
		if (jobService != null && !jobService.isEmpty()) {
			POPSystem.jobService.setAccessString(jobService);
		}
		
		Combox callback = null;
		if (callbackString != null && callbackString.length() > 0) {
			POPAccessPoint accessPoint = new POPAccessPoint(callbackString);
			// use factory to determine which combox to use
			ComboxFactoryFinder finder = ComboxFactoryFinder.getInstance();
			for (int i = 0; i < accessPoint.size(); i++) {
				// get protocol from accessPoint
				String protocol = accessPoint.get(i).getProtocol();
				ComboxFactory factory = finder.findFactory(protocol);
				
				// skip to next protocol
				if (factory == null) {
					continue;
				}
				
				// create callback
				try {
					callback = factory.createClientCombox(network);

					if (callback.connectToServer(accessPoint, 0)) {
						LogWriter.writeDebugInfo("[Broker] Connected to callback socket");
					} else {
						LogWriter.writeDebugInfo("[Broker] Error: fail to connect to callback:%s",
								accessPoint.toString());
					}
				} catch(IOException e) {
					LogWriter.writeExceptionLog(e);
					LogWriter.writeDebugInfo("[Broker] Failed to connect to callback socket");
					continue;
				}
				break;
			}
		}
		
		if (callback == null) {
			LogWriter.writeDebugInfo("[Broker] Error: callback is null");
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
		MessageHeader messageHeader = new MessageHeader();
		POPBuffer buffer = new BufferXDR();
		buffer.setHeader(messageHeader);
		buffer.putInt(status);
		broker.getAccessPoint().serialize(buffer);
		callback.send(buffer);
		LogWriter.writeDebugInfo("[Broker] Broker can be accessed at "+broker.getAccessPoint().toString());

		if (status == 0){
			broker.treatRequests();
			broker.close();
		}
		
		LogWriter.writeDebugInfo("[Broker] End broker life : "+objectName);
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

	/**
	 * The broker global request queue.
	 * @return 
	 */
	public RequestQueue getRequestQueue() {
		return requestQueue;
	}
	
	/**
	 * Register a tracking event in the broker.
	 * @param caller Who called the method.
	 * @param method The method called.
	 * @param time How much time did the execution take.
	 */
	private void registerTracking(POPRemoteCaller caller, String method, long time) {
		POPTracking userTracking = callerTracking.get(caller);
		// create if it's the first time we see this caller
		if (userTracking == null) {
			userTracking = new POPTracking(caller);
			callerTracking.put(caller, userTracking);
		}
		userTracking.track(method, time);
	}

	/**
	 * All the currently tracked users.
	 * @return An array of callerID via {@link Combox#partyIdentification() }
	 */
	public POPRemoteCaller[] getTrackingUsers() {
		return callerTracking.keySet().toArray(new POPRemoteCaller[callerTracking.size()]);
	}

	/**
	 * Statistics on a single user.
	 * @param caller A caller remote location
	 * @return 
	 */
	public POPTracking getTracked(POPRemoteCaller caller) {
		return callerTracking.get(caller);
	}
}
