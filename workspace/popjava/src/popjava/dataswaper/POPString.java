package popjava.dataswaper;

import popjava.buffer.POPBuffer;
/**
 * Compatible with the POP-C++ paroc_string implementation
 */
public class POPString implements IPOPBase {
	/**
	 * String value stored in this object
	 */
	private String value;

	/**
	 * Default constructor
	 */
	public POPString() {
		value = "";
	}

	/**
	 * Constructor with given value
	 * @param value	String value to be stored in this object
	 */
	public POPString(String value) {
		this.value = value;
	}
	
	/**
	 * Set the string value of this object
	 * @param value	new string value
	 */
	public void setValue(String value)
	{
		this.value=value;
	}

	/**
	 * Get the current value of this object
	 * @return	current string value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Serialize the POPString into the buffer
	 */
	@Override
	public boolean serialize(POPBuffer buffer) {
		buffer.putString(value);
		return false;
	}

	/**
	 * Deserilize the POPString from the buffer
	 */
	@Override
	public boolean deserialize(POPBuffer buffer) {
		value = buffer.getString();
		return false;
	}
}
