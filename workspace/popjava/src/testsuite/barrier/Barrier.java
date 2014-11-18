package testsuite.barrier;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import popjava.annotation.POPClass;
import popjava.annotation.POPSyncConc;
import popjava.base.*;

@POPClass
public class Barrier{
	
	protected int counter;
	protected final Lock lock = new ReentrantLock();
	protected final Condition event = lock.newCondition();
	
	public Barrier(){
		counter = 15;
	}
	
	public Barrier(int n) throws IOException{
		BufferedWriter out = new BufferedWriter(new FileWriter("/tmp/barrier", true));
		counter = n;
		out.write("Barrier closed for "+counter+"\n");
		out.close();
		System.out.println("The barrier is closed for "+ counter+" workers");
	}
	
	@POPSyncConc
	public void activate() throws InterruptedException, IOException {
		lock.lock();
		BufferedWriter out = new BufferedWriter(new FileWriter("/tmp/barrier", true));
		counter--;
		out.write("Counter = " + counter+ "\n");
		
		if(counter == 0) {
			out.write("Barrier open\n");
			out.close();
			event.signalAll();
		} else {
			out.write("Wait\n");
			out.close();
			event.await();
		}
		
		lock.unlock();
	}
	

}
