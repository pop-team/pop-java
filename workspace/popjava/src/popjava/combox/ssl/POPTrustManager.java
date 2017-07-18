package popjava.combox.ssl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import popjava.util.Configuration;
import popjava.util.LogWriter;
import popjava.util.WatchDirectory;

/**
 * Two origin KeyStore TrustManager, single instance with Directory Watch and auto-reload.
 * @see https://jcalcote.wordpress.com/2010/06/22/managing-a-dynamic-java-trust-store/
 * @author John Calcote
 * @author Davide Mazzoleni
 */
public class POPTrustManager implements X509TrustManager {
	
	private class WatcherMethods extends WatchDirectory.WatchMethod {
		Set<String> permutations = new HashSet<>();
		
		@Override
		public void create(String file) {
			if (!permutations.contains(file)) {
				permutations.add(file);
				
				// skip reload if its our certificate
				if (file.equals(instance.skipCertificate)) {
					instance.skipCertificate = null;
				} else {
					reload();
				}
			}
		}

		@Override
		public void delete(String file) {
			permutations.remove(file);
			reload();
		}
		
		private void reload() {
			try {
				// reload certificates
				reloadTrustManager();
			} catch(Exception e) {}
		}
	}

	// access to keystore
	private final String trustStorePath;
	private final String trustStorePass;
	private final String tempTrustStorePath;
	
	// certificates stores
	private X509TrustManager trustManager;
	private Map<String,Certificate> loadedCertificates;
	private Set<String> confidenceCertificates;
	
	// reload and add new certificates
	private WatchDirectory watcher;
	// don't reload whole manager if we add a certificate manually
	private String skipCertificate;
	
	// easy access
	private static Certificate publicCertificate;
	private static CertificateFactory certFactory;
	private static POPTrustManager instance;
        
	// static initializations
	static {
		try {
			certFactory = CertificateFactory.getInstance("X.509");
			instance = new POPTrustManager();
		} catch(Exception e) {

		}
	}
	
	private POPTrustManager() {
		this.trustStorePath = Configuration.TRUST_STORE;
		this.trustStorePass = Configuration.TRUST_STORE_PWD;
		this.tempTrustStorePath = Configuration.TRUST_TEMP_STORE_DIR;
		try {
			loadedCertificates = new HashMap<>();
			confidenceCertificates = new HashSet<>();
			watcher = new WatchDirectory(tempTrustStorePath, new WatcherMethods());
			Thread dirWatcher = new Thread(watcher, "TrustStore temporary folder watcher");
			dirWatcher.setDaemon(true);
			dirWatcher.start();
			reloadTrustManager();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static POPTrustManager getInstance() {
		return instance;
	}
	
	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		trustManager.checkClientTrusted(chain, authType);
	}

	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		trustManager.checkServerTrusted(chain, authType);
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		X509Certificate[] issuers = trustManager.getAcceptedIssuers();
		return issuers;
	}
	
	/**
	 * Tell if a certificate is confidence link certificate or a temporary link
	 * 
	 * @param thumbprint The identifier of the certificate
	 * @return true if it's a confidence link, false otherwise
	 */
	public boolean isConfidenceLink(String thumbprint) {
		return confidenceCertificates.contains(thumbprint);
	}
	
	private void saveCertificatesLocally() {
		loadedCertificates.clear();
		Certificate[] certificates = getAcceptedIssuers();
		for (Certificate cert : certificates) {
			loadedCertificates.put(getCertificateThumbprint(cert), cert);
		}
	}

	private void reloadTrustManager() throws Exception {
		long start = System.currentTimeMillis();
		// load keystore from specified cert store (or default)
		KeyStore trustedKS = KeyStore.getInstance(KeyStore.getDefaultType());
		InputStream trustedStore = new FileInputStream(trustStorePath);
		try {
			// load stores in memory
			trustedKS.load(trustedStore, trustStorePass.toCharArray());			
		} finally {
			trustedStore.close();
		}
		
		// mark certificate in the keystore as confidence certificates
		confidenceCertificates.clear();
		for (Enumeration<String> eAlias = trustedKS.aliases(); eAlias.hasMoreElements();) {
			String alias = eAlias.nextElement();
			Certificate cert = trustedKS.getCertificate(alias);
			confidenceCertificates.add(getCertificateThumbprint(cert));
                        
			// save public certificate
			if (alias.equals(Configuration.TRUST_STORE_PK_ALIAS)) {
				publicCertificate = cert;
			}
		}
		
		// add temporary certificates
		// get all files in directory and add them
		for (File file : new File(tempTrustStorePath).listFiles()) {
			if (file.isFile() && file.getName().endsWith(".cer")) {
				try {
					Certificate cert = certFactory.generateCertificate(new FileInputStream(file));
					trustedKS.setCertificateEntry(file.getName(), cert);
				} catch(Exception e) {
				}
			}
		}
		
		// initialize a new TMF with the trustedKS we just loaded
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(trustedKS);
		
		long end = System.currentTimeMillis();
		LogWriter.writeDebugInfo(String.format("[KeyStore] initiated in %d ms", end - start));

		// acquire X509 trust manager from factory
		TrustManager tms[] = tmf.getTrustManagers();
		for (int i = 0; i < tms.length; i++) {
			if (tms[i] instanceof X509TrustManager) {
				trustManager = (X509TrustManager) tms[i];
				saveCertificatesLocally();
				return;
			}
		}

		throw new NoSuchAlgorithmException("No X509TrustManager in TrustManagerFactory");
	}
	
