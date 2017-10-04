package junit.localtests.security;

import popjava.PopJava;
import popjava.annotation.POPClass;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPSyncSeq;
import popjava.base.POPObject;

/**
 *
 * @author dosky
 */
@POPClass
public class A extends POPObject {

	@POPObjectDescription(url = "localhost", protocols = "ssl")
	public A() {
	}
	
	@POPSyncSeq
	public void sync() {}
	
	@POPSyncSeq
	public boolean isCallFromCL() {
		return PopJava.getRemoteCaller().isUsingConfidenceLink();
	}
}
