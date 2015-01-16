package junit.localtests.deamontest;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import popjava.PopJava;
import popjava.baseobject.ConnectionType;
import popjava.service.POPJavaDeamon;
import popjava.system.POPSystem;

public class DeamonTest {
	
	/**
	 * Tests the creation of a local POP-Java object using the POP-Java deamon, without it running.
	 * This tests uses the dynamic pop object description method.
	 */
	@Test
	public void testDynamicCreationFail(){
		POPSystem.initialize();
		
		try{
			TestClass test = PopJava.newActive(TestClass.class, ConnectionType.DEAMON, "");
			test.test();
			fail("The object creation should fail");			
		}catch(Exception e){
		}
		
		POPSystem.end();
	}
	
	private static POPJavaDeamon startDeamon(String password){
		final POPJavaDeamon deamon = new POPJavaDeamon(password);
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					deamon.start();
				} catch (IOException e) {
				}
			}
		});
		thread.start();
		
		return deamon;
	}
	
	@Test
	public void testSuccess() throws IOException{
		POPSystem.initialize();
		
		POPJavaDeamon deamon = startDeamon("");
		
		TestClass test = PopJava.newActive(TestClass.class);
		deamon.close();
		int value = test.test();
		assertEquals(1234, value);
		
		POPSystem.end();
	}
	
	@Test
	public void testDynamicCreation() throws IOException{
		POPSystem.initialize();
		POPJavaDeamon deamon = startDeamon("");
		TestClass test = PopJava.newActive(TestClass.class, ConnectionType.DEAMON, "");
		deamon.close();
		int value = test.test();
		assertEquals(1234, value);
		
		POPSystem.end();
	}
	
	@Test
	public void testDynamicCreationPassword() throws IOException{
		POPSystem.initialize();
		String password = "12345";
		POPJavaDeamon deamon = startDeamon(password);
		TestClass test = PopJava.newActive(TestClass.class, ConnectionType.DEAMON, password);
		deamon.close();
		int value = test.test();
		assertEquals(1234, value);
		
		POPSystem.end();
	}
	
	@Test
	public void testDynamicCreationPasswordMissmatch() throws IOException{
		POPSystem.initialize();
		String password = "12345";
		POPJavaDeamon deamon = startDeamon(password);
		
		try{
			TestClass test = PopJava.newActive(TestClass.class, ConnectionType.DEAMON, "");
			test.test();
			fail("The object creation should fail");			
		}catch(Exception e){
		}
		
		deamon.close();
		
		POPSystem.end();
	}
}
