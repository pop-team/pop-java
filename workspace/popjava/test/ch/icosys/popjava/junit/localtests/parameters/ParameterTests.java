package ch.icosys.popjava.junit.localtests.parameters;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Ignore;
import org.junit.Test;

import ch.icosys.popjava.core.PopJava;
import ch.icosys.popjava.core.base.POPException;
import ch.icosys.popjava.core.system.POPSystem;
import ch.icosys.popjava.core.util.Configuration;

public class ParameterTests {

	@Test
	public void testBasics() {
		POPSystem.initialize();
		ParameterObject test = PopJava.newActive(this, ParameterObject.class);

		test.noParam();

		assertEquals(10, test.simple(10));

		POPSystem.end();
	}

	@Test
	public void testString() {
		System.out.println("A");
		POPSystem.initialize();
		ParameterObject test = PopJava.newActive(this, ParameterObject.class);

		System.out.println("B");
		test.setValue("asdf");

		System.out.println("C");
		assertEquals("asdf", test.getValue());

		System.out.println("D");
		POPSystem.end();

		System.out.println("E");
	}

	@Test
	public void testStringUTF16() {
		POPSystem.initialize();
		ParameterObject test = PopJava.newActive(this, ParameterObject.class);

		test.setValue("a\u20AC\uD834\uDD1Ed");

		assertEquals("a\u20AC\uD834\uDD1Ed", test.getValue());

		POPSystem.end();
	}

	@Test(expected = POPException.class)
	public void testSerializeError() {
		POPSystem.initialize();
		ParameterObject test = PopJava.newActive(this, ParameterObject.class);

		test.impossibleParam(new ArrayList<>());

		POPSystem.end();
	}

	@Test(expected = POPException.class)
	public void testSerializeErrorReturn() {
		POPSystem.initialize();	
		ParameterObject test = PopJava.newActive(this, ParameterObject.class);

		test.impossibleReturn();

		POPSystem.end();
	}

	@Test
	@Ignore // This is supposed to not work, can't instaniate an interface
	public void testInterfaceParameter() {
		POPSystem.initialize();
		ParameterObject test = PopJava.newActive(this, ParameterObject.class);

		test.testInterfaceErrorParameter(test);

		POPSystem.end();
	}
	
	@Test
	public void testParameterNonDestruction() {
		POPSystem.initialize();
		
		Configuration.getInstance().setDebug(true);
		
		ParameterObject a = PopJava.newActive(this, ParameterObject.class);
		ParameterObject b = PopJava.newActive(this, ParameterObject.class);
		
		System.out.println(PopJava.getAccessPoint(a));
		System.out.println(PopJava.getAccessPoint(b));
		
		for (int i = 0; i < 10; i++) {
			a.func(b);
		}
		
		try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		POPSystem.end();
	}
}
