package popjava.baseobject;

import java.util.ArrayList;

import popjava.buffer.POPBuffer;
import popjava.combox.ComboxSocketFactory;
import popjava.dataswaper.IPOPBase;
import popjava.system.POPSystem;

/**
 * This class represents multiple access to the broker-side parallel object
 */

public class POPAccessPoint implements IPOPBase {
	
	private boolean isService;
	private boolean noaddref = false;
	private int security;	

	/**
	 * The list of the different access points
	 */
	protected ArrayList<AccessPoint> accessPoints = new ArrayList<AccessPoint>();

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
			String localAddress = POPSystem.getHostIP();

			String accessString = String.format("socket://%s://%s:0", ComboxSocketFactory.PROTOCOL, localAddress);
			setAccessString(accessString);
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
		return true;
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
		String accessString = "";
		for (int index = 0; index < accessPoints.size(); index++) {
			accessString += accessPoints.get(index).toString() + " ";
		}
		accessString = accessString.trim();
		return accessString;
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

}
