package junit.localtests.deamontest;

import popjava.annotation.POPClass;
import popjava.annotation.POPConfig;
import popjava.annotation.POPConfig.Type;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPSyncConc;
import popjava.base.POPObject;
import popjava.baseobject.ConnectionType;

@POPClass
public class TestClass extends POPObject {

	@POPObjectDescription(connection=ConnectionType.DEAMON, url = POPObjectDescription.LOCAL_DEBUG_URL)
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
