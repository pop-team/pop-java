package junit.localtests.connectTo;

import static org.junit.Assert.*;

import org.junit.Test;

import popjava.PopJava;
import popjava.system.POPSystem;

public class ConnectToTest {

	@Test
	public void testConnectTo(){
		POPSystem.initialize();
		
		ConnectToObject object = PopJava.newActive(ConnectToObject.class, "1234");
		assertEquals("1234", object.getMessage());
		
		ConnectToObject object2 = PopJava.newActive(ConnectToObject.class, object.getAccessPoint().toString(), "3333");
		
		assertEquals("1234", object2.getMessage());

		
		POPSystem.end();
		
	}
	
}
