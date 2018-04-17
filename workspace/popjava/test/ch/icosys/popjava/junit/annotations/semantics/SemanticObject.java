package ch.icosys.popjava.junit.annotations.semantics;

import ch.icosys.popjava.core.annotation.POPAsyncConc;
import ch.icosys.popjava.core.annotation.POPAsyncMutex;
import ch.icosys.popjava.core.annotation.POPAsyncSeq;
import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPSyncConc;
import ch.icosys.popjava.core.annotation.POPSyncMutex;
import ch.icosys.popjava.core.annotation.POPSyncSeq;
import ch.icosys.popjava.core.base.POPObject;

@POPClass
public class SemanticObject extends POPObject {

	public SemanticObject() {
	}

	@POPSyncConc
	public void testSyncConc() {
	}

	@POPSyncSeq
	public void testSyncSeq() {
	}

	@POPSyncMutex
	public void testSyncMut() {
	}

	@POPAsyncConc
	public void testAsyncConc() {
	}

	@POPAsyncSeq
	public void testAsyncSeq() {
	}

	@POPAsyncMutex
	public void testAsyncMutex() {
	}
}
