package junit.localtests.interfaces;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import popjava.PopJava;
import popjava.system.POPSystem;

public class InterfacesTest {

	@Before
	public void before(){
		POPSystem.initialize();
	}
	
	@After
	public void after(){
		POPSystem.end();
	}
	
	@Test
	public void testInheritance(){

		RemoteInterface local = PopJava.newActive(RemoteObject.class);
		
		GenericObject test = local.getObject();
		
		assertEquals(test.test(), 1234);
	}
	
	@Test
	public void testInheritance2(){
		RemoteInterface local = PopJava.newActive(RemoteObject.class, "localhost");
		
		GenericObject test = local.getObject2();
		
		assertEquals(test.test(), 1234);
	}
	
}
