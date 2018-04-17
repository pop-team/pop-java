package ch.icosys.popjava.junit.localtests.priority;

import ch.icosys.popjava.core.PopJava;
import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPObjectDescription;
import ch.icosys.popjava.core.annotation.POPSyncConc;
import ch.icosys.popjava.core.base.POPObject;

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
