package junit.localtests.benchmark;

import java.util.concurrent.Semaphore;

import popjava.annotation.POPAsyncSeq;
import popjava.annotation.POPClass;
import popjava.annotation.POPConfig;
import popjava.annotation.POPSyncConc;
import popjava.annotation.POPSyncSeq;
import popjava.annotation.POPConfig.Type;
import popjava.annotation.POPSyncMutex;
import popjava.base.POPObject;

@POPClass
public class Writer extends POPObject{
	private int value;
	private int max;
	
	private Semaphore end = new Semaphore(0);
	
	public Writer(){
		
	}
	
	public Writer(@POPConfig(Type.URL) String ip, int max){
		this.max = max;
	}
	
	@POPAsyncSeq
	public void write(){
		value++;
		//System.out.println("Wrote "+value);
		if(value == max){
			end.release();
		}
	}
	
	@POPSyncMutex
	public int getWritten(){
		return value;
	}
	
	@POPSyncConc
	public void join(){
		try {
			end.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
