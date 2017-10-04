package junit.localtests.protocols;

import popjava.annotation.POPClass;
import popjava.annotation.POPConfig;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPSyncSeq;
import popjava.base.POPObject;

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
