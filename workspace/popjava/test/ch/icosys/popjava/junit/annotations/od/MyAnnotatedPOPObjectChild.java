package junit.annotations.od;

import popjava.annotation.POPConfig;
import popjava.annotation.POPConfig.Type;

public class MyAnnotatedPOPObjectChild extends MyAnnotatedPopObject{

	public MyAnnotatedPOPObjectChild(@POPConfig(Type.URL) String host) {
		super(host);
	}

}
