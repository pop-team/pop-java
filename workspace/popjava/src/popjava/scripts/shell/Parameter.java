package popjava.scripts.shell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A way to ask for parameters.
 * 
 * @author Davide Mazzoleni
 */
public class Parameter {

	private final Map<String, ParameterInfo> raw = new HashMap<>();
	private final Map<String, String> params = new HashMap<>();
	private final List<String> endArgs = new ArrayList<>();
	
	public Parameter(String[] params, ParameterInfo... expected) {
		List<String> lp = Arrays.asList(params);
		
		for (ParameterInfo pi : expected) {
			raw.put(pi.getAlias(), pi);
		}
		
		for (Iterator<String> itr = lp.iterator(); itr.hasNext();) {
			String key = itr.next();
			for (ParameterInfo pi : expected) {
				if (pi.keyMatch(key)) {
					if (!pi.hasArgument()) {
						this.params.put(pi.getAlias(), "");
					}
					else if (itr.hasNext()) {
						this.params.put(pi.getAlias(), itr.next());
					}
					else {
						throw new IllegalArgumentException(key + " require an argument");
					}
				} else {
					endArgs.add(key);
				}
			}
		}
	}
	
	public String get(String param, Object returnOnNull) {
		ParameterInfo pi = raw.get(param);
		if (pi == null && returnOnNull != null) {
			return returnOnNull.toString();
		}
		String r = params.get(param);
		if (r == null && returnOnNull != null) {
			r = returnOnNull.toString();
		}
		return r;
	}

	public String get(String param) {
		ParameterInfo pi = raw.get(param);
		if (pi == null) {
			return null;
		}
		String out = params.get(param);
		if (out == null && pi.hasArgument()) {
			ConsoleHandler ch = ConsoleHandler.getInstance();
			System.out.format("missing value for '%s': ", pi.getAlias());
			out = pi.isMasked() ? ch.readPassword() : ch.readLine();
		}
		return out;
	}

	public List<String> getEndArgs() {
		return Collections.unmodifiableList(endArgs);
	}
}
