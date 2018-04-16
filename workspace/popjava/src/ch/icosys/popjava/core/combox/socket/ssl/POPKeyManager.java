package ch.icosys.popjava.core.combox.socket.ssl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.ExtendedSSLSession;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.StandardConstants;
import javax.net.ssl.X509KeyManager;

import ch.icosys.popjava.core.util.Configuration;
import ch.icosys.popjava.core.util.LogWriter;
import ch.icosys.popjava.core.util.WatchDirectory;
import ch.icosys.popjava.core.util.ssl.SSLUtils;

/**
 * A wrapper for a standard X509KeyManager, it will unwrap the SNI from the handshake and use the sent host name - which
 * in our case is the POP Network UUID and the Private Key alias - and use it as the alias.
 *
 * @author Davide Mazzoleni
 */
public class POPKeyManager implements X509KeyManager {
	
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
				reloadKeyManager();
			} catch(Exception e) {}
		}
	}

	private final Configuration conf = Configuration.getInstance();

	// privates keys store
	private X509KeyManager keyManager;
	
	// reload and add new certificates
	private WatchDirectory keyStoreWatcher;

	public POPKeyManager() throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException {
		reloadKeyManager();
	}
	
	public final void reloadKeyManager() throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException {
		long start = System.currentTimeMillis();
		SSLUtils.invalidateSSLSessions();

		// open key store
		KeyStore keyStore = KeyStore.getInstance(conf.getSSLKeyStoreFormat().name());
		try (InputStream keyStoreStream = new FileInputStream(conf.getSSLKeyStoreFile())) {
			// load stores in memory
			keyStore.load(keyStoreStream, conf.getSSLKeyStorePassword().toCharArray());
		}
		
		// extract keys
		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		keyManagerFactory.init(keyStore, conf.getSSLKeyStorePrivateKeyPassword().toCharArray());
		
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
				Thread keyWatcher = new Thread(keyStoreWatcher, "KeyStore changes watcher (KeyManager)");
				keyWatcher.setDaemon(true);
				keyWatcher.start();
			}
		}
		
		long end = System.currentTimeMillis();
		LogWriter.writeDebugInfo(String.format("[KeyManager] initiated in %d ms", end - start));
		
		// keep the correct keymanager
		for (KeyManager itrKeyManager : keyManagerFactory.getKeyManagers()) {
			if (itrKeyManager instanceof X509KeyManager) {
				keyManager = (X509KeyManager) itrKeyManager;
				return;
			}
		}
		
		throw new NoSuchAlgorithmException("No X509KeyManager in KeyManagerFactory");
	}

	@Override
	public X509Certificate[] getCertificateChain(String alias) {
		return keyManager.getCertificateChain(alias);
	}

	@Override
	public PrivateKey getPrivateKey(String alias) {
		return keyManager.getPrivateKey(alias);
	}

	@Override
	public String[] getClientAliases(String keyType, Principal[] issuers) {
		return keyManager.getClientAliases(keyType, issuers);
	}

	@Override
	public String[] getServerAliases(String keyType, Principal[] issuers) {
		return keyManager.getServerAliases(keyType, issuers);
	}

	@Override
	public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
		return chooseSNIAlias(true, issuers, socket, keyType);
	}

	@Override
	public String chooseClientAlias(String[] keyTypes, Principal[] issuers, Socket socket) {
		return chooseSNIAlias(false, issuers, socket, keyTypes);
	}

	/**
	 * Return the received SNI as the alias for the server.
	 * 
	 * @param socket the socket of the connection
	 * @return the given SNI or the first certificate in the keystore, null if it's not a SSL connection
	 */
	private String chooseSNIAlias(boolean server, Principal[] issuers, Socket socket, String... keyTypes) {
		// we can only accept SSL Sockets
		if (!(socket instanceof SSLSocket)) {
			return null;
		}

		SSLSocket ssl = (SSLSocket) socket;
		ExtendedSSLSession handshakeSession = (ExtendedSSLSession) ssl.getHandshakeSession();

		// we need that the handshake is there
		if (handshakeSession == null) {
			return null;
		}

		String returns = null;
		// extract the SNI from the extended handshake
		for (SNIServerName sniNetwork : handshakeSession.getRequestedServerNames()) {
			if (sniNetwork.getType() == StandardConstants.SNI_HOST_NAME) {
				returns = ((SNIHostName) sniNetwork).getAsciiName();
				break;
			}
		}
		// if we don't have the requested alias, ask the default keymanager
		PrivateKey pk = keyManager.getPrivateKey(returns);
		if (pk == null) {
			if (server) return keyManager.chooseServerAlias(keyTypes[0], issuers, socket);
			else keyManager.chooseClientAlias(keyTypes, issuers, socket);
		}
		return returns == null ? null : returns.toLowerCase();
	}
}
