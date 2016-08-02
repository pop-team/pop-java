package junit.localtests.concurrency;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import popjava.annotation.POPAsyncConc;
import popjava.annotation.POPAsyncMutex;
import popjava.annotation.POPAsyncSeq;
import popjava.annotation.POPClass;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPSyncConc;
import popjava.base.POPObject;

@POPClass
public class ParallelObject extends POPObject{

	private AtomicInteger counter = new AtomicInteger();
	private AtomicInteger counter2 = new AtomicInteger();
	private volatile boolean error = false;
	
	private Semaphore sem = new Semaphore(0);
	
	@POPObjectDescription(url = "localhost")
	public ParallelObject(){
	}
	
	@POPAsyncMutex
	public void mutex(){
		System.out.println("Start mutex");
		if(counter.incrementAndGet() > 1){
			error = true;
			System.out.println("Mutex error");
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("End mutex");
		counter.decrementAndGet();
		counter2.incrementAndGet();
		sem.release();
	}
	
	@POPAsyncSeq
	public void sync(){
		System.out.println("Start seq");
		if(counter.get() > 1){
			error = true;
			System.out.println("Seq error");
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("End seq");
		counter2.incrementAndGet();
		sem.release();
	}
	
	private Semaphore semSleep = new Semaphore(0);
	
	@POPAsyncConc
	public void concSleep(){
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		semSleep.release();
	}
	
	@POPSyncConc
	public void concSleepEnd(int wait){
		try{
			for(int i = 0; i < wait; i++){
				semSleep.acquire();
			}
		}catch (InterruptedException e) {
			// TODO: handle exception
		}
		
	}
	
	@POPAsyncConc
	public void conc(){
		System.out.println("Start conc");
		if(counter.get() > 1){
			error = true;
			System.out.println("Conc error");
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("End conc");
		counter2.incrementAndGet();
		sem.release();
	}
	
	@POPSyncConc
	public boolean success(){
		try {
			sem.acquire(4);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Success ? "+counter2.intValue());
		return error == false && counter2.intValue() == 4;
	}
	
	@POPSyncConc
	public int ping(int sleep, int value) throws InterruptedException{
	    Thread.sleep(sleep);
	    System.out.println("ping "+sleep+" "+value);
	    return value;
	}
}
