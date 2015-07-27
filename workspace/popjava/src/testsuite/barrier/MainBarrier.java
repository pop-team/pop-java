package testsuite.barrier;

import java.io.IOException;

import popjava.PopJava;
import popjava.annotation.POPClass;
import popjava.baseobject.POPAccessPoint;

@POPClass(isDistributable = false)
public class MainBarrier {
    
	public static void main(String... argvs) throws IOException {
		System.out.println("Barrier: Starting test...");
		
		int nbWorkers = 12;
		if(argvs.length > 0) nbWorkers = Integer.parseInt(argvs[0]);
		
		try {
			Barrier b = new Barrier(nbWorkers);
			POPAccessPoint[] pa = new POPAccessPoint[nbWorkers];
			for (int i = 0; i < nbWorkers; i++) {
				Worker w = new Worker();
				pa[i] = PopJava.getAccessPoint(w);
				w.setNo(i);
				w.work(b);
			}
			//Give time to worker to finish their job
			Thread.sleep(2000);
			
			for (int i = 0; i < pa.length; i++) {
				Worker w = (Worker)PopJava.newActive(Worker.class, pa[i]);
				if(w.getNo()!=i+10){
					System.out.println("Barrier Test failed");
					return;
				}
			}
			System.out.println("Barrier test successful");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		
	}
}
