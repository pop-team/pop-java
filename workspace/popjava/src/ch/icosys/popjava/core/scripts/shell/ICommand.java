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
	 * @return keyword for this command
	 */
	String keyword();
	
	/**
	 * Execute the command with its parameters.
	 * 
	 * @param info the command
	 * @return exist status
	 */
	int execute(CommandInfo info);
	
	/**
	 * Help to comprehend the command.
	 * 
	 * @return help string
	 */
	String help();
	
	/**
	 * Short description of the command.
	 * 
	 * @return command description
	 */
	String description();
}
