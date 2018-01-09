package junit.localtests.security;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import popjava.util.ssl.KeyPairDetails;
import popjava.util.ssl.SSLUtils;
import popjava.util.Configuration;
import popjava.util.LogWriter;
import popjava.util.ssl.KeyStoreDetails;
import popjava.util.ssl.KeyStoreDetails.KeyStoreFormat;

/**
 *
 * @author dosky
 */
public class CreateKeyStoreTest {
	
	@Test(expected = InvalidParameterException.class)
	public void optAlias() {
		KeyPairDetails option = new KeyPairDetails("");
		option.validate();
	}
	
	@Test(expected = InvalidParameterException.class)
	public void optStorePassShort() {
		KeyStoreDetails option = new KeyStoreDetails("123", "123456", new File("1"));
		option.validate();
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void optStorePassNull() {
		KeyStoreDetails option = new KeyStoreDetails(null, "123456", new File("1"));
		option.validate();
	}
	
	@Test(expected = InvalidParameterException.class)
	public void optKeyPassShort() {
		KeyStoreDetails option = new KeyStoreDetails("123456", "123", new File("1"));
		option.validate();
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void optKeyPassNull() {
		KeyStoreDetails option = new KeyStoreDetails("123456", null, new File("1"));
		option.validate();
	}
	
	@Test(expected = InvalidParameterException.class)
	public void optFileNull() {
		KeyStoreDetails option = new KeyStoreDetails("123456", "123456", null);
		option.validate();
	}
	
	@Test(expected = InvalidParameterException.class)
	public void optPKCS12Pass() {
		KeyStoreDetails option = new KeyStoreDetails("123456", "654321", new File("1"));
		option.setKeyStoreFormat(KeyStoreFormat.PKCS12);
		option.validate();
	}
	
	@Test(expected = InvalidParameterException.class)
	public void optEmpty() {
		KeyPairDetails option = new KeyPairDetails();
		option.validate();
	}
	
	@Test(expected = InvalidParameterException.class)
	public void optExpiration() {
		KeyPairDetails option = new KeyPairDetails("alias");
		option.setValidUntil(null);
		option.validate();
	}
	
	@Test(expected = InvalidParameterException.class)
	public void optKeySize() {
		KeyPairDetails option = new KeyPairDetails("alias");
		option.setPrivateKeySize(512);
		option.validate();
	}
	
	@Test
	public void optFull() {
		KeyPairDetails option = new KeyPairDetails("alias");
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
		
		Configuration.getInstance().setDebug(true);
		
		LogWriter.writeDebugInfo("Creating KeyStore working SNI");
		KeyPairDetails keyDetails = new KeyPairDetails(alias);
		KeyStoreDetails ksDetails = new KeyStoreDetails(storepass, keypass, keyStoreFile);
		if (!SSLUtils.generateKeyStore(ksDetails, keyDetails)) {
			throw new Exception("Failed to create custom keystore");
		}
		
		LogWriter.writeDebugInfo("Setting up environment");
		Configuration conf = Configuration.getInstance();
		conf.setSSLKeyStoreOptions(ksDetails);

		// force reload with new keystore
		SSLUtils.reloadPOPManagers();
		
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
					out.flush();
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
		
		LogWriter.writeDebugInfo("Connecting client");
		try (SSLSocket serverClient = (SSLSocket) socketFactory.createSocket(serverSocket.getInetAddress(), serverSocket.getLocalPort());
			InputStream in = serverClient.getInputStream()) {
			// setup SNI
			SNIServerName network = new SNIHostName(alias);
			List<SNIServerName> nets = new ArrayList<>(1);
			nets.add(network);

			// set SNI as part of the parameters
			SSLParameters parameters = serverClient.getSSLParameters();
			parameters.setServerNames(nets);
			serverClient.setSSLParameters(parameters);

			// start handshake
			serverClient.startHandshake();

			byte[] actuals = new byte[expecteds.length];
			in.read(actuals);
			
			Assert.assertArrayEquals(expecteds, actuals);
		}
		
		serverSocket.close();
		
		LogWriter.writeDebugInfo("Cleanup");
		testDir.delete();
		
		Configuration.getInstance().setDebug(false);
	}
	
	@Test(expected = Exception.class)
	public void fullWrongSNI() throws Exception {
		testDir.create();
		File tmpKS = testDir.newFile();
		File tmpDir = testDir.newFolder();
		
		String alias = "myself";
		String storepass = "mypass";
		String keypass = "mykeypass";
		File keyStoreFile = tmpKS; 
		
		Configuration.getInstance().setDebug(true);
		
		LogWriter.writeDebugInfo("Creating KeyStore wrong SNI");
		KeyPairDetails keyDetails = new KeyPairDetails(alias);
		KeyStoreDetails ksDetails = new KeyStoreDetails(storepass, keypass, keyStoreFile);
		if (!SSLUtils.generateKeyStore(ksDetails, keyDetails)) {
			throw new Exception("Failed to create custom keystore");
		}
		
		LogWriter.writeDebugInfo("Setting up environment");
		Configuration conf = Configuration.getInstance();
		conf.setSSLKeyStoreOptions(ksDetails);
		
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
					out.flush();
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
		
		LogWriter.writeDebugInfo("Connecting client");
		try (SSLSocket serverClient = (SSLSocket) socketFactory.createSocket(serverSocket.getInetAddress(), serverSocket.getLocalPort());
			InputStream in = serverClient.getInputStream()) {
			// setup SNI
			SNIServerName network = new SNIHostName("This-is-the-wrong-SNI");
			List<SNIServerName> nets = new ArrayList<>(1);
			nets.add(network);

			// set SNI as part of the parameters
			SSLParameters parameters = serverClient.getSSLParameters();
			parameters.setServerNames(nets);
			serverClient.setSSLParameters(parameters);

			// start handshake
			serverClient.startHandshake();

			byte[] actuals = new byte[expecteds.length];
			in.read(actuals);
			
			Assert.assertArrayEquals(expecteds, actuals);
		}
		
		serverSocket.close();
		
		LogWriter.writeDebugInfo("Cleanup");
		testDir.delete();
		
		Configuration.getInstance().setDebug(false);
	}
}
