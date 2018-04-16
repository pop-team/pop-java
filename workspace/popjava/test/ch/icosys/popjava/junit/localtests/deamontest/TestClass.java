package ch.icosys.popjava.junit.localtests.deamontest;

import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPConfig;
import ch.icosys.popjava.core.annotation.POPObjectDescription;
import ch.icosys.popjava.core.annotation.POPSyncConc;
import ch.icosys.popjava.core.annotation.POPConfig.Type;
import ch.icosys.popjava.core.base.POPObject;
import ch.icosys.popjava.core.baseobject.ConnectionType;

@POPClass
public class TestClass extends POPObject {

	//@POPObjectDescription(connection=ConnectionType.DAEMON, url = POPObjectDescription.LOCAL_DEBUG_URL)
	
	@POPObjectDescription(connection=ConnectionType.DAEMON, connectionSecret = "password")
	public TestClass(){
	}
	
	@POPObjectDescription(url = POPObjectDescription.LOCAL_DEBUG_URL)
	public TestClass(@POPConfig(Type.CONNECTION) ConnectionType type,
			@POPConfig(Type.CONNECTION_PWD) String secret){
		
	}
	
	@POPSyncConc
	public int test(){
		return 1234;
	}
	
}
