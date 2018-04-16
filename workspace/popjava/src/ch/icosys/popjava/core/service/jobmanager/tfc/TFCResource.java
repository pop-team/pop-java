package ch.icosys.popjava.core.service.jobmanager.tfc;

import java.util.Objects;

import ch.icosys.popjava.core.baseobject.POPAccessPoint;

/**
 * Describe a TFC resource
 * @author Davide Mazzoleni
 */
public class TFCResource {
	private final String objectName;
	private final POPAccessPoint accessPoint;
	private final String secret;

	/**
	 * Describe a TFC resource
	 * @param objectName the object type {@link Class#getName()}
	 * @param accessPoint where we can find the object
	 * @param secret secret to remove it if still alive
	 */
	public TFCResource(String objectName, POPAccessPoint accessPoint, String secret) {
		this.objectName = objectName;
		this.accessPoint = accessPoint;
		this.secret = secret;
	}

	public String getObjectName() {
		return objectName;
	}

	public POPAccessPoint getAccessPoint() {
		return accessPoint;
	}

	public String getSecret() {
		return secret;
	}
	
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 67 * hash + Objects.hashCode(this.objectName);
		hash = 67 * hash + Objects.hashCode(this.accessPoint.toString());
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final TFCResource other = (TFCResource) obj;
		if (!Objects.equals(this.objectName, other.objectName)) {
			return false;
		}
		if (!Objects.equals(this.accessPoint.toString(), other.accessPoint.toString())) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return String.format("%s@%s", objectName, accessPoint.toString());
	}
}
