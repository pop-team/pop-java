package ch.icosys.popjava.core.codemanager;

import ch.icosys.popjava.core.baseobject.POPAccessPoint;
import ch.icosys.popjava.core.dataswaper.POPString;

public interface AppService {

	void registerCode(String objname, String platform, String codefile);

	int queryCode(String objname, String platform, POPString codefile);

	int getPlatform(String objname, POPString platform);

	POPAccessPoint getAccessPoint();

	String getPOPCAppID();

	void exit();
}
