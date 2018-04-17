package ch.icosys.popjava.core.scripts.shell;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ch.icosys.popjava.core.scripts.POPJShell;

/**
 * Base commands registration.
 * 
 * @author Davide Mazzoleni
 */
public class CommandHandler {

	private final Map<String, ICommand> commands = new HashMap<>();

	public int execute(CommandInfo ci) {
		ICommand command = commands.get(ci.getKeyword());
		if (command == null) {
			POPJShell.optionNotFound(ci.getKeyword(), help());
			return 1;
		} else if (ci.isNextHelp()) {
			System.out.println(command.help());
			return 0;
		} else {
			return command.execute(ci);
		}
	}

	public void add(ICommand command) {
		commands.put(command.keyword(), command);
	}

	public String help() {
		Collection<ICommand> comm = commands.values();
		StringBuilder sb = new StringBuilder();
		sb.append("Available options:\n");
		for (ICommand com : comm) {
			sb.append(String.format("  %-20s  %s\n", com.keyword(), com.description()));
		}
		return sb.toString();
	}
}
