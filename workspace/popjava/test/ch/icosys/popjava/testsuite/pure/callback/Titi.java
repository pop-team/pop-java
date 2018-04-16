package ch.icosys.popjava.testsuite.pure.callback;

import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPObjectDescription;
import ch.icosys.popjava.core.annotation.POPSyncSeq;

@POPClass(classId = 1034)
public class Titi{
    
	private int identity;
	private Toto t;
	
	@POPObjectDescription(url = "localhost")
	public Titi(){
		identity = -1;
	}
	
	@POPSyncSeq
	public void setIdent(int i){
		identity = i;
	}
	
	@POPSyncSeq
	public void setIdent(Toto t){
        System.out.println("A "+(t == null));
		this.t = t;
	}
	
	@POPSyncSeq
	public void computeIdent(){
        System.out.println("B "+(t == null));
	    t.setIdent(identity);
	    System.out.println("E");
	}
}
