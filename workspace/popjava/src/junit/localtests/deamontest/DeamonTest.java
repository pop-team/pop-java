package junit.localtests.deamontest;

import static org.junit.Assert.*;

import org.junit.Test;

import popjava.PopJava;
import popjava.system.POPSystem;

public class DeamonTest {

	@Test
	public void test(){
		POPSystem.initialize();
		
		TestClass test = PopJava.newActive(TestClass.class);
		
		int result = test.test();
		assertEquals(1234, result);
		
		POPSystem.end();
	}
}
