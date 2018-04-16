package popjava.base;

/**
 *	This class represent the different bind status that a connection between a interface and a broker can have.
 */
public class BindStatus {
	public static final int BIND_OK = 0;
	public static final int BIND_FORWARD_SESSION = 1;
	public static final int BIND_FORWARD_PERMANENT = 2;
	public static final int BIND_ALLOC_RETRY = 3;

	protected int code;
	protected String platform;
	protected String info;

	/**
	 *  Creates a new instance of BindStatus 
	 */
	public BindStatus() {
	}

	/**
	 * Get the code associated with this bind status
	 * @return	the associated code
	 */
	public int getCode() {
		return code;
	}

	/**
	 * Associate a platform with this bind status
	 * @param code the associated code
	 */
	public void setCode(int code) {
		this.code = code;
	}

	/**
	 * Get the platform associated with this bind status
	 * @return	the associated platform
	 */
	public String getPlatform() {
		return platform;
	}

	/**
	 * Associate a platform with this bind status
	 * @param platform string value of the platform
	 */
	public void setPlatform(String platform) {
		this.platform = platform;
	}

	/**
	 * Get informations of this bind status
	 * @return return informations as a string value
	 */
	public String getInfo() {
		return info;
	}

	/**
	 * Set informations to this bind status
	 * @param info	The informations to set as a string value
	 */
	public void setInfo(String info) {
		this.info = info;
	}

}
