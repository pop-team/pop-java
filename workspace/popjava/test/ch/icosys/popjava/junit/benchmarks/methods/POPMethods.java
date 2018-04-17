package ch.icosys.popjava.junit.benchmarks.methods;

import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPObjectDescription;
import ch.icosys.popjava.core.annotation.POPSyncMutex;
import ch.icosys.popjava.core.base.POPObject;

@POPClass
public class POPMethods extends POPObject {

	@POPObjectDescription(url = "localhost")
	public POPMethods() {

	}

	@POPSyncMutex
	public void noParamNoReturn() {

	}

	@POPSyncMutex
	public int noParamSimple() {
		return 100;
	}

	private static String[] complexReturn = new String[] { "asdfasdf", "asdfasdf", "asdfasdf" };

	@POPSyncMutex
	public String[] noParamComplex() {
		return complexReturn;
	}

	@POPSyncMutex
	public void simpleParam(int param) {

	}

	@POPSyncMutex
	public void complexParam(String[] param) {

	}
}
