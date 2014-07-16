package testsuite.pure.callback;

import popjava.annotation.POPClass;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPSyncSeq;
import popjava.base.POPObject;

@POPClass(classId = 1034)
public class Titi extends POPObject {
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
		this.t = t;
	}
	
	@POPSyncSeq
	public void computeIdent(){
	    t.setIdent(identity);
	}
}
