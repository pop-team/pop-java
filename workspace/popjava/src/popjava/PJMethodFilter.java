package popjava;

import javassist.util.proxy.MethodFilter;
import java.lang.reflect.Method;

import popjava.base.POPObject;

/**
 * This class is a method filter for the PJMethodHandler
 */
public class PJMethodFilter implements MethodFilter {

	/**
	 * Creates a new instance of PJMethodFilter
	 */
	static private java.util.ArrayList<String> filterMethodList = new java.util.ArrayList<String>();

	/**
	 * Create a filter list of not handled method
	 */
	static {
		java.util.ArrayList<String> notFilterMethodList = new java.util.ArrayList<String>();
		notFilterMethodList.add("serialize");
		notFilterMethodList.add("deserialize");
		notFilterMethodList.add("getAccessPoint");
		notFilterMethodList.add("exit");		
		Class<?> c = POPObject.class;
		Method[] methods = c.getDeclaredMethods();
		for (Method m : methods) {
			if (!notFilterMethodList.contains(m.getName())
					&& !filterMethodList.contains(m.getName())) {				
				filterMethodList.add(m.getName());
			}
		}
	}

	/**
	 * Default constructor
	 */
	public PJMethodFilter() {
	}

	/**
	 * Check if a method is handled by the method handler
	 * @param m	The method to check
	 * @return true if the method is handled
	 */
	public boolean isHandled(Method m) {
		return !filterMethodList.contains(m.getName());
	}
}
