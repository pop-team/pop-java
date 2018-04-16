package junit.localtests.enums;

import static org.junit.Assert.*;

import org.junit.Test;

import popjava.PopJava;
import popjava.system.POPSystem;

/**
 * Test enum parameters and return values
 * @author Beat Wolf
 *
 */
public class EnumTests {

	@Test
	public void testFail(){
		POPSystem.initialize();
		
		EnumRemoteObject object = PopJava.newActive(this, EnumRemoteObject.class, junit.localtests.enums.EnumRemoteObject.Test.B);
		object.setEnum(junit.localtests.enums.EnumRemoteObject.Test.C);
		
		assertEquals(junit.localtests.enums.EnumRemoteObject.Test.B, object.getConstructor());
		assertEquals(junit.localtests.enums.EnumRemoteObject.Test.C, object.getMethod());
		
		POPSystem.end();
	}
	
}
