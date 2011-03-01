package testsuite.callback;

import popjava.PopJava;
import popjava.base.POPException;
import popjava.system.POPSystem;

public class CallBackMain {
	
	
	public static void main(String... argvs){
		System.out.println("Callback test started ...");
		try{
			POPSystem.initialize(argvs);
			Toto t = (Toto)PopJava.newActive(Toto.class);
			t.setIdent(1234);
			
			int value = t.getIdent();
			System.out.println("Identity callback is "+ value);
			
			if(value==-1)
				System.out.println("Callback test successful");
			else
				System.out.println("Callback test failed");
		} catch (POPException e){
			
			System.out.println("Callback test failed : " + e.errorMessage);
		}
		
	}
}
