package popjava.service.jobmanager;

import popjava.annotation.POPClass;
import popjava.annotation.POPConfig;
import popjava.annotation.POPConfig.Type;
import popjava.annotation.POPParameter.Direction;
import popjava.baseobject.ObjectDescription;
import popjava.baseobject.POPAccessPoint;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPParameter;
import popjava.dataswaper.POPString;
import popjava.serviceadapter.POPJobManager;

@POPClass(classId = 10, deconstructor = false, useAsyncConstructor = false)
public class POPJavaJobManager extends POPJobManager{
	
	

	@POPObjectDescription(url = "localhost")
	public POPJavaJobManager() {
		// TODO call other constructor
	}
	
	public POPJavaJobManager(@POPConfig(Type.URL) String url) {
		// init object, read config files
	}
	
	@Override
	public int createObject(POPAccessPoint localservice,
			String objname,
			@POPParameter(Direction.IN) ObjectDescription od,
			int howmany, POPAccessPoint[] objcontacts,
			int howmany2, POPAccessPoint[] remotejobcontacts) {
		
		
		return 0;
	}

	@Override
	public int execObj(POPString objname, int howmany, int[] reserveIDs, String localservice, POPAccessPoint[] objcontacts) {
		return super.execObj(objname, howmany, reserveIDs, localservice, objcontacts);
	}

	// TODO add Reserve signature in super class
	
	@Override
	public void cancelReservation(int[] req, int howmany) {
		super.cancelReservation(req, howmany);
	}

	@Override
	public void dump() {
		super.dump();
	}

	@Override
	public void start() {
		super.start();
	}

	@Override
	public int query(POPString type, POPString value) {
		return super.query(type, value);
	}

	@Override
	public void selfRegister() {
		super.selfRegister();
	}

	@Override
	public void registerNode(String url) {
		super.registerNode(url);
	}

	// TODO add unregisterNode signature in super class
}
