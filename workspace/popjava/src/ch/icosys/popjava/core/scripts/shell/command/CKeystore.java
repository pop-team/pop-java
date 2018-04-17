package ch.icosys.popjava.core.scripts.shell.command;

import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.style.BCStyle;

import ch.icosys.popjava.core.scripts.POPJShell;
import ch.icosys.popjava.core.scripts.shell.CommandHandler;
import ch.icosys.popjava.core.scripts.shell.CommandInfo;
import ch.icosys.popjava.core.scripts.shell.ICommand;
import ch.icosys.popjava.core.scripts.shell.Parameter;
import ch.icosys.popjava.core.scripts.shell.ParameterInfo;
import ch.icosys.popjava.core.util.Configuration;
import ch.icosys.popjava.core.util.ssl.KeyPairDetails;
import ch.icosys.popjava.core.util.ssl.KeyStoreDetails;
import ch.icosys.popjava.core.util.ssl.SSLUtils;

/**
 * Keystore related commands.
 * 
 * @author Davide Mazzoleni
 */
public class CKeystore implements ICommand {

	private final CommandHandler commandHandler = new CommandHandler();

	public CKeystore() {
		initCommands();
	}

	private void initCommands() {
		commandHandler.add(new Create());
		commandHandler.add(new Remove());
		commandHandler.add(new Generate());
	}

	@Override
	public String keyword() {
		return "keystore";
	}

	@Override
	public int execute(CommandInfo info) {
		if (info.canAdvance()) {
			return commandHandler.execute(info.advance());
		} else {
			System.out.println(help());
			return 1;
		}
	}

	@Override
	public String help() {
		return commandHandler.help();
	}

	@Override
	public String description() {
		return "all keystore related operations.";
	}

	private class Create implements ICommand {

		@Override
		public String keyword() {
			return "create";
		}

		@Override
		public int execute(CommandInfo info) {
			Parameter parameters = info.extractParameter(new ParameterInfo("file", "--file", "-f"),
					new ParameterInfo("storepass", true, true, "--storepass", "-s"),
					new ParameterInfo("keypass", true, true, "--keyspass", "-k"),
					new ParameterInfo("alias", "--alias", "-a"), new ParameterInfo("rdn", "--rdn", "-r"));

			String file = parameters.get("file");
			String storepass = parameters.get("storepass");
			String keypass = parameters.get("keypass");
			String alias = parameters.get("alias");
			String rdn = parameters.get("rdn");

			KeyStoreDetails ksd = new KeyStoreDetails(storepass, keypass, new File(file).getAbsoluteFile());
			KeyPairDetails kpd = new KeyPairDetails(alias);

			if (rdn != null) {
				try {
					RDN[] parsedRDN = BCStyle.INSTANCE.fromString(rdn);
					for (RDN value : parsedRDN) {
						kpd.addRDN(value.getFirst().getType(), value.getFirst().getValue().toString());
					}
				} catch (Exception e) {
					System.err.println("Can't parse RDN, skipping. Look for RDN (Relative Distinguished Name) format."
							+ "\nMessage: " + e.getMessage());
				}
			}

			System.out.println("Generating keystore...");
			SSLUtils.generateKeyStore(ksd, kpd);

			System.out.println("Saving configuration...");
			Configuration conf = Configuration.getInstance();
			conf.setSSLKeyStoreOptions(ksd);
			try {
				conf.writeSystemConfiguration();
				POPJShell.configuration.setPrivateNetwork(kpd.getAlias());
			} catch (IOException e) {
				System.err.println("A problem occurred while saving system configuration: " + e.getMessage());
				return 1;
			}

			return 0;
		}

		@Override
		public String help() {
			return "usage: keystore create [OPTIONS]\n" + description() + "\n" + "Available options:\n"
					+ "  --file, -f          The file where the keystore will be saved\n"
					+ "  --storepass, -s     The password to check for keystore integrity\n"
					+ "  --keypass, -k       The password to protect the private keys in the keystore\n"
					+ "  --alias, -a         The private alias not shared with anyone\n"
					+ "  --rdn               The RDN string which will identify the certificate";
		}

		@Override
		public String description() {
			return "create a new keystore";
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
			Parameter parameters = info.extractParameter(new ParameterInfo("alias", "--alias", "-a"));

			String alias = parameters.get("alias");
			System.out.format("Removing alias '%s' from keystore.\n", alias);
			try {
				SSLUtils.removeAlias(alias);
			} catch (IOException ex) {
				System.err.format("Failed to remove alias %s.\n", alias);
			}

			return 0;
		}

		@Override
		public String help() {
			return "usage: keystore remove [OPTIONS]\n" + description() + "\n" + "Available options:\n"
					+ "  --alias, -a         The private alias not shared with anyone";
		}

		@Override
		public String description() {
			return "remove a certificate or private key from the loaded keystore";
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
			Parameter parameters = info.extractParameter(new ParameterInfo("alias", "--alias", "-a"),
					new ParameterInfo("rdn", "--rdn", "-r"));

			String alias = parameters.get("alias");
			String rdn = parameters.get("rdn");

			KeyStoreDetails ksd = Configuration.getInstance().getSSLKeyStoreOptions();
			KeyPairDetails kpd = new KeyPairDetails(alias);

			if (rdn != null) {
				try {
					RDN[] parsedRDN = BCStyle.INSTANCE.fromString(rdn);
					for (RDN value : parsedRDN) {
						kpd.addRDN(value.getFirst().getType(), value.getFirst().getValue().toString());
					}
				} catch (Exception e) {
					System.err.println("Can't parse RDN, skipping. Look for RDN (Relative Distinguished Name) format."
							+ "\nMessage: " + e.getMessage());
				}
			}

			System.out.println("Genereting Key Pair...");
			KeyStore.PrivateKeyEntry keyPair = SSLUtils.ensureKeyPairGeneration(kpd);

			try {
				SSLUtils.addKeyEntryToKeyStore(ksd, kpd, keyPair);
			} catch (Exception e) {
				System.err.println("A problem occurred while adding key to keystore: " + e.getMessage());
				return 1;
			}

			return 0;
		}

		@Override
		public String help() {
			return "usage: keystore generate [OPTIONS]\n" + description() + "\n" + "Available options:\n"
					+ "  --alias, -a         The private alias not shared with anyone\n"
					+ "  --rnd, -r           The RDN string which will identify the certificate";
		}

		@Override
		public String description() {
			return "generate a new private key/certificate pair";
		}
	}
}
