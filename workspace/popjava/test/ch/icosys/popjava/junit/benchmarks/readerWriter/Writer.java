package ch.icosys.popjava.junit.benchmarks.readerWriter;

import java.util.concurrent.Semaphore;

import ch.icosys.popjava.core.annotation.POPAsyncSeq;
import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPConfig;
import ch.icosys.popjava.core.annotation.POPSyncConc;
import ch.icosys.popjava.core.annotation.POPSyncMutex;
import ch.icosys.popjava.core.annotation.POPConfig.Type;
import ch.icosys.popjava.core.base.POPObject;

@POPClass
public class Writer extends POPObject {
	private int value;

	private int max;

	private Semaphore end = new Semaphore(0);

	public Writer() {

	}

	public Writer(@POPConfig(Type.URL) String ip, int max) {
		this.max = max;
	}

	@POPAsyncSeq
	public void write() {
		value++;
		// System.out.println("Wrote "+value);
		if (value == max) {
			end.release();
		}
	}

	@POPSyncMutex
	public int getWritten() {
		return value;
	}

	@POPSyncConc
	public void join() {
		try {
			end.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
