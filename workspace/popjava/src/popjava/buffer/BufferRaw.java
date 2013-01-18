package popjava.buffer;

import popjava.base.*;
import popjava.util.LogWriter;

import java.nio.*;

/**
 * This class is a RAW implementation of the buffer abstract class
 */
public class BufferRaw extends POPBuffer {
	/**
	 * Size of the buffer
	 */
	public static final int BufferLength = 1024;
	
	/**
	 * Byte buffer to store data
	 */
	protected ByteBuffer buffer;

	/**
	 * Default constructor
	 */
	public BufferRaw() {
		buffer = ByteBuffer.allocate(BufferLength);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		size = MessageHeader.HeaderLength;
		buffer.position(MessageHeader.HeaderLength);
	}

	/**
	 * Constructor with given values
	 * @param messageHeader	Message header to be associated with this buffer
	 */
	public BufferRaw(MessageHeader messageHeader) {
		super(messageHeader);
		buffer = ByteBuffer.allocate(BufferLength);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.position(MessageHeader.HeaderLength);
		size = MessageHeader.HeaderLength;
	}

	@Override
	public byte[] array() {
		return buffer.array();
	}

	@Override
	public MessageHeader extractHeader() {
		messageHeader = new MessageHeader();
		if (buffer.limit() >= MessageHeader.HeaderLength) {
			int requestType = this.getInt(4);
			messageHeader.setRequestType(requestType);
			switch (requestType) {
			case MessageHeader.Request:
				messageHeader.setClassId(this.getInt(8));
				messageHeader.setMethodId(this.getInt(12));
				messageHeader.setSenmatics(this.getInt(16));
				break;
			case MessageHeader.Exception:
				messageHeader.setExceptionCode(this
						.getInt(8));
				break;
			case MessageHeader.Response:
				break;
			default:
				break;
			}
			position(MessageHeader.HeaderLength);
		}
		return this.messageHeader;
	}


	@Override
	public boolean getBoolean() {
		int value = buffer.getInt();
		if (value == 0)
			return false;
		else
			return true;
	}

	@Override
	public float getFloat() {
		return buffer.getFloat();
	}

	@Override
	public int getInt() {
		return buffer.getInt();
	}

	/**
	 * Get int value at the specified index
	 * @param index	index of the value
	 * @return	the int value
	 */
	public int getInt(int index) {
		return buffer.getInt(index);
	}

	@Override
	public char getChar() {
		char c= buffer.getChar();
		this.position(this.position()+2);
		return c;
	}

	@Override
	public double getDouble() {
		return buffer.getDouble();
	}

	@Override
	public long getLong() {
		return buffer.getLong();
	}

	@Override
	public String getString() {
		int stringLength = getInt();
		try {
			byte[] data = new byte[stringLength];
			buffer.get(data, 0, stringLength);
			if ((stringLength % 4) != 0){
				position(position() + 4 - (stringLength % 4));
			}
			return (new String(data)).trim();
		} catch (Exception e) {			
			return "";
		}
	}
	
	private String getStringLength(int length){
		try {
			byte[] data = new byte[length];
			buffer.get(data, 0, length);
			if ((length % 4) != 0)				
				this.position(this.position() + 4 - (length % 4));
			return (new String(data)).trim();
		} catch (Exception e) {			
			return "";
		}
	}

	@Override
	public void put(byte value) {
		resize(4);
		buffer.put(value);
		this.position(this.position() + 3);
	}

	@Override
	public void put(byte[] data) {
		int len=data.length;
		if((len%4) != 0){
				len= len + 4 - len % 4;
		}
		resize(len);
		buffer.put(data);
		if((data.length%4) != 0){
			position(position() + 4 - data.length % 4);
		}
	}

	@Override
	public void put(byte[] data, int offset, int length) {
		int len=length;
		if((len%4)!=0)
				len=len+4-len%4;
		resize(length);		
		buffer.put(data, offset, length);
		if((length%4)!=0)
			this.position(this.position()+4-length%4);
	}

