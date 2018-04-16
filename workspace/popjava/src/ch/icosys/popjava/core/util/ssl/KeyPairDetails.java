package popjava.util.ssl;

import java.security.InvalidParameterException;
import java.util.*;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.style.BCStyle;

/**
 * This class is meant to be send to {@link SSLUtils#generateKeyPair(popjava.util.ssl.KeyPairDetails) }
 * 
 * @author Davide Mazzoleni
 */
public class KeyPairDetails {
	
	protected String alias;
	protected Date validUntil;
	protected int privateKeySize;
	protected final Map<ASN1ObjectIdentifier, String> rdn = new HashMap<>();

	// validation
	boolean hasName = false;

	/**
	 * Empty constructor
	 */
	public KeyPairDetails() {
	}
	
	/**
	 * Copy constructor
	 * 
	 * @param other the other KeyPair
	 */
	public KeyPairDetails(KeyPairDetails other) {
		Objects.requireNonNull(other);
		this.alias = other.alias;
		this.validUntil = other.validUntil;
		this.privateKeySize = other.privateKeySize;
		this.rdn.putAll(other.rdn);
		this.hasName = other.hasName;
	}
	
	/**
	 * Full constructor
	 * 
	 * @param alias The alias of this node, used to find its own public certificate
	 * @param validUntil Until when the certificate should be valid
	 * @param privateKeySize The complexity of the RSA key, must be greater than 1024 bits
	 */
	public KeyPairDetails(String alias, Date validUntil, int privateKeySize) {
		this.alias = alias;
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
	 *  validity := 365 days
	 *  keySize := 2048 bits
	 *
	 * @param alias The alias of this node, used to find its own public certificate
	 */
	public KeyPairDetails(String alias) {
		this(alias, new Date(System.currentTimeMillis() + 31536000_000L), 2048);
	}
	
	/**
	 * The alias of the certificate
	 * 
	 * @return the key alias
	 */
	public String getAlias() {
		return alias;
	}

	/**
	 * Set the alias of the certificate
	 * 
	 * @param alias the new alias of the key
	 */
	public void setAlias(String alias) {
		this.alias = alias;
	}

	/**
	 * Until when is the certificate valid, only used for the key store creation
	 * 
	 * @return the validity of the certificate
	 */
	public Date getValidUntil() {
		return validUntil;
	}

	/**
	 * Until when the certificate should be valid
	 * 
	 * @param validUntil the new validity date
	 */
	public void setValidUntil(Date validUntil) {
		this.validUntil = validUntil;
	}

	/**
	 * For how many day from now should the certificate be valid
	 * 
	 * @param days the number of days the certificate will be valid from today (now)
	 */
	public void setValidFor(int days) {
		long until = System.currentTimeMillis() + days * 86400_000L;
		this.validUntil = new Date(until);
	}

	/**
	 * Size in bits when creating a private key
	 * 
	 * @return the size of the private key in bits
	 */
	public int getPrivateKeySize() {
		return privateKeySize;
	}

	/**
	 * The complexity of the RSA key, must be greater than 1024 bits
	 * 
	 * @param keySize the new key size
	 */
	public void setPrivateKeySize(int keySize) {
		this.privateKeySize = keySize;
	}

	/**
	 * Specify the certificate Relative Distinguished Name (RDN).
	 *
	 * @see BCStyle
	 * @param name What we want to specify, like {@link BCStyle#CN}
	 * @param value the value for the RDN key
	 */
	public void addRDN(ASN1ObjectIdentifier name, String value) {
		this.rdn.put(name, value);
	}

	/**
	 * Remove element from the certificate name
	 * 
	 * @param name the RDN (key,value) pair to remove
	 */
	public void removeRDN(ASN1ObjectIdentifier name) {
		this.rdn.remove(name);
	}

	/**
	 * Get the whole certificate name as a unmodifiable map
	 * 
	 * @return the RDN which of this key
	 */
	public Map<ASN1ObjectIdentifier, String> getRDN() {
		return Collections.unmodifiableMap(rdn);
	}

	/**
	 * Check that the parameters are valid
	 */
	public void validate() {
		if (alias == null || alias.isEmpty()) {
			throw new InvalidParameterException("An alias must be given and not empty");
		}
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
