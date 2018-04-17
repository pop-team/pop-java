package ch.icosys.popjava.junit.localtests.connectTo;

import static org.junit.Assert.*;

import org.junit.Test;

import ch.icosys.popjava.core.PopJava;
import ch.icosys.popjava.core.system.POPSystem;

public class ConnectToTest {

	@Test
	public void testConnectTo() {
		POPSystem.initialize();

		ConnectToObject object = PopJava.newActive(this, ConnectToObject.class, "1234");
		assertEquals("1234", object.getMessage());

		ConnectToObject object2 = PopJava.newActive(this, ConnectToObject.class,
				PopJava.getAccessPoint(object).toString(), "3333");

		assertEquals("1234", object2.getMessage());

		POPSystem.end();
	}

}
