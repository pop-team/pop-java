package popjava.combox.ssl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import popjava.util.Configuration;
import popjava.util.LogWriter;

/**
 * Two origin KeyStore TrustManager.
 * @see https://jcalcote.wordpress.com/2010/06/22/managing-a-dynamic-java-trust-store/
 * @author John Calcote
 * @author Davide Mazzoleni
 */
public class DoubleX509TrustManager implements X509TrustManager {

	private final String trustStorePath;
	private final String trustStorePass;
	private final String tempTrustStorePath;
	
	private X509TrustManager trustManager;
	
	public DoubleX509TrustManager() throws Exception {
		this.trustStorePath = Configuration.TRUST_STORE;
		this.trustStorePass = Configuration.TRUST_STORE_PWD;
		this.tempTrustStorePath = Configuration.TRUST_TEMP_STORE_DIR;
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
		X509Certificate[] issuers = trustManager.getAcceptedIssuers();
		return issuers;
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
		
		// add temporary certificates
		CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
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
				return;
			}
		}

		throw new NoSuchAlgorithmException("No X509TrustManager in TrustManagerFactory");
	}

	public static void addCertToTempStore(byte[] certificate) {
		try {
			// certificate saving path
			Path path = Paths.get(Configuration.TRUST_TEMP_STORE_DIR, Arrays.hashCode(certificate) + ".cer");
			// don't it it exists
			if(path.toFile().exists()) {
				return;
			}
			// write it
			Files.write(path, certificate);
		} catch (Exception ex) {
			
		}
	}
}
