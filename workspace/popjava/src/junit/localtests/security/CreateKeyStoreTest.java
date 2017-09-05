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
import popjava.util.ssl.KeyStoreCreationOptions;
import popjava.util.ssl.SSLUtils;
import popjava.util.Configuration;
import popjava.util.LogWriter;
import popjava.util.ssl.KeyStoreDetails.KeyStoreFormat;

/**
 *
 * @author dosky
 */
public class CreateKeyStoreTest {
	
	@Test(expected = InvalidParameterException.class)
	public void optAlias() {
		KeyStoreCreationOptions option = new KeyStoreCreationOptions(null, "123456", "123456", new File("1"));
		option.validate();
	}
	
	@Test(expected = InvalidParameterException.class)
	public void optStorePassShort() {
		KeyStoreCreationOptions option = new KeyStoreCreationOptions("alias", "123", "123456", new File("1"));
		option.validate();
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void optStorePassNull() {
		KeyStoreCreationOptions option = new KeyStoreCreationOptions("alias", null, "123456", new File("1"));
		option.validate();
	}
	
	@Test(expected = InvalidParameterException.class)
	public void optKeyPassShort() {
		KeyStoreCreationOptions option = new KeyStoreCreationOptions("alias", "123456", "123", new File("1"));
		option.validate();
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void optKeyPassNull() {
		KeyStoreCreationOptions option = new KeyStoreCreationOptions("alias", "123456", null, new File("1"));
		option.validate();
	}
	
	@Test(expected = InvalidParameterException.class)
	public void optFileNull() {
		KeyStoreCreationOptions option = new KeyStoreCreationOptions("alias", "123456", "123456", null);
		option.validate();
	}
	
	@Test(expected = InvalidParameterException.class)
	public void optPKCS12Pass() {
		KeyStoreCreationOptions option = new KeyStoreCreationOptions("alias", "123456", "654321", new File("1"));
		option.setKeyStoreFormat(KeyStoreFormat.PKCS12);
		option.validate();
	}
	
	@Test(expected = InvalidParameterException.class)
	public void optEmpty() {
		KeyStoreCreationOptions option = new KeyStoreCreationOptions();
		option.validate();
	}
	
	@Test(expected = InvalidParameterException.class)
	public void optExpiration() {
		KeyStoreCreationOptions option = new KeyStoreCreationOptions("alias", "123456", "123456", new File("1"));
		option.setValidUntil(null);
		option.validate();
	}
	
	@Test(expected = InvalidParameterException.class)
	public void optKeySize() {
		KeyStoreCreationOptions option = new KeyStoreCreationOptions("alias", "123456", "123456", new File("1"));
		option.setPrivateKeySize(512);
		option.validate();
	}
	
	@Test
	public void optFull() {
		KeyStoreCreationOptions option = new KeyStoreCreationOptions("alias", "123456", "123456", new File("1"));
		option.setPrivateKeySize(4096);
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
		File keyStoreFile = tmpKS; 
		
		LogWriter.writeDebugInfo("Creating KeyStore");
		KeyStoreCreationOptions options = new KeyStoreCreationOptions(alias, storepass, keypass, keyStoreFile);
		SSLUtils.generateKeyStore(options);
		
		LogWriter.writeDebugInfo("Setting up environment");
		Configuration conf = Configuration.getInstance();
		conf.setSSLKeyStoreFile(keyStoreFile);
		conf.setSSLKeyStorePassword(storepass);
		conf.setSSLKeyStoreFormat(KeyStoreFormat.JKS);
		conf.setSSLKeyStorePrivateKeyPassword(keypass);
		conf.setSSLKeyStoreLocalAlias(alias);
		conf.setSSLKeyStoreTempLocation(tmpDir);
		
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
