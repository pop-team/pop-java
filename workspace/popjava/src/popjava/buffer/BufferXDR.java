package popjava.buffer;

import popjava.base.*;

import java.nio.*;
/**
 * This class is a XDR extension of the BufferRAW class
 */
public class BufferXDR extends BufferRaw {

	/**
	 * Default constructor. Create a new instance of XDR buffer
	 */
	public BufferXDR() {
		this(new MessageHeader());	
	}

	/**
	 * Constructor with given values
	 * @param messageHeader	Message header to be associated with this buffer
	 */
	public BufferXDR(MessageHeader messageHeader) {
		super(messageHeader);		
		buffer.order(ByteOrder.BIG_ENDIAN);
	}

	/**
	 * Insert a boolean value
	 * @param value The boolean value to be inserted
	 */
	@Override
	public void putBoolean(boolean value)
	{
		if(value){
			putInt(Integer.reverseBytes(1));
		}else{
			putInt(0);
		}
	}
	
	@Override
	public boolean getBoolean(){
	    int value = getInt();
	    if(value == 0){
	        return false;
	    }else if(value == Integer.reverseBytes(1)){
	        return true;
	    }
	    
	    throw new RuntimeException("Invalid Boolean encoding: "+value);
	}

	/**
	 * Transfirm an integer
	 */
	public int getTranslatedInteger(byte[] value) {		
		return value[0]<<24 | (value[1]&0xff)<<16 | (value[2]&0xff)<<8 | (value[3]&0xff);
	}

}
