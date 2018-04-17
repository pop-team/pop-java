package ch.icosys.popjava.junit.localtests.priority;

import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPObjectDescription;
import ch.icosys.popjava.core.annotation.POPSyncConc;
import ch.icosys.popjava.core.base.POPObject;

/**
 *
 * @author dosky
 */
@POPClass
public class Z extends POPObject implements IZ {

	@POPObjectDescription(url = "localhost")
	public Z() {
	}

	@POPSyncConc
	@Override
	public int getV() {
		return 100;
	}

}
