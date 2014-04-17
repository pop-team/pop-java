package junit.benchmarks.methods;

import org.junit.Test;

import popjava.PopJava;
import popjava.system.POPSystem;

public class TestMethods {

	private static final int REPETITIONS = 1000;
	
	@Test
	public void testPOPNoParamNoReturn(){
		POPSystem.initialize();
		POPMethods object = PopJava.newActive(POPMethods.class);
		
		long start = System.currentTimeMillis();
		
		for(int i = 0; i < REPETITIONS; i++){
			object.noParamNoReturn();
		}
		
		System.out.println("testPOPNoParamNoReturn() "+(System.currentTimeMillis() - start)+" ms");
		
		POPSystem.end();
	}
	
}
