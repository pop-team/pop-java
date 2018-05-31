package ch.icosys.popjava.junit.system;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;

import ch.icosys.popjava.core.interfacebase.Interface;
import ch.icosys.popjava.core.serviceadapter.POPAppService;
import ch.icosys.popjava.core.system.POPJavaConfiguration;
import ch.icosys.popjava.core.system.POPSystem;
import ch.icosys.popjava.core.util.Util;
import ch.icosys.popjava.core.util.Util.OSType;

public class POPSystemTest {

	@Test
	public void testGetIp() {
		String ip = POPSystem.getHostIP();
		assertNotSame("", ip);
		assertNotSame("127.0.0.1", ip);
		System.out.println(ip);
	}

	@Test
	public void testClassId() {
		POPAppService service = new POPAppService();
		assertEquals("AppCoreService", service.getClassName());
	}

	@Test
	public void testClassPathDetection() {
		String classpath = POPJavaConfiguration.getPOPJavaCodePath();
		if (Util.getOSType() == OSType.Windows) {
			assertFalse("Class path can not start with a /", classpath.startsWith("/"));
		}
	}

	@Test
	@Ignore
	public void testWildcard() {
		POPSystem.registerCode("asdf", "*");

		String test = Interface.getCodeFile(Interface.getAppcoreService(), "my test");

		assertTrue(test.contains("asdf"));
	}

	@Test
	public void testJarDetection() {
		assertFalse(POPJavaConfiguration.isJar());
	}

	@Test
	public void testParameterCleaning() {
		/*
		 * 
		 * String tempJobservice = Util.removeStringFromList(argvList, "-jobservice=");
		 * 
		 * if (tempJobservice != null && !tempJobservice.isEmpty()) { jobservice =
		 * tempJobservice; }
		 * 
		 * codeconf = Util.removeStringFromList(argvList, "-codeconf="); appservicecode
		 * = Util.removeStringFromList(argvList, "-appservicecode="); proxy =
		 * Util.removeStringFromList(argvList, "-proxy="); appservicecontact =
		 * Util.removeStringFromList(argvList, "-appservicecontact=");
		 */
	}
}
