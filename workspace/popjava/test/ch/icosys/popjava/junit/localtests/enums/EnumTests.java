package ch.icosys.popjava.junit.localtests.enums;

import static org.junit.Assert.*;

import org.junit.Test;

import ch.icosys.popjava.core.PopJava;
import ch.icosys.popjava.core.system.POPSystem;

/**
 * Test enum parameters and return values
 * 
 * @author Beat Wolf
 *
 */
public class EnumTests {

	@Test
	public void testFail() {
		POPSystem.initialize();

		EnumRemoteObject object = PopJava.newActive(this, EnumRemoteObject.class,
				ch.icosys.popjava.junit.localtests.enums.EnumRemoteObject.Test.B);
		object.setEnum(ch.icosys.popjava.junit.localtests.enums.EnumRemoteObject.Test.C);

		assertEquals(ch.icosys.popjava.junit.localtests.enums.EnumRemoteObject.Test.B, object.getConstructor());
		assertEquals(ch.icosys.popjava.junit.localtests.enums.EnumRemoteObject.Test.C, object.getMethod());

		POPSystem.end();
	}

}
