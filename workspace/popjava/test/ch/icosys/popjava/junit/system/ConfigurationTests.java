package ch.icosys.popjava.junit.system;

import static org.junit.Assert.assertFalse;

import org.junit.Test;

import ch.icosys.popjava.core.system.POPJavaConfiguration;

public class ConfigurationTests {

	@Test
	public void testPOPJavaLocation() {
		String location = POPJavaConfiguration.getPopJavaLocation();
		
		System.out.println(location);
		
		assertFalse(location.isEmpty());
	}
	
	@Test
	public void testClasspath() {
		String cp = POPJavaConfiguration.getClassPath();
		
		System.out.println(cp);
	}
	
}
