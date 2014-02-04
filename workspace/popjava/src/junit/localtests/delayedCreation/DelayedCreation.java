package junit.localtests.delayedCreation;

import static org.junit.Assert.*;

import org.junit.Test;

import popjava.PopJava;
import popjava.system.POPSystem;

public class DelayedCreation {

	
	@Test
	public void testDelayedCallback() throws InterruptedException{
		POPSystem.initialize();
		
		A a1 = PopJava.newActive(A.class);
		assertEquals(1234, a1.getTestValue());
		Thread.sleep(20000);
		A a2 = PopJava.newActive(A.class);
		assertEquals(1234, a2.getTestValue());
		
		POPSystem.end();
	}
}
