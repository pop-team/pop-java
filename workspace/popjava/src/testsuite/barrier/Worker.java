package testsuite.barrier;

import java.io.IOException;

import popjava.PopJava;
import popjava.base.*;

public class Worker extends POPObject {
	private int myNo;

	public Worker(){
		Class<?> c = Worker.class;
		initializePOPObject(c);
		addSemantic(c, "work", Semantic.Asynchronous | Semantic.Sequence);
		addSemantic(c, "setNo", Semantic.Synchronous | Semantic.Sequence);
		addSemantic(c, "getNo", Semantic.Synchronous | Semantic.Sequence);
		myNo=0;
	}
	
	public Worker(int no){
		Class<?> c = Worker.class;
		initializePOPObject(c);
		addSemantic(c, "work", Semantic.Asynchronous | Semantic.Sequence);
		addSemantic(c, "setNo", Semantic.Synchronous | Semantic.Sequence);
		addSemantic(c, "getNo", Semantic.Synchronous | Semantic.Sequence);
		myNo = no;
	}
	
	public void work(Barrier b) throws InterruptedException, POPException, IOException {
		b = (Barrier)PopJava.newActive(Barrier.class, b.getAccessPoint());
		//Job before synchronization
		b.activate();
		//Job after synchronization 
		myNo +=10;
	}
	
	public void setNo(int no){
		myNo = no;
	}
	
	public int getNo(){
		return myNo;
	}
}
