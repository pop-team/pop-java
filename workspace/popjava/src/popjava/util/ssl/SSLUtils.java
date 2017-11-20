package popjava.util.ssl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.Collections;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.TrustManager;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStrictStyle;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcContentSignerBuilder;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;
import popjava.combox.ssl.POPKeyManager;
import popjava.combox.ssl.POPTrustManager;
import popjava.service.jobmanager.network.POPNode;
import popjava.util.Configuration;
import popjava.util.LogWriter;
import popjava.util.MethodUtil;

/**
 * Utilities for using SSL and certificates
 * 
 * @author Davide Mazzoleni
 */
public class SSLUtils {
	
	/** Single instance of SSLContext for each process */
	private static SSLContext sslContextInstance = null;
	/** Single instance of POPTrustManager */
	private static POPTrustManager trustManager = null;
	/** Single instance of POPKeyManager */
	private static POPKeyManager keyManager = null;
	/** Factory to create X.509 certificate from RSA text input */
	private static CertificateFactory certFactory;

	/** Keep track of the keystore location so we can reload it */
	private static File keyStoreLocation = null;
	
	private static final Configuration conf = Configuration.getInstance();
	
	/** A secure random for the whole class */
	private static final SecureRandom RANDOM = new SecureRandom();

	
	// static initialization of objects
	static {
		try {
			certFactory = CertificateFactory.getInstance("X.509");
		} catch(Exception e) {

		}
	}
	
	private SSLUtils() {
	}
	
	/**
	 * Load the KeyStore with the parameters from {@link Configuration#getSSLKeyStoreOptions()}
	 * 
	 * @return
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws KeyStoreException 
	 */
	private static KeyStore loadKeyStore() throws IOException, NoSuchAlgorithmException, CertificateException, KeyStoreException {
		KeyStore keyStore = KeyStore.getInstance(conf.getSSLKeyStoreFormat().name());
		keyStore.load(new FileInputStream(conf.getSSLKeyStoreFile()), conf.getSSLKeyStorePassword().toCharArray());
		return keyStore;
	}
	
	/**
	 * Save the KeyStore with the parameters from {@link Configuration#getSSLKeyStoreOptions()}
	 * 
	 * @param keyStore
	 * @throws KeyStoreException
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws Exception 
	 */
	private static void storeKeyStore(KeyStore keyStore) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, Exception {
		try (FileOutputStream fos = new FileOutputStream(conf.getSSLKeyStoreFile())) {
			keyStore.store(fos, conf.getSSLKeyStorePassword().toCharArray());
		}
	}
	
	/**
	 * Get a correctly initialized SSLContext
	 * 
	 * @return 
	 * @throws java.security.KeyStoreException 
	 * @throws java.io.IOException 
	 * @throws java.security.NoSuchAlgorithmException
	 * @throws java.security.cert.CertificateException
	 * @throws java.security.UnrecoverableKeyException
	 * @throws java.security.KeyManagementException
	 */
	public static SSLContext getSSLContext() throws KeyStoreException, IOException, NoSuchAlgorithmException, 
		CertificateException, UnrecoverableKeyException, KeyManagementException {
		// init SSLContext once
		if (sslContextInstance == null || !conf.getSSLTemporaryCertificateLocation().equals(keyStoreLocation)) {
			keyStoreLocation = conf.getSSLTemporaryCertificateLocation();
			// custom POP managers
			if (trustManager == null) {
				trustManager = new POPTrustManager();
				keyManager = new POPKeyManager();
				TrustManager[] trustManagers = new TrustManager[]{ trustManager };
				KeyManager[] keyManagers = new KeyManager[] { keyManager };

				// create the context the first time, and update it when necessary
				if (sslContextInstance == null) {
					sslContextInstance = SSLContext.getInstance(conf.getSSLProtocolVersion());
				}
				// init ssl context with everything
				sslContextInstance.init(keyManagers, trustManagers, RANDOM);
			} else {
				trustManager.reloadTrustManager();
				keyManager.reloadKeyManager();
			}
		}

		return sslContextInstance;
	}
	
	/**
	 * Invalidate all open SSL Sessions because there was a change in certificate somewhere.
	 */
	public static void invalidateSSLSessions() {
		if (sslContextInstance == null) {
			return;
		}
		invalidateSSLSessions(sslContextInstance.getClientSessionContext());
		invalidateSSLSessions(sslContextInstance.getServerSessionContext());
	}
	
