package junit.localtests.referencePassing;

import static org.junit.Assert.*;

import org.junit.Test;

import popjava.PopJava;
import popjava.system.POPSystem;

public class ReferenceTest {

	@Test
	public void test(){
		POPSystem.start();
		
		A a = PopJava.newActive(A.class);
		
		B b = a.getB();
		
		assertEquals("asdf", b.value());
		
		POPSystem.end();
	}
	
}