	@Override
	public void putBoolean(boolean value) {
		if (value == true)
			this.putInt(1);
		else
			this.putInt(0);
	}

	@Override
	public void putChar(char value) {
		resize(4);
		buffer.putChar(value);
		buffer.position(this.position()+2);
	}

	@Override
	public void putFloat(float value) {
		resize(Float.SIZE / Byte.SIZE);
		buffer.putFloat(value);
	}

	@Override
	public void putInt(int value) {
		resize(Integer.SIZE / Byte.SIZE);
		buffer.putInt(value);
	}
	
	/**
	 * Insert int value at a specified index in the buffer
	 * @param index	index to put the value
	 * @param value	the int value to be inserted
	 */
	public void putInt(int index, int value) {
		resize(index, Integer.SIZE / Byte.SIZE);
		buffer.putInt(index, value);
	}

	@Override
	public void putDouble(double value) {
		resize(Double.SIZE / Byte.SIZE);
		buffer.putDouble(value);
	}

	@Override
	public void putLong(long value) {
		resize(Long.SIZE / Byte.SIZE);
		buffer.putLong(value);
	}
	
	/**
     * http://www.javacodegeeks.com/2010/11/java-best-practices-char-to-byte-and.html
     * Around 30% faster than String.getBytes()
     * @param str
     * @return
     */
    private static byte[] stringToBytesASCII(String str) {
    	byte[] b = new byte[str.length()];
        for (int i = 0; i < b.length; i++) {
            b[i] = (byte) str.charAt(i);
        }
        return b;
    }

	@Override
	public void putString(String data) {
		
		if (data != null && data.length() > 0) {
			int stringLength = data.length() + 1; //0 terminated
			byte[] datas = stringToBytesASCII(data);
			
			//Integrate putInt code so that resize is called only once
			resize(stringLength +(Integer.SIZE / Byte.SIZE));
			buffer.putInt(stringLength);
			
			buffer.put(datas);
			buffer.put((byte) 0);//0 terminated
			if ((stringLength % 4) != 0){
				position(position() + 4 - (stringLength % 4));
			}
		} else {
			putInt(0);
		}
	}

	@Override
	public void reset() {
		buffer.clear();
		size = MessageHeader.HeaderLength;
		this.position(size);
	}

	@Override
	public void resetToReceive() {
		buffer.clear();
		size = 0;
		this.position(size);
	}

	@Override
	public int getTranslatedInteger(byte[] value) {
		return value[3]<<24 | (value[2]&0xff)<<16 | (value[1]&0xff)<<8 | (value[0]&0xff);		
	}

