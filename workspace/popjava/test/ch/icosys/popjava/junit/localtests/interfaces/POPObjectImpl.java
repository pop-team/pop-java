package ch.icosys.popjava.junit.localtests.interfaces;

import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPConfig;
import ch.icosys.popjava.core.annotation.POPSyncConc;
import ch.icosys.popjava.core.annotation.POPConfig.Type;
import ch.icosys.popjava.core.base.POPObject;

@POPClass
public class POPObjectImpl extends POPObject implements GenericObject{


	public POPObjectImpl() {
		
	}
	
	public POPObjectImpl(@POPConfig(Type.URL) String url) {
		
	}
	
	@POPSyncConc
	@Override
	public int test() {
		return 1234;
	}
	
}
