package testsuite.jinteger;

import popjava.annotation.POPClass;

@POPClass(isDistributable = false)
public class TestRef {

	/**
	 * @param args in
	 */
	public static void main(String... args) {
	    Integer2 i = new Integer2();
        Jinteger j = new Jinteger();
        
        i.set(20);
        System.out.println("i="+i.get());
        j.set(12);
        System.out.println("j="+j.get());
        
        i.add(j);
        System.out.println("i+j="+i.get());
	}

}
