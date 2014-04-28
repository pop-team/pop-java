package popjava.system;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.security.CodeSource;

import popjava.broker.Broker;
import popjava.codemanager.POPJavaAppService;

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
			brokerCommand = "java -cp %s "+Broker.class.getName()+" -codelocation=";
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
			appCoreService = "valgrind --log-file=/home/asraniel/valgrind.txt /usr/local/popc/services/appservice";
			//appCoreService = "/usr/local/popc/services/appservice";
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
            if(url.getFile().endsWith("popjava.jar") && exists){
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
	
	public static String getPOPJavaCodePath(){
		String popJar = "";
		
		CodeSource temp = POPSystem.class.getProtectionDomain().getCodeSource();
		if(temp != null){
			String location = temp.getLocation().toString();
			if(location.endsWith(".jar")){
				popJar = location;
			}			
		}
		
		//This is used for debug environment where popjava is not in a jar file
		if(popJar.isEmpty()){
			URL [] urls = ((URLClassLoader)POPJavaAppService.class.getClassLoader()).getURLs();
			for(int i = 0; i < urls.length; i++){
				URL url = urls[i];
				try {
					String path = new File(url.toURI()).getAbsolutePath();
					popJar += path;
					if(i != urls.length - 1){
		            	popJar += File.pathSeparatorChar;
		            }
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}	            
	        }
		}
		return popJar;
	}
	
	public static String getPopJavaJar(){
		String popJar = "";
		for(URL url: ((URLClassLoader)POPJavaAppService.class.getClassLoader()).getURLs()){
			
            boolean exists = false;
            try{ //WIndows hack
                exists = new File(url.toURI()).exists();
            }catch(Exception e){
                exists = new File(url.getPath()).exists();
            }
            if(exists && url.getFile().endsWith("popjava.jar")){
            	popJar = url.getPath();
            }
        }
		
		if(popJar.isEmpty()){
			for(URL url: ((URLClassLoader)POPJavaAppService.class.getClassLoader()).getURLs()){
				if(url.getPath().endsWith(File.separator)){
					popJar = url.getPath();
					break;
				}
			}
		}
		
		return popJar;
	}

}
