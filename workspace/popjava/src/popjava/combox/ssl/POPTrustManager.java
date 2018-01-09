package popjava.combox.ssl;

import popjava.util.ssl.SSLUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import popjava.util.Configuration;
import popjava.util.LogWriter;
import popjava.util.RuntimeDirectoryThread;
import popjava.util.WatchDirectory;

/**
 * Two origin KeyStore TrustManager, single instance with Directory Watch and auto-reload.
 * See https://jcalcote.wordpress.com/2010/06/22/managing-a-dynamic-java-trust-store/
 * @author John Calcote
 * @author Davide Mazzoleni
 */
public class POPTrustManager implements X509TrustManager {
	
	private class TemporaryDirectoryWatcher extends WatchDirectory.WatchMethod {
		@Override
		public void create(String file) {
			if (file.endsWith(".cer")) {
				reload();
			}
		}

		@Override
		public void delete(String file) {
			if (file.endsWith(".cer")) {
				reload();
			}
		}
		
		private void reload() {
			try {
				// reload certificates
				reloadTrustManager();
			} catch(Exception e) {}
		}
	}
	
	private class KeyStoreWatcher extends WatchDirectory.WatchMethod {
		private final Path keyStore;
		public KeyStoreWatcher(Path keyStore) {
			this.keyStore = keyStore;
		}
		@Override
		public void modify(String s) {
			// filter to handle only the keystore
			if (keyStore.equals(keyStore.getParent().resolve(s))) {
				reload();
			}
		}
		private void reload() {
			try {
				// reload certificates
				reloadTrustManager();
			} catch(Exception e) {}
		}
	}
	
	private final Configuration conf = Configuration.getInstance();
	
	// certificates store
	private X509TrustManager trustManager;
	// Map[Fingerprint, Certificate]
	private final Map<String,Certificate> loadedCertificates = new HashMap<>();
	// Set[Fingerprint]
	private final Set<String> confidenceCertificates = new HashSet<>();
	// Map[Fingerprint, Network]
	private final Map<String,String> certificatesNetwork = new HashMap<>();
	// Map[Alias, Certificate]
	private final Map<String,Certificate> aliasCertificates = new HashMap<>();
	
	// reload and add new certificates
	private WatchDirectory temporaryWatcher;
	private WatchDirectory keyStoreWatcher;
	
	public POPTrustManager() throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException {
		reloadTrustManager();
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
		return trustManager.getAcceptedIssuers();
	}
	
	/**
	 * Tell if a certificate is confidence link certificate or a temporary link
	 * 
	 * @param fingerprint The identifier of the certificate
	 * @return true if it's a confidence link, false otherwise
	 */
	public boolean isConfidenceLink(String fingerprint) {
		return confidenceCertificates.contains(fingerprint);
	}
	
	/**
	 * Get the network assigned to a specific certificate
	 * 
	 * @param fingerprint the fingerprint we want the certificate to
	 * @return the certificate or null if unknown
	 */
	public String getNetworkFromFingerprint(String fingerprint) {
		return certificatesNetwork.get(fingerprint);
	}
	
	/**
	 * Refresh loadedCertificates after a reload of the keystore or of the temp dir
	 */
	private void saveCertificatesToMemory() {
		Map<String,Certificate> temp = new HashMap<>();
		Certificate[] certificates = getAcceptedIssuers();
		for (Certificate cert : certificates) {
			temp.put(SSLUtils.certificateFingerprint(cert), cert);
		}
		// empty and swap
		loadedCertificates.clear();
		loadedCertificates.putAll(temp);
	}

