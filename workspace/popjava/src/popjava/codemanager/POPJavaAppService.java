package popjava.codemanager;

import java.util.HashMap;
import java.util.Map;

import popjava.annotation.POPClass;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPSyncSeq;
import popjava.base.POPObject;
import popjava.dataswaper.POPString;
import popjava.system.POPJavaConfiguration;
import popjava.util.LogWriter;

@POPClass(classId = 99923, deconstructor = false)
public class POPJavaAppService extends POPObject implements AppService{
	
	public static final String ALL_PLATFORMS = "*-*";
	
	@POPObjectDescription(url = "localhost")
	public POPJavaAppService() {
		//this.definedMethodId = true;
	}
	
	//Platform, objectname, codefile
	private Map<String, Map<String, String>> registeredCode = new HashMap<String, Map<String,String>>();
	
	/**
	 * Register a executable code file in the CodeMgr service
	 * @param objname	Name of the parallel object
	 * @param platform	Platform of the executable
	 * @param codefile	Path of the executable code file
	 */
	@Override
    @POPSyncSeq(id = 13)
	public void registerCode(String objname, String platform, String codefile) {
		Map<String, String> platf = registeredCode.get(platform);
		
		if(platf == null){
			platf = new HashMap<String, String>();
			registeredCode.put(platform, platf);
		}
		platf.put(objname, codefile);
	}

	/**
	 * Query the CodeMgr to retrieve the code file for a specific object on a specific architecture
	 * @param objname	Name of the object
	 * @param platform	Platform desired
	 * @param codefile	Output argument - code file for the specific object and the specific platform
	 * @return	0 if the code file is not available
	 */
	@Override
    @POPSyncSeq(id = 14)
	public int queryCode(String objname, String platform, POPString codefile) {
		Map<String, String> platf = registeredCode.get(platform);
		String storeCodeFile = null;
		
		if(platf == null){
		    //If the specified platform does not have the code, fall back to the platform agnostic code
			if(!platform.equals(ALL_PLATFORMS)){
			    if(registeredCode.containsKey(ALL_PLATFORMS)){
			        storeCodeFile = registeredCode.get(ALL_PLATFORMS).get(objname);
			    }
			    
			    if(storeCodeFile == null){
			        LogWriter.writeDebugInfo("Platform "+platform+ " not found");
			    }
			}else{ //We search for any platform, so lets try the individual platforms
			    for(String possiblePlatform: registeredCode.keySet()){
			        storeCodeFile = registeredCode.get(possiblePlatform).get(objname);
			        if(storeCodeFile != null){
			            break;
			        }
	            }
			}
		}else{
			storeCodeFile = platf.get(objname);
		}
		/*if(storeCodeFile == null){
			storeCodeFile = getLocalJavaFileLocation(objname);
		}*/
		
		if(storeCodeFile == null){
			return 0;
		}
		
		codefile.setValue(storeCodeFile);
		
		return 1;
	}
	
	public String getLocalJavaFileLocation(String objname){
		String codePath = null;
		try{
			ClassLoader classloader = getClass().getClassLoader();
			Class<?> javaClass = classloader.loadClass(objname);
						
			codePath = String.format(
					POPJavaConfiguration.getBrokerCommand(),
					POPJavaConfiguration.getPopJavaJar(),
					POPJavaConfiguration.getPOPJavaCodePath()) + 
					javaClass.getProtectionDomain().getCodeSource().getLocation().getPath();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return codePath;
	}

	/**
	 * Query the CodeMgr to know the platforms of a specific object
	 * @param objname	Name of the object
	 * @param platform	Output argument - platform available for the object
	 * @return	number of platform available
	 */
	@Override
    @POPSyncSeq(id = 15)
	public int getPlatform(String objname, POPString platform) {
		return 0;
	}
	
	@Override
    @POPSyncSeq
	public String getPOPCAppID(){
		return "PopJavaApp";
	}
	
}
