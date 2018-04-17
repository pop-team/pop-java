package ch.icosys.popjava.core.scripts.shell.command;

import java.io.IOException;

import ch.icosys.popjava.core.scripts.shell.CommandInfo;
import ch.icosys.popjava.core.scripts.shell.ICommand;
import ch.icosys.popjava.core.util.Configuration;

/**
 * Turn on and off the debug option.
 * 
 * @author Davide Mazzoleni
 */
public class CDebug implements ICommand {

	@Override
	public String keyword() {
		return "debug";
	}

	@Override
	public int execute(CommandInfo info) {
		Configuration conf = Configuration.getInstance();
		boolean set = !conf.isDebug();
		conf.setDebug(set);
		try {
			conf.writeSystemConfiguration();
		} catch (IOException ex) {
			System.out.println("Couldn't save global debug option.");
		}

		System.out.println("Debug set to " + set);
		return 0;
	}

	@Override
	public String help() {
		return "usage: debug\n" + description();
	}

	@Override
	public String description() {
		return "toggle system debug option";
	}

}
