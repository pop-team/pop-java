package popjava.scripts.shell;

/**
 * Describe a command.
 * 
 * @author Davide Mazzoleni
 */
public interface ICommand {
	
	/**
	 * The keyword to trigger this command.
	 * 
	 * @return 
	 */
	String keyword();
	
	/**
	 * Execute the command with its parameters.
	 * 
	 * @param command
	 * @return 
	 */
	int execute(CommandInfo info);
	
	/**
	 * Help to comprehend the command.
	 * 
	 * @return 
	 */
	String help();
	
	/**
	 * Short description of the command.
	 * 
	 * @return 
	 */
	String description();
}
