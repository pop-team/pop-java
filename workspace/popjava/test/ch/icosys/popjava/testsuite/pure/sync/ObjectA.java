package ch.icosys.popjava.testsuite.pure.sync;

import java.util.concurrent.Semaphore;

import ch.icosys.popjava.core.annotation.POPAsyncConc;
import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPObjectDescription;
import ch.icosys.popjava.core.annotation.POPSyncConc;
import ch.icosys.popjava.core.annotation.POPSyncMutex;

@POPClass
public class ObjectA {

	// TODO: this should work better without semaphores
	public static final int SLEEP = 1000;

	private Semaphore sem = new Semaphore(0);

	private Semaphore sem2 = new Semaphore(0);

	private int calls = 0;

	@POPObjectDescription(url = "localhost")
	public ObjectA() {

	}

	@POPAsyncConc
	public void getA() {
		System.out.println("getA() - A");
		calls++;
		try {
			Thread.sleep(SLEEP);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("getA() - B");
		sem.release();
	}

	@POPAsyncConc
	public void getB() {
		System.out.println("getB() - A");
		try {
			Thread.sleep(SLEEP);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		calls++;
		System.out.println("getB() - B");
		sem.release();
	}

	@POPSyncMutex
	public int getC() {
		System.out.println("getC() - A");
		try {
			sem.acquire();
			sem.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return calls;
	}

	@POPSyncConc
	public void testSelfCall() {
		this.concSleep();
		this.concSleep();
		this.concSleep();

		try {
			sem2.acquire();
			sem2.acquire();
			sem2.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@POPSyncConc
	public void concSleep() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		sem2.release();
	}
}
