package popjava.baseobject;

/**
 * This enum contains all available connections
 * 
 * @author Davide Mazzoleni
 */
public enum ConnectionProtocol {
	ALL("", false),
	SOCKET("socket", false),
	SSL("ssl", true);
	
	private final String name;
	private final boolean secure;

	ConnectionProtocol(String name, boolean secure) {
		this.name = name;
		this.secure = secure;
	}

	public String getName() {
		return name;
	}

	public boolean isSecure() {
		return secure;
	}
}
