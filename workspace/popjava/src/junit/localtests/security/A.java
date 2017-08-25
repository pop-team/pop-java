package junit.localtests.security;

import popjava.PopJava;
import popjava.annotation.POPClass;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPSyncConc;
import popjava.base.POPObject;
import popjava.baseobject.ConnectionProtocol;

/**
 *
 * @author dosky
 */
@POPClass
public class A extends POPObject {

	@POPObjectDescription(url = "localhost", protocol = ConnectionProtocol.SSL)
	public A() {
	}
	
	@POPSyncConc
	public boolean isCallFromCL() {
		return PopJava.getRemoteCaller().isUsingConfidenceLink();
	}
}
