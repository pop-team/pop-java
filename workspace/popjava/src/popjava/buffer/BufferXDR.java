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
		super();
		buffer.order(ByteOrder.BIG_ENDIAN);		
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
	public void putBoolean(boolean value)
	{
		if(value)
			super.putInt(Integer.reverseBytes(1));
		else
			super.putInt(0);
	}

	/**
	 * Transfirm an integer
	 */
	public int getTranslatedInteger(byte[] value) {		
		return value[0]<<24 | (value[1]&0xff)<<16 | (value[2]&0xff)<<8 | (value[3]&0xff);
	}

}
