package ch.icosys.popjava.junit.localtests.creation;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.icosys.popjava.core.PopJava;
import ch.icosys.popjava.core.system.POPSystem;

/**
 *
 * @author dosky
 */
public class NestedPOPCreation {

	@Before
	public void before() {
		POPSystem.initialize();
	}

	@After
	public void after() {
		POPSystem.end();
	}

	@Test(expected = Exception.class)
	public void creation() {
		ObjA a = PopJava.newActive(this, ObjA.class, 10);
		ObjB b = a.getB();

		Assert.assertEquals(a.getbVal(), b.getVal());
	}
}
