package testsuite.integer;

import popjava.PopJava;
import popjava.base.POPException;
import popjava.system.POPSystem;

public class TestInteger {
	public static void main(String... args){
		try {
			POPSystem.initialize(args);
			Integer i1 = (Integer) PopJava.newActive(Integer.class);
			Integer i2 = (Integer) PopJava.newActive(Integer.class);
			i1.set(23);
			i2.set(25);
			System.out.println("i1 = "+i1.get());
			System.out.println("i2 = "+i2.get());
			i1.add(i2);
			int sum =  i1.get();
			System.out.println("i1+i2 = "+sum);
			if(sum==48)
				System.out.println("Test Integer Successful");
			else
				System.out.println("Test Integer failed");
			POPSystem.end();
		} catch (POPException e) {
			POPSystem.end();
			System.out.println("Error : Unable to end the application :"+e.errorMessage);
		}

	}
}
