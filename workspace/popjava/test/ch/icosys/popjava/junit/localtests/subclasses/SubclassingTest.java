package ch.icosys.popjava.junit.localtests.subclasses;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

import ch.icosys.popjava.core.PopJava;
import ch.icosys.popjava.core.system.POPSystem;

public class SubclassingTest {

	@Test
	@Ignore
	public void test() {
		POPSystem.initialize();

		D d = PopJava.newActive(this, C.class);

		A a = d.getTest();

		assertNotNull(a);

		assertEquals("asdf", a.a());

		POPSystem.end();
	}

}
