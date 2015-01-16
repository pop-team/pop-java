package testsuite.popc_integer;

import popjava.annotation.POPClass;

@POPClass(isDistributable = false)
public class TestPopcInteger {
	
	public static void main(String... argvs){
		System.out.println("POPC Integer test started ...");
		
		Integer first, second;
        first = new Integer();
        first.set(10);
        System.out.println("o1 = "+first.get());
        second = new Integer();
        second.set(20);
        System.out.println("o2 = "+second.get());
         
        //Passing a POP-Java reference that points a POP-C++ parallel object to a POP-C++ parallel object
        first.add(second);
        int value = first.get();
        System.out.println("10 + 20 = "+ value);
        
        if(value==30){
            System.out.println("POPC Integer test successful");
        } else{
            System.out.println("POPC Integer test failed");
        }
	}
}
