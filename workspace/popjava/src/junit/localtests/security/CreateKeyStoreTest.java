package junit.localtests.security;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.InvalidParameterException;
import java.util.Random;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import popjava.util.ssl.KeyStoreOptions;
import popjava.util.ssl.SSLUtils;
import popjava.util.Configuration;
import popjava.util.LogWriter;
import popjava.util.ssl.KeyStoreOptions.KeyStoreFormat;

/**
 *
 * @author dosky
 */
public class CreateKeyStoreTest {
	
	@Test(expected = InvalidParameterException.class)
	public void optAlias() {
		KeyStoreOptions option = new KeyStoreOptions(null, "123456", "123456", "1");
		option.validate();
	}
	
	@Test(expected = InvalidParameterException.class)
	public void optStorePassShort() {
		KeyStoreOptions option = new KeyStoreOptions("alias", "123", "123456", "1");
		option.validate();
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void optStorePassNull() {
		KeyStoreOptions option = new KeyStoreOptions("alias", null, "123456", "1");
		option.validate();
	}
	
	@Test(expected = InvalidParameterException.class)
	public void optKeyPassShort() {
		KeyStoreOptions option = new KeyStoreOptions("alias", "123456", "123", "1");
		option.validate();
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void optKeyPassNull() {
		KeyStoreOptions option = new KeyStoreOptions("alias", "123456", null, "1");
		option.validate();
	}
	
	@Test(expected = InvalidParameterException.class)
	public void optFileNull() {
		KeyStoreOptions option = new KeyStoreOptions("alias", "123456", "123456", null);
		option.validate();
	}
	
	@Test(expected = InvalidParameterException.class)
	public void optPKCS12Pass() {
		KeyStoreOptions option = new KeyStoreOptions("alias", "123456", "654321", "1");
		option.setKeyStoreFormat(KeyStoreFormat.PKCS12);
		option.validate();
	}
	
	@Test(expected = InvalidParameterException.class)
	public void optEmpty() {
		KeyStoreOptions option = new KeyStoreOptions();
		option.validate();
	}
	
	@Test(expected = InvalidParameterException.class)
	public void optExpiration() {
		KeyStoreOptions option = new KeyStoreOptions("alias", "123456", "123456", "1");
		option.setValidUntil(null);
		option.validate();
	}
	
	@Test(expected = InvalidParameterException.class)
	public void optKeySize() {
		KeyStoreOptions option = new KeyStoreOptions("alias", "123456", "123456", "1");
		option.setKeySize(512);
		option.validate();
	}
	
	@Test
	public void optFull() {
		KeyStoreOptions option = new KeyStoreOptions("alias", "123456", "123456", "1");
		option.setKeySize(4096);
		option.setValidFor(90);
		option.validate();
	}
	
	@Rule
	public TemporaryFolder testDir = new TemporaryFolder();
	
	@Test
	public void full() throws Exception {
		testDir.create();
		File tmpKS = testDir.newFile();
		File tmpDir = testDir.newFolder();
		
		String alias = "myself";
		String storepass = "mypass";
		String keypass = "mykeypass";
		String keyStoreFile = tmpKS.getAbsolutePath(); 
		
		LogWriter.writeDebugInfo("Creating KeyStore");
		KeyStoreOptions options = new KeyStoreOptions(alias, storepass, keypass, keyStoreFile);
		SSLUtils.generateKeyStore(options);
		
		LogWriter.writeDebugInfo("Setting up environment");
		Configuration conf = Configuration.getInstance();
		conf.setKeyStoreFile(new File(keyStoreFile));
		conf.setKeyStorePassword(storepass);
		conf.setKeyStoreFormat(KeyStoreFormat.JKS);
		conf.setKeyStorePrivateKeyPassword(keypass);
		conf.setKeyStoreLocalAlias(alias);
		conf.setKeyStoreTempLocation(tmpDir);
		
		LogWriter.writeDebugInfo("Starting SSL Context");
		// test create context
		SSLContext sslContext = SSLUtils.getSSLContext();
		// create factories
		SSLServerSocketFactory serverFactory = sslContext.getServerSocketFactory();
		SSLSocketFactory socketFactory = sslContext.getSocketFactory();
		
		// start server
		LogWriter.writeDebugInfo("Starting server");
		final SSLServerSocket serverSocket = (SSLServerSocket) serverFactory.createServerSocket(0);
		serverSocket.setNeedClientAuth(true);
		
		// expected message
		Random rnd = new Random();
		final byte[] expecteds = new byte[1024 + rnd.nextInt(1024)];
		rnd.nextBytes(expecteds);
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				try (Socket client = serverSocket.accept(); OutputStream out = client.getOutputStream()) {
					out.write(expecteds);
				} catch(IOException e) {
				}
			}
		}).start();
		
		LogWriter.writeDebugInfo("Connecting client");
		try (Socket server = socketFactory.createSocket(serverSocket.getInetAddress(), serverSocket.getLocalPort());
			InputStream in = server.getInputStream()) {
			byte[] actuals = new byte[expecteds.length];
			in.read(actuals);
			
			Assert.assertArrayEquals(expecteds, actuals);
		}
		
		serverSocket.close();
		
		LogWriter.writeDebugInfo("Cleanup");
		testDir.delete();
	}
}
