package popjava.util.ssl;

import java.io.File;
import java.security.InvalidParameterException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.style.BCStyle;

/**
 * This class is meant to be send to {@link SSLUtils#generateKeyStore(KeyStoreCreationOptions)} ) }
 * 
 * @author Davide Mazzoleni
 */
public class KeyStoreCreationOptions extends KeyStoreDetails {
	
	protected Date validUntil;
	protected int privateKeySize;
	protected final Map<ASN1ObjectIdentifier, String> rdn = new HashMap<>();

	// validation
	boolean hasName = false;

	/**
	 * Empty constructor
	 */
	public KeyStoreCreationOptions() {
	}
	
	/**
	 * Copy constructor
	 * 
	 * @param other 
	 */
	public KeyStoreCreationOptions(KeyStoreCreationOptions other) {
		super(other);
		this.validUntil = other.validUntil;
		this.privateKeySize = other.privateKeySize;
		this.rdn.putAll(other.rdn);
		this.hasName = other.hasName;
	}
	
	/**
	 * Full constructor
	 * 
	 * @param alias The alias of this node, used to find its own public certificate
	 * @param keyStorePass The main password for the KeyStore, protect from tempering with the file
	 * @param privateKeyPass The password of the primate key, used to extract it
	 * @param keyStoreFile Where to save the file
	 * @param keyStoreFormat Which format to save the KeyStore: JKS, PKCS12 (may have issue)
	 * @param validUntil Until when the certificate should be valid
	 * @param privateKeySize The complexity of the RSA key, must be greater than 1024 bits
	 */
	public KeyStoreCreationOptions(String alias, String keyStorePass, String privateKeyPass, File keyStoreFile, KeyStoreFormat keyStoreFormat, Date validUntil, int privateKeySize) {
		super(alias, keyStorePass, privateKeyPass, keyStoreFile, keyStoreFormat);
		this.rdn.put(BCStyle.OU, "PopJava");
		this.rdn.put(BCStyle.CN, alias);
		this.validUntil = validUntil;
		this.privateKeySize = privateKeySize;
		hasName = true;
	}
	
	/**
	 * Parameters to create a KeyStore with sane defaults.
	 * Consider using {@link #setValidFor(int)}
	 * Defaults are: 
	 *  keyStoreFormat := JKS 
	 *  validity := 365 days
	 *  keySize := 2048 bits
	 *
	 * @param alias The alias of this node, used to find its own public certificate
	 * @param storepass The main password for the KeyStore, protect from tempering with the file
	 * @param keypass The password of the primate key, used to extract it
	 * @param keyStoreFile Where to save the file
	 */
	public KeyStoreCreationOptions(String alias, String storepass, String keypass, File keyStoreFile) {
		this(alias, storepass, keypass, keyStoreFile, KeyStoreFormat.JKS,
				new Date(System.currentTimeMillis() + 31536000_000l), 2048);
	}

	/**
	 * Until when is the certificate valid, only used for the key store creation
	 * 
	 * @return 
	 */
	public Date getValidUntil() {
		return validUntil;
	}

	/**
	 * Until when the certificate should be valid
	 * 
	 * @param validUntil 
	 */
	public void setValidUntil(Date validUntil) {
		this.validUntil = validUntil;
	}

	/**
	 * For how many day from now should the certificate be valid
	 * 
	 * @param days 
	 */
	public void setValidFor(int days) {
		long until = System.currentTimeMillis() + days * 86400_000l;
		this.validUntil = new Date(until);
	}

	/**
	 * Size in bits when creating a private key
	 * 
	 * @return 
	 */
	public int getPrivateKeySize() {
		return privateKeySize;
	}

	/**
	 * The complexity of the RSA key, must be greater than 1024 bits
	 * 
	 * @param keySize 
	 */
	public void setPrivateKeySize(int keySize) {
		this.privateKeySize = keySize;
	}

	/**
	 * Specify the certificate Relative Distinguished Name (RDN).
	 *
	 * @see BCStyle
	 * @param name What we want to specify, like {@link BCStyle#CN}
	 * @param value
	 */
	public void addRDN(ASN1ObjectIdentifier name, String value) {
		this.rdn.put(name, value);
	}

	/**
	 * Remove element from the certificate name
	 * 
	 * @param name 
	 */
	public void removeRDN(ASN1ObjectIdentifier name) {
		this.rdn.remove(name);
	}

	/**
	 * Get the whole certificate name as a unmodifiable map
	 * 
	 * @return 
	 */
	public Map<ASN1ObjectIdentifier, String> getRDN() {
		return Collections.unmodifiableMap(rdn);
	}

	@Override
	public void validate() {
		super.validate();
		if (rdn.isEmpty()) {
			throw new InvalidParameterException("At least one argument of the RDN must be provided");
		}
		if (validUntil == null) {
			throw new InvalidParameterException("A expiration date must be set");
		}
		if (privateKeySize < 1024) {
			throw new InvalidParameterException("Keys below 1024 bits are insecure (consider using 2048 or higher)");
		}
	}
}
