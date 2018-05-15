package ch.icosys.popjava.junit.localtests.concurrency;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import ch.icosys.popjava.core.annotation.POPAsyncConc;
import ch.icosys.popjava.core.annotation.POPAsyncMutex;
import ch.icosys.popjava.core.annotation.POPAsyncSeq;
import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPObjectDescription;
import ch.icosys.popjava.core.annotation.POPSyncConc;
import ch.icosys.popjava.core.base.POPObject;

@POPClass
public class ParallelObject extends POPObject {

	private AtomicInteger counter = new AtomicInteger();

	private AtomicInteger counter2 = new AtomicInteger();

	private volatile boolean error = false;

	private Semaphore sem = new Semaphore(0);

	@POPObjectDescription(url = "localhost")
	public ParallelObject() {
	}

	@POPAsyncMutex(id = 22222)
	public void mutex() {
		System.out.println("Start mutex");
		if (counter.incrementAndGet() > 1) {
			error = true;
			System.out.println("Mutex error");
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		counter.decrementAndGet();
		counter2.incrementAndGet();
		sem.release();
		System.out.println("End mutex");
	}

	@POPAsyncSeq(id = 11111)
	public void seq() {
		System.out.println("Start seq");
		if (counter.get() > 1) {
			error = true;
			System.out.println("Seq error");
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		counter2.incrementAndGet();
		sem.release();
		System.out.println("End seq");
	}

	private Semaphore semSleep = new Semaphore(0);

	@POPAsyncConc
	public void concSleep() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		semSleep.release();
	}

	@POPSyncConc
	public void concSleepEnd(int wait) {
		try {
			for (int i = 0; i < wait; i++) {
				semSleep.acquire();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	@POPAsyncConc(id = 33333)
	public void conc() {
		System.out.println("Start conc");
		if (counter.get() > 1) {
			error = true;
			System.out.println("Conc error");
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		counter2.incrementAndGet();
		sem.release();
		System.out.println("End conc");
	}

	@POPSyncConc
	public boolean success(int permits) {
		try {
			sem.acquire(permits);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Success ? " + counter2.intValue());
		return !error && counter2.intValue() == permits;
	}

	@POPSyncConc
	public int ping(int sleep, int value) throws InterruptedException {
		Thread.sleep(sleep);
		System.out.println("ping " + sleep + " " + value);
		return value;
	}
}
