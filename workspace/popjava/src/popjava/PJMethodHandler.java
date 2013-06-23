package popjava;

import popjava.annotation.POPParameter;
import popjava.base.*;
import popjava.baseobject.POPAccessPoint;
import popjava.buffer.BufferFactory;
import popjava.buffer.POPBuffer;
import popjava.interfacebase.Interface;
import popjava.util.ClassUtil;
import popjava.util.LogWriter;
import popjava.util.Util;
import javassist.util.proxy.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is responsible to invoke methods on the parallel object
 */
public class PJMethodHandler extends Interface implements MethodHandler {

	/**
	 * Default semantic of a constructor
	 */
	protected final int constructorSemanticId = 21;

	protected POPObject popObjectInfo = null;

	/**
	 * Creates a new instance of PJComboxMethodHandler
	 */
	public PJMethodHandler() {

	}

	/**
	 * Associate an POPObject with this handler
	 * @param popObject	The POPObject to associate
	 */
	public PJMethodHandler(POPObject popObject) {
		popObjectInfo = popObject;
	}

	/**
	 * Construct a parallel object
	 * @param targetClass	Class to be created
	 * @param argvs			Arguments of the constructor
	 * @return	true if the object is instantiate
	 * @throws POPException				Thrown if any problem occurred during the parallel object creation
	 * @throws NoSuchMethodException	Thrown if the constructor is not found
	 */
	public boolean popConstructor(Class<?> targetClass, Object... argvs)
			throws POPException, NoSuchMethodException {

		Constructor<?> constructor = null;
		Class<?>[] parameterTypes = ClassUtil.getObjectTypes(argvs);
		constructor = ClassUtil.getConstructor(targetClass, parameterTypes);
		
		parameterTypes = constructor.getParameterTypes();
		// Repair the parameter type, for example MyConstructor(int value,
		// String ... data)
		allocate(popObjectInfo.getClassName());

		MethodInfo methodInfo = popObjectInfo.getMethodInfo(constructor);
		MessageHeader messageHeader = new MessageHeader(
				methodInfo.getClassId(), methodInfo.getMethodId(),
				constructorSemanticId);
		BufferFactory factory = combox.getBufferFactory();
		POPBuffer popBuffer = factory.createBuffer();
		popBuffer.setHeader(messageHeader);
		for (int index = 0; index < argvs.length; index++) {
			/*if(argvs[index] instanceof POPObject){
				argvs[index]= PopJava.newActive(argvs[index].getClass().getSuperclass(),
						((POPObject)argvs[index]).getAccessPoint());
			}*/
			
			popBuffer.putValue(argvs[index], parameterTypes[index]);
		}

		popDispatch(popBuffer);
		POPBuffer responseBuffer = combox.getBufferFactory().createBuffer();
		popResponse(responseBuffer);
		for (int index = 0; index < parameterTypes.length; index++) {
			responseBuffer.deserializeReferenceObject(parameterTypes[index],
					argvs[index]);
		}
		return true;

	}

	/**
	 * Bind the interface-side with the broker-side
	 * @param accesspoint	Access point of the broker-side
	 * @return	true if the binding is succeed
	 * @throws POPException	throw an exception if the binding is not succeed
	 */
	public boolean bindObject(POPAccessPoint accesspoint) throws POPException {
		popAccessPoint.setAccessString(accesspoint.toString());
		return bind(accesspoint);
	}

