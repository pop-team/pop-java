package ch.icosys.popjava.junit.popcpp;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.icosys.popjava.core.PopJava;
import ch.icosys.popjava.core.system.POPSystem;
import ch.icosys.popjava.core.util.Configuration;
import ch.icosys.popjava.testsuite.integer.Integer;

import static org.junit.Assert.*;

/**
 * Test connection to POPC++. Make sure the command SXXpopc start has been
 * executed
 * 
 * @author Beat Wolf
 *
 */
public class POPCPPJobmanagerTest {

	private static boolean popcppConnect;

	private static final Configuration conf = Configuration.getInstance();

	@BeforeClass
	public static void setup() {
		popcppConnect = conf.isConnectToPOPcpp();
		conf.setConnectToPOPcpp(true);
	}

	@Test
	public void testLocalHostFind() {
		POPSystem.initialize();

		Integer i1 = (Integer) PopJava.newActive(null, Integer.class, true);

		assertEquals(20, i1.get());

		POPSystem.end();

	}

	@AfterClass
	public static void finish() {
		conf.setConnectToPOPcpp(popcppConnect);
	}
}
