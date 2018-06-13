package ch.icosys.popjava.junit.localtests.bidirectional;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;

import ch.icosys.popjava.core.PopJava;
import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.system.POPSystem;
import ch.icosys.popjava.core.util.Configuration;

@POPClass(isDistributable = false)
public class BiDirectionalTest {
	
	@Test
	public void test() {
		POPSystem.initialize();

		Configuration.getInstance().setDebug(true);
		
		BiDirectionalObject a = PopJava.newActive(this, BiDirectionalObject.class, 1, false);
		
		assertEquals(2, a.test());
		System.out.println("B");
		POPSystem.end();
		System.out.println("C");
	}

	@Ignore
	@Test
	public void debug() {
		POPSystem.initialize();

		Configuration.getInstance().setDebug(true);

		BiDirectionalObject a = PopJava.newActive(this, BiDirectionalObject.class, 1, true);

		assertEquals(2, a.test());

		POPSystem.end();
	}
}
