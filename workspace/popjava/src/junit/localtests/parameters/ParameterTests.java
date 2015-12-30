package junit.localtests.parameters;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Test;

import popjava.PopJava;
import popjava.base.POPException;
import popjava.system.POPSystem;

public class ParameterTests {

    @Test
    public void testBasics(){
        POPSystem.initialize();
        ParameterObject test = PopJava.newActive(ParameterObject.class);
        
        test.noParam();
        
        assertEquals(10, test.simple(10));
        
        POPSystem.end();
        
    }
    
    @Test(expected = POPException.class)
    public void testSerializeError(){
        POPSystem.initialize();
        ParameterObject test = PopJava.newActive(ParameterObject.class);
        
        test.impossibleParam(new ArrayList<String>());
        
        POPSystem.end();
    }
    
    @Test(expected = POPException.class)
    public void testSerializeErrorReturn(){
        POPSystem.initialize();
        ParameterObject test = PopJava.newActive(ParameterObject.class);
        
        test.impossibleReturn();
        
        POPSystem.end();
    }
}
