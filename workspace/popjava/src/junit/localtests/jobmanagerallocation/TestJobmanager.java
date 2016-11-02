package junit.localtests.jobmanagerallocation;

import static org.junit.Assert.*;

import org.junit.Test;

import popjava.PopJava;
import popjava.system.POPSystem;

public class TestJobmanager {

	@Test
	public void testNoUrl(){
		POPSystem.initialize();
		
		TestObject test = PopJava.newActive(TestObject.class);
		
		assertEquals(1234, test.getValue());
		
		POPSystem.end();
	}
	
}
