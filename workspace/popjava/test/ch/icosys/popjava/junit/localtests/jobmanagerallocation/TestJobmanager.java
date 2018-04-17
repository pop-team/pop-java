package ch.icosys.popjava.junit.localtests.jobmanagerallocation;

import static org.junit.Assert.*;

import org.junit.Test;

import ch.icosys.popjava.core.PopJava;
import ch.icosys.popjava.core.system.POPSystem;

public class TestJobmanager {

	@Test
	public void testNoUrl() {
		POPSystem.initialize();

		TestObject test = PopJava.newActive(this, TestObject.class);

		assertEquals(1234, test.getValue());

		POPSystem.end();
	}

}
