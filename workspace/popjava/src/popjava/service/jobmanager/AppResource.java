package popjava.service.jobmanager;

import popjava.baseobject.POPAccessPoint;

/**
 * This is an Application/Object own resource
 * @author Davide Mazzoleni
 */
public class AppResource extends Resource {
	protected String appId;
	protected String reqId;
	protected long startTime; // NOTE may be not needed, used to recheck object liveness
	protected POPAccessPoint contact;
	protected POPAccessPoint appService;
}
