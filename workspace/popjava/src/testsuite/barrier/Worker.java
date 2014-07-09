package testsuite.barrier;

import java.io.IOException;

import popjava.PopJava;
import popjava.base.*;

public class Worker extends POPObject {
	private int myNo;

	public Worker(){
		Class<?> c = Worker.class;
		initializePOPObject();
		addSemantic(c, "work", Semantic.ASYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "setNo", Semantic.SYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "getNo", Semantic.SYNCHRONOUS | Semantic.SEQUENCE);
		myNo=0;
	}
	
	public Worker(int no){
		Class<?> c = Worker.class;
		initializePOPObject();
		addSemantic(c, "work", Semantic.ASYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "setNo", Semantic.SYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "getNo", Semantic.SYNCHRONOUS | Semantic.SEQUENCE);
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
