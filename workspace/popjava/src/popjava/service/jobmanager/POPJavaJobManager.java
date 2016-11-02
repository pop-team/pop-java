package popjava.service.jobmanager;

import java.util.ArrayList;
import java.util.List;

import popjava.PopJava;
import popjava.annotation.POPClass;
import popjava.annotation.POPConfig;
import popjava.annotation.POPConfig.Type;
import popjava.annotation.POPParameter.Direction;
import popjava.baseobject.ObjectDescription;
import popjava.baseobject.POPAccessPoint;
import popjava.interfacebase.Interface;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPParameter;
import popjava.serviceadapter.POPJobService;

@POPClass(classId = 10, deconstructor = false, useAsyncConstructor = false)
public class POPJavaJobManager extends POPJobService{
	
	private static class RemoteNodes{
		private final String url;
		
		public RemoteNodes(String url){
			this.url = url;
		}
		
		public String getUrl(){
			return url;
		}
	}
	
	private List<RemoteNodes> remotes = new ArrayList<RemoteNodes>();

	@POPObjectDescription(url = "localhost")
	public POPJavaJobManager(){
		initNodes();
	}
	
	public POPJavaJobManager(@POPConfig(Type.URL) String url){
		initNodes();
	}
	
	private void initNodes(){
		remotes.add(new RemoteNodes("localhost"));
	}
	
	private String getFreeNode(){
		if(remotes.size() == 0){
			return "";
		}
		return remotes.get(0).getUrl();
	}
	
	public int createObject(POPAccessPoint localservice,
			String objname,
			@POPParameter(Direction.IN) ObjectDescription od,
			int howmany, POPAccessPoint[] objcontacts,
			int howmany2, POPAccessPoint[] remotejobcontacts) {
		
		if(howmany <= 0){
			return 0;
		}
		
		od.setHostname(getFreeNode());
		
		for(int i = 0; i < howmany; i++){
			boolean success = Interface.tryLocal(objname, objcontacts[i], od);
		}
		
		return 0;
	}

}
