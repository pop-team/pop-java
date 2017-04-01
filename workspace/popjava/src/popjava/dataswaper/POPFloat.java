package popjava.dataswaper;

import popjava.buffer.POPBuffer;
/**
 * Primitive settable float for POPJava, needed for {@link popjava.annotation.POPParameter} direction use in methods 
 * @author Davide Mazzoleni
 */
public class POPFloat implements IPOPBase {
	/**
	 * float value stored in this object
	 */
	private float value;

	/**
	 * Default constructor
	 */
	public POPFloat() {
		value = 0;
	}

	/**
	 * Constructor with given value
	 * @param value	float value to be stored in this object
	 */
	public POPFloat(float value) {
		this.value = value;
	}
	
	/**
	 * Set the float value of this object
	 * @param value	new float value
	 */
	public void setValue(float value)
	{
		this.value=value;
	}

	/**
	 * Get the current value of this object
	 * @return	current float value
	 */
	public float getValue() {
		return value;
	}

	/**
	 * Serialize the POPFloat into the buffer
	 */
	@Override
	public boolean serialize(POPBuffer buffer) {
		buffer.putFloat(value);
		return false;
	}

	/**
	 * Deserialize the POPFloat from the buffer
	 */
	@Override
	public boolean deserialize(POPBuffer buffer) {
		value = buffer.getFloat();
		return false;
	}
	
	@Override
	public String toString(){
		return String.valueOf(value);
	}
}
