package popjava.scripts.shell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Describe a command for the main POPJShell
 * 
 * @author Davide Mazzoleni
 */
public class CommandInfo {
	
	/**
	 * The first keyword
	 */
	private final String keyword;
	
	/**
	 * All the other
	 */
	private final String[] params;

	/**
	 * Transform a line into its components
	 * 
	 * @param line 
	 */
	public CommandInfo(String line) {
		Scanner s = new Scanner(line);
		
		this.keyword = s.next();
		
		List<String> args = new ArrayList<>();
		String arg = "";
		char quote = '\0';
		while (s.hasNext()) {
			arg += s.next();
			
			char start = arg.charAt(0);
			char end = arg.charAt(arg.length() - 1);
			if (start == '"' || start == '\'') {
				quote = start;
				arg = arg.substring(1);
			}
			else if (end == quote) {
				arg = arg.substring(0, arg.length() - 1);
				args.add(arg);
				arg = "";
			}
			else {
				args.add(arg);
				arg = "";
			}
		}
		
		this.params = args.toArray(new String[arg.length()]);
	}
	
	/**
	 * Internal constructor for {@link #advance() }
	 * 
	 * @param keyword
	 * @param params 
	 */
	private CommandInfo(String keyword, String... params) {
		this.keyword = keyword;
		this.params = params;
	}
	
	/**
	 * Extract the parameters from the remaining arguments.
	 * 
	 * @param expected
	 * @return 
	 */
	public Parameter extractParameter(ParameterInfo... expected) {
		return new Parameter(params, expected);
	}
	
	/**
	 * Replace the keyword with the first parameter in {@link #params}.
	 * Before calling this method, check if you can with {@link #canAdvance() }.
	 * 
	 * @return a new CommandInfo or null in can it can't advance anymore.
	 */
	public CommandInfo advance() {
		if (!canAdvance()) {
			return null;
		}
		return new CommandInfo(params[0], Arrays.copyOfRange(params, 1, params.length));
	}
	
	/**
	 * Report if we can advance the command one (1) step.
	 * 
	 * @return true if we can, false if we can't.
	 */
	public boolean canAdvance() {
		return params.length > 0;
	}
	
	/**
	 * Does the command ask to call for help?
	 * 
	 * @return 
	 */
	public boolean isNextHelp() {
		return canAdvance() && (params[0].equals("-h") || params[0].equals("--help"));
	}

	/**
	 * The keyword for this command.
	 * 
	 * @return 
	 */
	public String getKeyword() {
		return keyword;
	}

	/**
	 * A copy of the remaining parameters.
	 * 
	 * @return 
	 */
	public String[] getParams() {
		return Arrays.copyOf(params, params.length);
	}
}
