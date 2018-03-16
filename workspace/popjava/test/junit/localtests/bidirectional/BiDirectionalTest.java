package junit.localtests.bidirectional;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import popjava.PopJava;
import popjava.annotation.POPClass;
import popjava.system.POPSystem;

@POPClass(isDistributable = false)
public class BiDirectionalTest {

	@Test
	public void test() {
		POPSystem.initialize();
		
		BiDirectionalObject a =  PopJava.newActive(BiDirectionalObject.class, 1234);
		
		assertEquals(1234, a.test());
		
		POPSystem.end();
	}
	
}
