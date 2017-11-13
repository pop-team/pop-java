package popjava.scripts.shell;

/**
 * Describe a possible parameter.
 * 
 * @author Davide Mazzoleni
 */
public class ParameterInfo {
	
	private final String alias;
	private final String[] options;
	private final boolean argument;
	private final boolean mask;

	public ParameterInfo(String alias, String[] options, boolean argument, boolean mask) {
		this.alias = alias;
		this.options = options;
		this.argument = argument;
		this.mask = mask;
	}
	
	public ParameterInfo(String alias, String[] options, boolean argument) {
		this(alias, options, argument, false);
	}

	public ParameterInfo(String alias, String[] options) {
		this(alias, options, true);
	}

	public String[] getOptions() {
		return options;
	}

	public boolean hasArgument() {
		return argument;
	}

	public boolean isMasked() {
		return mask;
	}

	public String getAlias() {
		return alias;
	}

	public boolean keyMatch(String key) {
		for (String option : options) {
			if (option.equals(key)) {
				return true;
			}
		}
		return false;
	}
}
