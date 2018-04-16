package ch.icosys.popjava.junit.localtests.annotations.objects;

import ch.icosys.popjava.core.annotation.POPAsyncMutex;
import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPConfig;
import ch.icosys.popjava.core.annotation.POPObjectDescription;
import ch.icosys.popjava.core.annotation.POPParameter;
import ch.icosys.popjava.core.annotation.POPSyncConc;
import ch.icosys.popjava.core.annotation.POPConfig.Type;
import ch.icosys.popjava.core.annotation.POPParameter.Direction;
import ch.icosys.popjava.core.base.POPObject;

@POPClass(classId = 1000)
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
   
   public Integer fail(){
       return this;
   }
}
