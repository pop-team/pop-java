package testsuite.demo;

import popjava.PopJava;
import popjava.annotation.POPAsyncSeq;
import popjava.annotation.POPClass;
import popjava.annotation.POPConfig;
import popjava.annotation.POPConfig.Type;
import popjava.annotation.POPSyncConc;
import popjava.annotation.POPSyncMutex;

@POPClass
public class Demopop{
	
	int iD = 11;
	
	public Demopop(){
	}
	
	public Demopop(int newID, int wanted, int minp){
	    //TODO: Implement power annotation
		//od.setPower(wanted, minp);
//		printf("POPCobject with ID=%d created (by JobMgr) on machine:%s\n", newID, (const char*)POPSystem::GetHost());
		iD = newID;
		System.out.println("Demopop with ID="+iD+" created (byJobMgr) on machine:"+PopJava.getAccessPoint(this).toString());
	}

	
	public Demopop(int newID, @POPConfig(Type.URL) String machine){
//		printf("POPCobject with ID=%d created on machine:%s\n", newID, (const char*)POPSystem::GetHost());
		iD = newID;
		System.out.println("Demopop with ID="+iD+" created on machine:"+PopJava.getAccessPoint(this).toString());
	}

	@POPAsyncSeq	
	public void sendIDto(Demopop o)
	{
//		printf("POPCobject:%d on machine:%s is sending his iD to object:%d\n", iD, (const char*)POPSystem::GetHost(), o.getID());
		o.recAnID(iD);
	}

	@POPSyncConc
	public int getID()	{
		return iD;
	}

	@POPSyncConc
	public void recAnID(int i)
	{
//		printf("POPCobject:%d on machine:%s is receiving id = %d\n", iD, (const char*)POPSystem::GetHost(), i);
	}

	@POPSyncMutex
	public void wait(int sec)
	{
//		printf("POPCobject:%d on machine:%s is waiting %d sec.\n", iD, (const char*)POPSystem::GetHost(), sec);
		try {
			Thread.sleep(sec*1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
