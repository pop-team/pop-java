package testsuite.method;

//Import added by the POP-Java compiler
import popjava.PopJava;
import popjava.base.POPException;
import popjava.system.POPSystem;

public class MainClass {
	public static void main(String... args) {
		try {
			POPSystem.initialize(args);
			MethodObj mo = (MethodObj) PopJava.newActive(MethodObj.class);
			mo.setSeq(1);
			mo.setSeq(3);
			mo.setSeq(2);
			mo.setSeq(4);
			int i = 0;
			while (i < 4) {

				System.out.println("Value is " + mo.get());
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
				i++;
			}
			System.out.println("value is "+ mo.get());
			POPSystem.end();
		} catch (POPException e) {
			POPSystem.end();
			System.err.println("POP-Java exception catched :" + e.errorMessage);
		}
	}
}