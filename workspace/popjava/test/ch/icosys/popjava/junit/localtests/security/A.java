package ch.icosys.popjava.junit.localtests.security;

import ch.icosys.popjava.core.PopJava;
import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPObjectDescription;
import ch.icosys.popjava.core.annotation.POPSyncSeq;
import ch.icosys.popjava.core.base.POPObject;

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
