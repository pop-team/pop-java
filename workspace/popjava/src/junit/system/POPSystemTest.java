package junit.system;

import static org.junit.Assert.*;

import org.junit.Test;

import popjava.system.POPSystem;

public class POPSystemTest {

	@Test
	public void testGetIp(){
		String ip = POPSystem.getHostIP();
		System.out.println(ip);
		assertNotSame("", ip);
	}

}
