package junit.localtests.annotations;

import popjava.annotation.POPAsyncMutex;
import popjava.annotation.POPClass;
import popjava.annotation.POPConfig;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPParameter;
import popjava.annotation.POPSyncConc;
import popjava.annotation.POPConfig.Type;
import popjava.annotation.POPParameter.Direction;
import popjava.base.POPObject;

@POPClass
public class Integer extends POPObject{
   private int value;
   
   public Integer(){
	   
   }
   
   @POPObjectDescription(jvmParameters = "-XX:-UseParallelGC")
   public Integer(@POPConfig(Type.URL) String url){
    value = 0;
   }

   @POPSyncConc
   public int get(){
      return value;
   }

   @POPAsyncMutex
   public void add(Integer i){
      value += i.get();
   }

   @POPAsyncMutex
   public void set(int val){
      value = val;
   }
   
   @POPSyncConc
   public int arrayChanger(int [] array){
		for(int i = 0; i < array.length; i++){
			array[i] *= 2;
		}
		
		return 19;
   }
   
   @POPSyncConc
   public int arrayChanger2(@POPParameter(Direction.IN) int [] array){
		for(int i = 0; i < array.length; i++){
			array[i] *= 2;
		}
		
		return 19;
   }
   
   @POPSyncConc
   public boolean arrayChanger3(@POPParameter(Direction.OUT) int [] array){
		return array == null;
   }
}
