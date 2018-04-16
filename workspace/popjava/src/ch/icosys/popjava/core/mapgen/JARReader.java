package popjava.mapgen;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JARReader {
	private String file;

	public JARReader(String file) {
		this.file = file;
	}

	/**
	 * Returns the names of all POP-Java classes found in a certain jar file
	 * @return list of par classes
	 * @throws IOException can't read jar
	 * @throws ClassNotFoundException can't find class
	 */
	public ArrayList<String> getParclassFromJar() throws IOException,
			ClassNotFoundException {
		ArrayList<String> parclasses = new ArrayList<>();
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

					
					try{
						Class<?> c = Class.forName(className, true, loader);
						
						if (ClassReader.isParclass(c)) {
							parclasses.add(className);
						}
					}catch(NoClassDefFoundError ex){
						//ex.printStackTrace();
					}
				}
		}
		jf.close();
		return parclasses;
	}

}
