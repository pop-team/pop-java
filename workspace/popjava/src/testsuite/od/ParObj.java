package testsuite.od;

import popjava.base.POPObject;

public class ParObj extends POPObject  {
	public ParObj()  {
		Class<?> c = ParObj.class;
		initializePOPObject();
		od.setHostname("localhost");	
		
	}
}