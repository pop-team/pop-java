package junit.localtests.subclassReturns;

import static org.junit.Assert.*;

import org.junit.Test;

import popjava.PopJava;
import popjava.system.POPSystem;

public class SubclassingTest {

	@Test
	public void test(){
		POPSystem.initialize();
		
		D d = PopJava.newActive(C.class);
		
		A a = d.getTest();
		
		assertNotNull(a);

		assertEquals("asdf", a.a());
		
		POPSystem.end();
	}
	
}
