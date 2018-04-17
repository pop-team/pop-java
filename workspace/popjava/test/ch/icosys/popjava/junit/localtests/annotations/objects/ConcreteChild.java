package ch.icosys.popjava.junit.localtests.annotations.objects;

import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPSyncConc;

@POPClass
public class ConcreteChild extends AbstractChild {

	public String testNonAbstract() {
		return "B";
	}

	@Override
	public String getStuff() {
		return "My String";
	}

	@POPSyncConc
	@Override
	public String getStuff2() {
		return "My String2";
	}

}
