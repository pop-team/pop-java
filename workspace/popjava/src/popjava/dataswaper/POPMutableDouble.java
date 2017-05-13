package popjava.dataswaper;

import popjava.buffer.POPBuffer;
/**
 * Primitive settable double for POPJava, needed for {@link popjava.annotation.POPParameter} direction use in methods 
 * @author Davide Mazzoleni
 */
public class POPMutableDouble implements IPOPBase {
	/**
	 * double value stored in this object
	 */
	private double value;

	/**
	 * Default constructor
	 */
	public POPMutableDouble() {
		value = 0;
	}

	/**
	 * Constructor with given value
	 * @param value	double value to be stored in this object
	 */
	public POPMutableDouble(double value) {
		this.value = value;
	}
	
	/**
	 * Set the double value of this object
	 * @param value	new double value
	 */
	public void setValue(double value)
	{
		this.value=value;
	}

	/**
	 * Get the current value of this object
	 * @return	current double value
	 */
	public double getValue() {
		return value;
	}

	/**
	 * Serialize the POPDouble into the buffer
	 */
	@Override
	public boolean serialize(POPBuffer buffer) {
		buffer.putDouble(value);
		return false;
	}

	/**
	 * Deserialize the POPDouble from the buffer
	 */
	@Override
	public boolean deserialize(POPBuffer buffer) {
		value = buffer.getDouble();
		return false;
	}
	
	@Override
	public String toString(){
		return String.valueOf(value);
	}
}
