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

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getReqId() {
		return reqId;
	}

	public void setReqId(String reqId) {
		this.reqId = reqId;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public POPAccessPoint getContact() {
		return contact;
	}

	public void setContact(POPAccessPoint contact) {
		this.contact = contact;
	}

	public POPAccessPoint getAppService() {
		return appService;
	}

	public void setAppService(POPAccessPoint appService) {
		this.appService = appService;
	}
	
	
}
