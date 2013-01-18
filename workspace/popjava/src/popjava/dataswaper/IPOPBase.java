package popjava.dataswaper;

import popjava.buffer.POPBuffer;
/**
 * This interface declare the needed method for the serialization and the deserialization of an object
 *
 */
public interface IPOPBase {

	/**
	 * Serialize an object into the buffer
	 * @param buffer	The buffer to serialize in
	 * @return	true if the serialization process succeed
	 */
	boolean serialize(POPBuffer buffer);

	/**
	 * Deserialize an object from the buffer
	 * @param buffer	The buffer to deserialize from
	 * @return	true if the deserialization process succeed
	 */
	boolean deserialize(POPBuffer buffer);

}