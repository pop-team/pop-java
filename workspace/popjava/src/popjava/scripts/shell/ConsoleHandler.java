package popjava.scripts.shell;

import java.io.Console;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Handle the visualization of the Shell and utility like history.
 * 
 * @author Davide Mazzoleni
 */
public class ConsoleHandler {
	
	private final Scanner scanner;
	private final Console console;
	
	private final boolean hasConsole;

	private static ConsoleHandler instance;

	public static ConsoleHandler getInstance() {
		if (instance == null) {
			instance = new ConsoleHandler();
		}
		return instance;
	}
	
	private ConsoleHandler() {
		scanner = new Scanner(System.in);
		console = System.console();
		hasConsole = console != null;
	}

	public CommandInfo readCommand() {
		String line = readLine();
		return line == null ? null : new CommandInfo(line);
	}
	
	public String readPassword() {
		if (hasConsole) {
			return new String(console.readPassword());
		} else {
			return scanner.nextLine();
		}
	}
	
	public String readLine() {
		String line;
		if (hasConsole) {
			line = console.readLine();
		} else {
			line = scanner.nextLine();
		}
		return line == null || line.trim().isEmpty() ? null : line;
	}
}
