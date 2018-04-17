package ch.icosys.popjava.junit.localtests.referencePassing;

import static org.junit.Assert.*;

import org.junit.Test;

import ch.icosys.popjava.core.PopJava;
import ch.icosys.popjava.core.system.POPSystem;
import ch.icosys.popjava.core.util.Configuration;

public class ReferenceTest {

	/**
	 * This test randomly fails. This is most likely because the object B created in
	 * getB() is destroyed too soon
	 * 
	 */
	@Test
	public void test() {
		Configuration.getInstance().setDebug(true);

		POPSystem.initialize();

		A a = PopJava.newActive(this, A.class);

		B b = a.getB();

		assertEquals("asdf", b.value());

		POPSystem.end();
	}

}
