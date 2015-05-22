package test;

import popjava.annotation.POPClass;

@POPClass(isDistributable = false)
public class CallBackMain {
    
	public static void main(String... argvs){
		System.out.println("Callback test started ...");
		Toto t = new Toto();
		t.setIdent(1234);
		
		int value = t.getIdent();
		System.out.println("Identity callback is "+ value);
	
		if(value==-1){
			System.out.println("Callback test successful");
		}else{
			System.out.println("Callback test failed");
		}
	}
	
}
