package testsuite.chartest;

import popjava.PopJava;
import popjava.base.POPException;
import popjava.system.POPSystem;

public class CharTestMain {

	/**
	 * @param args
	 */
	public static void main(String... argvs) {
		System.out.println("Char Array test started ...");
		try{
			POPSystem.initialize(argvs);
			PARObject pa = (PARObject)PopJava.newActive(PARObject.class); 
			char[] tab = {'t', 'e', 's', 't', 'o'};
			pa.sendChar(tab.length, tab);
			System.out.println(tab);
			POPSystem.end();
		} catch(POPException e){
			POPSystem.end();
			System.out.println("ERROR:" + e.getMessage());
		}

	}

}