	public final void reloadTrustManager() throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException {
		long start = System.currentTimeMillis();
		SSLUtils.invalidateSSLSessions();
		// load keystore from specified cert store (or default)
		KeyStore trustedKS = KeyStore.getInstance(conf.getSSLKeyStoreFormat().name());
		try (InputStream trustedStore = new FileInputStream(conf.getSSLKeyStoreFile())) {
			// load stores in memory
			trustedKS.load(trustedStore, conf.getSSLKeyStorePassword().toCharArray());
		}
		
		// mark certificate in the keystore as confidence certificates
		confidenceCertificates.clear();
		for (Enumeration<String> certAlias = trustedKS.aliases(); certAlias.hasMoreElements();) {
			String alias = certAlias.nextElement();
			Certificate cert = trustedKS.getCertificate(alias);
			String fingerprint = SSLUtils.certificateFingerprint(cert);
			confidenceCertificates.add(fingerprint);
			
			// extract network or leave the alias as the fingerprint
			int atLocation = alias.indexOf('@');
			if (atLocation >= 0) {
				String network = alias.substring(atLocation + 1);
				certificatesNetwork.put(fingerprint, network);
			} else {
				certificatesNetwork.put(fingerprint, alias);
			}
			
			// save for the alias -> certificate matcher
			aliasCertificates.put(alias, cert);
		}
		
		// add temporary certificates
		// get all files in directory and add them
		File tempCertDir = conf.getSSLTemporaryCertificateLocation();
		if (tempCertDir != null) {
			if (tempCertDir.exists()) {
				for (File file : tempCertDir.listFiles()) {
					if (file.isFile() && file.getName().endsWith(".cer")) {
						try {
							Certificate cert = SSLUtils.certificateFromBytes(Files.readAllBytes(file.toPath()));
							String alias = file.getName().substring(0, file.getName().length() - 4);
							trustedKS.setCertificateEntry(alias, cert);
						} catch(Exception e) {
						}
					}
				}
			}
			// directory doesn't exists, create it (may have changed)
			else {
				// create temp dir if not found
				RuntimeDirectoryThread rdt = new RuntimeDirectoryThread(tempCertDir);
				rdt.addCleanupHook();
			}
			// watch temporaray certificate directory
			if (tempCertDir.canRead()) {
				// stop previous watcher
				boolean createWatcher = true;
				if (temporaryWatcher != null) {
					if (tempCertDir.toPath().equals(temporaryWatcher.getWatchedDir())) {
						createWatcher = false;
					}
					// change of directory
					else {
						temporaryWatcher.stop();
					}
				}

				if (createWatcher) {
					temporaryWatcher = new WatchDirectory(tempCertDir.toPath(), new TemporaryDirectoryWatcher(),
						StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);
					Thread dirWatcher = new Thread(temporaryWatcher, "TrustStore temporary folder watcher");
					dirWatcher.setDaemon(true);
					dirWatcher.start();
				}
			}
		}
		
		// watch keystore
		File keyStoreFile = conf.getSSLKeyStoreFile();
		if (keyStoreFile != null && keyStoreFile.canRead()) {
			Path keyStorePath = keyStoreFile.toPath().toAbsolutePath();
			
			// stop previous watcher
			boolean createWatcher = true;
			if (keyStoreWatcher != null) {
				if (keyStorePath.getParent().equals(keyStoreWatcher.getWatchedDir())) {
					createWatcher = false;
				}
				// change of directory
				else {
					keyStoreWatcher.stop();
				}
			}

			if (createWatcher) {
				keyStoreWatcher = new WatchDirectory(keyStorePath.getParent(), new KeyStoreWatcher(keyStorePath), 
					StandardWatchEventKinds.ENTRY_MODIFY);
				Thread keyWatcher = new Thread(keyStoreWatcher, "KeyStore changes watcher (TrustManager)");
				keyWatcher.setDaemon(true);
				keyWatcher.start();
			}
		}
		
		// initialize a new TMF with the trustedKS we just loaded
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(trustedKS);
		
		long end = System.currentTimeMillis();
		LogWriter.writeDebugInfo(String.format("[TrustManager] initiated in %d ms", end - start));

		// acquire X509 trust manager from factory
		TrustManager tms[] = tmf.getTrustManagers();
		for (TrustManager tm : tms) {
			if (tm instanceof X509TrustManager) {
				trustManager = (X509TrustManager) tm;
				saveCertificatesToMemory();
				return;
			}
		}

		throw new NoSuchAlgorithmException("No X509TrustManager in TrustManagerFactory");
	}
	
	/**
	 * Do we know the certificate
	 * 
	 * @param cert the certificate to check
	 * @return true is known, false otherwise
	 */
	public boolean isCertificateKnown(Certificate cert) {
		return loadedCertificates.values().contains(cert);
	}
	
	/**
	 * Any certificate from the local Trust manager
	 * 
	 * @param fingerprint the fingerprint of the certificate
	 * @return the certificate or null if unknown
	 */
	public Certificate getCertificate(String fingerprint) {
		return loadedCertificates.get(fingerprint);
	}

	/**
	 * The certificate of a specified alias
	 * 
	 * @param uuid the alias of the certificate, usually the network UUID
	 * @return the certificate or null if not found
	 */
	public Certificate getCertificateFromAlias(String uuid) {
		Objects.requireNonNull(uuid);
		return aliasCertificates.get(uuid.toLowerCase());
	}
}
