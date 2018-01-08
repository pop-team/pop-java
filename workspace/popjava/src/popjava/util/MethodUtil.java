package popjava.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import popjava.annotation.POPAsyncConc;
import popjava.annotation.POPAsyncMutex;
import popjava.annotation.POPAsyncSeq;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPSyncConc;
import popjava.annotation.POPSyncMutex;
import popjava.annotation.POPSyncSeq;
import popjava.annotation.POPSemantic;

/**
 * Utilities to be used with methods
 */
public class MethodUtil {
	
	/**
	 * Tell if a given method as a POPSync* or POPAsync* annotation.
	 * 
	 * @param method a method
	 * @return true if the annotation is found, false otherwise
	 */
	public static boolean isMethodPOPAnnotated(Method method){
		Annotation[] annotations = method.getDeclaredAnnotations();
		for (Annotation annotation : annotations) {
			POPSemantic semantic = annotation.annotationType().getAnnotation(POPSemantic.class);
			if (semantic != null) {
				return true;
			}
		}
		
		try{
			if(method.getDeclaringClass().getSuperclass() != null){
				Method parentMethod = method.getDeclaringClass().getSuperclass().getMethod(method.getName(), method.getParameterTypes());

				if(parentMethod != null){
					return isMethodPOPAnnotated(parentMethod);
				}
			}
		}catch (NoSuchMethodException e) {
			// we shouldn't go deeper at this point
		}
		return false;
	}
	
	/**
	 * Get the specified annotation if it exist, if it does it will check the hierarchy to see if it was not changed
	 * from previous declarations.
	 * 
	 * @param <A> The wanted annotation
	 * @param m  The method to check
	 * @param type The class of 
	 * @return the annotation or null if not found
	 * @throws RuntimeException when the POP annotation soddenly change from a previous implementation
	 */
	@SuppressWarnings("unchecked")
	public static<A extends Annotation> A getMethodPOPAnnotation(Method m, Class<A> type) {
		Annotation mainAnnotation = null;
		try{
			Class declaringClass = m.getDeclaringClass();
			Method workingMethod = m;
			while (declaringClass != Object.class) {
				Annotation currentAnnotation = workingMethod.getAnnotation(type);
				// until we have the annotation we want
				if (mainAnnotation == null) {
					mainAnnotation = currentAnnotation;
				}
				// check if there is a different one defined
				else if (currentAnnotation == null && workingMethod.getAnnotation(POPSemantic.class) != null) {
					throw new RuntimeException("Method " + m.toGenericString() + " has mismatching POP Annotations");
				} 
				declaringClass = declaringClass.getSuperclass();
				workingMethod = declaringClass.getMethod(m.getName(), m.getParameterTypes());
			}
		}catch (NoSuchMethodException e) {
			// we shouldn't go deeper at this point
		}
		return (A) mainAnnotation;
	}
	
	/**
	 * Generate an ID or use the one specified
	 * 
	 * @param method
	 * @return 
	 */
	public static int methodId(Method method){
	    int id = -1;
	    
		if(method.isAnnotationPresent(POPSyncConc.class)){
	        id = method.getAnnotation(POPSyncConc.class).id();
        }
        
		else if(method.isAnnotationPresent(POPSyncSeq.class)){
            id =  method.getAnnotation(POPSyncSeq.class).id();
        }
        
		else if(method.isAnnotationPresent(POPSyncMutex.class)){
            id =  method.getAnnotation(POPSyncMutex.class).id();
        }
        
		else if(method.isAnnotationPresent(POPAsyncConc.class)){
            id =  method.getAnnotation(POPAsyncConc.class).id();
        }
        
		else if(method.isAnnotationPresent(POPAsyncSeq.class)){
            id =  method.getAnnotation(POPAsyncSeq.class).id();
        }
        
		else if(method.isAnnotationPresent(POPAsyncMutex.class)){
            id =  method.getAnnotation(POPAsyncMutex.class).id();
        }
        
        if(id >= 0){
            return id;
        }
        
		String methodSign = ClassUtil.getMethodSign(method);
	    return Math.abs(methodSign.hashCode());
	}
	
	/**
	 * Generate an ID
	 * 
	 * @param constructor
	 * @return 
	 */
	public static int constructorId(Constructor<?> constructor){
		if(constructor.isAnnotationPresent(POPObjectDescription.class)){
			int id = constructor.getAnnotation(POPObjectDescription.class).id();
			if (id != -1) {
				return id;
			}
		}
		String constructorSign = ClassUtil.getMethodSign(constructor);
	    return Math.abs(constructorSign.hashCode());
	}
	
	/**
	 * Check if an array of annotations contains a specific type of annotation
	 * 
	 * @param annotations
	 * @param clazz
	 * @return 
	 */
	public static boolean hasAnnotation(Annotation[] annotations, Class<? extends Annotation> clazz) {
		return getAnnotation(annotations, clazz) != null;
	}
	
	/**
	 * Get the annotation we are looking for
	 * 
	 * @param <T>
	 * @param annotations
	 * @param clazz
	 * @return 
	 */
	@SuppressWarnings("unchecked")
	public static<T extends Annotation> T getAnnotation(Annotation[] annotations, Class<T> clazz) {
		if (annotations == null || clazz == null) {
			return null;
		}
		for (Annotation annotation : annotations) {
			if (annotation.annotationType().equals(clazz)) {
				return (T) annotation;
			}
		}
		return null;
	}
	
	/**
	 * Get the caller of the method
	 * 
	 * @return 
	 */
	public static String getCaller() {
		StackTraceElement caller = Thread.currentThread().getStackTrace()[3];
		return caller.getClassName() + "." + caller.getMethodName();
	}
	
	/**
	 * Whitelist for method access
	 * This only work with POJO, for POP Object use {@link }
	 * 
	 * @param signatures [class].[method] list of whitelisted method
	 */
	public static void grant(String... signatures) {
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		String caller = stack[3].getClassName() + "." + stack[3].getMethodName();
		String callee = stack[2].getClassName() + "." + stack[2].getMethodName();
		if (!Arrays.asList(signatures).contains(caller)) {
			throw new RuntimeException("Access denied to method " + callee);
		} 
	}
}
