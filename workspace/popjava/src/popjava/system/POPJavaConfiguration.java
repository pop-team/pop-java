package popjava.system;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import popjava.broker.Broker;
import popjava.scripts.Popjavac;
import popjava.serviceadapter.POPAppService;

public class POPJavaConfiguration {
	
	private static String getConfigurationValue(String value){
		try {
			ConfigurationWorker cw = new ConfigurationWorker();
			
			String configValue = cw.getValue(value);
		} catch (Exception e) {
		}
		
		return null;
	}
	
	public static String getBrokerCommand(){
		String brokerCommand = getConfigurationValue(ConfigurationWorker.POPJ_BROKER_COMMAND_ITEM);
		if(brokerCommand == null){
			brokerCommand = "java -javaagent:%s -cp %s "+Broker.class.getName()+" -codelocation=";
		}
		
		return brokerCommand;
	}
	
	/**
	 * Retrieve the POP-C++ AppCoreService executable location
	 * @return string value of the POP-C++ AppCoreService executable location
	 */
	public static String getPopAppCoreService(){
		String appCoreService = getConfigurationValue(ConfigurationWorker.POPC_APPCORESERVICE_ITEM);
		
		if(appCoreService == null){
//			String service = POPSystem
//			.getEnviroment(POPSystem.PopAppCoreServiceEnviromentName);
//	if (service.length() <= 0)
//		return DefaultPopAppCoreService;
//	return service;
		    //appCoreService = "gdb -ex=run --args /usr/local/popc/services/appservice";
			//appCoreService = "valgrind --log-file=/home/asraniel/valgrind.txt /usr/local/popc/services/appservice";
			appCoreService = "/usr/local/popc/services/appservice";
		}
		
		return appCoreService;
	}
	
	
	private static final String DEFAULT_POPJ_LOCATION = "/usr/local/popj";
	/**
	 * Retrieve the POP-Java installation location
	 * @return	string value of the POP-java location
	 */
	public static String getPopJavaLocation() {
		String popJavaLocation = getConfigurationValue(ConfigurationWorker.POPJ_LOCATION_ITEM);

		if(popJavaLocation == null){ //Popjava was not actually installed
			if(new File(DEFAULT_POPJ_LOCATION).exists()){
				return DEFAULT_POPJ_LOCATION;
			}
			
			URL temp = getMyJar();
			if(temp != null){
				File source = new File(temp.getFile()).getParentFile().getParentFile();
				return source.getAbsolutePath();
			}
			
			return "";
		}
		
		return popJavaLocation;
	}
	
	private static URL getMyJar(){
		POPJavaConfiguration me = new POPJavaConfiguration();
		
        for(URL url: ((URLClassLoader)me.getClass().getClassLoader()).getURLs()){
            boolean exists = false;
            try{ //WIndows hack
                exists = new File(url.toURI()).exists();
            }catch(Exception e){
                exists = new File(url.getPath()).exists();
            }
            if(url.getFile().endsWith(Popjavac.POP_JAVA_JAR_FILE) && exists){
                return url;
            }
        }
        return null;
    }
	
	/**
	 * Retrieve the POP-Java plugin location
	 * @return string value of the POP-Java plugin location
	 */
	public static String getPopPluginLocation() {
		String popJavaPluginLocation = getConfigurationValue(ConfigurationWorker.POPJ_PLUGIN_ITEM);
		
//		String pluginLocation = POPSystem
//		.getEnviroment(POPSystem.PopPluginLocationEnviromentName);
//if (pluginLocation.length() <= 0) {
//	return DefaultPopPluginLocation;
//}
//return pluginLocation;
		if(popJavaPluginLocation == null){
			popJavaPluginLocation = "";
		}
		
		return popJavaPluginLocation;
	}
	
	public static boolean isJar(){
		CodeSource temp = POPSystem.class.getProtectionDomain().getCodeSource();
		if(temp != null){
		    File location = null;
		    
		    try {
		        location = new File(temp.getLocation().toURI());
		    } catch(URISyntaxException e) {
		        location = new File(temp.getLocation().getPath());
		    }
		            
			if(location.isFile() && location.getAbsolutePath().endsWith(".jar")){
				return true;
			}			
		}
		
		return false;
	}
	
	public static String getClassPath(){
	    String popJar = "";
	    
	    URL [] urls = ((URLClassLoader)POPAppService.class.getClassLoader()).getURLs();
        
        Set<String> paths = new HashSet<String>();
        for(int i = 0; i < urls.length; i++){
            URL url = urls[i];
            try {
                String path = new File(url.toURI()).getAbsolutePath();
                paths.add(path);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        
        List<String> pathList = new ArrayList<String>(paths);
        
        for(int i = 0; i < pathList.size(); i++){
            popJar += pathList.get(i);
            if(i != pathList.size() - 1){
                popJar += File.pathSeparatorChar;
            }
        }
        
        return popJar;
	}
	
	public static String getPOPJavaCodePath(){
		String popJar = "";
		
		CodeSource temp = POPSystem.class.getProtectionDomain().getCodeSource();
		if(temp != null){
		    
		    File location = null;
            
            try {
                location = new File(temp.getLocation().toURI());
            } catch(URISyntaxException e) {
                location = new File(temp.getLocation().getPath());
            }
            
			if(location.isFile() && location.getAbsolutePath().endsWith(".jar")){
				popJar = location.getAbsolutePath();
			}			
		}
		
		//This is used for debug environment where popjava is not in a jar file
		if(popJar.isEmpty()){
			popJar = getClassPath();
		}
		
		return popJar;
	}
	
	//TODO: check if this AND getPOPJavaCodePath() are really both needed
	public static String getPopJavaJar(){
        String popJar = "";
        
        CodeSource temp = POPSystem.class.getProtectionDomain().getCodeSource();
        if(temp != null){
            
            File location = null;
            
            try {
                location = new File(temp.getLocation().toURI());
            } catch(URISyntaxException e) {
                location = new File(temp.getLocation().getPath());
            }
            
            if(location.isFile() && location.getAbsolutePath().endsWith(".jar")){
                popJar = location.getAbsolutePath();
            }           
        }
        
        if(!popJar.endsWith(".jar")){
            popJar = "build/jar/"+Popjavac.POP_JAVA_JAR_FILE;
        }
        
        return popJar;
    }

}
