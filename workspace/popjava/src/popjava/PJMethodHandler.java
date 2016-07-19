package popjava;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyObject;
import popjava.annotation.POPParameter;
import popjava.base.MessageHeader;
import popjava.base.MethodInfo;
import popjava.base.POPException;
import popjava.base.POPObject;
import popjava.base.Semantic;
import popjava.baseobject.POPAccessPoint;
import popjava.buffer.BufferFactory;
import popjava.buffer.POPBuffer;
import popjava.interfacebase.Interface;
import popjava.util.ClassUtil;
import popjava.util.Configuration;
import popjava.util.LogWriter;
import popjava.util.Util;

/**
 * This class is responsible to invoke methods on the parallel object
 */
public class PJMethodHandler extends Interface implements MethodHandler {

	/**
	 * Default semantic of a constructor
	 */
	protected final int constructorSemanticId = 21;

	protected POPObject popObjectInfo = null;
	private AtomicInteger requestID = new AtomicInteger(1);

	private AtomicBoolean setup = new AtomicBoolean(false);
	

	/**
	 * Associate an POPObject with this handler
	 * @param popObject	The POPObject to associate
	 */
	public PJMethodHandler(POPObject popObject) {
		popObjectInfo = popObject;
	}

	public void setSetup(){
		setup.set(true);
	}
	
	/**
	 * Construct a parallel object
	 * @param targetClass	Class to be created
	 * @param argvs			Arguments of the constructor
	 * @return	true if the object is instantiate
	 * @throws POPException				Thrown if any problem occurred during the parallel object creation
	 * @throws NoSuchMethodException	Thrown if the constructor is not found
	 */
	public void popConstructor(final Class<?> targetClass, final Object... argvs)
			throws POPException, NoSuchMethodException {
		replacePOPObjectArguments(argvs);
		
		final Constructor<?> constructor = ClassUtil.getConstructor(targetClass, ClassUtil.getObjectTypes(argvs));
		
		final Class<?>[] parameterTypes = constructor.getParameterTypes();
		
		final Runnable constructorRunnable = new Runnable() {
			
			@Override
			public void run() {
				
				// Repair the parameter type, for example MyConstructor(int value,
				// String ... data)
				try{
					allocate(popObjectInfo.getClassName());

					MethodInfo methodInfo = popObjectInfo.getMethodInfo(constructor);
					MessageHeader messageHeader = new MessageHeader(
							methodInfo.getClassId(), methodInfo.getMethodId(),
							constructorSemanticId);
					messageHeader.setRequestID(requestID.incrementAndGet());
					
					BufferFactory factory = combox.getBufferFactory();
					POPBuffer popBuffer = factory.createBuffer();
					popBuffer.setHeader(messageHeader);
					
					Annotation [][] annotations = constructor.getParameterAnnotations();
					for (int index = 0; index < argvs.length; index++) {
						if(Util.isParameterNotOfDirection(annotations[index], POPParameter.Direction.OUT) && 
						        Util.isParameterUseable(annotations[index])){
							popBuffer.putValue(argvs[index], parameterTypes[index]);
						}
					}
					popDispatch(popBuffer);
					
					POPBuffer responseBuffer = combox.getBufferFactory().createBuffer();
					popResponse(responseBuffer, messageHeader.getRequestID());
					
					for (int index = 0; index < parameterTypes.length; index++) {
						if(Util.isParameterNotOfDirection(annotations[index], POPParameter.Direction.IN) &&
						        Util.isParameterUseable(annotations[index])
								&&
								!(argvs[index] instanceof POPObject && !Util.isParameterOfAnyDirection(annotations[index]))){
							responseBuffer.deserializeReferenceObject(parameterTypes[index],
									argvs[index]);
						}
						
						if(argvs[index] instanceof POPObject){
							POPObject object = (POPObject)argvs[index];
							if(object.isTemporary()){
								object.exit();
							}
						}
					}
				}catch(POPException e){
					e.printStackTrace();
				}
				
				
				setup.set(true);
			}
		};
		
		if(Configuration.ASYNC_CONSTRUCTOR){
			new Thread(constructorRunnable	, "POPJava constructor").start();
		}else{
			constructorRunnable.run();
		}
	}

