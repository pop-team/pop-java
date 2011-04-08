package testsuite.multiobj;

import popjava.*;
import popjava.base.POPException;
import popjava.system.POPSystem;

public class Mutliobj {
	public static void main(String... argvs){
		System.out.println("Multiobjet test started ...");
		try {
			POPSystem.initialize(argvs);
			MyObj1 o1 = (MyObj1)PopJava.newActive(MyObj1.class);
			o1.set(0);

			System.out.println("Result is : " + o1.get());
			
			System.out.println("Multiobjet test finished ...");
			POPSystem.end();
		} catch (POPException e) {
			POPSystem.end();
			System.out.println("Mutliobjet test failed : "+e.errorMessage);
		}		
	}
}
