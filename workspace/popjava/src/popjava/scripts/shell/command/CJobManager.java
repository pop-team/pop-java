/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package popjava.scripts.shell.command;

import popjava.scripts.shell.CommandHandler;
import popjava.scripts.shell.CommandInfo;
import popjava.scripts.shell.ICommand;

/**
 *
 * @author dosky
 */
public class CJobManager implements ICommand {
	
	private final CommandHandler commandHandler = new CommandHandler();

	@Override
	public String keyword() {
		return "jobmanager";
	}

	@Override
	public int execute(CommandInfo other) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public String help() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public String description() {
		return "configuration of the local job manager";
	}
	
}
