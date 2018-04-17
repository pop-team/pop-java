package ch.icosys.popjava.core;

import ch.icosys.popjava.core.base.POPObject;
import ch.icosys.popjava.core.baseobject.POPTracking;
import ch.icosys.popjava.core.util.POPRemoteCaller;

/**
 * Accounting API for POP Objects.
 * 
 * @author Davide Mazzoleni
 */
public class POPAccounting {

	/**
	 * Cast an object to a POP one, throw an IllegalArgumentException if it isn't
	 * one.
	 * 
	 * @param obj
	 *            the object to cast
	 * @return the POPObject instance
	 */
	private static POPObject cast(Object obj) {
		if (obj instanceof POPObject) {
			return (POPObject) obj;
		}
		throw new IllegalArgumentException("Given object is not a POPObject.");
	}

	/**
	 * Check if a POP Object has tracking and accounting capabilities enabled.
	 * 
	 * @param popObject
	 *            The object we want to check.
	 * @return true if it's enabled, false otherwise
	 */
	public static boolean isEnabledFor(Object popObject) {
		POPObject obj = cast(popObject);
		return isEnabledFor(obj);
	}

	/**
	 * For internal calls, skip casting.
	 * 
	 * @param obj
	 *            The object we want information about.
	 * @return true if it's enabled, false otherwise
	 */
	private static boolean isEnabledFor(POPObject obj) {
		return obj.isTracking();
	}

	/**
	 * Return to the owner of the object (the same machine where the object reside)
	 * information on who contacted it.
	 * 
	 * @param popObject
	 *            The object we want information about.
	 * @return all the user which used this object
	 */
	public static POPRemoteCaller[] getUsers(Object popObject) {
		POPObject obj = cast(popObject);
		if (!isEnabledFor(obj)) {
			throw new IllegalStateException("Tracking is not enabled on this POP Object.");
		}
		return obj.getTrackedUsers();
	}

	/**
	 * Get information on a specific caller.
	 * 
	 * @param popObject
	 *            The object we want information about.
	 * @param caller
	 *            One of the caller return by {@link #getUsers(java.lang.Object) }
	 * @return A tracking object or null.
	 * @throws IllegalArgumentException
	 *             if tracking is not enabled
	 */
	public static POPTracking getInformation(Object popObject, POPRemoteCaller caller) {
		POPObject obj = cast(popObject);
		if (!isEnabledFor(obj)) {
			throw new IllegalStateException("Tracking is not enabled on this POP Object.");
		}
		return obj.getTracked(caller);
	}

	/**
	 * Get information on a specific what was tracked about me.
	 * 
	 * @param popObject
	 *            The object we want information about.
	 * @return the tracking object
	 * @throws IllegalArgumentException
	 *             if tracking is not enabled
	 */
	public static POPTracking getMyInformation(Object popObject) {
		POPObject obj = cast(popObject);
		if (!isEnabledFor(obj)) {
			throw new IllegalStateException("Tracking is not enabled on this POP Object.");
		}
		return obj.getTracked();
	}
}
