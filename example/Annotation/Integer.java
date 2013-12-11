//Import added by the POP-Java compiler
import popjava.PopJava;
import popjava.base.POPException;
import popjava.annotation.*;
import popjava.base.POPObject;
import popjava.base.Semantic;
import popjava.serviceadapter.POPAppService;
import popjava.system.POPSystem;

import popjava.annotation.*;
import popjava.annotation.POPConfig.Type;
import popjava.annotation.POPParameter.Direction;
@POPClass public class Integer extends POPObject  {
    public Integer(){
        initializePOPObject();//A
    }
    private int value;
    public Integer(@ POPConfig(Type.URL) String url) {
        initializePOPObject();//B
        value = 0;
    }
    @ POPSyncConc public int get ( )     {
                 return value;
    }
    @ POPAsyncMutex public void add ( Integer i)     {
        i=(Integer)PopJava.newActive(Integer.class, i.getAccessPoint());
         value += i.get();
    }
    @ POPAsyncMutex public void set ( int val )     {
                 value = val;
    }
    @ POPSyncConc public int arrayChanger ( int [ ] array )     {
                 for(int i = 0;
        i < array.length;
        i ++) {
            array [ i ] *= 2;
        }
        return 19;
    }
    @ POPSyncConc public int arrayChanger2 ( @ POPParameter ( Direction . IN ) int [ ] array )     {
                 for(int i = 0;
        i < array.length;
        i ++) {
            array [ i ] *= 2;
        }
        return 19;
    }
    @ POPSyncConc public boolean arrayChanger3 ( @ POPParameter ( Direction . OUT ) int [ ] array )     {
                 return array == null;
    }
    }
