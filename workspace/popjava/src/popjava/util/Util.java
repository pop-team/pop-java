package popjava.util;

import java.lang.annotation.Annotation;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import popjava.annotation.POPParameter;
import popjava.annotation.POPParameter.Direction;
import popjava.system.POPJavaConfiguration;
import popjava.system.POPSystem;

/**
 * This class gives some static utility methods
 */

public final class Util {
	
	private static final Random RANDOM = new SecureRandom();

	/**
	 * Check if the two contact string are the same
	 * @param source	First contact string
	 * @param dest		Second contact string
	 * @return	true if the contact strings are the same
	 */
	public static boolean sameContact(String source, String dest) {
	    if (source == null || source.isEmpty() || dest == null || dest.isEmpty()){
            return false;
        }
	    
		if (source.compareTo(dest) == 0){
			return true;
		}
		
		String[] allDestHost = dest.split("[ \t\r\n]");
		for (String str : allDestHost) {
			if (source.contains(str)){
				return true;
			}
		}
		return false;

	}

	/**
	 * Check if the contact string is the local host
	 * @param hostname	Contact string
	 * @return	true if the contact string is the local host
	 */
	public static boolean isLocal(String hostname) {
		String myHost = POPSystem.getHostIP();

		return (hostname == null || hostname.length() == 0
				|| sameContact(myHost, hostname)
				|| hostname.equals("localhost") ||
				hostname.equals("127.0.0.1") ||
				hostname.equals("127.0.1.1"));
	}

	/**
	 * Remove a string in an array list
	 * @param list		The array list to work with
	 * @param prefix	The prefix of the string to remove
	 * @return	The entire string removed
	 */
	public static String removeStringFromList(List<String> list, String prefix) {
		String result = null;

		int index = 0;
		for (index = 0; index < list.size(); index++) {
			String str = list.get(index);
			if (str.startsWith(prefix)) {
				result = str.substring(prefix.length());
				list.remove(index);
				break;
			}
		}

		return result;
	}

	/**
	 * Compare two no null Strings
	 * @param s1	First string
	 * @param s2	Second string
	 * @return	true if the strings are equal
	 */
	public static boolean isStringEqual(String s1, String s2) {
		return s1 != null && s2 != null && s1.equals(s2);

	}

	/**
	 * Compare two not null string. Case insensitive
	 * @param s1	First string
	 * @param s2	Second String
	 * @return	true if the strings are equal
	 */
	public static boolean isNoCaseStringEqual(String s1, String s2) {
		return s1 != null && s2 != null && s1.equalsIgnoreCase(s2);

	}
	
	/**
	 * Join an array of String
	 * @param delimiter Separator of each string
	 * @param join What we want to join
	 * @return the joined array
	 */
	public static String join(String delimiter, String... join) {
		Objects.requireNonNull(join);
		
		StringBuilder sb = null;
		for (String s : join) {
			if (sb != null) {
				sb.append(delimiter);
			} else {
				sb = new StringBuilder();
			}
			
			sb.append(s);
		}
		return sb != null ? sb.toString() : null;
	}
	
	/**
	 * Join a list of String
	 * @param delimiter Separator of each string
	 * @param join What we want to join
	 * @return the joined list
	 */
	public static String join(String delimiter, List<String> join) {
		return join(delimiter, join.toArray(new String[join.size()]));
	}
	
	/**
	 * Generate a random string of the given length
	 * @param length	Length of the generated string
	 * @return	The generated string
	 */
	public static String generateRandomString(int length) {
		StringBuilder result = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			int randomInt = RANDOM.nextInt() & 0x7FFFFFFF; // positive int
			randomInt = (int) (((float) randomInt / Integer.MAX_VALUE) * 24 + 'A');
			result.append((char) randomInt);
		}
		return result.toString();
	}
	
	/**
	 * Generate UUID string
	 * @return a UUID as a string
	 */
	public static String generateUUID() {
		return UUID.randomUUID().toString();
	}

	/**
	 * Split a command formatted as a string value into an array list
	 * @param command	The command formatted as a string value
	 * @return	The split command as an array list
	 */
	public static ArrayList<String> splitTheCommand(String command) {
		return new ArrayList<>(Arrays.asList(command.trim().split(" ")));
	}

	/**
	 * Match a parent platform string with a child platform string
	 * @param parent	The parent platform string
	 * @param child		The child platform string
	 * @return	true if the string match
	 */
	public static boolean matchPlatform(String parent, String child) {
		String regex = parent.replaceAll("\\*", ".+");
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(child); // get a matcher object
		if (m.find()) {
			if (m.start() == 0 && m.end() == child.length()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Transform a byte array into an int value
	 * @param value	The byte array to transform
	 * @return	The int value
	 */
	public static int byteArrayToInt(byte[] value) {
		return value[0]<<24 | (value[1]&0xff)<<16 | (value[2]&0xff)<<8 | (value[3]&0xff);		
	}
	
	/**
	 * Returns true of one of the annotations defines a IN only parameter
	 * @param annotations an array of annotations
	 * @return true if the direction is set to IN
	 */
	public static boolean isParameterNotOfDirection(Annotation [] annotations, POPParameter.Direction direction){
		for(Annotation annotation: annotations){
			if(annotation.annotationType() == POPParameter.class){
				POPParameter popParameter = (POPParameter) annotation;
				if(popParameter.value() == direction){
					return false;
				}
			}
		}
		
		return true;
	}
	
	public static boolean isParameterOfAnyDirection(Annotation [] annotations){
		for(Annotation annotation: annotations){
			if(annotation.annotationType() == POPParameter.class){
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean isParameterUsable(Annotation [] annotations){
        for(Annotation annotation: annotations){
            if(annotation.annotationType() == POPParameter.class){
                POPParameter popParameter = (POPParameter) annotation;
                if(popParameter.value() == Direction.IGNORE){
                    return false;
                }
            }
        }
        
        return true;
    }
	
	public enum OSType{
		UNIX,
		Windows
	}	
	
	/**
	 * Returns the OS type on which this machine runs. Can return windows or unix.
	 * @return win or unix depending on the environment
	 */
	public static OSType getOSType(){
		if(System.getProperty("os.name").toLowerCase().contains("win")){
			return OSType.Windows;
		}
		
		return OSType.UNIX;
	}
	
	public String getLocalJavaFileLocation(String objname){
		String codePath = null;
		try{
			ClassLoader classloader = getClass().getClassLoader();
			Class<?> javaClass = classloader.loadClass(objname);
						
			codePath = String.format(
					POPJavaConfiguration.getBrokerCommand(),
					POPJavaConfiguration.getPopJavaJar(),
					POPJavaConfiguration.getClassPath()) + 
					javaClass.getProtectionDomain().getCodeSource().getLocation().getPath();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return codePath;
	}

}
