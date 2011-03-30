package popjava.baseobject;

/**
 * This class defined a POPReference. As parallel object are not executed on the same machine, the reference of a parallel object is its access points.
 */
public class POPReference {
	/**
	 * Object containing the access points
	 */
	private POPAccessPoint ap;
	
	/**
	 * Create a new POPReference
	 */
	public POPReference(){
		setAp(new POPAccessPoint());
	}
	
	/**
	 * Set the access points
	 * @param ap	Access points to be set
	 */
	public void setAccessPoint(POPAccessPoint ap){
		this.setAp(ap);
	}
	
	/**
	 * 
	 */
	public void getReferenceForPOPCInteraction(){
		
	}

	public void setAp(POPAccessPoint ap) {
		this.ap = ap;
	}

	public POPAccessPoint getAp() {
		return ap;
	}
}
