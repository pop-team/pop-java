package popjava.util;

import java.lang.reflect.Method;
import java.util.Arrays;
import popjava.annotation.POPAsyncConc;
import popjava.annotation.POPAsyncMutex;
import popjava.annotation.POPAsyncSeq;
import popjava.annotation.POPSyncConc;
import popjava.annotation.POPSyncMutex;
import popjava.annotation.POPSyncSeq;

/**
 * Utilities to be used with methods
 */
public class MethodUtil {
		
	public static boolean isMethodPOPAnnotated(Method method){
		if(method.isAnnotationPresent(POPSyncConc.class)){
			return true;
		}
		
		if(method.isAnnotationPresent(POPSyncSeq.class)){
			return true;
		}
		
		if(method.isAnnotationPresent(POPSyncMutex.class)){
			return true;
		}
		
		if(method.isAnnotationPresent(POPAsyncConc.class)){
			return true;
		}
		
		if(method.isAnnotationPresent(POPAsyncSeq.class)){
			return true;
		}
		
		if(method.isAnnotationPresent(POPAsyncMutex.class)){
			return true;
		}
		
		try{
			if(method.getDeclaringClass().getSuperclass() != null){
				Method parentMethod = method.getDeclaringClass().getSuperclass().getMethod(method.getName(), method.getParameterTypes());
				
				if(parentMethod != null){
					return isMethodPOPAnnotated(parentMethod);
				}
			}
			
			
		}catch (NoSuchMethodException e) {
			// TODO: handle exception
		}
		
		
		return false;
	}
	
	public static int methodId(Method method, int defaultID){
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
	    return defaultID;
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
	 * @param signatures <class>.<method> list of whitelisted method
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
