package popjava.scripts;

import java.util.ArrayList;
import java.util.List;

public class ScriptUtils {
	
	public static boolean containsOption(String [] args, String option){
		for(String argument: args){
			if(argument.equals(option)){
				return true;
			}
		}
		
		return false;
	}
	
	public static String getOption(List<String> parameters, String defaultValue, String ... options){
        for(int i = 0; i < parameters.size(); i++){
        	for(String option: options){
        		if(parameters.get(i).equals(option)){
                    if(parameters.size() > i + 1){
                    	String value = parameters.get(i+1);
                    	parameters.remove(i);
                    	parameters.remove(i);
                        return value;
                    }
                }
        	}            
        }
        
        return defaultValue;
    }
	
	public static List<String> arrayToList(String ... args){
		ArrayList<String> arguments = new ArrayList<String>();
		
		for(String arg: args){
			arguments.add(arg);
		}
		return arguments;
	}
}
