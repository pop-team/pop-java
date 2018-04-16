package ch.icosys.popjava.junit.annotations.od;

import ch.icosys.popjava.core.annotation.POPConfig;
import ch.icosys.popjava.core.annotation.POPConfig.Type;

public class MyAnnotatedPOPObjectChild extends MyAnnotatedPopObject{

	public MyAnnotatedPOPObjectChild(@POPConfig(Type.URL) String host) {
		super(host);
	}

}
