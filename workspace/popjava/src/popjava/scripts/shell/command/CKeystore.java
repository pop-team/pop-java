package popjava.scripts.shell.command;

import java.io.File;
import popjava.scripts.shell.CommandHandler;
import popjava.scripts.shell.CommandInfo;
import popjava.scripts.shell.ICommand;
import popjava.scripts.shell.Parameter;
import popjava.scripts.shell.ParameterInfo;
import popjava.util.ssl.KeyPairDetails;
import popjava.util.ssl.KeyStoreDetails;
import popjava.util.ssl.SSLUtils;

/**
 * Keystore related commands.
 * 
 * @author Davide Mazzoleni
 */
public class CKeystore implements ICommand {
	
	private final CommandHandler commandHandler = new CommandHandler();

	public CKeystore() {
		init();
	}

	private void init() {
		commandHandler.add(new Create());
		commandHandler.add(new Add());
		commandHandler.add(new Remove());
		commandHandler.add(new Generate());
	}

	@Override
	public String keyword() {
		return "keystore";
	}

	@Override
	public int execute(CommandInfo info) {
		return commandHandler.execute(info.advance());
	}

	@Override
	public String help() {
		return commandHandler.help();
	}

	@Override
	public String description() {
		return "all keystore related operations.";
	}
	
	
	public class Create implements ICommand {

		@Override
		public String keyword() {
			return "create";
		}

		@Override
		public int execute(CommandInfo info) {
			Parameter parameters = info.extractParameter(
				new ParameterInfo("file", new String[]{"--file", "-f"}),
				new ParameterInfo("storepass", new String[]{"--storepass", "-s"}, true, true),
				new ParameterInfo("keypass", new String[]{"--keyspass","-k"}, true, true),
				new ParameterInfo("alias", new String[]{"--alias","-a"}, true),
				new ParameterInfo("rdn", new String[]{"--rdn","-r"}, true)
			);
			
			String file = parameters.get("file");
			String storepass = parameters.get("storepass");
			String keypass = parameters.get("keypass");
			String alias = parameters.get("alias");
			String rdn = parameters.get("rdn");
			
			KeyStoreDetails ksd = new KeyStoreDetails(storepass, keypass, new File(file));
			KeyPairDetails kpd = new KeyPairDetails(alias);
			// TODO RND filling
			
			SSLUtils.generateKeyStore(ksd, kpd);
			
			// TODO save config
			
			return 0;
		}

		@Override
		public String help() {
			return "usage: keystore create [OPTIONS]\n" +
				description() +
				"\n" +
				"Available options:\n" +
				"  --file, -f          The file where the keystore will be saved\n" +
				"  --storepass, -s     The password to check for keystore integrity\n" +
				"  --keypass, -k       The password to protect the private keys in the keystore\n" +
				"  --alias, -a         The private alias not shared with anyone\n" +
				"  --rdn               The RDN string which will identify the certificate";
		}

		@Override
		public String description() {
			return "create a new keystore";
		}
		
	}

	private class Add implements ICommand {

		public Add() {
		}

		@Override
		public String keyword() {
			return "add";
		}

		@Override
		public int execute(CommandInfo info) {
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}

		@Override
		public String help() {
			return "usage: keystore add [OPTIONS]\n" +
				description() +
				"\n" +
				"Available options:\n" +
				"  --file, -f          The file where the keystore will be saved\n" +
				"  --storepass, -s     The password to check for keystore integrity\n" +
				"  --alias, -a         The private alias not shared with anyone";
		}

		@Override
		public String description() {
			return "add a new existing certificate to the keystore";
		}
	}

	private class Remove implements ICommand {

		public Remove() {
		}

		@Override
		public String keyword() {
			return "remove";
		}

		@Override
		public int execute(CommandInfo info) {
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}

		@Override
		public String help() {
			return "usage: keystore remove [OPTIONS]\n" +
				description() +
				"\n" +
				"Available options:\n" +
				"  --file, -f          The file where the keystore will be saved\n" +
				"  --storepass, -s     The password to check for keystore integrity\n" +
				"  --alias, -a         The private alias not shared with anyone";
		}

		@Override
		public String description() {
			return "remove a certificate or private key from an existing keystore";
		}
	}

	private class Generate implements ICommand {

		public Generate() {
		}

		@Override
		public String keyword() {
			return "generate";
		}

		@Override
		public int execute(CommandInfo info) {
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}

		@Override
		public String help() {
			return "usage: keystore generate [OPTIONS]\n" +
				description() +
				"\n" +
				"Available options:\n" +
				"  --file, -f          The file where the keystore will be saved\n" +
				"  --storepass, -s     The password to check for keystore integrity\n" +
				"  --keypass, -k       The password to protect the private keys in the keystore\n" +
				"  --alias, -a         The private alias not shared with anyone\n" +
				"  --rnd, -r           The RDN string which will identify the certificate";
		}

		@Override
		public String description() {
			return "generate a new private key/certificate pair";
		}
	}
}
