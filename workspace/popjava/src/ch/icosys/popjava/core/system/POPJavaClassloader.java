package ch.icosys.popjava.core.system;

import java.net.URL;
import java.net.URLClassLoader;

public class POPJavaClassloader extends URLClassLoader {

	public POPJavaClassloader(URL[] urls) {
		super(urls);
	}

	@Override
	protected Class<?> loadClass(String arg0, boolean arg1) throws ClassNotFoundException {
		// System.out.println("loadClass "+arg0+" "+arg1);
		return super.loadClass(arg0, arg1);
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		System.out.println("loadClass " + name);
		return super.loadClass(name);
	}

}
