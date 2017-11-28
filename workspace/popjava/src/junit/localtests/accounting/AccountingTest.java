package junit.localtests.accounting;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import popjava.annotation.POPAsyncConc;
import popjava.annotation.POPAsyncSeq;
import popjava.annotation.POPClass;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPSyncConc;
import popjava.annotation.POPSyncSeq;
import popjava.system.POPSystem;

import static org.junit.Assert.*;
import popjava.PopJava;
import popjava.annotation.POPSyncMutex;
import popjava.base.POPObject;
import popjava.baseobject.POPTrackingMethod;

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
		A a = PopJava.newActive(A.class);
		for (int i = 0; i < ITERATIONS; i++) {
			a.seq();
		}
		asserts(a, ".seq()");
	}
	
	@Test
	public void syncConcTest() {
		A a = PopJava.newActive(A.class);
		for (int i = 0; i < ITERATIONS; i++) {
			a.conc();
		}
		asserts(a, ".conc()");
	}
	
	@Test
	public void asyncSeqTest() {
		A a = PopJava.newActive(A.class);
		for (int i = 0; i < ITERATIONS; i++) {
			a.aseq();
		}
		a.sync();
		asserts(a, ".aseq()");
	}
	
	@Test
	public void asyncConcTest() {
		A a = PopJava.newActive(A.class);
		for (int i = 0; i < ITERATIONS; i++) {
			a.aconc();
		}
		a.sync();
		asserts(a, ".aconc()");
	}
	
	@Test
	public void mixedTest() {
		A a = PopJava.newActive(A.class);
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
		asserts(a, ".aconc()");
		asserts(a, ".aseq()");
		asserts(a, ".conc()");
	}
	
	private void asserts(A a, String methodName) {
		assertTrue("Tracking should be enabled", a.isTracking());
		assertEquals("There should be one user", 1, a.getTrackedUsers().length);
		POPTrackingMethod method = a.getTracked().getCalls().stream()
			.filter(m -> m.getMethod().contains(methodName))
			.findFirst().get();
		assertEquals("Iterations don't match", ITERATIONS, method.getNumCalls());
		assertTrue("Time used should be positive", method.getTimeUsed() > 0);
	}
	
	@POPClass
	public static class A extends POPObject {

		@POPObjectDescription(url = "localhost", tracking = true)
		public A() {
		}
		
		@POPSyncSeq
		public void seq() {
			job();
		}
		
		@POPSyncConc
		public void conc() {
			job();
		}
		
		@POPAsyncSeq
		public void aseq() {
			job();
		}
		
		@POPAsyncConc
		public void aconc() {
			job();
		}
		
		@POPSyncMutex
		public void sync() {
			
		}
		
		private double job() {
			double sum = 0d;
			for (int n = 0; n < 1_000_000; n++) {
				sum = Math.sqrt(n);
			}
			return sum;
		}
	}
}
