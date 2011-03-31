package testsuite.od;
import popjava.PopJava;
import popjava.base.POPException;
import popjava.system.POPSystem;

public class MainClass {
	public static void main(String... args){
		try{
			POPSystem.initialize(args);
			System.out.println("Create object without JobMgr");
			ParObj po = (ParObj)PopJava.newActive(ParObj.class);
			System.out.println(po.getAccessPoint().toString());
			System.out.println("Creation successful");
			POPSystem.end();
		} catch(POPException e) {
			POPSystem.end();
			System.err.println("POP-Java exception catched :"+e.errorMessage);
		}
	}
}
