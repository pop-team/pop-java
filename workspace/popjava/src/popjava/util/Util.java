package popjava.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import popjava.system.POPSystem;

/**
 * This class gives some static utility methods
 */

public class Util {

	/**
	 * Default empty constructor
	 */
	private Util() {
	}

	/**
	 * Check if the two contact string are the same
	 * @param source	First contact string
	 * @param dest		Second contact string
	 * @return	true if the contact strings are the same
	 */
	public static boolean sameContact(String source, String dest) {
		if (source.compareTo(dest) == 0){
			return true;
		}
		if (source == null || source.length() == 0 || dest == null
				|| dest.length() == 0){
			return false;
		}
		String[] allDestHost = dest.split("[ \t\r\n]");
		for (String str : allDestHost) {
			if (source.indexOf(str) >= 0){
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
		String myhost = POPSystem.getHost();
		
		boolean isLocal = (hostname == null || hostname.length() == 0
				|| popjava.util.Util.sameContact(myhost, hostname)
				|| hostname.compareTo("localhost") == 0 || hostname
				.compareTo("127.0.0.1") == 0);
		
		return isLocal;
	}

	/**
	 * Remove a string in an array list
	 * @param list		The array list to work with
	 * @param prefix	The prefix of the string to remove
	 * @return	The entire string removed
	 */
	public static String removeStringFromArrayList(ArrayList<String> list,
			String prefix) {
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
		if (s1 == null || s2 == null)
			return false;
		return s1.equals(s2);

	}

	/**
	 * Compare two not null string. Case insensitive
	 * @param s1	First string
	 * @param s2	Second String
	 * @return	true if the strings are equal
	 */
	public static boolean isNoCaseStringEqual(String s1, String s2) {
		if (s1 == null || s2 == null){
			return false;
		}
		
		return s1.equalsIgnoreCase(s2);

	}

	/**
	 * Generate a random string of the given length
	 * @param length	Length of the generated string
	 * @return	The generated string
	 */
	public static String generateRandomString(int length) {
		String result = "";
		Random random = new Random((new Date()).getTime());
		for (int i = 0; i < length; i++) {
			int randomInt = random.nextInt();
			if (randomInt < 0){
				randomInt = -randomInt;
			}
			randomInt = (int) (((float) randomInt / Integer.MAX_VALUE) * 24 + (int) 'A');
			result += Character.toString((char) randomInt);
		}
		return result;

	}

	/**
	 * Split a command formatted as a string value into an array list
	 * @param command	The command formatted as a string value
	 * @return	The split command as an array list
	 */
	public static ArrayList<String> splitTheCommand(String command) {
		return new ArrayList<String>(Arrays.asList(command.trim().split(" ")));
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
}
