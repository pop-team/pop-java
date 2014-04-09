package junit.system;

import static org.junit.Assert.*;

import org.junit.Test;

import popjava.serviceadapter.POPAppService;
import popjava.system.POPSystem;

public class POPSystemTest {

	@Test
	public void testGetIp(){
		String ip = POPSystem.getHostIP();
		assertNotSame("", ip);
	}
	

	@Test
	public void testClassId(){
		POPAppService service = new POPAppService();
		assertEquals("AppCoreService", service.getClassName());
	}

}
