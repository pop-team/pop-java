package popjava.base;


/**
 * Message header is include in all communication between Interface and Broker side.
 */
public class MessageHeader {

	public static final int REQUEST = 0;
	public static final int RESPONSE = 1;
	public static final int EXCEPTION = 2;
	
	public static final int BIND_STATUS_CALL = 0;
	public static final int ADD_REF_CALL = 1;
	public static final int DEC_REF_CALL = 2;
	public static final int GET_ENCODING_CALL = 3;
	public static final int KILL_ALL = 4;
	public static final int OBJECT_ALIVE_CALL = 5;

	public static final int HEADER_LENGTH = 24;

	protected int requestType;
	protected int classId;
	protected int methodId;
	protected int semantics;
	protected int exceptionCode;
	protected int requestID = -1; //ID of the request. Must be unique for all concurrent requests from same source to same target. 

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
		requestType = REQUEST;
		exceptionCode = 0;
	}

	/**
	 * Initialize a new message header for sending a response
	 */
	public MessageHeader() {
		requestType = RESPONSE;
		exceptionCode = 0;
		classId = 0;
		methodId = 0;
		semantics = Semantic.SYNCHRONOUS;
	}

	/**
	 * Initialize a new message header for sending an exception
	 * @param exceptionCode	code of the exception to be sent
	 */
	public MessageHeader(int exceptionCode) {
		requestType = EXCEPTION;
		this.exceptionCode = exceptionCode;
		classId = 0;
		methodId = 0;
		semantics = Semantic.SYNCHRONOUS;
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
	
	public void setRequestID(int requestID){
		this.requestID = requestID;
	}
	
	public int getRequestID(){
		return requestID;
	}

	/**
	 * Format message header as a string value
	 */
	@Override
    public String toString() {
		return String.format("MessageHeaderInfo: RequestType=%s,ClassId=%d,MethodId=%d,Sematics=%d,ErrorCode=%d",
						getRequestTypeName(getRequestType()), getClassId(), getMethodId(), getSenmatics(), getExceptionCode());

	}

	private static String getRequestTypeName(int type){
	    switch(type){
	    case REQUEST:
	        return "Request";
	    case RESPONSE:
	        return "Response";
	    case EXCEPTION:
	        return "Exception";
	    }
	    
	    return "UNKOWN "+type;
	}
}