	/**
	 * Do we know the certificate
	 * 
	 * @param cert
	 * @return 
	 */
	public boolean isCertificateKnown(Certificate cert) {
		return loadedCertificates.values().contains(cert);
	}

	/**
	 * Add a certificate and don't reload locally
	 * 
	 * @see #addCertToTempStore(byte[], boolean) 
	 * @param certificate 
	 */
	public void addCertToTempStore(byte[] certificate) {
		addCertToTempStore(certificate, false);
	}
	
	/**
	 * Add a new certificate to the temporary store
	 * 
	 * @param certificate
	 * @param reload 
	 */
	public void addCertToTempStore(byte[] certificate, boolean reload) {
		try {
			// load it
			ByteArrayInputStream fi = new ByteArrayInputStream(certificate);
			Certificate cert = certFactory.generateCertificate(fi);
			fi.close();
			
			// stop if already loaded
			String thumbprint = getCertificateThumbprint(cert);
			if (loadedCertificates.keySet().contains(thumbprint)) {
				return;
			}
			
			// certificate output name
			String outName = thumbprint + ".cer";
			
			// certificates temprary path
			Path path = Paths.get(Configuration.TRUST_TEMP_STORE_DIR, outName);
			// move to local directory
			Files.write(path, certificate);
			
			// handle local reload
			if (reload) {
				skipCertificate = outName;
				reloadTrustManager();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static Certificate getLocalPublicCertificate() {
		return publicCertificate;
	}
	
	public Certificate getCertificate(String thumbprint) {
		return loadedCertificates.get(thumbprint);
	}
	
	/**
	 * Get the bytes of a certificate
	 * 
	 * @param cert
	 * @return 
	 */
	public static byte[] getCertificateBytes(Certificate cert) {
		StringBuilder sb = new StringBuilder();
		try {
			sb.append("-----BEGIN CERTIFICATE-----\n");
			sb.append(javax.xml.bind.DatatypeConverter.printBase64Binary(cert.getEncoded())).append("\n");
			sb.append("-----END CERTIFICATE-----\n");
		} catch(CertificateEncodingException e) {
		}
		return sb.toString().getBytes();
	}
	
	public byte[] getCertificateBytes(String thumbprint) {
		return getCertificateBytes(getCertificate(thumbprint));
	}
	
	/**
	 * Get the SHA-1 thumbprint of a certificate
	 * 
	 * @see https://stackoverflow.com/questions/1270703/how-to-retrieve-compute-an-x509-certificates-thumbprint-in-java
	 * @param cert
	 * @return The hex representation of the thumbprint
	 */
	public static String getCertificateThumbprint(Certificate cert) {
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
	 * Get the SHA-1 thumbprint of a certificate from its byte array representation
	 * 
	 * @param certificate
	 * @return 
	 */
	public static String getCertificateThumbprint(byte[] certificate) {
		try {
			// load certificate
			ByteArrayInputStream fi = new ByteArrayInputStream(certificate);
			Certificate cert = certFactory.generateCertificate(fi);
			fi.close();
			// compute hash
			return getCertificateThumbprint(cert);
		} catch(CertificateException | IOException e) {
		}
		return null;
	}
	
	/**
	 * Get a correctly initialized SSLContext
	 * 
	 * @return 
	 * @throws java.lang.Exception A lot of potential exceptions
	 */
	public static SSLContext getNewSSLContext() throws Exception {
		// init SSLContext to create SSLSockets
		// load private key
		KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		keyStore.load(new FileInputStream(Configuration.TRUST_STORE), Configuration.TRUST_STORE_PWD.toCharArray());
		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		keyManagerFactory.init(keyStore, Configuration.TRUST_STORE_PK_PWD.toCharArray());
		// https://jcalcote.wordpress.com/2010/06/22/managing-a-dynamic-java-trust-store/
		TrustManager[] trustManagers = new TrustManager[]{ POPTrustManager.getInstance() };

		// init ssl context with everything
		SSLContext sslContext = SSLContext.getInstance(Configuration.SSL_PROTOCOL);
		sslContext.init(keyManagerFactory.getKeyManagers(), trustManagers, null);

		return sslContext;
	}
}
