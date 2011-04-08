//Import added by the POP-Java compiler
import popjava.PopJava;
import popjava.base.POPException;
import popjava.baseobject.ObjectDescription;
import popjava.system.POPSystem;

public class TestInteger {
	public static void main(String... args){
		try{
			POPSystem.initialize(args);
			 Integer i1 =  (Integer)PopJava.newActive(Integer.class);
			Integer i2 =  (Integer)PopJava.newActive(Integer.class);
			i1.set(10);
			i2.set(15);
			System.out.println("i1=" + i1.get());
			System.out.println("i2=" + i2.get());
			i1.add(i2);
			System.out.println("i1+i2=" + i1.get());
		} catch(POPException e) {
			System.err.println("POP-Java exception catched :"+e.errorMessage);
		}
	}
}
