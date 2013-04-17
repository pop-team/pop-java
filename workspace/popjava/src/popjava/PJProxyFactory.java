package popjava;

import popjava.base.*;
import popjava.baseobject.*;
import popjava.buffer.*;
import popjava.system.*;
import popjava.util.*;

import java.lang.reflect.*;
import javassist.util.proxy.*;

/**
 * POP-Java Proxy Factory : this class provide methods to create a proxy factory for a specified class. This class uses the Javassit library.
 * 
 *
 */
public class PJProxyFactory extends ProxyFactory {
	/**
	 * Target class to create a proxy
	 */
	protected Class<?> targetClass;

	/**
	 * Create a new proxy factory for the specified class
	 * @param targetClass : Class to be created by the Factory
	 */
	public PJProxyFactory(Class<?> targetClass) {
		this.targetClass = targetClass;
		setSuperclass(targetClass);
		MethodFilter methodFilter = new PJMethodFilter();
		setFilter(methodFilter);
	}

	/**
	 * Create a new object from the factory
	 * @param argvs : arguments to pass trough the constructor of the specific object
	 * @return the instance of the object
	 * @throws POPException
	 */
	public Object newPOPObject(Object... argvs) throws POPException {
		ObjectDescription objectDescription = POPSystem.getDefaultOD();
		return newPOPObject(objectDescription, argvs);
	}

	/**
	 * Create a new object from specific class and object description.
	 * @param od : Object description with the resource requirements
	 * @param argvs : arguments to pass trough the constructor of the specific object
	 * @return the instance of the object
	 * @throws POPException
	 */
	public Object newPOPObject(ObjectDescription od, Object... argvs)
			throws POPException {
		try {
			POPObject popObject = null;
			try{
				Class<?>[] parameterTypes = ClassUtil.getObjectTypes(argvs);
				Constructor<?> constructor = targetClass.getConstructor(parameterTypes);
				popObject = (POPObject) constructor.newInstance(argvs);
				popObject.loadDynamicOD(constructor, argvs);
			}catch(Exception e){
				Constructor<?> constructor = targetClass.getConstructor();
				popObject = (POPObject) constructor.newInstance();
				popObject.loadDynamicOD(constructor);
			}
			
			ObjectDescription originalOd = popObject.getOd();
			originalOd.merge(od);
			PJMethodHandler methodHandler = new PJMethodHandler(popObject);
			methodHandler.setOd(originalOd);
			methodHandler.popConstructor(targetClass, argvs);
			this.setHandler(methodHandler);
			Class<?> c = this.createClass();
			Object result = c.newInstance();
			((ProxyObject) result).setHandler(methodHandler);
			return result;
		} catch (java.lang.InstantiationException e) {
			e.printStackTrace();
		} catch (java.lang.IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Bind an Interface to her parallel object (her associated Broker)
	 * @param accessPoint : The accesspoint of the broker
	 * @return ProxyObject which represent the Interface side
	 * @throws POPException : if anything goes wrong
	 */
	public Object bindPOPObject(POPAccessPoint accessPoint) throws POPException {
		try {
			Constructor<?> constructor = targetClass.getConstructor();
			POPObject popObject = (POPObject) constructor.newInstance();
			popObject.loadDynamicOD(constructor);
			PJMethodHandler methodHandler = new PJMethodHandler(popObject);
			methodHandler.bindObject(accessPoint);
			this.setHandler(methodHandler);
			Class<?> c = this.createClass();
			Object result = c.newInstance();
			((ProxyObject) result).setHandler(methodHandler);
			return result;
		} catch (java.lang.InstantiationException e) {
			e.printStackTrace();
		} catch (java.lang.IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Recover a parallel object from the buffer
	 * @param buffer : buffer from which the object is recovered 
	 * @return the object recovered
	 * @throws POPException
	 */
	public Object newActiveFromBuffer(POPBuffer buffer) throws POPException {
		POPObject result = null;
		try {
			Constructor<?> constructor = targetClass.getConstructor();
			POPObject popObject = (POPObject) constructor.newInstance();
			popObject.loadDynamicOD(constructor);
			PJMethodHandler methodHandler = new PJMethodHandler(popObject);
			this.setHandler(methodHandler);
			Class<?> c = this.createClass();
			result = (POPObject) c.newInstance();
			((ProxyObject) result).setHandler(methodHandler);
			if (!result.deserialize(buffer)) {
				LogWriter.writeDebugInfo("bad deserialize");
				POPException.throwObjectBindException(methodHandler
						.getAccessPoint());
			}
		} catch (java.lang.InstantiationException e) {
			e.printStackTrace();
		} catch (java.lang.IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		LogWriter.writeDebugInfo("result");
		return result;
	}
}
