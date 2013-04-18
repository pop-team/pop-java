package junit.annotations.od;

import popjava.annotation.POPConfig;
import popjava.annotation.POPConfig.Type;
import popjava.base.POPObject;

public class MyAnnotatedPopObject extends POPObject{

	
	public MyAnnotatedPopObject(@POPConfig(Type.URL) String host){
		initializePOPObject();
	}
	
}