	/**
	 * Given a SessionContext, it invalidate all of its tokens.
	 * 
	 * @param context 
	 */
	private static void invalidateSSLSessions(SSLSessionContext context) {
		for (Enumeration<byte[]> sessionEnum = context.getIds(); sessionEnum.hasMoreElements();) {
			byte[] id = sessionEnum.nextElement();
			SSLSession session = context.getSession(id);
			session.invalidate();
		}
	}

	/**
	 * Forcefully reload the Trust and Key Managers if they exists.
	 */
	public static void reloadPOPManagers() {
		try {
			if (trustManager != null) {
				trustManager.reloadTrustManager();
			}
			if (keyManager != null) {
				keyManager.reloadKeyManager();
			}
		} catch(Exception e) {
			LogWriter.writeDebugInfo("[SSLUtils] Failed to reload Managers: %s", e.getCause());
			LogWriter.writeExceptionLog(e);
		}
	}
	
	/**
	 * A constant hash String identifier from a node.
	 * NOTE: aliases seems to be all lowercase
	 * 
	 * @param node
	 * @param networkUUID 
	 * @return 
	 */
	private static String confidenceLinkAlias(POPNode node, String networkUUID) {
		return String.format("%x@%s", node.hashCode(), networkUUID.toLowerCase());
	}
	
	/**
	 * Add or Replace confidence link
	 * 
	 * @param node
	 * @param certificate
	 * @param networkUUID 
	 * @param mode false if we want to add the certificate, true if we want to replace it
	 * @throws IOException 
	 */
	private static void addConfidenceLink(POPNode node, Certificate certificate, String networkUUID, boolean mode) throws IOException {
		try {
			// load the already existing keystore
			KeyStore keyStore = loadKeyStore();

			// node identifier
			String nodeAlias = confidenceLinkAlias(node, networkUUID);

			// exit if already have the node, use replaceConfidenceLink if you want to change certificate
			List<String> aliases = Collections.list(keyStore.aliases());
			// if `mode' is true the certificate must be present
			// if `mode' is false the certificate must NOT be present
			if (aliases.contains(nodeAlias) ^ mode) {
				return;
			} else {
				// add/replace a new entry
				keyStore.setCertificateEntry(nodeAlias, certificate);
			}

			// override the existing keystore
			storeKeyStore(keyStore);
		} catch(Exception e) {
			throw new IOException("Failed to save Confidence Link in KeyStore.");
		}
	}
	
	/**
	 * Add a new certificate to the keystore, this will be written anew on disk.
	 * We use the node's hash as alias to identify the match.
	 * 
	 * @param node A node created somehow, directly or with the factory
	 * @param certificate The certificate we want to add as a confidence link
	 * @param networkUUID The network associated to this certificate
	 * @throws IOException If we were not able to write to file
	 */
	public static void addConfidenceLink(POPNode node, Certificate certificate, String networkUUID) throws IOException {
		addConfidenceLink(node, certificate, networkUUID, false);
	}
	
	/**
	 * Add a new certificate to the keystore, this will be written anew on disk.
	 * We use the node's hash as alias to identify the match.
	 * 
	 * @param node A node created somehow, directly or with the factory
	 * @param certificate The certificate we want to add as a confidence link
	 * @param networkUUID The network associated to this certificate
	 * @throws IOException If we were not able to write to file
	 */
	public static void replaceConfidenceLink(POPNode node, Certificate certificate, String networkUUID) throws IOException {
		addConfidenceLink(node, certificate, networkUUID, true);
	}
	
	/**
	 * Remove an entry from the keystore, this will be written anew on disk.
	 * We use the node's hash as alias to identify the match.
	 * 
	 * @param node A node created somehow, directly or with the factory
	 * @param networkUUID The ID of the network
	 * @throws IOException Many
	 */
	public static void removeConfidenceLink(POPNode node, String networkUUID) throws IOException {
		// node identifier
		String nodeAlias = confidenceLinkAlias(node, networkUUID);
		removeAlias(nodeAlias);
	}
	
