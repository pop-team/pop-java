import popjava.annotation.POPClass;



@POPClass(isDistributable = false)
public class MultiObj {
	public static void main(String... argvs){		
		System.out.println("Multiobjet test started ...");
		MyObj1 o1 = new MyObj1();
		o1.set(0);
		System.out.println("Result is : " + o1.get());

		System.out.println("Multiobjet test finished ...");
	}
}

