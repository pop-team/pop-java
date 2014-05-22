package popjava.scripts;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import popjava.system.POPSystem;

public class POPJRunProxy {

	public static void main(String ... args) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		if(args.length == 0){
			System.err.println("No parameters specified");
			return;
		}
		
		Class<?> mainClass = POPJRunProxy.class.getClassLoader().loadClass(args[0]);
		try {
			Method mainMethod = mainClass.getMethod("main", String[].class);
			
			String [] correctedArgs = new String[args.length - 1];
			for(int i = 0;i < correctedArgs.length;i++){
				correctedArgs[i] = args[i + 1];
			}
			
			correctedArgs = POPSystem.initialize(correctedArgs);
			
			mainMethod.invoke(null, (Object)correctedArgs);
			
			POPSystem.end();
		} catch (NoSuchMethodException e) {
			System.err.println("Could not find method main in "+args[0]);
		}
	}
	
}
