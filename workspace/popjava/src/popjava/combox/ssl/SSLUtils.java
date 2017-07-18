package popjava.combox.ssl;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import popjava.util.Configuration;

/**
 * Utilities for the 
 * 
 * @author Davide Mazzoleni
 */
public class SSLUtils {
	
	/** Single instance of SSLContext for each process */
	private static SSLContext sslContextInstance = null;
	/** Factory to create X.509 certificate from RSA text input */
	private static CertificateFactory certFactory;

	
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
	 * Get a correctly initialized SSLContext
	 * 
	 * @return 
	 * @throws java.lang.Exception A lot of potential exceptions
	 */
	public static SSLContext getSSLContext() throws Exception {
		// init SSLContext once
		if (sslContextInstance == null) {
			// load private key
			KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			keyStore.load(new FileInputStream(Configuration.TRUST_STORE), Configuration.TRUST_STORE_PWD.toCharArray());
			KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			keyManagerFactory.init(keyStore, Configuration.TRUST_STORE_PK_PWD.toCharArray());
			TrustManager[] trustManagers = new TrustManager[]{ POPTrustManager.getInstance() };

			// init ssl context with everything
			sslContextInstance = SSLContext.getInstance(Configuration.SSL_PROTOCOL);
			sslContextInstance.init(keyManagerFactory.getKeyManagers(), trustManagers, new SecureRandom());
		}

		return sslContextInstance;
	}
	
	
	/**
	 * Get the SHA-1 thumbprint of a certificate from its byte array representation
	 * 
	 * @param certificate
	 * @return 
	 */
	public static String certificateThumbprint(byte[] certificate) {
		try {
			// load certificate
			ByteArrayInputStream fi = new ByteArrayInputStream(certificate);
			Certificate cert = certFactory.generateCertificate(fi);
			fi.close();
			// compute hash
			return certificateThumbprint(cert);
		} catch(CertificateException | IOException e) {
		}
		return null;
	}
	
	/**
	 * Get the SHA-1 thumbprint of a certificate
	 * 
	 * @see https://stackoverflow.com/questions/1270703/how-to-retrieve-compute-an-x509-certificates-thumbprint-in-java
	 * @param cert
	 * @return The hex representation of the thumbprint
	 */
	public static String certificateThumbprint(Certificate cert) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			byte[] der = cert.getEncoded();
			md.update(der);
			byte[] digest = md.digest();
			// TODO is there a better way to do this?
			return javax.xml.bind.DatatypeConverter.printHexBinary(digest);
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
			sb.append(javax.xml.bind.DatatypeConverter.printBase64Binary(cert.getEncoded())).append("\n");
			sb.append("-----END CERTIFICATE-----\n");
		} catch(CertificateEncodingException e) {
		}
		return sb.toString().getBytes();
	}
	
	/**
	 * The local public certificate
	 * 
	 * @return null or a certificate that can be transformed with {@link #getCertificateBytes}
	 */
	public static Certificate getLocalPublicCertificate() {
		return POPTrustManager.getLocalPublicCertificate();
	}

	/**
	 * Add a new certificate to the temporary storerage
	 * 
	 * @see #addCertToTempStore(byte[], boolean) 
	 * @param certificate 
	 */
	public static void addCertToTempStore(byte[] certificate) {
		addCertToTempStore(certificate, false);
	}
	
	/**
	 * Add a new certificate to the temporary store
	 * 
	 * @param certificate
	 * @param reload 
	 */
	public static void addCertToTempStore(byte[] certificate, boolean reload) {
		try {
			// load it
			ByteArrayInputStream fi = new ByteArrayInputStream(certificate);
			Certificate cert = certFactory.generateCertificate(fi);
			fi.close();
			
			// stop if already loaded
			POPTrustManager trustManager = POPTrustManager.getInstance();
			if (trustManager.isCertificateKnown(cert)) {
				return;
			}
			
			// certificate output name
			String thumbprint = certificateThumbprint(cert);
			String outName = thumbprint + ".cer";
			
			// certificates temprary path
			Path path = Paths.get(Configuration.TRUST_TEMP_STORE_DIR, outName);
			// move to local directory
			Files.write(path, certificate);
			
			// handle local reload
			if (reload) {
				POPTrustManager.getInstance().reloadTrustManager();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
