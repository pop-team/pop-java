package ch.icosys.popjava.junit.localtests.accounting;

import ch.icosys.popjava.core.annotation.POPAsyncConc;
import ch.icosys.popjava.core.annotation.POPAsyncSeq;
import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPObjectDescription;
import ch.icosys.popjava.core.annotation.POPParameter;
import ch.icosys.popjava.core.annotation.POPParameter.Direction;
import ch.icosys.popjava.core.annotation.POPSyncConc;
import ch.icosys.popjava.core.annotation.POPSyncMutex;
import ch.icosys.popjava.core.annotation.POPSyncSeq;
import ch.icosys.popjava.core.base.POPObject;

/**
 *
 * @author dosky
 */
@POPClass
public class A extends POPObject {

    @POPObjectDescription(url = "localhost", tracking = true)
    public A() {}

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

    @POPSyncSeq
    public byte[] sendByteArrayIN(@POPParameter(Direction.IN) byte[] ba) {
	return ba;
    }

    @POPSyncSeq
    public byte[] sendByteArrayINOUT(/*@POPParameter(Direction.INOUT)*/ byte[] ba) {
	return ba;
    }

    private double job() {
	double sum = 0;
	for (int n = 0; n < 1_000_000; n++) {
	    sum = Math.sqrt(n);
	}
	return sum;
    }
    
}
