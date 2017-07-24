package popjava.combox.ssl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
		
		@Override
		public void create(String file) {
			reload();
		}

		@Override
		public void delete(String file) {
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
		this.trustStorePath = Configuration.KEY_STORE;
		this.trustStorePass = Configuration.KEY_STORE_PWD;
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

	/**
	 * The TrustManager instance 
	 * 
	 * @return 
	 */
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
	
	/**
	 * Sav
	 */
	private void saveCertificatesToMemory() {
		Map<String,Certificate> temp = new HashMap<>();
		Certificate[] certificates = getAcceptedIssuers();
		for (Certificate cert : certificates) {
			temp.put(SSLUtils.certificateThumbprint(cert), cert);
		}
		// empty and swap
		loadedCertificates.clear();
		loadedCertificates.putAll(temp);
	}

	final protected void reloadTrustManager() throws Exception {
		long start = System.currentTimeMillis();
		// load keystore from specified cert store (or default)
		KeyStore trustedKS = KeyStore.getInstance(Configuration.KEY_STORE_FORMAT);
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
			confidenceCertificates.add(SSLUtils.certificateThumbprint(cert));
                        
			// save public certificate
			if (alias.equals(Configuration.KEY_STORE_PK_ALIAS)) {
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
				saveCertificatesToMemory();
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
	 * Public certificate from the ones loaded
	 * 
	 * @return 
	 */
	protected static Certificate getLocalPublicCertificate() {
		return publicCertificate;
	}
	
	/**
	 * Any certificate from the local Trust manager
	 * 
	 * @param thumbprint
	 * @return 
	 */
	public Certificate getCertificate(String thumbprint) {
		return loadedCertificates.get(thumbprint);
	}
}
