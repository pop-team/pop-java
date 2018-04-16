package junit.localtests.accounting;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import popjava.system.POPSystem;
import popjava.PopJava;
import popjava.baseobject.POPTrackingMethod;

import static org.junit.Assert.*;

/**
 *
 * @author dosky
 */
public class AccountingTest {
	
	@Before
	public void b() {
		POPSystem.initialize();
	}
	
	@After
	public void a() {
		POPSystem.end();
	}
	
	public static final long ITERATIONS = 50;
	
	@Test
	public void syncSeqTest() {
		A a = PopJava.newActive(this, A.class);
		for (int i = 0; i < ITERATIONS; i++) {
			a.seq();
		}
		asserts(a, ".seq()");
	}
	
	@Test
	public void syncConcTest() {
		A a = PopJava.newActive(this, A.class);
		for (int i = 0; i < ITERATIONS; i++) {
			a.conc();
		}
		asserts(a, ".conc()");
	}
	
	@Test
	public void asyncSeqTest() {
		A a = PopJava.newActive(this, A.class);
		for (int i = 0; i < ITERATIONS; i++) {
			a.aseq();
		}
		a.sync();
		asserts(a, ".aseq()");
	}
	
	@Test
	public void asyncConcTest() {
		A a = PopJava.newActive(this, A.class);
		for (int i = 0; i < ITERATIONS; i++) {
			a.aconc();
		}
		a.sync();
		asserts(a, ".aconc()");
	}
	
	@Test
	public void mixedTest() {
		A a = PopJava.newActive(this, A.class);
		for (int i = 0; i < ITERATIONS; i++) {
			a.aconc();
		}
		for (int i = 0; i < ITERATIONS; i++) {
			a.aseq();
		}
		for (int i = 0; i < ITERATIONS; i++) {
			a.conc();
		}
		a.sync();
		asserts(a, ".aseq()");
		asserts(a, ".aconc()");
		asserts(a, ".conc()");
	}
	
	private void asserts(A a, String methodName) {
		assertTrue("Tracking should be enabled", a.isTracking());
		assertEquals("There should be one user", 1, a.getTrackedUsers().length);
		POPTrackingMethod method = a.getTracked().getCalls().stream()
			.filter(m -> m.getMethod().contains(methodName))
			.findFirst().get();
		assertEquals("Iterations don't match", ITERATIONS, method.getNumCalls());
		assertTrue("Time used should be positive", method.getTimeUsed() >= 0);
	}

}
