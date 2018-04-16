package junit.localtests.delayedCallback;

import static org.junit.Assert.*;

import org.junit.Test;

import popjava.PopJava;
import popjava.system.POPSystem;

public class DelayedCallbackTest {

	@Test
	public void testDelayedCallback(){
		POPSystem.initialize();
		A a = PopJava.newActive(this, A.class);
		a.test();
		
		assertEquals(1234, a.getValue());
		
		POPSystem.end();
	}
	
}
