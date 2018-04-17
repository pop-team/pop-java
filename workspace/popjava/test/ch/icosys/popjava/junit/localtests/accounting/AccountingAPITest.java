package ch.icosys.popjava.junit.localtests.accounting;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.icosys.popjava.core.POPAccounting;
import ch.icosys.popjava.core.PopJava;
import ch.icosys.popjava.core.system.POPSystem;
import ch.icosys.popjava.core.util.POPRemoteCaller;

import static org.junit.Assert.*;

/**
 * Testing accounting API
 * 
 * @author dosky
 */
public class AccountingAPITest {
	@Before
	public void b() {
		POPSystem.initialize();
	}

	@After
	public void a() {
		POPSystem.end();
	}

	@Test
	public void apiEnabledTest() {
		A a = PopJava.newActive(this, A.class);
		Object ao = a;
		assertTrue(POPAccounting.isEnabledFor(ao));
	}

	@Test
	public void apiUsers() {
		A a = PopJava.newActive(this, A.class);
		Object ao = a;
		assertEquals(1, POPAccounting.getUsers(ao).length);
	}

	@Test
	public void apiRetrieveSpecific() {
		A a = PopJava.newActive(this, A.class);
		Object ao = a;
		POPRemoteCaller user = POPAccounting.getUsers(ao)[0];
		assertTrue(POPAccounting.getInformation(ao, user) != null);
	}

	@Test
	public void apiRetrieveMyself() {
		A a = PopJava.newActive(this, A.class);
		Object ao = a;
		@SuppressWarnings("unused")
		POPRemoteCaller user = POPAccounting.getUsers(ao)[0];
		assertTrue(POPAccounting.getMyInformation(ao) != null);
	}
}
