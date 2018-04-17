package ch.icosys.popjava.junit.localtests.referencePassing;

import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPObjectDescription;
import ch.icosys.popjava.core.annotation.POPSyncConc;
import ch.icosys.popjava.core.base.POPObject;

@POPClass
public class B extends POPObject {

	@POPObjectDescription(url = "localhost")
	public B() {

	}

	@POPSyncConc
	public String value() {
		return "asdf";
	}

}
