package popjava.scripts.shell;

import java.io.Console;
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
		return new CommandInfo(readLine());
	}
	
	public String readPassword() {
		if (hasConsole) {
			return new String(console.readPassword());
		} else {
			return scanner.nextLine();
		}
	}
	
	public String readLine() {
		if (hasConsole) {
			return console.readLine();
		} else {
			return scanner.nextLine();
		}
	}

	public void write(String format, Object... values) {
		if (format == null || format.trim().isEmpty()) {
			format = "";
		}
		if (hasConsole) {
			console.printf(format, values);
		} else {
			System.out.format(format, values);
		}
	}
}
