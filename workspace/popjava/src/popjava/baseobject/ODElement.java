package popjava.baseobject;

import popjava.buffer.*;
/**
 * This class represents an ODElement for the object description. An ODElement is an element that has a required and a minimum value.
 * For example, the power required for an object is set trough an ODElement. The power must have a required and a minimum value.
 */
public class ODElement {

	private float requiredValue = -1;
	private float minValue = -1;

	/**
	 * Constructor a POPODElement, the require value and min value are 0
	 */
	public ODElement() {

	}

	/**
	 * Create a new ODElement with given values
	 * @param requiredValue	Required value for this OD element
	 * @param minValue		Minimum value for this OD element
	 */
	public ODElement(float un, float deux) {
		this.requiredValue = un;
		this.minValue = deux;
	}

	/**
	 * Serialize the ODElement into the buffer
	 * @param buffer	The buffer to serialize in
	 */
	public void serialize(Buffer buffer) {
		buffer.putFloat(requiredValue);
		buffer.putFloat(minValue);
	}

	/**
	 * Deserilize the ODElement from the buffer
	 * @param buffer	Buffer to deserialize from
	 * @return the ODElement deserilized
	 */
	public static ODElement deserialize(Buffer buffer) {
		return new ODElement(buffer.getFloat(), buffer.getFloat());
	}

	/**
	 * Set the required value for this ODElement
	 * @param requiredValue Required value
	 */
	public void setRequiredValue(float requiredValue) {
		this.requiredValue = requiredValue;
	}

	/**
	 * Set the minimum value of this element
	 * @param minValue
	 */
	public void setMinValue(float minValue) {
		this.minValue = minValue;
	}

	/**
	 * Get the required value of this ODElement
	 * @return	the required value
	 */
	public float getRequiredValue() {
		return requiredValue;
	}

	/**
	 * Get the minimum value of this ODElement
	 * @return	the minimum value
	 */
	public float getMinValue() {
		return minValue;
	}

	/**
	 * Set the values of the ODElement
	 * @param requiredValue	Required value
	 * @param minValue		Minimum value
	 */
	public void set(float requiredValue, float minValue) {
		this.requiredValue = requiredValue;
		this.minValue = minValue;
	}

	/**
	 * Set values with an ODElement
	 * @param od	The ODElement with new values
	 */
	public void set(ODElement od) {
		this.requiredValue = od.getRequiredValue();
		this.minValue = od.getMinValue();
	}

	/**
	 * Check if the current object is empty
	 * @return	true if the current object is empty
	 */
	public boolean isEmpty() {
		if (requiredValue < minValue) {
			requiredValue = 0;
			minValue = 0;
		}
		if (requiredValue <= 0 && minValue <= 0)
			return true;
		return false;
	}

	/**
	 * Format the ODElement as a string value
	 */
	public String toString() {
		if (isEmpty())
			return "";
		else {
			return String.format("Require:%f.Min:%f", requiredValue, minValue);
		}
	}
}