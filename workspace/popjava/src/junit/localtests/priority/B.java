package junit.localtests.priority;

import java.util.logging.Level;
import java.util.logging.Logger;
import popjava.annotation.POPClass;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPSyncConc;
import popjava.annotation.POPSyncMutex;
import popjava.base.POPObject;

/**
 *
 * @author dosky
 */
@POPClass
public class B extends POPObject implements A {

	private int value;
	
	@POPObjectDescription(url = "localhost")
	public B() {
	}
	
	@POPSyncConc
	@Override
	public void a() {
		try {
			value = 100;
			Thread.sleep(1000);
		} catch (InterruptedException ex) {
			Logger.getLogger(B.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@POPSyncConc
	@Override
	public void b() {
		try {
			value = 200;
			Thread.sleep(500);
		} catch (InterruptedException ex) {
			Logger.getLogger(B.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@POPSyncMutex
	public int getValue() {
		return value;
	}
	
	@POPSyncMutex
	public void sync(){}
}
