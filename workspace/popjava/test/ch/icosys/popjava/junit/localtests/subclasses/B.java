package ch.icosys.popjava.junit.localtests.subclasses;

import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPObjectDescription;
import ch.icosys.popjava.core.annotation.POPSyncConc;
import ch.icosys.popjava.core.base.POPObject;

@POPClass
public class B extends POPObject implements A{

	@POPObjectDescription(url = "localhost")
	public B(){
		
	}
	
	@Override
	@POPSyncConc
	public String a() {
		return "asdf";
	}

	
	
}
