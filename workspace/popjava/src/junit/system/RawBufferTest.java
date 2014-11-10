package junit.system;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

import popjava.buffer.BufferRaw;

public class RawBufferTest {


    @Test
    public void testStringStorage(){
        
        
        for(int i = 0;i < 100; i++){
            BufferRaw buffer = new BufferRaw();
            String a = getRandomString(1, 100);
            
            buffer.putString(a);
            buffer.extractHeader();
            
            assertEquals(a, buffer.getString());
        }
        
    }
    
    private static String getRandomString(int min, int max){
        int length = min + new Random().nextInt(max - min);
        
        String result = "";
        for(int i = 0;i < length; i++){
            result += "A";
        }
        
        return result;
    }
    
}
