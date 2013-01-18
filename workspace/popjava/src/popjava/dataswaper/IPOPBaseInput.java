package popjava.dataswaper;

import popjava.buffer.POPBuffer;

/**
 * This type is used for communicate with the pop-c++ only. It is compatible with the in type
 * Be careful when use this type
 */

public interface IPOPBaseInput {
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
