import java.util.concurrent.Semaphore;

import popjava.PopJava;
import popjava.annotation.*;
import popjava.base.POPObject;
 
@POPClass
public class B extends A{

	private int loopCount = 10;
	private Semaphore locker = new Semaphore(4);
	private int testVariable = 0;

	private B me;
	
	@POPObjectDescription(url = "localhost")
	public B(){
	}
	
	
	@POPSyncConc
	public void test(){
		for(int i = 0; i< loopCount; i++){
			PopJava.getThis(this).pause();
		}
		
		for(int i = 0; i< loopCount; i++){
			try{
				locker.acquire();
			}catch(Exception e){
			}
		}
	}

	@POPAsyncConc
	public void pause(){
		try{
			Thread.sleep(1000);
		}catch(Exception e){
		}
		
		locker.release();
	}
}