	/**
	 * Invoke a method on an object
	 * @param self		The object to call the method
	 * @param m			The method to be called
	 * @param proceed	The method to proceed the call
	 * @param argvs		Arguments of the methods
	 * @return	Any object if the method has a return value
	 * @throws	Throw any exception if the method throws any exception
	 */
	public Object invoke(Object self, Method m, Method proceed, Object[] argvs)
			throws Throwable {
		
		Object result = null;
		// If serialize or de-serialize
		boolean[] canExecute = new boolean[1];
		result = invokeCustomMethod(self, m, proceed, canExecute, argvs);
		if (canExecute[0]){
			return result;
		}

		Class<?> proceedClass = m.getDeclaringClass();
		if (!POPObject.class.isAssignableFrom(proceedClass))
			return null;
		Class<?> returnType = m.getReturnType();
		// Invoke the method
		result = new Object();
		MethodInfo info = popObjectInfo.getMethodInfo(m);

		m.setAccessible(true);
		int methodSemantics = (Integer) popObjectInfo.getSemantic(info);
		MessageHeader messageHeader = new MessageHeader(info.getClassId(), info
				.getMethodId(), methodSemantics);
		POPBuffer popBuffer = combox.getBufferFactory().createBuffer();
		popBuffer.setHeader(messageHeader);
		Class<?>[] parameterTypes = m.getParameterTypes();
		
		
		for (int index = 0; index < argvs.length; index++) {
			popBuffer.putValue(argvs[index], parameterTypes[index]);
		}
		popDispatch(popBuffer);
		
		if ((methodSemantics & Semantic.Synchronous) != 0) {
			POPBuffer responseBuffer = combox.getBufferFactory().createBuffer();
			popResponse(responseBuffer);
			
			//Recover the data from the calling method. The called method can
			//Modify the content of an array and it gets copied back in here
			Annotation[][] annotations = m.getParameterAnnotations();
			
			for (int index = 0; index < parameterTypes.length; index++) {
				
				if(Util.serializeParameter(annotations[index])){
					responseBuffer.deserializeReferenceObject(
							parameterTypes[index], argvs[index]);
				}
			}
			
			//Get the return value in case the called method has one
			if (returnType != Void.class && returnType != void.class){
				result = responseBuffer.getValue(returnType);
			}
			
		} else {
			//If the method is async and has a return type, return default value
			//This should never happen
			if (returnType != Void.class && returnType != void.class) {
				try {
					if (returnType.isPrimitive()) {
						result = ClassUtil
								.getDefaultPrimitiveValue(returnType);
					} else {
						result = null;
					}
				} catch (Exception e) {
					result = null;
				}

			}

		}
		return result;

	}

	private int hashMethod(Method m){
		String hash = m.getName();
		for(Class<?> type: m.getParameterTypes()){
			hash += type.getName();
		}
		
		return hash.hashCode();
	}
	
	private ConcurrentHashMap<Integer, Method> methodCache = new ConcurrentHashMap<Integer, Method>();
	private Set<Integer> methodMisses = Collections.newSetFromMap(new ConcurrentHashMap<Integer, Boolean>());
	
	/**
	 * Return a copy of the given method
	 * @param method	Method to be copied	
	 * @return	Method copy
	 */
	private Method getSameInterfaceMethod(Method method) {
		int methodHash = hashMethod(method);
		
		if(methodMisses.contains(methodHash)){
			return null;
		}
		
		Method m = methodCache.get(methodHash);
		
		if(m != null){
			return m;
		}
		
		try {
			m = getClass().getMethod(method.getName(), method.getParameterTypes());
			methodCache.put(methodHash, m);
			return m;
		} catch (Exception e){
			methodMisses.add(methodHash);
		}
		
		return null;
	}

	/**
	 * Try to invoke a custom method of the associated class
	 * @param self	The object on which the method have to be invoked
	 * @param m		Method to be invoked
	 * @param proceed	
	 * @param canExcute	
	 * @param argvs	
	 * @return
	 */
	private Object invokeCustomMethod(Object self, Method m, Method proceed,
			boolean[] canExcute, Object[] argvs) {
		canExcute[0] = false;
		String methodName = m.getName();
		if (argvs.length == 1 && (methodName.equals("serialize")) || (methodName.equals("deserialize"))) {
			boolean result = false;
			POPBuffer buffer = (POPBuffer) argvs[0];
			if (methodName.equals("serialize")) {
				canExcute[0] = true;
				result = serialize(buffer);
			} else if (methodName.equals("deserialize")) {
				canExcute[0] = true;
				result = deserialize(buffer);
			}
			return result;
		} else if (methodName.equals("exit") && argvs.length == 0) {
			canExcute[0] = true;
			invokeExit();
		} else {
			Method interfaceMethod = getSameInterfaceMethod(m);
			if (interfaceMethod != null) {
				try {
					Object result = interfaceMethod.invoke(this, argvs);
					canExcute[0] = true;
					return result;
				} catch (Exception exception) {
				}
			}
		}

		return new Object();
	}

	/**
	 * Close all files
	 */
	private void invokeExit() {
		close();
	}

	/**
	 * Format a string of this object
	 */
	public String toString() {
		return getClass().getName() + ":" + popAccessPoint.toString();
	}
}
