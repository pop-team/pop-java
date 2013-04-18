package junit.annotations.semantics;

import popjava.annotation.POPAsyncConc;
import popjava.annotation.POPAsyncMutex;
import popjava.annotation.POPAsyncSeq;
import popjava.annotation.POPSyncConc;
import popjava.annotation.POPSyncMutex;
import popjava.annotation.POPSyncSeq;
import popjava.base.POPObject;

public class SemanticObject extends POPObject{
	
	public SemanticObject(){
		initializePOPObject();
	}
	
	@POPSyncConc
	public void testSyncConc(){
	}
	
	@POPSyncSeq
	public void testSyncSeq(){
	}
	
	@POPSyncMutex
	public void testSyncMut(){
	}
	
	@POPAsyncConc
	public void testAsyncConc(){
	}
	
	@POPAsyncSeq
	public void testAsyncSeq(){
	}
	
	@POPAsyncMutex
	public void testAsyncMutex(){
	}
}
