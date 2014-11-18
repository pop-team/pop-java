package testsuite.multiobj;

import popjava.*;
import popjava.annotation.POPClass;

@POPClass(isMain = true)
public class Mutliobj {
    
	public static void main(String... argvs){
	    MyObj1 o1 = (MyObj1)PopJava.newActive(MyObj1.class);
        o1.set(0);

        System.out.println("Result is : " + o1.get());
        
        System.out.println("Multiobjet test finished ...");
	}
}