	/**
	 * Remove form the KeyStore the specified alias.
	 * 
	 * @param alias
	 * @throws java.io.IOException
	 */
	public static void removeAlias(String alias) throws IOException {
		try {
			// load the already existing keystore
			KeyStore keyStore = loadKeyStore();

			// exit if already have the node, use replaceConfidenceLink if you want to change certificate
			List<String> aliases = Collections.list(keyStore.aliases());
			if (!aliases.contains(alias)) {
				return;
			}

			// add a new entry
			keyStore.deleteEntry(alias);

			// override the existing keystore
			storeKeyStore(keyStore);
		} catch(Exception e) {
			throw new IOException("Failed to remove alias [" + alias + "] from KeyStore.");
		}
	}
	
	/**
	 * Get the SHA-1 fingerprint of a certificate from its byte array representation
	 * 
	 * @param certificate
	 * @return 
	 */
	public static String certificateFingerprint(byte[] certificate) {
		try {
			// load certificate
			ByteArrayInputStream fi = new ByteArrayInputStream(certificate);
			Certificate cert = certFactory.generateCertificate(fi);
			fi.close();
			// compute hash
			return SSLUtils.certificateFingerprint(cert);
		} catch(CertificateException | IOException e) {
		}
		return null;
	}
	
	/**
	 * Get the SHA-1 fingerprint of a certificate
	 * 
	 * @see https://stackoverflow.com/questions/1270703/how-to-retrieve-compute-an-x509-certificates-thumbprint-in-java
	 * @param cert
	 * @return The hex representation of the fingerprint
	 */
	public static String certificateFingerprint(Certificate cert) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			byte[] der = cert.getEncoded();
			md.update(der);
			byte[] digest = md.digest();
			final StringBuilder builder = new StringBuilder();
			for(byte b : digest) {
				builder.append(String.format("%02X", b));
			}
			return builder.toString();
		} catch (NoSuchAlgorithmException | CertificateEncodingException ex) {
		}
		return null;
	}
	
	/**
	 * Get the bytes of a certificate
	 * 
	 * @param cert
	 * @return 
	 */
	public static byte[] certificateBytes(Certificate cert) {
		StringBuilder sb = new StringBuilder();
		try {
			sb.append("-----BEGIN CERTIFICATE-----\n");
			sb.append(Base64.getEncoder().encodeToString(cert.getEncoded())).append("\n");
			sb.append("-----END CERTIFICATE-----\n");
		} catch(CertificateEncodingException e) {
		}
		return sb.toString().getBytes(StandardCharsets.UTF_8);
	}
	
	/**
	 * Transform a certificate byte array to a real certificate.
	 * 
	 * @param certificate A byte array in PEM format
	 * @return The certificate or null
	 * @throws CertificateException 
	 */
	public static Certificate certificateFromBytes(byte[] certificate) throws CertificateException {
		Certificate cert = null;
		try (ByteArrayInputStream fi = new ByteArrayInputStream(certificate)) {
			cert = certFactory.generateCertificate(fi);
		} catch(IOException e) {
			LogWriter.writeDebugInfo("[SSLUtils] invalid array for certificate conversion: %s", e.getMessage());
		}
		return cert;
	}

	/**
	 * Check if the given certificate is inside the Trust Manager.
	 * 
	 * @param certificate
	 * @return 
	 */
	public static boolean isCertificateKnown(Certificate certificate) {
		ensureManagerCreation();
		return trustManager.isCertificateKnown(certificate);
	}

	/**
	 * Try to extract the network certificate from the fingerprint, and the alias inside the KeyStore.
	 * 
	 * @param fingerprint
	 * @return 
	 */
	public static String getNetworkFromCertificate(String fingerprint) {
		ensureManagerCreation();
		return trustManager.getNetworkFromCertificate(fingerprint);
	}

	/**
	 * Given a fingerprint (SHA1) it will return the Public Key associated with it.
	 * 
	 * @param fingerprint
	 * @return 
	 */
	public static Certificate getCertificate(String fingerprint) {
		ensureManagerCreation();
		return trustManager.getCertificate(fingerprint);
	}

	/**
	 * Given a UUID it will return the matching local public certificate for this network.
	 * 
	 * @param uuid
	 * @return 
	 */
	public static Certificate getCertificateFromAlias(String uuid) {
		ensureManagerCreation();
		return trustManager.getCertificateFromAlias(uuid);
	}

	/**
	 * Return if a given fingerprint certificate is part of the Confidence Link group.
	 * 
	 * @param fingerprint
	 * @return 
	 */
	public static boolean isConfidenceLink(String fingerprint) {
		ensureManagerCreation();
		return trustManager.isConfidenceLink(fingerprint);
	}

	/**
	 * Add a new certificate to the temporary storage
	 * 
	 * @see #addCertToTempStore(byte[], boolean) 
	 * @param certificate 
	 */
	public static void addCertToTempStore(byte[] certificate) {
		/*MethodUtil.grant(
			"popjava.service.jobmanager.POPJavaJobManager.askResourcesDiscovery",
			"popjava.service.jobmanager.POPJavaJobManager.callbackResult",
			"popjava.service.jobmanager.network.POPConnectorJobManager.askResourcesDiscoveryAction",
			"popjava.service.jobmanager.network.POPConnectorTFC.askResourcesDiscoveryAction"
		);*/
		addCertToTempStore(certificate, true);
	}
	
	/**
	 * Add a new certificate to the temporary store
	 * 
	 * @param certificate
	 * @param reload 
	 */
	public static void addCertToTempStore(byte[] certificate, boolean reload) {
		/*MethodUtil.grant(
			"popjava.util.ssl.SSLUtils.addCertToTempStore",
			"popjava.base.POPObject.PopRegisterFutureConnectorCertificate",
			"popjava.interfacebase.Interface.deserialize"
		);*/
		ensureManagerCreation();
		try {			
			// load it
			Certificate cert = certificateFromBytes(certificate);
			
			// stop if already loaded
			if (trustManager.isCertificateKnown(cert)) {
				return;
			}
			
			// certificate output name
			String fingerprint = SSLUtils.certificateFingerprint(cert);
			String outName = fingerprint + ".cer";
			
			// certificates temprary path
			Path path = Paths.get(conf.getSSLTemporaryCertificateLocation().toString(), outName);
			// move to local directory
			Files.write(path, certificate);
			
			// handle local reload
			if (reload) {
				trustManager.reloadTrustManager();
			}
		} catch (Exception ex) {
			LogWriter.writeDebugInfo("[SSLUtils] failed to save certificate: ", ex.getMessage());
		}
	}
	
	/**
	 * Call {@link #getSSLContext() } and create the two manager if they don't exists.
	 */
	private static void ensureManagerCreation() {
		try {
			getSSLContext();
		} catch(Exception e) {}
	}
	
	/**
	 * Create a new KeyStore with a new Private Key and Certificate
	 * 
	 * @param ksOptions details on the key store
	 * @param keyOptions  details on the key we want to generate
	 * @return true if we were able to create the keystore
	 */
	public static boolean generateKeyStore(KeyStoreDetails ksOptions, KeyPairDetails keyOptions) {
		// something the key generated seems to be invalid (invalidated by bouncycastle)
		// we retry for a while in that case
		int limit = 30;
		boolean generated = false;
		KeyStore.PrivateKeyEntry privateKeyEntry = null;
		do {
			try {
				privateKeyEntry = generateKeyPair(keyOptions);
				generated = true;
			} catch(Exception e) {
				LogWriter.writeDebugInfo("[KeyStore] Secure Private Key generation problem. Retrying after message: %s.", e.getMessage());
			}
			
			if (limit-- <= 0) {
				return false;
			}
		} while (!generated);
		
		try {
			addKeyEntryToKeyStore(ksOptions, keyOptions, privateKeyEntry, false);
			generated = true;
		} catch(IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException | UnrecoverableKeyException e) {
			LogWriter.writeDebugInfo("[KeyStore] Generation failed with message: %s.", e.getMessage());
			generated = false;
		}
		
		return generated;
	}
	
	/**
	 * Given the keystore information, the key pair details and a real Priva Key / Certificate pair, add it to the keystore.
	 * @param ksOptions
	 * @param keyOptions
	 * @param privateKeyEntry
	 * @throws IOException
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws UnrecoverableKeyException 
	 */
	public static void addKeyEntryToKeyStore(KeyStoreDetails ksOptions, KeyPairDetails keyOptions, KeyStore.PrivateKeyEntry privateKeyEntry) throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException {
		addKeyEntryToKeyStore(ksOptions, keyOptions, privateKeyEntry, true);
	}
	
	/**
	 * Given the keystore information, the key pair details and a real Priva Key / Certificate pair, add it to the keystore.
	 * 
	 * @param ksOptions
	 * @param keyOptions
	 * @param privateKeyEntry
	 * @throws IOException
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException 
	 * @throws java.security.UnrecoverableKeyException 
	 */
	private static void addKeyEntryToKeyStore(KeyStoreDetails ksOptions, KeyPairDetails keyOptions, KeyStore.PrivateKeyEntry privateKeyEntry, boolean reload) throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException {
		// initialize a keystore
		KeyStore ks = KeyStore.getInstance(ksOptions.getKeyStoreFormat().name());
		try (InputStream in = new FileInputStream(ksOptions.getKeyStoreFile())) {
			ks.load(in, ksOptions.getKeyStorePassword().toCharArray());
		} catch(Exception e) {
			ks.load(null);
		}

		// add private key to the new keystore
		KeyStore.PasswordProtection passwordProtection = new KeyStore.PasswordProtection(ksOptions.getPrivateKeyPassword().toCharArray());
		ks.setEntry(keyOptions.getAlias(), privateKeyEntry, passwordProtection);

		// write to memory
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ks.store(out, ksOptions.getKeyStorePassword().toCharArray());
		out.close();

		// load a "clean" version and save to disk
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		KeyStore ksout = KeyStore.getInstance(ksOptions.getKeyStoreFormat().name());
		ksout.load(in, ksOptions.getKeyStorePassword().toCharArray());
		ksout.store(new FileOutputStream(ksOptions.getKeyStoreFile()), ksOptions.getKeyStorePassword().toCharArray());
		
		if (reload && trustManager != null) {
			trustManager.reloadTrustManager();
			keyManager.reloadKeyManager();
		}
	}
	
	/**
	 * Generate a Private Key and a corresponding public certificate.
	 * This process may fail if bouncycastle consider the generate key not to be secure.
	 * 
	 * @param options
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 * @throws OperatorCreationException
	 * @throws CertificateException 
	 */
	public static KeyStore.PrivateKeyEntry generateKeyPair(KeyPairDetails options) throws NoSuchAlgorithmException, IOException, OperatorCreationException, CertificateException, IllegalArgumentException {		
		options.validate();
		
		// generate keys
		KeyPairGenerator pairGenerator = KeyPairGenerator.getInstance("RSA");
		pairGenerator.initialize(options.getPrivateKeySize());
		KeyPair pair = pairGenerator.generateKeyPair();

		// public certificate setup
		RSAPublicKey rsaPublicKey = (RSAPublicKey) pair.getPublic();
		RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) pair.getPrivate();


		// generate certificate from key pair
		SubjectPublicKeyInfo pubKey;
		try (InputStream der = new ByteArrayInputStream(rsaPublicKey.getEncoded());
				ASN1InputStream asn1InputStream	= new ASN1InputStream(der)) {
			pubKey = SubjectPublicKeyInfo.getInstance((ASN1Sequence) asn1InputStream.readObject());
		}

		// name of the certificate (RDN) -> OU=Group,O=Org,CN=Myself
		X500NameBuilder nameBuilder = new X500NameBuilder(new BCStrictStyle());
		for (Map.Entry<ASN1ObjectIdentifier, String> entry : options.getRDN().entrySet()) {
			nameBuilder.addRDN(entry.getKey(), entry.getValue());
		}

		// sign ourselves
		X500Name subjectName = nameBuilder.build();
		X509v3CertificateBuilder certBuilder = new X509v3CertificateBuilder(subjectName, BigInteger.valueOf(RANDOM.nextInt()),
			GregorianCalendar.getInstance().getTime(), options.getValidUntil(), subjectName, pubKey);

		// signature for the certificate
		AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find("SHA1withRSA");
		AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);
		BcContentSignerBuilder sigGen = new BcRSAContentSignerBuilder(sigAlgId, digAlgId);

		// create RSAKeyParameters, the private key format expected by Bouncy Castle
		RSAKeyParameters keyParams = new RSAKeyParameters(true, rsaPrivateKey.getPrivateExponent(), rsaPrivateKey.getModulus());

		ContentSigner contentSigner = sigGen.build(keyParams);
		X509CertificateHolder certificateHolder = certBuilder.build(contentSigner);

		// convert the X509Certificate from BouncyCastle format to the java.security format
		JcaX509CertificateConverter certConverter = new JcaX509CertificateConverter();
		X509Certificate x509Certificate = certConverter.getCertificate(certificateHolder);
		
		// keyStore entry for private key, Key and Certificate
		KeyStore.PrivateKeyEntry privateKeyEntry = new KeyStore.PrivateKeyEntry(rsaPrivateKey, new Certificate[]{x509Certificate});

		return privateKeyEntry;
	}
}
