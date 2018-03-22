package popjava.baseobject;

import java.util.ArrayList;

import popjava.buffer.POPBuffer;
import popjava.dataswaper.IPOPBase;
import popjava.system.POPSystem;

/**
 * This class represents multiple access to the broker-side parallel object
 */

public class POPAccessPoint implements IPOPBase {
	
	private boolean isService;
	private boolean noaddref = false;
	private int security;
	private String fingerprint = null;
	private byte[] x509certificate = null;

	/**
	 * The list of the different access points
	 */
	protected final ArrayList<AccessPoint> accessPoints = new ArrayList<>();

	/**
	 * Create a new POPAccessPoint()
	 */
	public POPAccessPoint() {

	}

	/**
	 * Create a new POPAccessPoint an make some initialization tasks
	 * @param initialize Set to false if you don't want the initialization
	 */
	public POPAccessPoint(boolean initialize) {
		if (initialize) {
			accessPoints.addAll(POPSystem.getDefaultAccessPoint().accessPoints);
		}
	}

	/**
	 * Create a new POPAccessPoint with a formatted string
	 * @param accessString Formatted string to create the POPAccessPoint
	 */
	public POPAccessPoint(String accessString) {
		setAccessString(accessString);
	}

	/**
	 * Serialize the object into the buffer to be sent over the network
	 */
	@Override
    public boolean serialize(POPBuffer buffer) {
		buffer.putString(toString());
		buffer.putInt(security);
		buffer.putBoolean(isService);
		buffer.putBoolean(noaddref);
		// serialize fingerprint if necessary
		if (fingerprint != null) {
			buffer.putBoolean(true);
			buffer.putString(fingerprint);
		} else {
			buffer.putBoolean(false);
		}
		// serialize certificate if necessary
		if (x509certificate != null) {
			buffer.putBoolean(true);
			buffer.putArray(x509certificate);
		} else {
			buffer.putBoolean(false);
		}
		return true;
	}

	/**
	 * Deserialize the object from the buffer received from the network
	 */
	@Override
    public boolean deserialize(POPBuffer buffer) {
		String accessPoint = buffer.getString();
		setAccessString(accessPoint);
		security = buffer.getInt();
		isService = buffer.getBoolean();
		noaddref = buffer.getBoolean();
		if (buffer.getBoolean()) {
			fingerprint = buffer.getString();
		}
		if (buffer.getBoolean()) {
			int size = buffer.getInt();
			x509certificate = buffer.getByteArray(size);
		}
		return true;
	}

	public String getFingerprint() {
		return fingerprint;
	}

	public void setFingerprint(String fingerprint) {
		this.fingerprint = fingerprint;
	}

	public byte[] getX509certificate() {
		return x509certificate;
	}

	public void setX509certificate(byte[] x509certificate) {
		this.x509certificate = x509certificate;
	}

	/**
	 * Add an access point to the POPAccessPoint
	 * @param accessPoint New access point to be added
	 */
	public void addAccessPoint(AccessPoint accessPoint) {
		if (!accessPoints.contains(accessPoint)) {
			accessPoints.add(accessPoint);
		}
	}

	/**
	 * Check if the current object is empty
	 * @return true is the current object is not set
	 */
	public boolean isEmpty() {
		return accessPoints.isEmpty();
	}

	/**
	 * Format the POPAccessPoint to a string value
	 */
	@Override
    public String toString() {
		StringBuilder accessString = new StringBuilder();
		for (AccessPoint accessPoint : accessPoints) {
			accessString.append(accessPoint.toString()).append(" ");
		}
		accessString = new StringBuilder(accessString.toString().trim());
		return accessString.toString();
	}

	/**
	 * Add an access point by a formatted string
	 * @param accessString Formatted string to be added as an access point
	 */
	public void setAccessString(String accessString) {
		accessPoints.clear();
		String[] accessStrings = accessString.split("[ \t\r\n]");
		
		for (String str : accessStrings) {
			str = str.trim();
			if (str.length() > 0) {
				AccessPoint acessPoint = AccessPoint.create(str);
				if (acessPoint != null){
					accessPoints.add(acessPoint);
				}
			}
		}
	}

	/**
	 * Get the number of different access points
	 * @return Number of access points
	 */
	public int size() {
		return accessPoints.size();
	}

	/**
	 * Get the access point at specified index
	 * @param index	index of the access point to return
	 * @return the access points at the specified index 
	 */
	public AccessPoint get(int index) {
		return accessPoints.get(index);
	}
	
	public boolean hasSameAccessPoint(POPAccessPoint ap) {
	    if(ap == null) {
	        return false;
	    }
	    
		for(int i = 0; i < ap.size(); i++) {
			if(accessPoints.contains(ap.get(i))) {
				return true;
			}
		}
		
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((accessPoints == null) ? 0 : accessPoints.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		POPAccessPoint other = (POPAccessPoint) obj;
		if (accessPoints == null) {
			if (other.accessPoints != null)
				return false;
		} else if (!accessPoints.equals(other.accessPoints))
			return false;
		return true;
	}


}
