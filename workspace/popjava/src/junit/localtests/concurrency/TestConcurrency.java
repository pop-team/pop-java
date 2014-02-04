package junit.localtests.concurrency;

import static org.junit.Assert.*;

import org.junit.Test;

import popjava.PopJava;
import popjava.system.POPSystem;

public class TestConcurrency {

	@Test
	public void test(){
		POPSystem.initialize();
		
		ParallelObject object = PopJava.newActive(ParallelObject.class);
		
		object.sync();
		object.mutex();
		object.sync();
		object.conc();
		
		boolean correct = object.success();
		
		POPSystem.end();
		
		assertTrue(correct);
	}
}
