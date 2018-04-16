package popjava.dataswaper;

import popjava.buffer.POPBuffer;
/**
 * Primitive settable char for POPJava, needed for {@link popjava.annotation.POPParameter} direction use in methods 
 * @author Davide Mazzoleni
 */
public class POPMutableChar implements IPOPBase {
	/**
	 * char value stored in this object
	 */
	private char value;

	/**
	 * Default constructor
	 */
	public POPMutableChar() {
		value = 0;
	}

	/**
	 * Constructor with given value
	 * @param value	char value to be stored in this object
	 */
	public POPMutableChar(char value) {
		this.value = value;
	}
	
	/**
	 * Set the char value of this object
	 * @param value	new char value
	 */
	public void setValue(char value)
	{
		this.value=value;
	}

	/**
	 * Get the current value of this object
	 * @return	current char value
	 */
	public char getValue() {
		return value;
	}

	/**
	 * Serialize the POPChar into the buffer
	 */
	@Override
	public boolean serialize(POPBuffer buffer) {
		buffer.putChar(value);
		return false;
	}

	/**
	 * Deserialize the POPChar from the buffer
	 */
	@Override
	public boolean deserialize(POPBuffer buffer) {
		value = buffer.getChar();
		return false;
	}
	
	@Override
	public String toString(){
		return String.valueOf(value);
	}
}
