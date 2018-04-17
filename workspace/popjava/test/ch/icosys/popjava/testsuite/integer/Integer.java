package ch.icosys.popjava.testsuite.integer;

import ch.icosys.popjava.core.annotation.POPAsyncSeq;
import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPObjectDescription;
import ch.icosys.popjava.core.annotation.POPSyncMutex;
import ch.icosys.popjava.core.base.POPObject;

@POPClass
public class Integer extends POPObject {
	protected int value;

	@POPObjectDescription(url = "localhost")
	public Integer() {
		value = 10;
	}

	public Integer(boolean test) {
		value = 20;
	}

	@POPSyncMutex
	public int get() {
		return this.value;
	}

	@POPSyncMutex
	public void add(Integer i) {
		int val = i.get();
		this.value += val;
	}

	@POPAsyncSeq
	public void set(int val) {
		this.value = val;
	}
}
