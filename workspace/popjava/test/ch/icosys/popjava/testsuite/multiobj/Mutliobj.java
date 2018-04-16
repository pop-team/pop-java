package ch.icosys.popjava.testsuite.multiobj;

import ch.icosys.popjava.core.*;
import ch.icosys.popjava.core.annotation.POPClass;

@POPClass(isDistributable = false)
public class Mutliobj {
    
	public static void main(String... argvs){
	    MyObj1 o1 = (MyObj1)PopJava.newActive(null, MyObj1.class);
        o1.set(0);

        System.out.println("Result is : " + o1.get());
        
        System.out.println("Multiobjet test finished ...");
	}
}
