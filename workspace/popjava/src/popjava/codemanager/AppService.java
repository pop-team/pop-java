package popjava.codemanager;

import popjava.baseobject.POPAccessPoint;
import popjava.dataswaper.POPString;

public interface AppService {
	

	public void registerCode(String objname, String platform, String codefile);
	
	public int queryCode(String objname, String platform, POPString codefile);
	
	public int getPlatform(String objname, POPString platform);
	
	public POPAccessPoint getAccessPoint();
	
	public String getPOPCAppID();
	
	public void exit();
}
