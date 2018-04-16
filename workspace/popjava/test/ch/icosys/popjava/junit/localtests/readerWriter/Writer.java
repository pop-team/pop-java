package ch.icosys.popjava.junit.localtests.readerWriter;

import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPConfig;
import ch.icosys.popjava.core.annotation.POPSyncMutex;
import ch.icosys.popjava.core.annotation.POPConfig.Type;
import ch.icosys.popjava.core.base.POPObject;

@POPClass
public class Writer extends POPObject{
	private int value;
	
	public Writer(){
		
	}
	
	public Writer(@POPConfig(Type.URL) String ip){
		
	}
	
	@POPSyncMutex
	public void write(){
		value++;
	}
	
	@POPSyncMutex
	public int getWritten(){
		return value;
	}
	
}
