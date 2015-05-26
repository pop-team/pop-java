import java.lang.*;

import popjava.PopJava;
import popjava.annotation.POPClass;

@POPClass(isDistributable = false)
public class MainClass {
	public static void main(String[] args) {
		System.out.println("Create object without JobMgr");
		ParObj po1 = new ParObj();
		System.out.println(PopJava.getAccessPoint(po1).toString());
		System.out.println("Creation of object 1 successful");

		ParObj po2 = new ParObj("localhost");
		System.out.println(PopJava.getAccessPoint(po2).toString());
		System.out.println("Creation of object 2 successful");
	}
}
