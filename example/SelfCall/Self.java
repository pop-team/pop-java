import ch.icosys.popjava.core.annotation.*;

@POPClass
public class Self {

	private int loopCount = 10;
	private Semaphore locker = new Semaphore(0);
	private int testVariable = 0;

	@POPObjectDescription(url = "localhost")
	public Self(){
	}
	
	@POPSyncConc
	public void test(){
		this.testVariable = 10;
		
		Self test = this;
		for(int i = 0; i< loopCount; i++){
			test.pause();
		}
		
		for(int i = 0; i< loopCount; i++){
			try{
				locker.acquire();
			}catch(Exception e){
			}
		}
	}
	
	@POPSyncConc
	public void test2(){
		this.testVariable = 20;
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
	
	@POPSyncMutex
	public int getValue(){
		return testVariable;
	}
}