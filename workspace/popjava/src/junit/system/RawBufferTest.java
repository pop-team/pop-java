package junit.system;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

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
    
    @Test
    public void testByteArrayStorage(){
        for(int i = 0;i < 100; i++){
            BufferRaw buffer = new BufferRaw();
            byte [] test = getRandomString(0, 100).getBytes();
            byte [] test2 = getRandomString(0, 100).getBytes();
            
            buffer.putByteArray(test);
            buffer.putByteArray(test2);
            buffer.extractHeader();
            
            byte [] result = buffer.getByteArray(buffer.getInt());
            byte [] result2 = buffer.getByteArray(buffer.getInt());
            
            assertArrayEquals(test, result);
            assertArrayEquals(test2, result2);
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
