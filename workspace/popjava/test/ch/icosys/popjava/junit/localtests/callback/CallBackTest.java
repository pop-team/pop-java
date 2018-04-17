package ch.icosys.popjava.junit.localtests.callback;

import static org.junit.Assert.*;

import org.junit.Test;

import ch.icosys.popjava.core.PopJava;
import ch.icosys.popjava.core.system.POPSystem;
import ch.icosys.popjava.core.util.Configuration;
import ch.icosys.popjava.testsuite.callback.Toto;

public class CallBackTest {

	@Test
	public void testCallback() {
		Configuration.getInstance().setDebug(true);
		System.out.println("Callback test started ...");
		POPSystem.initialize();

		Toto t = PopJava.newActive(this, Toto.class);
		t.setIdent(1234);

		int value = t.getIdent();
		System.out.println("Identity callback is " + value);
		POPSystem.end();

		assertEquals(-1, value);

	}
}
