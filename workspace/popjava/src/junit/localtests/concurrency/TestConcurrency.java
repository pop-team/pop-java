package junit.localtests.concurrency;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import popjava.PopJava;
import popjava.system.POPSystem;

public class TestConcurrency {

	@Test
	public void test(){
		POPSystem.initialize();
		
		ParallelObject object = PopJava.newActive(ParallelObject.class);
		
		object.sync();
		object.mutex();
		object.sync();
		object.conc();
		
		boolean correct = object.success();
		
		POPSystem.end();
		
		assertTrue(correct);
	}
	
	@Test
	public void testThreadedConcurrency() throws InterruptedException{
	    POPSystem.initialize();
        
        final ParallelObject object = PopJava.newActive(ParallelObject.class);
        
        new Thread(new Runnable() {
            
            @Override
            public void run() {
                try{
                    int ret = object.ping(1500, 20);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
        }).start();
        
        for(int i = 0; i < 15; i++){
            assertEquals(i, object.ping(0, i));
        }
        
        POPSystem.end();
	}
}
