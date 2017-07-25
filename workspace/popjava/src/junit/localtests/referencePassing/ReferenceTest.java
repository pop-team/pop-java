package junit.localtests.referencePassing;

import static org.junit.Assert.*;

import org.junit.Test;

import popjava.PopJava;
import popjava.system.POPSystem;

public class ReferenceTest {

/**
 * THis test randomly fails.
 * THis is most likely because the object B created in getB() is destroyed too soon
 * 
 */
	@Test
	public void test(){
		POPSystem.start();
		
		A a = PopJava.newActive(A.class);
		
		B b = a.getB();
		
		assertEquals("asdf", b.value());
		
		POPSystem.end();
	}
	
}
