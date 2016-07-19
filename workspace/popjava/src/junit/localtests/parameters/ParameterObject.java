package junit.localtests.parameters;

import java.util.ArrayList;
import java.util.List;

import popjava.PopJava;
import popjava.annotation.POPAsyncMutex;
import popjava.annotation.POPClass;
import popjava.annotation.POPSyncConc;
import popjava.annotation.POPSyncMutex;
import popjava.base.POPObject;

@POPClass
public class ParameterObject extends POPObject implements MyInterface{

    private String temp;
    
    @POPSyncConc
    public void noParam(){
        
    }
    
    @POPSyncConc
    public int simple(int a){
        return a;
    }
    
    @POPSyncConc
    public void impossibleParam(List<String> test){
        
    }
    
    @POPAsyncMutex
    public void setValue(String string){
        temp = string;
    }
    
    @POPSyncMutex
    public String getValue(){
        return temp;
    }
    
    
    @POPSyncConc
    public List<String> impossibleReturn(){
        return new ArrayList<String>();
    }
    
    @POPSyncConc
    public void testInterfaceErrorParameter(MyInterface obj){
    	
    }
    
    @POPSyncConc
    public MyInterface testInterfaceErrorReturn(){
    	return PopJava.getThis(this);
    }
}
