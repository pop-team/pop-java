package test;

import popjava.annotation.POPClass;
import popjava.annotation.POPSyncSeq;

@POPClass
public class Titi {
	private int identity;
	
	public Titi(){
		identity = -1;
	}
	
	@POPSyncSeq
	public void setIdent(int i){
		identity = i;
	}

	@POPSyncSeq
	public void computeIdent(Toto toto){
		toto.setIdent(identity);
	}
}

