import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import popjava.base.POPObject;

/**
 * This class is used to read a .class file and to determine if it's a parallel
 * class.
 * 
 * @author clementval
 */
public class ClassReader {
	private String cleanPath;
	private String className;
	private String packageName = "";

	/**
	 * Create a new ClassReader with the full path of the .class file
	 * 
	 * @param path
	 *            Full .class file location
	 */
	public ClassReader(String path) {
		cleanPath = PathWorker.getCleanPath(path);
		className = PathWorker.getFileWithoutExt(path);
	}

	/**
	 * Check if the class is a parallel class.
	 * 
	 * @return True if the class extend POPObject
	 * @throws ClassNotFoundException
	 *             Thrown if the class file is not found
	 * @throws MalformedURLException
	 *             Thrown if the URL to the class is wrong
	 */
	public boolean isParclass() throws ClassNotFoundException,
			MalformedURLException {
		ClassLoader loader = URLClassLoader.newInstance(new URL[] { new URL(
				"file://" + cleanPath) }, getClass().getClassLoader());
		Class<?> c = Class.forName(className, true, loader);
		Class<?> sc = c.getSuperclass();
		if (sc == POPObject.class) {
			Package p = c.getPackage();
			if (p != null)
				packageName = p.getName();
			return true;
		}
		return false;
	}

	/**
	 * Get the full name of the class with its package
	 * 
	 * @return full class name
	 */
	public String getClassFullName() {
		if (packageName.equals(""))
			return className;
		else
			return packageName + "." + className;
	}

	/**
	 * Get the path of the directory where the class is located
	 * 
	 * @return The path of the directory
	 */
	public String getCleanPath() {
		return cleanPath;
	}

}
