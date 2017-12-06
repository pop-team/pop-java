package popjava;

import javassist.util.proxy.MethodFilter;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import popjava.base.POPObject;
import popjava.util.MethodUtil;

/**
 * This class is a method filter for the PJMethodHandler
 */
public class PJMethodFilter implements MethodFilter {

	/**
	 * Creates a new instance of PJMethodFilter
	 */
	private static final Set<String> filterMethodList = new HashSet<>();

	/*
	 * Create a filter list of not handled method
	 */
	static {
		Set<String> notFilterMethodList = new HashSet<>();
		notFilterMethodList.add("serialize");
		notFilterMethodList.add("deserialize");
		notFilterMethodList.add("getAccessPoint");
		notFilterMethodList.add("getRemote");
		notFilterMethodList.add("exit");
		Class<?> c = POPObject.class;
		Method[] methods = c.getDeclaredMethods();
		for (Method m : methods) {
			if (!notFilterMethodList.contains(m.getName()) && !MethodUtil.isMethodPOPAnnotated(m)) {				
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
