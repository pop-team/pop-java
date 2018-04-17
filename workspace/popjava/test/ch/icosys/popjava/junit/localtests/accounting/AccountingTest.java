package ch.icosys.popjava.junit.localtests.accounting;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import ch.icosys.popjava.core.PopJava;
import ch.icosys.popjava.core.base.MessageHeader;
import ch.icosys.popjava.core.baseobject.POPTrackingMethod;
import ch.icosys.popjava.core.system.POPSystem;

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

	private static final int MESSAGE_HEADER_LENGTH = MessageHeader.HEADER_LENGTH; // See
	// POP
	// buffer
	// message
	// header

	private static final int BYTE_ARRAY_HEADER_LENGTH = 4; // 4 for its size

	private static final int BYTE_ARRAY_CONTENT_LENGTH = 10;

	private static final int BYTE_ARRAY_PADDING_LENGTH = 2; // JVM padds arrays
	// for their size to
	// be multiple of 8
	// bytes => 4 + 10 +
	// 2 = 16 = 2 * 8

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
	@Ignore
	public void asyncConcTest() {
		A a = PopJava.newActive(this, A.class);
		for (int i = 0; i < ITERATIONS; i++) {
			a.aconc();
		}
		a.sync();
		asserts(a, ".aconc()");
	}

	@Test
	@Ignore
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
		POPTrackingMethod method = a.getTracked().getCalls().stream().filter(m -> m.getMethod().contains(methodName))
				.findFirst().get();
		assertEquals("Iterations don't match", ITERATIONS, method.getTotalCalls());
		assertTrue("Time used should be positive", method.getTotalTime() >= 0);
	}

	@Test
	public void transferByteArrayINSizeTest() {
		final long totalBytes = ITERATIONS * (MESSAGE_HEADER_LENGTH + BYTE_ARRAY_HEADER_LENGTH
				+ BYTE_ARRAY_CONTENT_LENGTH + BYTE_ARRAY_PADDING_LENGTH);
		A a = PopJava.newActive(this, A.class);
		byte[] ba = new byte[BYTE_ARRAY_CONTENT_LENGTH];
		for (int i = 0; i < ITERATIONS; i++) {
			a.sendByteArrayIN(ba);
		}
		POPTrackingMethod method = a.getTracked().getCalls().stream()
				.filter(m -> m.getMethod().contains(".sendByteArrayIN")).findFirst().get();
		assertTrue("Total input parameters size of " + method.getTotalInputParamsSize() + " bytes should be equal to "
				+ totalBytes + " bytes", method.getTotalInputParamsSize() == totalBytes);
		assertTrue("Total output results size of " + method.getTotalOutputResultSize() + " bytes should be equal to "
				+ totalBytes + " bytes", method.getTotalOutputResultSize() == totalBytes);
	}

	@Test
	public void transferByteArrayINOUTSizeTest() {
		final long totalBytesIN = ITERATIONS * (MESSAGE_HEADER_LENGTH + BYTE_ARRAY_HEADER_LENGTH
				+ BYTE_ARRAY_CONTENT_LENGTH + BYTE_ARRAY_PADDING_LENGTH);
		final long totalBytesOUT = ITERATIONS * (MESSAGE_HEADER_LENGTH
				+ 2 * (BYTE_ARRAY_HEADER_LENGTH + BYTE_ARRAY_CONTENT_LENGTH + BYTE_ARRAY_PADDING_LENGTH));
		A a = PopJava.newActive(this, A.class);
		byte[] ba = new byte[BYTE_ARRAY_CONTENT_LENGTH];
		for (int i = 0; i < ITERATIONS; i++) {
			a.sendByteArrayINOUT(ba);
		}
		POPTrackingMethod method = a.getTracked().getCalls().stream()
				.filter(m -> m.getMethod().contains(".sendByteArrayINOUT")).findFirst().get();
		assertTrue("Total input parameters size of " + method.getTotalInputParamsSize() + " bytes should be equal to "
				+ totalBytesIN + " bytes", method.getTotalInputParamsSize() == totalBytesIN);
		assertTrue("Total output results size of " + method.getTotalOutputResultSize() + " bytes should be equal to "
				+ totalBytesOUT + " bytes", method.getTotalOutputResultSize() == totalBytesOUT);
	}

}
