package popjava.util.ssl;

import java.io.File;
import java.security.InvalidParameterException;

/**
 * This class is meant describe a KeyStore for {@link popjava.util.Configuration}
 *
 * @author Davide Mazzoleni
 */
public class KeyStoreDetails {
	
	public static enum KeyStoreFormat {
		JKS,
		PKCS12;
	}

	protected String localAlias;
	protected String keyStorePassword;
	protected String privateKeyPassword;
	protected File keyStoreFile;
	protected KeyStoreFormat keyStoreFormat;
	
	/**
	 * When using this constructor we must ensure {@link #validate()} return true
	 */
	public KeyStoreDetails() {
	}
	
	/**
	 * Copy constructor
	 * @param other 
	 */
	public KeyStoreDetails(KeyStoreDetails other) {
		this.localAlias = other.localAlias;
		this.keyStorePassword = other.keyStorePassword;
		this.privateKeyPassword = other.privateKeyPassword;
		this.keyStoreFile = other.keyStoreFile;
		this.keyStoreFormat = other.keyStoreFormat;
	}

	/**
	 * Full constructor
	 * 
	 * @param alias The alias of this node, used to find its own public certificate
	 * @param storepass The main password for the KeyStore, protect from tempering with the file
	 * @param keypass The password of the primate key, used to extract it
	 * @param keyStoreFile Where to save the file
	 * @param keyStoreFormat Which format to save the KeyStore: JKS, PKCS12 (may have issue)
	 */
	public KeyStoreDetails(String alias, String storepass, String keypass, File keyStoreFile, KeyStoreFormat keyStoreFormat) {
		this.localAlias = alias;
		this.keyStorePassword = storepass;
		this.privateKeyPassword = keypass;
		this.keyStoreFile = keyStoreFile;
		this.keyStoreFormat = keyStoreFormat;
	}
	
	/**
	 * Parameters to create a KeyStore with JKS as default keystore
	 *
	 * @param alias The alias of this node, used to find its own public certificate
	 * @param storepass The main password for the KeyStore, protect from tempering with the file
	 * @param keypass The password of the primate key, used to extract it
	 * @param keyStoreFile Where to save the file
	 */
	public KeyStoreDetails(String alias, String storepass, String keypass, File keyStoreFile) {
		this(alias, storepass, keypass, keyStoreFile, KeyStoreFormat.JKS);
	}

	/**
	 * The alias of the public certificate in the keystore
	 * 
	 * @return 
	 */
	public String getLocalAlias() {
		return localAlias;
	}

	/**
	 * Set the alias of the local node
	 * 
	 * @param alias 
	 */
	public void setLocalAlias(String alias) {
		this.localAlias = alias;
	}

	public String getKeyStorePassword() {
		return keyStorePassword;
	}

	/**
	 * Password of the keystore file, protect the integrity of the file
	 * 
	 * @param storepass 
	 */
	public void setKeyStorePassword(String storepass) {
		this.keyStorePassword = storepass;
	}

	public String getPrivateKeyPassword() {
		return privateKeyPassword;
	}

	/**
	 * Password of the Private Key in the KeyStore
	 * 
	 * @param keypass 
	 */
	public void setPrivateKeyPassword(String keypass) {
		this.privateKeyPassword = keypass;
	}

	/**
	 * The file pointing to the keystore
	 * 
	 * @return 
	 */
	public File getKeyStoreFile() {
		return keyStoreFile;
	}

	/**
	 * Where to save the file
	 * 
	 * @param keyStoreFile 
	 */
	public void setKeyStoreFile(File keyStoreFile) {
		this.keyStoreFile = keyStoreFile;
	}

	/**
	 * The format of the keystore
	 * 
	 * @return 
	 */
	public KeyStoreFormat getKeyStoreFormat() {
		return keyStoreFormat;
	}

	/**
	 * Format of the keystore.
	 * Be aware that format different from JKS could have issues.
	 * 
	 * @param keyStoreFormat 
	 */
	public void setKeyStoreFormat(KeyStoreFormat keyStoreFormat) {
		this.keyStoreFormat = keyStoreFormat;
	}

	/**
	 * @throws InvalidParameterException when something is set incorrectly for creating a new KeyStore
	 */
	public void validate() {
		if (localAlias == null || localAlias.isEmpty()) {
			throw new InvalidParameterException("An alias must be given and not empty");
		}
		if (keyStorePassword == null || keyStorePassword.length() < 6) {
			throw new InvalidParameterException("Store password must be set and at least 6 character long");
		}
		if (privateKeyPassword == null || privateKeyPassword.length() < 6) {
			throw new InvalidParameterException("Key password must be set and at least 6 character long");
		}
		if (keyStoreFile == null) {
			throw new InvalidParameterException("KeyStore file must be set");
		}
		if (keyStoreFormat == null) {
			throw new InvalidParameterException("A format for the keystore must be provided: JKS, PKCS12, ...");
		}
		if (keyStoreFormat == KeyStoreFormat.PKCS12 && !privateKeyPassword.equals(keyStorePassword)) {
			throw new InvalidParameterException("When using PKCS12 storePass and keyPass must match");
		}
	}
}
