package popjava.dataswaper;

import popjava.buffer.POPBuffer;
/**
 * Primitive settable int for POPJava, needed for {@link popjava.annotation.POPParameter} direction use in methods 
 * @author Davide Mazzoleni
 */
public class POPMutableInt implements IPOPBase {
	/**
	 * int value stored in this object
	 */
	private int value;

	/**
	 * Default constructor
	 */
	public POPMutableInt() {
		value = 0;
	}

	/**
	 * Constructor with given value
	 * @param value	int value to be stored in this object
	 */
	public POPMutableInt(int value) {
		this.value = value;
	}
	
	/**
	 * Set the int value of this object
	 * @param value	new int value
	 */
	public void setValue(int value)
	{
		this.value=value;
	}

	/**
	 * Get the current value of this object
	 * @return	current int value
	 */
	public int getValue() {
		return value;
	}

	/**
	 * Serialize the POPInt into the buffer
	 */
	@Override
	public boolean serialize(POPBuffer buffer) {
		buffer.putInt(value);
		return false;
	}

	/**
	 * Deserialize the POPInt from the buffer
	 */
	@Override
	public boolean deserialize(POPBuffer buffer) {
		value = buffer.getInt();
		return false;
	}
	
	@Override
	public String toString(){
		return String.valueOf(value);
	}
}
