package ch.icosys.popjava.junit.annotations.od;

import ch.icosys.popjava.core.annotation.POPConfig;
import ch.icosys.popjava.core.annotation.POPConfig.Type;
import ch.icosys.popjava.core.base.POPObject;

public class MyAnnotatedPopObject extends POPObject {

	public MyAnnotatedPopObject(@POPConfig(Type.URL) String host) {
	}

}
