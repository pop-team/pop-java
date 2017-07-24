package popjava.baseobject;

/**
 * This enum contains all available connections
 * 
 * @author Davide Mazzoleni
 */
public enum ConnectionProtocol {
	ALL(""),
	SOCKET("socket"),
	SSL("ssl");
	
	private final String name;

	private ConnectionProtocol(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
