//Import added by the POP-Java compiler
import popjava.PopJava;
import popjava.base.POPException;
import popjava.base.POPObject;
import popjava.base.Semantic;
import popjava.serviceadapter.POPAppService;
import popjava.system.POPSystem;

import popjava.annotation.*;
import popjava.annotation.POPConfig.Type;
public class Integer extends POPObject  {
    public Integer(){
        initializePOPObject();
    }
    private int value;
    public Integer(@ POPConfig(Type.URL) String url) {
        initializePOPObject();
        value = 0;
    }
    /*public Integer(){
      value = 0;
   }*/    @ POPSyncConc public int get ( )     {
         POPSystem.writeLog("Get value on Integer ("+getPOPCReference()+"): "+value);
         return value;
    }
    @ POPAsyncMutex public void add ( Integer i)     throws POPException {
        i=(Integer)PopJava.newActive(Integer.class, i.getAccessPoint());
         POPSystem.writeLog("Add integer ("+i.getPOPCReference()+") to the Integer ("+getPOPCReference()+")");
         value += i.get();
    }
    @ POPAsyncMutex public void set ( int val )     {
         POPSystem.writeLog("Set value on Integer ("+getPOPCReference()+"): "+val);
         value = val;
    }
    }