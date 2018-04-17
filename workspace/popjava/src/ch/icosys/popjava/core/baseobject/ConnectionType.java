package ch.icosys.popjava.core.baseobject;

/**
 * Connection type to be used if the object has to be created on a remote
 * machine
 * 
 * @author Beat Wolf
 *
 */
public enum ConnectionType {
	SSH("SSH"), DAEMON("POP-Java daemon"), ANY("Any");

	ConnectionType(String name) {
		this.name = name;
	}

	private final String name;

	@Override
	public String toString() {
		return name;
	}
}
