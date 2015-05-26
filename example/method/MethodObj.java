import java.lang.*;

import popjava.annotation.POPAsyncConc;
import popjava.annotation.POPAsyncMutex;
import popjava.annotation.POPAsyncSeq;
import popjava.annotation.POPClass;
import popjava.annotation.POPSyncConc;

@POPClass
public class MethodObj {
   private int value;
   
   public MethodObj(){
      value = 0;
   }

   @POPSyncConc
   public int get(){
      return value;
   }

   @POPAsyncSeq
   public void setSeq(int val){
      try{
         Thread.sleep(1000);
         value = val;
      } catch(InterruptedException e){

      }
   }

   @POPAsyncConc
   public void setConc(int val){
      System.out.println("Test "+val);
      try{
         Thread.sleep(1000);
         value = val;
      } catch(InterruptedException e){

      }
   }

   @POPAsyncMutex
   public void setMutex(int val){
      try{
         value = val;
         Thread.sleep(3000);
      } catch(InterruptedException e){

      }
   }
}
