import popjava.annotation.POPAsyncSeq;
import popjava.annotation.POPClass;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPSyncConc;
import popjava.annotation.POPSyncMutex;
import popjava.annotation.POPConfig;

@POPClass
public class Integer {
   private int value;
   
   public Integer(){
       
   }
   
   public Integer(@POPConfig(POPConfig.Type.URL) String url){
    value = 0;
   }

   @POPSyncConc
   public int get(){
      return value;
   }

   @POPSyncMutex
   public void add(Integer i){	
      value += i.get();
   }

   @POPAsyncSeq
   public void set(int val){
      value = val;
   }
}
