package ch.icosys.popjava.testsuite.jinteger;

import ch.icosys.popjava.core.annotation.POPAsyncSeq;
import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPSyncConc;

@POPClass(classId = 1001, deconstructor = true)
public class Jinteger{
    
	private int data;
	
	public Jinteger(){
	}
	
	@POPAsyncSeq
	public void set(int value){
		data = value;
	}
	
	@POPSyncConc
	public int get(){
		return data;
	}
}
