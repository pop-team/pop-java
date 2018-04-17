package ch.icosys.popjava.testsuite.jinteger;

import ch.icosys.popjava.core.annotation.POPAsyncSeq;
import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPSyncConc;
import ch.icosys.popjava.core.annotation.POPSyncMutex;

@POPClass(classId = 1000, className = "Integer2", deconstructor = true)
public class Integer2 {

	private int data;

	public Integer2() {
	}

	@POPSyncMutex
	public void add(Jinteger i) {
		data += i.get();
	}

	@POPAsyncSeq
	public void set(int value) {
		data = value;
	}

	@POPSyncConc
	public int get() {
		return data;
	}
}