	/**
	 * 
	 */
	public String toIntString() {
		int position = this.position();
		this.position(0);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < size() / 4; i++) {
			sb.append(this.getInt());
			sb.append('_');
		}
		this.position(position);
		return sb.toString();
	}

	/**
	 * 
	 */
	public String toCharString() {
		int position = this.position();
		this.position(0);
		StringBuilder sb = new StringBuilder();
		sb.append("Data in bytes:");
		for (int i = 0; i < size(); i++) {
			byte data = buffer.get();
			int byteValue = new Integer(data).intValue();
			if (byteValue < 0) {
				byteValue = data & 0x80;
				byteValue += data & 0x7F;
			}
			sb.append(byteValue);
			sb.append('_');
		}
		this.position(position);
		return sb.toString();
	}

	/**
	 * Get the current buffer's position
	 * @return	the buffer's position
	 */
	public int position() {
		return buffer.position();
	}

	/**
	 * Set the pointer to the index
	 * @param index	index to set the pointer
	 */
	public void position(int index) {
		if (index > size){
			size = index;
		}
		buffer.position(index);
	}

	/**
	 * Return the buffer's limit
	 * @return the limit of this buffer
	 */
	protected int limit() {
		return buffer.limit();
	}

	/**
	 * Resize the current buffer to store more data
	 * @param moreCapacity	The additional capacity to add on the current buffer
	 */
	public void resize(int moreCapacity) {
		size += moreCapacity;
		int position = this.position() + moreCapacity;
		int capacity = buffer.capacity();
		if (position > capacity * 4. / 5) {
			int newCapacity = (int)(position * 5./4);
			ByteBuffer tempBuffer = ByteBuffer.allocate(newCapacity);
			tempBuffer.order(buffer.order());
			tempBuffer.put(buffer.array(),0,buffer.position());
			buffer = tempBuffer;
		}
	}

	public void resize(int position, int moreCapacity) {
		position = position + moreCapacity;
		if (position > size){
			size = position;
		}
		int capacity = buffer.capacity();
		if (position > capacity / 2) {
			int newCapacity = position * 2;
			ByteBuffer tempBuffer = ByteBuffer.allocate(newCapacity);
			tempBuffer.order(buffer.order());
			tempBuffer.put(buffer);
			buffer = tempBuffer;
		}
	}

	

	@Override
	public void putBooleanArray(boolean[] value) {
		int[] transfert = new int[value.length];
		for (int i = 0; i < value.length; i++) {
			if(value[i])
				transfert[i]=1;
			else 
				transfert[i]=0;
		}
		this.putIntArray(transfert);
	}


	@Override
	public void putDoubleArray(double[] value) {
		int arrayLength = 0;
		if (value != null)
			arrayLength = value.length;
		this.putInt(arrayLength);
		if(arrayLength>0)
		{
			this.resize(arrayLength*Double.SIZE/Byte.SIZE);
			DoubleBuffer doubleBuffer=buffer.asDoubleBuffer();
			doubleBuffer.put(value);
			this.position(this.position()+arrayLength*Double.SIZE/Byte.SIZE);			
		}
	}

	@Override
	public void putFloatArray(float[] value) {
		int arrayLength = 0;
		if (value != null)
			arrayLength = value.length;
		this.putInt(arrayLength);
		if(arrayLength>0)
		{
			this.resize(arrayLength*Float.SIZE/Byte.SIZE);
			FloatBuffer floatBuffer=buffer.asFloatBuffer();
			floatBuffer.put(value);
			this.position(this.position()+arrayLength*Float.SIZE/Byte.SIZE);
		}
	}

	@Override
	public void putIntArray(int[] value) {
		int arrayLength = 0;
		if (value != null)
			arrayLength = value.length;
		this.putInt(arrayLength);
		if(arrayLength>0)
		{
			this.resize(arrayLength*Integer.SIZE/Byte.SIZE);
			IntBuffer intBuffer=buffer.asIntBuffer();
			intBuffer.put(value);
			this.position(this.position()+arrayLength*Integer.SIZE/Byte.SIZE);			
		}		
	}

	@Override
	public void putLongArray(long[] value) {
		int arrayLength = 0;
		if (value != null)
			arrayLength = value.length;
		this.putInt(arrayLength);
		if(arrayLength>0)
		{
			this.resize(arrayLength*Long.SIZE/Byte.SIZE);
			LongBuffer longBuffer=buffer.asLongBuffer();
			longBuffer.put(value);
			this.position(this.position()+arrayLength*Long.SIZE/Byte.SIZE);
		}		
	}


	@Override
	public byte get() {
		return buffer.get();
	}

	@Override
	public boolean[] getBooleanArray(int length) {
		int[] transfert = this.getIntArray(length);
		boolean[] ret = new boolean[transfert.length];
		for (int i = 0; i < transfert.length; i++) {
			if(transfert[i]==1)
				ret[i] = true;
			else
				ret[i] = false;
		}
		return ret;
	}

	@Override
	public byte[] getByteArray(int length) {
		byte[]result=new byte[length];
		buffer.get(result);
		if((length%4)!=0)
			buffer.position(this.position()+4-length%4);
		return result;
	}

	@Override
	public double[] getDoubleArray(int length) {
		DoubleBuffer doubleBuffer=buffer.asDoubleBuffer();
		double[]result=new double[length];
		doubleBuffer.get(result);		
		this.position(this.position()+length*Double.SIZE/Byte.SIZE);
		return result;
	}

	@Override
	public float[] getFloatArray(int length) {
		float[]result=new float[length];
		FloatBuffer floatBuffer=buffer.asFloatBuffer();
		floatBuffer.get(result);
		this.position(this.position()+length*Float.SIZE/Byte.SIZE);
		return result;
	}

	@Override
	public int[] getIntArray(int length) {
		int[]result=new int[length];
		IntBuffer intBuffer=buffer.asIntBuffer();
		intBuffer.get(result);
		this.position(this.position()+length*Integer.SIZE/Byte.SIZE);
		return result;
	}

	@Override
	public long[] getLongArray(int length) {
		long[]result=new long[length];
		LongBuffer longBuffer=buffer.asLongBuffer();
		longBuffer.get(result);
		this.position(this.position()+length*Long.SIZE/Byte.SIZE);
		return result;
	}

	@Override
	public void putByteArray(byte[] value) {
		this.putInt(value.length);
		this.put(value);
	}
	@Override
	public int packMessageHeader() {
		int index = 0;
		for (index = 0; index < 5; index++) {
			putInt(index * 4, 0); //0, 4, 8, 12
		}
		int type = messageHeader.getRequestType();
		//LogWriter.writeDebugInfo("Pack header "+size() +" "+position());
		putInt(0, size());
		
		putInt(4, type);
		switch (type) {
			case MessageHeader.Request:
				putInt(8, messageHeader.getClassId());
				putInt(12, messageHeader.getMethodId());
				putInt(16, messageHeader.getSenmatics());
				break;
			case MessageHeader.Exception:
				putInt(8, messageHeader.getExceptionCode());
				break;
			case MessageHeader.Response:
				break;
			default:
				break;
		}
		return 0;
	}

	@Override
	public short getShort() {
		return buffer.getShort();
	}

	@Override
	public void putShort(short value) {
		resize(Short.SIZE / Byte.SIZE);
		buffer.putShort(value);
	}

	@Override
	public short[] getShortArray(int length) {
		short[]result=new short[length];
		ShortBuffer shortBuffer=buffer.asShortBuffer();
		shortBuffer.get(result);
		this.position(this.position()+length*Short.SIZE/Byte.SIZE);
		return result;
	}

	@Override
	public void putShortArray(short[] value) {
		int arrayLength = 0;
		if (value != null)
			arrayLength = value.length;
		this.putInt(arrayLength);
		if(arrayLength>0)
		{
			this.resize(arrayLength*Short.SIZE/Byte.SIZE);
			ShortBuffer shortBuffer=buffer.asShortBuffer();
			shortBuffer.put(value);
			this.position(this.position()+arrayLength*Short.SIZE/Byte.SIZE);			
		}		
		
	}

	@Override
	public char[] getCharArray(int length) {
		String s = getStringLength(length);
		char[] ret = s.toCharArray();
		return ret;
//		try {
//			byte[] data = new byte[length];
//			char[] charData = new char[length];
//			buffer.get(data, 0, length);
//			if ((length % 4) != 0)				
//				this.position(this.position() + 4 - (length % 4));
//			String value = (new String(data)).trim();
//			value.getChars(0, length, charData, 0);
//			return charData;
//		} catch (Exception e) {			
//			return new char[length];
//		}
		/*char[] result = new char[length];
		CharBuffer charBuffer = buffer.asCharBuffer();
		charBuffer.get(result);
		this.position(this.position()+length*(Byte.SIZE*2)/Byte.SIZE);
		return result;*/
	}

	@Override
	public void putCharArray(char[] value) {
		String arrayAsString = new String(value);
		this.putString(arrayAsString);
		/*int arrayLength = 0;
		if (value != null)
			arrayLength = value.length;
		this.putInt(arrayLength);
		if(arrayLength>0)
		{
			this.resize(arrayLength*(Byte.SIZE*2)/Byte.SIZE);
			CharBuffer charBuffer = buffer.asCharBuffer();
			charBuffer.put(value);
			this.position(this.position()+arrayLength*(Byte.SIZE*2)/Byte.SIZE);			
		}	*/	
	}
}
