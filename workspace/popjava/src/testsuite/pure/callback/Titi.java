package testsuite.pure.callback;

import popjava.annotation.POPClass;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPSyncSeq;
import popjava.base.POPObject;

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
