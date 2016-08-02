package junit.localtests.parameters;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Ignore;
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
    
    @Test
    public void testString(){
        POPSystem.initialize();
        ParameterObject test = PopJava.newActive(ParameterObject.class);
        
        test.setValue("asdf");
        
        assertEquals("asdf", test.getValue());
        
        POPSystem.end();
    }
    
    @Test
    @Ignore
    //This does not work and should not work, but it should throw an exception
    public void testStringUTF16(){
        POPSystem.initialize();
        ParameterObject test = PopJava.newActive(ParameterObject.class);
        
        test.setValue("a€𝄞d");
        
        assertEquals("a€𝄞d", test.getValue());
        
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
    
    @Test
    @Ignore //This is supposed to not work, can't instaniate an interface
    public void testInterfaceParameter(){
        POPSystem.initialize();
        ParameterObject test = PopJava.newActive(ParameterObject.class);
        
        test.testInterfaceErrorParameter(test);
        
        POPSystem.end();
    }
}
