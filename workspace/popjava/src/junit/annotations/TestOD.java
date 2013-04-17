package junit.annotations;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestOD {

	@Test
	public void testDynamicOd() throws NoSuchMethodException, SecurityException{
		String url = "1234";
		MyAnnotatedPopObject obj = new MyAnnotatedPopObject(url);
		obj.loadDynamicOD(MyAnnotatedPopObject.class.getDeclaredConstructor(String.class), url);
		
		assertEquals(url, obj.getOd().getHostName());
	}
	
	@Test
	public void testStaticOd() throws NoSuchMethodException, SecurityException{
		MyAnnotatedPopObject2 obj = new MyAnnotatedPopObject2();
		obj.loadDynamicOD(obj.getClass().getConstructor());
		assertEquals("1111", obj.getOd().getHostName());
		
		MyAnnotatedPopObject2 obj2 = new MyAnnotatedPopObject2(1);
		obj2.loadDynamicOD(obj2.getClass().getDeclaredConstructors()[1], 1);
		assertEquals("2222", obj2.getOd().getHostName());
	}
	
	@Test
	public void testInherited() throws NoSuchMethodException, SecurityException{
		String host = "3333";
		MyAnnotatedPOPObjectChild child = new MyAnnotatedPOPObjectChild(host);
		child.loadDynamicOD(MyAnnotatedPOPObjectChild.class.getDeclaredConstructor(String.class), host);
		
		assertEquals(host, child.getOd().getHostName());
	}
}
