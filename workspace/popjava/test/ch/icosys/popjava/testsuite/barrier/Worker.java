package ch.icosys.popjava.testsuite.barrier;

import java.io.IOException;

import ch.icosys.popjava.core.annotation.POPAsyncSeq;
import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPSyncSeq;
import ch.icosys.popjava.core.base.*;

@POPClass
public class Worker{
	private int myNo;
	
	public Worker(){
	    this(0);
	}
	
	public Worker(int no){
		myNo = no;
	}
	
	@POPAsyncSeq
	public void work(Barrier b) throws InterruptedException, POPException, IOException {
		//b = (Barrier)PopJava.newActive(Barrier.class, b.getAccessPoint());
	    
		//Job before synchronization
		b.activate();
		//Job after synchronization 
		myNo +=10;
	}
	
	@POPSyncSeq
	public void setNo(int no){
		myNo = no;
	}
	
	@POPSyncSeq
	public int getNo(){
		return myNo;
	}
}
