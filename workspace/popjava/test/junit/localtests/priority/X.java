package junit.localtests.priority;

import popjava.PopJava;
import popjava.annotation.POPClass;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPSyncConc;
import popjava.base.POPObject;

/**
 *
 * @author dosky
 */
@POPClass
public class X extends POPObject implements IX {

	@POPObjectDescription(url = "localhost")
	public X() {
	}
	
	@POPSyncConc
	@Override
	public Z getZ() {
		Z z = PopJava.newActive(this, Z.class);
		return z;
	}
	
}
