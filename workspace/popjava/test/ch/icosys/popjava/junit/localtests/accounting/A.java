package junit.localtests.accounting;

import popjava.annotation.POPAsyncConc;
import popjava.annotation.POPAsyncSeq;
import popjava.annotation.POPClass;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPSyncConc;
import popjava.annotation.POPSyncMutex;
import popjava.annotation.POPSyncSeq;
import popjava.base.POPObject;

/**
 *
 * @author dosky
 */
@POPClass
public class A extends POPObject {

	@POPObjectDescription(url = "localhost", tracking = true)
	public A() {
	}

	@POPSyncSeq
	public void seq() {
		job();
	}

	@POPSyncConc
	public void conc() {
		job();
	}

	@POPAsyncSeq
	public void aseq() {
		job();
	}

	@POPAsyncConc
	public void aconc() {
		job();
	}

	@POPSyncMutex
	public void sync() {

	}

	private double job() {
		double sum = 0d;
		for (int n = 0; n < 1_000_000; n++) {
			sum = Math.sqrt(n);
		}
		return sum;
	}
}
