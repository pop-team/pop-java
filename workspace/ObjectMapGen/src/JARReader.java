import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import popjava.base.POPObject;

public class JARReader {
	private String file;

	public JARReader(String file) {
		this.file = file;
	}

	public ArrayList<String> getParclassFromJar() throws IOException,
			ClassNotFoundException {
		ArrayList<String> parclasses = new ArrayList<String>();
		JarFile jf = new JarFile(file);
		Enumeration<JarEntry> e = jf.entries();
		while (e.hasMoreElements()) {
			JarEntry je = e.nextElement();
			String className = je.getName();
			if (className.endsWith(".class")) {
				className = className.substring(0, className.indexOf("."));
				className = className.replace("/", ".");
				ClassLoader loader = URLClassLoader.newInstance(
						new URL[] { new URL("file://" + file) }, getClass()
								.getClassLoader());
				Class<?> c = Class.forName(className, true, loader);
				Class<?> sc = c.getSuperclass();
				while(sc != null && sc != POPObject.class)
					sc = sc.getSuperclass();
				if (sc == POPObject.class) {
					parclasses.add(className);
				}
			}
		}
		return parclasses;
	}

}
