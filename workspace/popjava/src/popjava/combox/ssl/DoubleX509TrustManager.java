package popjava.combox.ssl;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
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
	private final String tempTrustStorePass;
	
	private X509TrustManager trustManager;
	
	public DoubleX509TrustManager(String tspath, String tspass, String temptspath, String temptspass) throws Exception {
		this.trustStorePath = tspath;
		this.trustStorePass = tspass;
		this.tempTrustStorePath = temptspath;
		this.tempTrustStorePass = temptspass;
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
		// load temporay keystore
		KeyStore temporaryKS = KeyStore.getInstance(KeyStore.getDefaultType());
		InputStream temporaryStore = new FileInputStream(tempTrustStorePath);
		try {
			// load stores in memory
			trustedKS.load(trustedStore, trustStorePass.toCharArray());
			temporaryKS.load(temporaryStore, tempTrustStorePass.toCharArray());
		} finally {
			trustedStore.close();
			temporaryStore.close();
		}
		
		// add temporary to trusted
		ArrayList<String> trustedAliases = Collections.list(trustedKS.aliases());
		ArrayList<String> aliases = Collections.list(temporaryKS.aliases());
		for (String alias : aliases) {
			if (!trustedAliases.contains(alias))
				trustedKS.setCertificateEntry(alias, temporaryKS.getCertificate(alias));
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

	// TODO write to file and add to tempKS
	public static void addCertToTempStore(byte[] certificate, String temptspath, String temptspass) {
		try {
			// import the cert into file trust store
			Runtime.getRuntime().exec("keytool -importcert ...");
			
			
		} catch (Exception ex) {
		
		}
	}
}
