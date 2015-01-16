package testsuite.od;
import popjava.PopJava;
import popjava.annotation.POPClass;

@POPClass(isDistributable = false)
public class MainClass {
	public static void main(String... args){
	    System.out.println("Create object without JobMgr");
        ParObj po = new ParObj();
        System.out.println(PopJava.getAccessPoint(po).toString());
        System.out.println("Creation successful");
	}
}
