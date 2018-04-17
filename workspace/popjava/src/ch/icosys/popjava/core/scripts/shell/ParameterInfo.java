package ch.icosys.popjava.core.scripts.shell;

/**
 * Describe a possible parameter. NOTE: It should be more generic for many kind
 * of parameters, but let's not think about it.
 * 
 * @author Davide Mazzoleni
 */
public class ParameterInfo {

	private final String alias;

	private final String[] options;

	private final boolean argument;

	private final boolean mask;

	public ParameterInfo(String alias, boolean argument, boolean mask, String... options) {
		this.alias = alias;
		this.options = options;
		this.argument = argument;
		this.mask = mask;
	}

	public ParameterInfo(String alias, boolean argument, String... options) {
		this(alias, argument, false, options);
	}

	public ParameterInfo(String alias, String... options) {
		this(alias, true, options);
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
