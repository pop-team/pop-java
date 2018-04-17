package ch.icosys.popjava.junit.localtests.deamontest;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import ch.icosys.popjava.core.PopJava;
import ch.icosys.popjava.core.baseobject.ConnectionType;
import ch.icosys.popjava.core.service.deamon.POPJavaDeamon;
import ch.icosys.popjava.core.system.POPSystem;

public class DeamonTest {

	/**
	 * Tests the creation of a local POP-Java object using the POP-Java deamon,
	 * without it running. This tests uses the dynamic pop object description
	 * method.
	 */
	@Test
	public void testDynamicCreationFail() {
		POPSystem.initialize();

		try {
			TestClass test = PopJava.newActive(this, TestClass.class, ConnectionType.DAEMON, "");
			test.test();
			fail("The object creation should fail");
		} catch (Exception e) {
		}

		POPSystem.end();
	}

	private static POPJavaDeamon startDeamon(String password) throws InterruptedException {
		final POPJavaDeamon deamon = new POPJavaDeamon(password);
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					deamon.start();
				} catch (IOException e) {
				}
			}
		}, "POPJava deamon");
		thread.start();

		return deamon;
	}

	@Test
	public void testSuccess() throws IOException, InterruptedException {
		POPSystem.initialize();

		POPJavaDeamon deamon = startDeamon("");

		TestClass test = PopJava.newActive(this, TestClass.class);
		int value = test.test();
		deamon.close();
		assertEquals(1234, value);

		POPSystem.end();
	}

	@Test
	public void testDynamicCreation() throws IOException, InterruptedException {
		POPSystem.initialize();
		POPJavaDeamon deamon = startDeamon("");
		TestClass test = PopJava.newActive(this, TestClass.class, ConnectionType.DAEMON, "");
		int value = test.test();

		deamon.close();

		assertEquals(1234, value);

		POPSystem.end();
	}

	@Test
	public void testDynamicCreationPassword() throws IOException, InterruptedException {
		POPSystem.initialize();
		String password = "12345";
		POPJavaDeamon deamon = startDeamon(password);
		TestClass test = PopJava.newActive(this, TestClass.class, ConnectionType.DAEMON, password);
		int value = test.test();
		deamon.close();

		assertEquals(1234, value);

		POPSystem.end();
	}

	@Test
	public void testDynamicCreationPasswordMissmatch() throws IOException, InterruptedException {
		POPSystem.initialize();
		String password = "12345";
		POPJavaDeamon deamon = startDeamon(password);

		try {
			TestClass test = PopJava.newActive(this, TestClass.class, ConnectionType.DAEMON, "");
			test.test();
			fail("The object creation should fail");
		} catch (Exception e) {
		}

		deamon.close();

		POPSystem.end();
	}

	@Test
	public void testMultiObjectCreation() throws InterruptedException, IOException {
		POPSystem.initialize();
		String password = "12345";
		POPJavaDeamon deamon = startDeamon(password);

		try {

			for (int i = 0; i < 5; i++) {
				TestClass test = PopJava.newActive(this, TestClass.class, ConnectionType.DAEMON, password);
				test.test();
			}
		} catch (Exception e) {
		}

		deamon.close();

		POPSystem.end();
	}
}
