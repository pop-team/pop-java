package popjava.codemanager;

import popjava.baseobject.POPAccessPoint;
import popjava.dataswaper.POPString;

public interface AppService {
	
	void registerCode(String objname, String platform, String codefile);
	
	int queryCode(String objname, String platform, POPString codefile);
	
	int getPlatform(String objname, POPString platform);
	
	POPAccessPoint getAccessPoint();
	
	String getPOPCAppID();
	
	void exit();
}
