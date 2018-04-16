package junit.system;

import static org.junit.Assert.*;

import org.junit.Test;

import popjava.util.ClassUtil;

public class ConstructorTests {

    @Test
    public void testEmptyConstructor(){
        Class<?> [] parameters = new Class<?>[]{};
        Class<?> [] constructor = new Class<?>[]{};
        
        assertTrue(ClassUtil.areParameterTypesTheSame(parameters, constructor));
    }
    
    @Test
    public void testBugConstructor(){
        Class<?> [] constructor = new Class<?>[]{Integer.class, String.class};
        Class<?> [] parameters = new Class<?>[]{Integer.class, Integer.class, Integer.class};
        
        assertFalse(ClassUtil.areParameterTypesTheSame(constructor, parameters));
        assertFalse(ClassUtil.areParameterTypesTheSame(parameters, constructor));
    }
}
