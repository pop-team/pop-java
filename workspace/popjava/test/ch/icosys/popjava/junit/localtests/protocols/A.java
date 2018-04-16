package ch.icosys.popjava.junit.localtests.protocols;

import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPConfig;
import ch.icosys.popjava.core.annotation.POPObjectDescription;
import ch.icosys.popjava.core.annotation.POPSyncSeq;
import ch.icosys.popjava.core.base.POPObject;

/**
 *
 * @author dosky
 */
@POPClass
public class A extends POPObject {
	
	@POPObjectDescription(url = "localhost")
	public A() {
	}
	
	public A(@POPConfig(POPConfig.Type.URL) String url, @POPConfig(POPConfig.Type.PROTOCOLS) String[] protocols) {
	}
	
	@POPSyncSeq
	public void sync() {
	}
}
