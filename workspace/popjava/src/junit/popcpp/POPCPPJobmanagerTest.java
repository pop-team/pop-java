package junit.popcpp;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;
import popjava.PopJava;
import popjava.system.POPSystem;
import popjava.util.Configuration;
import testsuite.integer.Integer;

/**
 * Test connection to POPC++. Make sure the command
 * SXXpopc start
 * has been executed
 * @author Beat Wolf
 *
 */
public class POPCPPJobmanagerTest {

	private static boolean popcppConnect;
	
	@BeforeClass
	public static void setup(){
		popcppConnect = Configuration.CONNECT_TO_POPCPP;
		Configuration.CONNECT_TO_POPCPP = true;
	}
	
	@Test
	public void testLocalHostFind(){
		POPSystem.initialize();
		
		Integer i1 = (Integer) PopJava.newActive(Integer.class, true);
		
		assertEquals(20, i1.get());
		
		POPSystem.end();
		
	}
	
	
	@AfterClass
	public static void finish(){
		Configuration.CONNECT_TO_POPCPP = popcppConnect;
	}
}
