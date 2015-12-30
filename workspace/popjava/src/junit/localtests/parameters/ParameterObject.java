package junit.localtests.parameters;

import java.util.ArrayList;
import java.util.List;

import popjava.annotation.POPClass;
import popjava.annotation.POPSyncConc;
import popjava.base.POPObject;

@POPClass
public class ParameterObject extends POPObject{

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
    
    @POPSyncConc
    public List<String> impossibleReturn(){
        return new ArrayList<String>();
    }
    
}
