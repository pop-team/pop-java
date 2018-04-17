package ch.icosys.popjava.core.annotation;

/**
 * Enum that lists all possible encodings that can be used
 * 
 * @author Beat Wolf
 *
 */
public enum Encoding {

	Default(""), Raw("raw"), XDR("xdr");

	private String name;

	Encoding(String name) {
		this.name = name;
	}

	public String toString() {
		return name;
	}
}
