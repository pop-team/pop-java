package testsuite.barrier;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


import popjava.base.*;

public class Barrier extends POPObject {
	
	protected int counter;
	protected final Lock lock = new ReentrantLock();
	protected final Condition event = lock.newCondition();
	
	public Barrier(){
		Class<?> c = Barrier.class;
		initializePOPObject();
		addSemantic(c, "activate", Semantic.Synchronous | Semantic.Concurrent);
		counter = 15;
	}
	
	public Barrier(Integer n) throws IOException{
		BufferedWriter out = new BufferedWriter(new FileWriter("/tmp/barrier", true));
		Class<?> c = Barrier.class;
		initializePOPObject();
		addSemantic(c, "activate", Semantic.Synchronous | Semantic.Concurrent);
		counter = n.intValue();
		out.write("Barrier closed for "+counter+"\n");
		out.close();
		System.out.println("The barrier is closed for "+ counter+" workers");
	}
	
	public void activate() throws InterruptedException, IOException {
		lock.lock();
		BufferedWriter out = new BufferedWriter(new FileWriter("/tmp/barrier", true));
		counter--;
		out.write("Counter = " + counter+ "\n");
		
		
		if(counter == 0) {
			out.write("Barrier open\n");
			out.close();
			event.signalAll();
		}
		else {
			
			out.write("Wait\n");
			out.close();
			event.await();
			
		}
		lock.unlock();
	}
	

}
