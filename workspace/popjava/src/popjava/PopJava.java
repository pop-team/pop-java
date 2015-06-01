package popjava;

import popjava.base.POPException;
import popjava.base.POPObject;
import popjava.baseobject.ObjectDescription;
import popjava.baseobject.POPAccessPoint;
import popjava.buffer.POPBuffer;
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
