package popjava.dataswaper;

import popjava.buffer.POPBuffer;
/**
 * Primitive settable byte for POPJava, needed for {@link popjava.annotation.POPParameter} direction use in methods 
 * @author Davide Mazzoleni
 */
public class POPMutableByte implements IPOPBase {
	/**
	 * byte value stored in this object
	 */
	private byte value;

	/**
	 * Default constructor
	 */
	public POPMutableByte() {
		value = 0;
	}

	/**
	 * Constructor with given value
	 * @param value	byte value to be stored in this object
	 */
	public POPMutableByte(byte value) {
		this.value = value;
	}
	
	/**
	 * Set the byte value of this object
	 * @param value	new byte value
	 */
	public void setValue(byte value)
	{
		this.value=value;
	}
	
	/**
	 * Set the byte value of this object, from an int
	 * @param value	new byte value from int
	 */
	public void setValue(int value) {
		this.value=(byte)value;
	}

	/**
	 * Get the current value of this object
	 * @return	current byte value
	 */
	public byte getValue() {
		return value;
	}

	/**
	 * Serialize the POPByte into the buffer
	 */
	@Override
	public boolean serialize(POPBuffer buffer) {
		buffer.put(value);
		return false;
	}

	/**
	 * Deserialize the POPByte from the buffer
	 */
	@Override
	public boolean deserialize(POPBuffer buffer) {
		value = buffer.get();
		return false;
	}
	
	@Override
	public String toString(){
		return String.valueOf(value);
	}
}
