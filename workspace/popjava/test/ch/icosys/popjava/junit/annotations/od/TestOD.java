package ch.icosys.popjava.junit.annotations.od;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestOD {

	@Test
	public void testDynamicOd() throws NoSuchMethodException, SecurityException {
		String url = "1234";
		MyAnnotatedPopObject obj = new MyAnnotatedPopObject(url);
		obj.loadPOPAnnotations(MyAnnotatedPopObject.class.getDeclaredConstructor(String.class), url);

		assertEquals(url, obj.getOd().getHostName());
	}

	@Test
	public void testStaticOd() throws NoSuchMethodException, SecurityException {
		MyAnnotatedPopObject2 obj = new MyAnnotatedPopObject2();
		obj.loadPOPAnnotations(obj.getClass().getConstructor());
		assertEquals("1111", obj.getOd().getHostName());

		MyAnnotatedPopObject2 obj2 = new MyAnnotatedPopObject2(1);
		obj2.loadPOPAnnotations(obj2.getClass().getDeclaredConstructors()[1], 1);
		assertEquals("2222", obj2.getOd().getHostName());
	}

	@Test
	public void testInherited() throws NoSuchMethodException, SecurityException {
		String host = "3333";
		MyAnnotatedPOPObjectChild child = new MyAnnotatedPOPObjectChild(host);
		child.loadPOPAnnotations(MyAnnotatedPOPObjectChild.class.getDeclaredConstructor(String.class), host);

		assertEquals(host, child.getOd().getHostName());
	}
}
