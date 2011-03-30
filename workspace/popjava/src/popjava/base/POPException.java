package popjava.base;

import popjava.baseobject.POPAccessPoint;
import popjava.buffer.Buffer;
import popjava.dataswaper.IPOPBase;
/**
 * This class is the base implementation for all POP exception
 */
public class POPException extends Exception implements IPOPBase {

	private static final long serialVersionUID = 4815021357012126570L;
	/**
	 * Code of the error in the exception
	 */
	public int errorCode;
	/**
	 * Message associated with the exception
	 */
	public String errorMessage;

	/**
	 * Create a new POPException with the given value
	 * @param errorCode		Code of the error
	 * @param errorMessage	Assiociated message
	 */
	public POPException(int errorCode, String errorMessage) {
		super(errorMessage);
		this.errorMessage = errorMessage;
		this.errorCode = errorCode;
	}

	/**
	 * Create a new empty POPException
	 */
	public POPException() {
		this.errorMessage = "";
		this.errorCode = 0;
	}

	/**
	 * Method to throw a new exception : No resource found
	 * @throws POPException exception thrown by this method
	 */
	public static void throwObjectNoResource() throws POPException {
		throw new POPException(POPErrorCode.OBJECT_NO_RESOURCE,
				"Object No Resource");
	}

	/**
	 * Throw an exception when the object binding is not a success
	 * @param accessPoint	Access point of the object
	 * @throws POPException exception thrown by this method
	 */
	public static void throwObjectBindException(POPAccessPoint accessPoint)
			throws POPException {
		throw new POPException(POPErrorCode.OBJECT_BIND_FAIL,
				"Cannot bind to access point:" + accessPoint.toString());
	}

	/**
	 * Throw an exception when the buffer format is not correct
	 * @param c	Class 
	 * @throws POPException exception thrown by this method
	 */
	public static void throwBufferFormatException(Class<?> c)
			throws POPException {
		throw new POPException(POPErrorCode.POP_BUFFER_FORMAT, String.format(
				"Wrong buffer format. Cannot get the class %s from buffer", c
						.getName()));
	}

	/**
	 * Throw an exception when invoke a serialize method
	 * @throws POPException exception thrown by this method
	 */
	public static void throwReflectException(String methodName,
			String errorMessage) throws POPException {
		throw createReflectException(methodName, errorMessage);
	}

	/**
	 * Create an exception when invoke a serialize method
	 * @param methodName	Name of the method
	 * @param errorMessage	Message
	 * @return	the exception
	 */
	public static POPException createReflectException(String methodName,
			String errorMessage) {
		String message = String.format(
				"Exception when invoke method %s. More Information:%s",
				methodName, errorMessage);
		return new POPException(POPErrorCode.REFLECT_INVOKE_EXCEPTION, message);
	}

	/**
	 * Throw an exception when invoke a serialize method
	 * @throws POPException exception thrown by this method
	 */
	public static void throwReflectSerializeException(String className,
			String errorMessage) throws POPException {

		String message = String
				.format(
						"Exception when invoke method serialize(buffer) of class %s. More Information:%s",
						className, errorMessage);
		throw new POPException(POPErrorCode.REFLECT_SERIALIZE_EXCEPTION,
				message);
	}

	/**
	 * Throw an exception when method is not found
	 * @throws POPException exception thrown by this method
	 */
	public static void throwReflectMethodNotFoundException(String className,
			int methodId, String errorMessage) throws POPException {

		throw createReflectMethodNotFoundException(className, methodId,
				errorMessage);
	}

	/**
	 * Create an exception when method is not found
	 * @return	the exception
	 */
	public static POPException createReflectMethodNotFoundException(
			String className, int methodId, String errorMessage) {
		String message = String
				.format(
						"Cannot found the method id %d in class %s. More Information:%s",
						methodId, className, errorMessage);
		return new POPException(
				POPErrorCode.REFLECT_METHOD_NOT_FOUND_EXCEPTION, message);
	}

	/**
	 * Create an exception when the buffer is not available
	 * @return	the exception
	 */
	public static POPException createBufferNotAvailableException() {
		return new POPException(POPErrorCode.POP_BUFFER_NOT_AVAILABLE,
				"The buffer hasn't been initialized");
	}

	/**
	 * Throw an exception when the buffer is not available
	 * @return	the exception
	 * @throws POPException exception thrown by this method
	 */
	public static POPException throwBufferNotAvailableException()
			throws POPException {
		throw createBufferNotAvailableException();
	}

	/**
	 * Crate an exception when the combox is not available
	 * @return	the exception
	 */
	public static POPException createComboxNotAvailableException() {
		return new POPException(POPErrorCode.POP_COMBOX_NOT_AVAILABLE,
				"The combox hasn't been initialized");
	}
	

	/**
	 * Throw an exception when the combox is not available
	 * @return	the exception
	 * @throws POPException exception thrown by this method
	 */
	public static POPException throwComboxNotAvailableException()
			throws POPException {
		throw createComboxNotAvailableException();
	}

	/**
	 * Throw an exception when the creation of access point is not a success
	 * @return	the exception
	 */
	public static POPException createAccessPointNotAvailableException() {
		return new POPException(POPErrorCode.POP_ACCESSPOINT_NOT_AVAILABLE,
				"The accesspoint hasn't been initialized");
	}

	/**
	 * Throw an exception when the access point of an object is not available
	 * @return	the exception
	 * @throws POPException exception thrown by this method
	 */
	public static POPException throwAccessPointNotAvailableException()
			throws POPException {
		throw createAccessPointNotAvailableException();
	}

	/**
	 * Throw an exception when trying to create a null object
	 * @return	the exception
	 */
	public static POPException createNullObjectNotAllowException()
	{
		return new POPException(POPErrorCode.NOT_ALLOW_PUT_NULL_OBJECT_TP_BUFFER,
		"Not allow to put null object to buffer except a null array");
	}
	
	/**
	 * Throw an exception when trying to create a null object
	 * @return	the exception
	 * @throws POPException exception thrown by this method
	 */
	public static POPException throwNullObjectNotAllowException()
	throws POPException {
			throw createNullObjectNotAllowException();
	}
	
	/**
	 * Deserialize an exception from the buffer
	 * @param buffer	The buffer to deserialize from
	 */
	@Override
	public boolean deserialize(Buffer buffer) {
		this.errorCode = buffer.getInt();
		errorMessage = buffer.getString();
		return true;
	}

	/**
	 * Serialize an exception into the buffer
	 * @param buffer	The buffer to serialize in
	 */
	@Override
	public boolean serialize(Buffer buffer) {
		buffer.putInt(errorCode);
		buffer.putString(errorMessage);
		return true;
	}

}
