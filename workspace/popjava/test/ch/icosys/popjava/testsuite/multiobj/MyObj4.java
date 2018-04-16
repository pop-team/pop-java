package ch.icosys.popjava.testsuite.multiobj;

import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPObjectDescription;
import ch.icosys.popjava.core.annotation.POPSyncConc;
import ch.icosys.popjava.core.annotation.POPSyncSeq;

@POPClass(classId = 1211)
public class MyObj4{
	protected int data;
	
	@POPObjectDescription(url = "localhost")
	public MyObj4(){		
	}
	
	@POPSyncSeq
	public void set(int value) {
		data = value;
	}
	
	@POPSyncConc
	public int get(){
		return data + 1000;
	}
}