	/**
	 * Bind the interface-side with the broker-side
	 * @param accesspoint	Access point of the broker-side
	 * @return	true if the binding is succeed
	 * @throws POPException	throw an exception if the binding is not succeed
	 */
	public boolean bindObject(POPAccessPoint accesspoint) throws POPException {
		popAccessPoint.setAccessString(accesspoint.toString());

		setup.set(true);
		
		
		return bind(accesspoint);
	}

	/**
	 * Invoke a method on a remote object
	 * @param self		The object to call the method
	 * @param m			The method to be called
	 * @param proceed	The method to proceed the call
	 * @param argvs		Arguments of the methods
	 * @return	Any object if the method has a return value
	 * @throws	Throw any exception if the method throws any exception
	 */
	@Override
    public Object invoke(Object self, Method m, Method proceed, Object[] argvs)
			throws Throwable {
		
		//TODO: Busy waiting, bad, remove with lock?
		while(!setup.get()){
			Thread.sleep(50);
		}
		
		replacePOPObjectArguments(argvs);
		
		Object result = null;
		// If serialize or de-serialize
		boolean[] canExecute = new boolean[1];
		result = invokeCustomMethod(self, m, proceed, canExecute, argvs);
		if (canExecute[0]){
			return result;
		}

		Class<?> proceedClass = m.getDeclaringClass();
		if (!POPObject.class.isAssignableFrom(proceedClass)){
			return null;
		}
		Class<?> returnType = m.getReturnType();
		// Invoke the method
		result = new Object();
		MethodInfo info = popObjectInfo.getMethodInfo(m);

		m.setAccessible(true);
		int methodSemantics = popObjectInfo.getSemantic(info);
		MessageHeader messageHeader = new MessageHeader(info.getClassId(), info
				.getMethodId(), methodSemantics);
		messageHeader.setRequestID(requestID.incrementAndGet());
		
		POPBuffer popBuffer = combox.getBufferFactory().createBuffer();
		popBuffer.setHeader(messageHeader);
		Class<?>[] parameterTypes = m.getParameterTypes();
		
		Annotation[][] annotations = m.getParameterAnnotations();
		
		for (int index = 0; index < argvs.length; index++) {
			if(Util.isParameterNotOfDirection(annotations[index], POPParameter.Direction.OUT) &&
			        Util.isParameterUseable(annotations[index])){
				popBuffer.putValue(argvs[index], parameterTypes[index]);
			}
		}
		
		popDispatch(popBuffer);
		if ((methodSemantics & Semantic.SYNCHRONOUS) != 0) {
			POPBuffer responseBuffer = combox.getBufferFactory().createBuffer();
			
		    popResponse(responseBuffer, messageHeader.getRequestID());
			
			//Recover the data from the calling method. The called method can
			//Modify the content of an array and it gets copied back in here
			for (int index = 0; index < parameterTypes.length; index++) {
				if(Util.isParameterNotOfDirection(annotations[index], POPParameter.Direction.IN) &&
				        Util.isParameterUseable(annotations[index])
						&&
						!(argvs[index] instanceof POPObject && !Util.isParameterOfAnyDirection(annotations[index]))){
					responseBuffer.deserializeReferenceObject(parameterTypes[index], argvs[index]);
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
						result = ClassUtil.getDefaultPrimitiveValue(returnType);
					} else {
						result = null;
					}
				} catch (Exception e) {
					result = null;
				}
			}
		}
		
		
		for (int index = 0; index < argvs.length; index++) {
			if(argvs[index] instanceof POPObject){
				POPObject object = (POPObject)argvs[index];
				LogWriter.writeDebugInfo("Closing POPObject again");
				if(object.isTemporary()){
					object.exit();
				}
			}
		}
		
		
		return result;
	}
	
	private void replacePOPObjectArguments(Object[] args){
		for(int i = 0; i < args.length; i++){
			if(args[i] instanceof POPObject && !(args[i] instanceof ProxyObject)){
				POPObject object = (POPObject)args[i];
				args[i] = PopJava.newActive(object.getClass(), object.getAccessPoint());
				((POPObject)args[i]).makeTemporary();
			}
		}
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
	private Object invokeCustomMethod(Object self, Method m, Method proceed, boolean[] canExcute, Object[] argvs) {
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
			LogWriter.writeDebugInfo("Close method handler through exit");
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
	@Override
    public String toString() {
		return getClass().getName() + ":" + popAccessPoint.toString();
	}
}
