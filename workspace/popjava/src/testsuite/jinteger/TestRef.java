package testsuite.jinteger;

import popjava.PopJava;
import popjava.base.POPException;
import popjava.system.POPSystem;

public class TestRef {

	/**
	 * @param args
	 */
	public static void main(String... argvs) {
		try {
			POPSystem.initialize(argvs);
			Integer2 i = (Integer2)PopJava.newActive(Integer2.class);
			Jinteger j = (Jinteger)PopJava.newActive(Jinteger.class);
			
			i.set(20);
			System.out.println("i="+i.get());
			j.set(12);
			System.out.println("j="+j.get());
			
			i.add(j);
			System.out.println("i+j="+i.get());
		} catch (POPException e) {
			e.printStackTrace();
		}
	}

}
