package junit.localtests.deamontest;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import popjava.PopJava;
import popjava.service.POPJavaDeamon;
import popjava.system.POPSystem;

public class DeamonTest {

	@Test
	public void testFail(){
		POPSystem.initialize();
		
		try{
			TestClass test = PopJava.newActive(TestClass.class);
			test.test();
			fail("The object creation should fail");			
		}catch(Exception e){
		}
		
		POPSystem.end();
	}
	
	@Test
	public void testSuccess() throws IOException{
		POPSystem.initialize();
		
		final POPJavaDeamon deamon = new POPJavaDeamon();
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
		
		TestClass test = PopJava.newActive(TestClass.class);
		deamon.stop();
		int value = test.test();
		assertEquals(1234, value);
		
		
		
		POPSystem.end();
	}
}
