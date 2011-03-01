package popjava.base;
/**
 * Message header is include in all communication between Interface and Broker side.
 */
public class MessageHeader {

	static public final int Request = 0;
	static public final int Response = 1;
	static public final int Exception = 2;

	static public final int BindStatusCall = 0;
	static public final int AddRefCall = 1;
	static public final int DecRefCall = 2;
	static public final int GetEncodingCall = 3;
	static public final int KillCall = 4;
	static public final int ObjectAliveCall = 5;

	static public final int HeaderLength = 20;

	protected int requestType;
	protected int classId;
	protected int methodId;
	protected int semantics;
	protected int exceptionCode;

	/**
	 * Initialize a new message header with parameters
	 * @param classId	Identifier of the parallel class
	 * @param methodId	Identifier of the method in the parallel class
	 * @param semantics	Invocation semantic of the method in the parallel class
	 */
	public MessageHeader(int classId, int methodId, int semantics) {
		this.classId = classId;
		this.methodId = methodId;
		this.semantics = semantics;
		requestType = Request;
		exceptionCode = 0;
	}

	/**
	 * Initialize a new message header for sending a response
	 */
	public MessageHeader() {
		requestType = Response;
		exceptionCode = 0;
		classId = 0;
		methodId = 0;
		semantics = Semantic.Synchronous;
	}

	/**
	 * Initialize a new message header for sending an exception
	 * @param exceptionCode	code of the exception to be sent
	 */
	public MessageHeader(int exceptionCode) {
		requestType = Exception;
		this.exceptionCode = exceptionCode;
		classId = 0;
		methodId = 0;
		semantics = Semantic.Synchronous;
	}

	/**
	 * Get the request type 
	 * @return	The request type stored in the message header
	 */
	public int getRequestType() {
		return requestType;
	}

	/**
	 * Set the request type in the header message. Request type can be Request, Response or Exception
	 * @param requestType	type of the request
	 */
	public void setRequestType(int requestType) {
		this.requestType = requestType;
	}

	/**
	 * Get the class identifier stored in this message header
	 * @return	The class identifier
	 */
	public int getClassId() {
		return classId;
	}

	/**
	 * Set the class identifier in the message header
	 * @param classId 	The class identifier to be set
	 */
	public void setClassId(int classId) {
		this.classId = classId;
	}

	/**
	 * Get the method identifier set in this message header
	 * @return	the method identifier
	 */
	public int getMethodId() {
		return methodId;
	}

	/**
	 * Set the method identifier in the message header
	 * @param methodId The method identifier to be set
	 */
	public void setMethodId(int methodId) {
		this.methodId = methodId;
	}

	/**
	 * Get the semantics stored in this message header
	 * @return	The semantic stored in the message header
	 */
	public int getSenmatics() {
		return semantics;
	}

	/**
	 * Set the semantic in the message header
	 * @param senmatics	Semantic to be set
	 */
	public void setSenmatics(int senmatics) {
		this.semantics = senmatics;
	}

	/**
	 * Get the exception code stored in this message header
	 * @return	The exception code stored in the message header
	 */
	public int getExceptionCode() {
		return exceptionCode;
	}

	/**
	 * Set the exception code in this message header
	 * @param exceptionCode
	 */
	public void setExceptionCode(int exceptionCode) {
		this.exceptionCode = exceptionCode;
	}

	/**
	 * Format message header as a string value
	 */
	public String toString() {
		String info = String
				.format(
						"MessageHeaderInfo: RequestType=%d,ClassId=%d,MethodId=%d,Senmatics=%d,ErrorCode=%d",
						this.getRequestType(), this.getClassId(), this
								.getMethodId(), this.getSenmatics(), this
								.getExceptionCode());
		return info;

	}

}
