package junit.localtests.readerWriter;

import popjava.annotation.POPClass;
import popjava.annotation.POPConfig;
import popjava.annotation.POPConfig.Type;
import popjava.annotation.POPSyncMutex;
import popjava.base.POPObject;

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
