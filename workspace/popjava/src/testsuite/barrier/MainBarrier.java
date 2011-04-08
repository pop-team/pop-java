package testsuite.barrier;

import java.io.IOException;

import popjava.PopJava;
import popjava.base.POPException;
import popjava.baseobject.POPAccessPoint;
import popjava.system.POPSystem;

public class MainBarrier {
	public static void main(String... argvs) throws IOException {
		System.out.println("Barrier: Starting test...");
		int nbWorkers = 12;
		if(argvs.length > 0) nbWorkers = Integer.parseInt(argvs[0]);
		try {
			POPSystem.initialize(argvs);
			Barrier b = (Barrier)PopJava.newActive(Barrier.class, new Integer(nbWorkers));
			POPAccessPoint[] pa = new POPAccessPoint[nbWorkers];
			for (int i = 0; i < nbWorkers; i++) {
				Worker w = (Worker)PopJava.newActive(Worker.class);
				pa[i] = w.getAccessPoint();
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
			POPSystem.end();
		} catch (POPException e) {
			POPSystem.end();
			System.out.println("Exception occured : " + e.errorMessage);
		} catch (InterruptedException e) {
			POPSystem.end();
			e.printStackTrace();
		}
		
		
	}
}
