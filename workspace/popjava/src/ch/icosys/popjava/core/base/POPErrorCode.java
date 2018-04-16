package ch.icosys.popjava.core.base;

/**
 * This class regroup all POP error code. They are the same as the ones defined in the POP-C++ implementation.
 */
public final class POPErrorCode {
	public static final int USER_DEFINE_ERROR = 10000;
	public static final int OBJECT_NO_RESOURCE = USER_DEFINE_ERROR + 1;
	public static final int OBJECT_BIND_FAIL = USER_DEFINE_ERROR + 2;
	public static final int OBJECT_MISMATCH_METHOD = USER_DEFINE_ERROR + 3;
	public static final int CODE_SERVICE_FAIL = USER_DEFINE_ERROR + 4;

	public static final int ALLOCATION_EXCEPTION = USER_DEFINE_ERROR + 5;
	public static final int OBJECT_EXECUTABLE_NOTFOUND = USER_DEFINE_ERROR + 6;

	public static final int POP_BUFFER_FORMAT = USER_DEFINE_ERROR + 7;

	public static final int POP_APPSERVICE_FAIL = USER_DEFINE_ERROR + 8;
	public static final int POP_JOBSERVICE_FAIL = 10009;
	public static final int POP_EXEC_FAIL = 10010;

	public static final int POP_BIND_BAD_REPLY = USER_DEFINE_ERROR + 11;

	public static final int POP_NO_PROTOCOL = USER_DEFINE_ERROR + 12;
	public static final int POP_NO_ENCODING = USER_DEFINE_ERROR + 13;

	public static final int REFLECT_INVOKE_EXCEPTION = USER_DEFINE_ERROR + 14;
	public static final int REFLECT_SERIALIZE_EXCEPTION = USER_DEFINE_ERROR + 15;
	public static final int REFLECT_METHOD_NOT_FOUND_EXCEPTION = USER_DEFINE_ERROR + 16;

	public static final int POP_BUFFER_NOT_AVAILABLE = USER_DEFINE_ERROR + 16;
	public static final int POP_COMBOX_NOT_AVAILABLE = USER_DEFINE_ERROR + 17;
	public static final int POP_ACCESSPOINT_NOT_AVAILABLE = USER_DEFINE_ERROR + 18;

	public static final int NOT_ALLOW_PUT_NULL_OBJECT_TP_BUFFER = USER_DEFINE_ERROR + 19;
	public static final int UNKNOWN_EXCEPTION = USER_DEFINE_ERROR + 20;
    public static final int METHOD_ANNOTATION_EXCEPTION = USER_DEFINE_ERROR + 21; 
	
	public static final int USER_DEFINE_LASTERROR = USER_DEFINE_ERROR + 22;

	/**
	 * Default constructor
	 */
	private POPErrorCode() {

	}

}
