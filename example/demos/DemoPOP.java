import popjava.PopJava;
import popjava.annotation.POPAsyncConc;
import popjava.annotation.POPAsyncSeq;
import popjava.annotation.POPClass;
import popjava.annotation.POPSyncConc;
import popjava.annotation.POPSyncMutex;
import popjava.base.POPObject;
import popjava.system.POPSystem;
import popjava.util.ClassUtil;
import popjava.util.Util;

@POPClass
public class DemoPOP {
   private int id;
   
   public DemoPOP(){
       
   }
   
   public DemoPOP(int newID){
      id = newID;
   }

   private String getAccesPoint(DemoPOP obj){
       return PopJava.getAccessPoint(obj).toString();
   }
   
   @POPAsyncSeq
   public void sendIdTo(DemoPOP dp){
      
       
      System.out.println("POPCobject:" + id + " on machine:"+ getAccesPoint(this) + " is sending his iD to object:" +getAccesPoint(dp));
	   dp.recAnID(id);

   }

   @POPSyncConc
   public int getID(){
      return id;
   }

   @POPAsyncConc
   public void recAnID(int i){
      System.out.println("POPobject:"+id+" on machine:"+getAccesPoint(this)+" is receiving id="+i);
   }

   @POPSyncMutex
   public void wait(int sec){
      try{
         Thread.sleep(sec*1000);
      } catch(Exception e){
         System.out.println("Exception occured in wait");   
      }
   }


}
