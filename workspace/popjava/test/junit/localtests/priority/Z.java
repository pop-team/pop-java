package junit.localtests.priority;

import popjava.annotation.POPClass;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPSyncConc;
import popjava.base.POPObject;

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
