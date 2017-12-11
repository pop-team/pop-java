package popjava;

import java.util.ArrayList;
import java.util.List;
import javassist.util.proxy.ProxyObject;
import popjava.base.POPException;
import popjava.base.POPObject;
import popjava.baseobject.ObjectDescription;
import popjava.baseobject.POPAccessPoint;
import popjava.broker.Broker;
import popjava.buffer.POPBuffer;
import popjava.service.jobmanager.POPJavaJobManager;
import popjava.service.jobmanager.network.POPNodeAJobManager;
import popjava.service.jobmanager.network.POPNode;
import popjava.system.POPSystem;
import popjava.util.Configuration;
import popjava.util.LogWriter;
import popjava.util.POPRemoteCaller;
import popjava.util.ssl.SSLUtils;

/**
 * 
 * This class is used to create parallel object. All the methods from this class are static so no instantiation is needed.
 *
 */
public class PopJava {

	private PopJava() {
	}
	
	/**
	 * Static method used to create a new parallel object by passing an object description
	 * @param targetClass			the parallel class to be created
	 * @param objectDescription		the object description for the resource requirements 
	 * @param argvs					arguments of the constructor (may be empty)
	 * @return references to the parallel object created
	 * @throws POPException 
	 */
	@SuppressWarnings("unchecked")
	public static <T> T newActive(Class<T> targetClass,
			ObjectDescription objectDescription, Object ... argvs)
			throws POPException {
		POPSystem.start();
		PJProxyFactory factoryProxy = new PJProxyFactory(targetClass);
		return (T) factoryProxy.newPOPObject(objectDescription, argvs);
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
	@SuppressWarnings("unchecked")
	public static <T> T newActive(Class<T> targetClass, Object... argvs)
			throws POPException {
		POPSystem.start();
		PJProxyFactory factoryProxy = new PJProxyFactory(targetClass);
		return (T) factoryProxy.newPOPObject(argvs);
	}

	/**
	 * Static method used to connect to an already existing parallel object
	 * TODO rename as connect?
	 * @param targetClass	the parallel class to be created
	 * @param accessPoint	access point of the living object
	 * @return references to the parallel object
	 * @throws POPException
	 */
	@SuppressWarnings("unchecked")
	public static <T> T newActive(Class<T> targetClass,
			POPAccessPoint accessPoint) throws POPException {
		POPSystem.start();
		PJProxyFactory factoryProxy = new PJProxyFactory(targetClass);
		return (T) factoryProxy.bindPOPObject(accessPoint);
	}

	/**
	 * Static method used to connect to an already existing parallel object with a custom network
	 * @param targetClass	the parallel class to be created
	 * @param networkUUID	the network that we will try to connect to
	 * @param accessPoint	access point of the living object
	 * @return references to the parallel object
	 * @throws POPException
	 */
	public static <T> T connect(Class<T> targetClass, String networkUUID, POPAccessPoint accessPoint) {
		POPSystem.start();
		PJProxyFactory factoryProxy = new PJProxyFactory(targetClass);
		return (T) factoryProxy.bindPOPObject(accessPoint, networkUUID);
	}

	/**
	 * Static method used to create a parallel object from the buffer
	 * @param targetClass	the parallel class to be recovered
	 * @param buffer		buffer from which the object must be recovered
	 * @return references to the parallel object
	 * @throws POPException
	 */
	@SuppressWarnings("unchecked")
	public static <T> T newActiveFromBuffer(Class<T> targetClass, POPBuffer buffer)
			throws POPException {
		POPSystem.start();
		PJProxyFactory factoryProxy = new PJProxyFactory(targetClass);
		return (T) factoryProxy.newActiveFromBuffer(buffer);
	}
	
	/**
	 * Search a live object in the network.
	 * 
	 * A research should look something like this:
	 * <pre>
	 * ObjectDescription od = new ObjectDescription();
	 * od.setNetwork(publishNetworkUUID);
	 * POPAccessPoint[] liveObjects = PopJava.newTFCSearch(TFCObject.class, maxNumInstances, od);
	 * for (POPAccessPoint ap : liveObjects) {
	 *	// choose or connect
	 *	...
	 * }
	 * 
	 * TFCObject remote = PopJava.connect(TFCObject.class, publishNetworkUUID, choosenAccessPoint);
	 * </pre>
	 * 
	 * @param targetClass The class we are looking for remotely
	 * @param maxInstances The maximal number of instances we would like
	 * @param od Parameters for the research, mainly the network we want to look into
	 * @return An array with all the 
	 */
	public static POPAccessPoint[] newTFCSearch(Class targetClass, int maxInstances, ObjectDescription od) {
		POPSystem.start();
		// we ARE in a TFC environment
		od.setConnector("tfc");
		
		// we must specify a network
		if (od.getNetwork().isEmpty()) {
			return new POPAccessPoint[0];
		}
		// we must ask for at least one instance
		if (maxInstances <= 0) {
			return new POPAccessPoint[0];
		}
		
		// create and fill array of AccesPoints
		POPAccessPoint[] instances = new POPAccessPoint[maxInstances];
		for (int i = 0; i < maxInstances; i++) {
			instances[i] = new POPAccessPoint();
		}
		
		// connect to local job manager
		Configuration conf = Configuration.getInstance();
		String protocol = conf.getJobManagerProtocols()[0];
		int port = conf.getJobManagerPorts()[0];
		POPAccessPoint jma = new POPAccessPoint(String.format("%s://%s:%d",
			protocol, POPSystem.getHostIP(), port));
		POPJavaJobManager jm = PopJava.newActive(POPJavaJobManager.class, jma);
		
		// we use create object in combination with a TFC connector
		// this will get us a varing number of access points registered on the network
		try {
			jm.createObject(POPSystem.appServiceAccessPoint, targetClass.getName(), od, instances.length, instances, 0, new POPAccessPoint[0]);
		} catch(Exception e) {
			LogWriter.writeDebugInfo("[TFC] Can't look for resources: %s", e.getCause());
			LogWriter.writeExceptionLog(e);
		} finally {
			jm.exit();
		}
		
		// return only active access points
		List<POPAccessPoint> actives = new ArrayList<>();
		for (POPAccessPoint instance : instances) {
			if (!instance.isEmpty()) {
				actives.add(instance);
				
				byte[] cert = instance.getX509certificate();
				if (cert != null && cert.length > 0) {
					SSLUtils.addCertToTempStore(cert);
				}
			}
		}
		return actives.toArray(new POPAccessPoint[actives.size()]);
	}

	/**
	 * Return the 
	 * 
	 * @param targetClass
	 * @param networkUUID
	 * @param node
	 * @return 
	 */
	public static POPAccessPoint[] newTFCSearchOn(Class targetClass, String networkUUID, POPNode node) {
		POPAccessPoint[] aps = new POPAccessPoint[0];
		if (!(node instanceof POPNodeAJobManager)) {
			return aps;
		}
		
		// cast node and connect to remote job manager
		POPNodeAJobManager jmNode = (POPNodeAJobManager) node;
		try {
			// make local reserach on node
			POPJavaJobManager jobManager = jmNode.getJobManager(networkUUID);
			
			try {
				aps = jobManager.localTFCSearch(networkUUID, targetClass.getName());
			} catch (Exception e) { throw e; }
			finally {
				// exit since the node keep connection alives
				jobManager.exit();
			}
		} catch (Exception e) {
			LogWriter.writeDebugInfo("[TFC] Can't connect to [%s].", node);
		}
		
		return aps;
	}
	
	public static POPAccessPoint getAccessPoint(Object object){
		if(object == null){
			throw new NullPointerException("Reference to POPJava object was null");
		}
		
		if(object instanceof POPObject){
			POPObject temp = (POPObject) object;
			return temp.getAccessPoint();
		}
		
		throw new RuntimeException("Object was not of type "+POPObject.class.getName());
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getThis(T object){
		if (object instanceof POPObject) {
			return (T) ((POPObject) object).getThis(object.getClass());
		}
		return null;
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
	
	/**
	 * Return the remote source for the call to this method
	 * @return 
	 */
	public static POPRemoteCaller getRemoteCaller() {
		return Broker.getRemoteCaller();
	}
	
	/**
	 * Given a Proxy Object (Client) connected to a POP Object (Server) we get the identifier of the server.
	 * @param object
	 * @return 
	 */
	public static POPRemoteCaller getRemote(Object object) {
		if(object instanceof ProxyObject){
			ProxyObject origin = (ProxyObject) object;
			return ((PJMethodHandler) origin.getHandler()).getRemote();
		}
		throw new IllegalArgumentException("The obejct is not a valid Proxy Object");
	}
}
