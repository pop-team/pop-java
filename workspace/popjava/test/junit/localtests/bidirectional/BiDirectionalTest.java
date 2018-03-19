package junit.localtests.bidirectional;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import popjava.PopJava;
import popjava.annotation.POPClass;
import popjava.system.POPSystem;
import popjava.util.Configuration;

@POPClass(isDistributable = false)
public class BiDirectionalTest {

	@Test
	public void test() {
		POPSystem.initialize();
		
		Configuration.getInstance().setDebug(true);
		
		BiDirectionalObject a =  PopJava.newActive(this, BiDirectionalObject.class, 1, false);
		
		assertEquals(2, a.test());
		
		POPSystem.end();
	}
	
	@Test
	public void debug() {
		POPSystem.initialize();
		
		Configuration.getInstance().setDebug(true);
		
		BiDirectionalObject a =  PopJava.newActive(this, BiDirectionalObject.class, 1, true);
		
		assertEquals(2, a.test());
		
		POPSystem.end();
	}
}
