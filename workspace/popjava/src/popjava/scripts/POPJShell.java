package popjava.scripts;

import java.io.FileNotFoundException;
import popjava.scripts.shell.CommandHandler;
import popjava.scripts.shell.CommandInfo;
import popjava.scripts.shell.ConsoleHandler;
import popjava.scripts.shell.command.CJobManager;
import popjava.scripts.shell.command.CKeystore;


/**
 * This is a simple shell an administrator can run to configure POP-Java and the POPJavaJobManager.
 * 
 * @author Davide Mazzoleni
 */
public class POPJShell {
	
	private final ConsoleHandler consoleHandler;
	private final CommandHandler commandHandler;

	public POPJShell() throws FileNotFoundException {
		consoleHandler = ConsoleHandler.getInstance();
		commandHandler = new CommandHandler();
		initCommands();
	}
	
	private void start() {
		while (true) {
			try {
				System.out.print("$ ");
				CommandInfo ci = consoleHandler.readCommand();
				commandHandler.execute(ci);
			} catch (Exception e) {
			}
		}
	}

	private void initCommands() {
		commandHandler.add(new CKeystore());
		commandHandler.add(new CJobManager());
	}
	
	public static void optionNotFound(String keyword, String help) {
		System.out.format("%s: command not found\n", keyword);
		System.out.println(help);
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		POPJShell shell = new POPJShell();
		shell.start();
	}
}
