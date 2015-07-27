import popjava.annotation.POPClass;
import popjava.base.POPObject;

@POPClass(isDistributable = false)
public class TestInteger {
    
	public static void main(String... args){
		Integer i1 = new Integer();
		
		if(i1 == null || !(((Object)i1) instanceof POPObject)){
		    System.out.println("Error, the Integer object is not actually a POPJava object");
		    return;
		}
		
		Integer i2 = new Integer();
		i1.set(23);
		i2.set(25);
		System.out.println("i1 = "+i1.get());
		System.out.println("i2 = "+i2.get());
		i1.add(i2);
		int sum =  i1.get();
		System.out.println("i1+i2 = "+sum);
		if(sum==48){
			System.out.println("Test Integer Successful");
		} else{
			System.out.println("Test Integer failed");
		}
	}
}

