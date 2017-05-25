package popjava;

import java.util.Arrays;
import javassist.util.proxy.ProxyObject;
import popjava.base.POPException;
import popjava.base.POPObject;
import popjava.baseobject.ObjectDescription;
import popjava.baseobject.POPAccessPoint;
import popjava.buffer.POPBuffer;
import popjava.combox.ComboxFactoryFinder;
import popjava.interfacebase.Interface;
import popjava.service.jobmanager.POPJavaJobManager;
import popjava.service.jobmanager.connector.POPConnectorTFC;
import popjava.serviceadapter.POPJobManager;
import popjava.system.POPSystem;

/**
 * 
 * This class is used to create parallel object. All the methods from this class are static so no instantiation is needed.
 *
 */
public class PopJava {

	/** Creates a new instance of PopJava */
	public PopJava() {
	}
	
	/**
	 * Static method used to create a new parallel object by passing an object description
	 * @param targetClass			the parallel class to be created
	 * @param objectDescription		the object description for the resource requirements 
	 * @param argvs					arguments of the constructor (may be empty)
	 * @return references to the parallel object created
	 * @throws POPException 
	 */
	public static <T> T newActive(Class<T> targetClass,
			ObjectDescription objectDescription, Object ... argvs)
			throws POPException {
	    POPSystem.start();
		PJProxyFactory factoryProxy = new PJProxyFactory(targetClass);
		return (T)factoryProxy.newPOPObject(objectDescription, argvs);
	}
	
	public static Object newActive(String targetClass, Object... argvs) throws POPException, ClassNotFoundException{
	    return newActive(Class.forName(targetClass), argvs);
	}
	
	/**
	 * Static method used to create a new parallel object
	 * @param targetClass	the parallel class to be created
	 * @param argvs			arguments of the constructor (may be empty)
	 * @return references to the parallel object created
	 * @throws POPException
	 */
	public static <T> T newActive(Class<T> targetClass, Object... argvs)
			throws POPException {
	    POPSystem.start();
		PJProxyFactory factoryProxy = new PJProxyFactory(targetClass);
		return (T)factoryProxy.newPOPObject(argvs);
	}

	/**
	 * Static method used to create a parallel object from an existing access point
	 * @param targetClass	the parallel class to be created
	 * @param accessPoint	access point of the living object
	 * @return references to the parallel object
	 * @throws POPException
	 */
	public static <T> T newActive(Class<T> targetClass,
			POPAccessPoint accessPoint) throws POPException {
	    POPSystem.start();
		PJProxyFactory factoryProxy = new PJProxyFactory(targetClass);
		return (T)factoryProxy.bindPOPObject(accessPoint);
	}

	/**
	 * Static method used to create a parallel object from the buffer
	 * @param targetClass	the parallel class to be recovered
	 * @param buffer		buffer from which the object must be recovered
	 * @return references to the parallel object
	 * @throws POPException
	 */
	public static <T> T newActiveFromBuffer(Class<T> targetClass, POPBuffer buffer)
			throws POPException {
	    POPSystem.start();
		PJProxyFactory factoryProxy = new PJProxyFactory(targetClass);
		return (T)factoryProxy.newActiveFromBuffer(buffer);
	}
	
	/**
	 * Return multiple an array of T
	 * @param targetClass The class we are looking for remotely
	 * @param instances How many instances we would like to fill
	 * @param od Parameters for the research, mainly the network we want to look into
	 * @return The actual number of instances available in the the instances array
	 */
	public static int newTFCSearch(Class targetClass, POPAccessPoint[] instances, ObjectDescription od) {
		POPSystem.start();
		// we ARE in a TFC environment
		od.setConnector(POPConnectorTFC.IDENTITY);
		
		// we must specify a network
		if (od.getNetwork().isEmpty()) {
			return 0;
		}
		// we must ask for at least one instance
		if (instances == null || instances.length == 0) {
			return 0;
		}
		
		// fill with something if value is null
		for (int i = 0; i < instances.length; i++) {
			if (instances[i] == null) {
				instances[i] = new POPAccessPoint();
			}
		}
		
		// connect to local job manager
		POPJavaJobManager jm = getLocalJobManager();
		
		// we use create object in combination with a TFC connector
		// this will get us a varing number of access points registered on the network
		jm.createObject(POPSystem.appServiceAccessPoint, targetClass.getName(), od, instances.length, instances, 0, new POPAccessPoint[0]);
		jm.exit();
		
		// 
		Arrays.sort(instances, (a, b) -> a.isEmpty() ? 1 : -1);
		return (int) Arrays.asList(instances).stream().filter(t -> !t.isEmpty()).count();
	}
	
	/**
	 * Register a POPObject and make it available for discovery in a network
	 * @param object
	 * @param tfcNetwork
	 * @param secret
	 * @return 
	 */
	public static boolean registerTFC(Object object, String tfcNetwork, String secret) {
	    if(object instanceof POPObject){
	        POPObject temp = (POPObject) object;
			POPJavaJobManager jm = getLocalJobManager();
			boolean status = jm.registerTFCObject(tfcNetwork, temp.getClassName(), temp.getAccessPoint(), secret);
			jm.exit();
			return status;
		}
		return false;
	}
	
	/**
	 * Unregister a POPObject from the local JobManager
	 * @param object
	 * @param tfcNetwork
	 * @param secret 
	 */
	public static void unregisterTFC(Object object, String tfcNetwork, String secret) {
	    if(object instanceof POPObject){
	        POPObject temp = (POPObject) object;
			POPJavaJobManager jm = getLocalJobManager();
			jm.unregisterTFCObject(tfcNetwork, temp.getClassName(), temp.getAccessPoint(), secret);
			jm.exit();
		}
	}
	
	/**
	 * Open a connection to the local JobManager, this connection need to be closed with a call to .exit()
	 * @return 
	 */
	private static POPJavaJobManager getLocalJobManager() {
		ComboxFactoryFinder finder = ComboxFactoryFinder.getInstance();
		POPAccessPoint jma = new POPAccessPoint(String.format("%s://%s:%d", 
				finder.get(0).getComboxName(), POPSystem.getHostIP(), POPJobManager.DEFAULT_PORT));
		POPJavaJobManager jm = PopJava.newActive(POPJavaJobManager.class, jma);
		return jm;
	}
	
	public static POPAccessPoint getAccessPoint(Object object){
	    if(object instanceof POPObject){
	        POPObject temp = (POPObject) object;
	        return temp.getAccessPoint();
	    }
	    
	    throw new RuntimeException("Object was not of type "+POPObject.class.getName());
	}
	
	public static <T extends Object> T getThis(T object){
	    return (T) ((POPObject) object).getThis(object.getClass());
	}
	
	/**
	 * Destroys a POP object.
	 * @param object
	 */
	public static void destroy(Object object){
		((POPObject)object).exit();
	}
	
	/**
	 * Disconnects the POP object without desroying the remove object.
	 * The remote object will close if it has no connections left.
	 * @param object
	 */
	public static void disconnect(Object object){
		if(object instanceof ProxyObject){
			((PJMethodHandler)((ProxyObject)object).getHandler()).decRef();
			((PJMethodHandler)((ProxyObject)object).getHandler()).close();
		}
	}
	
	/**
	 * Returns true if POP-Java is loaded and enabled
	 * @return
	 */
	public static boolean isPOPJavaActive(){
	    try {
	        popjava.javaagent.POPJavaAgent.getInstance();
	    } catch (Exception e) {
	        return false;
	    }
	    
	    return true;
	}
}
