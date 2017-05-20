package popjava.combox.ssl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.Certificate;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import popjava.base.MessageHeader;
import popjava.baseobject.AccessPoint;
import popjava.baseobject.POPAccessPoint;
import popjava.buffer.POPBuffer;
import popjava.combox.Combox;
import popjava.util.Configuration;
import popjava.util.LogWriter;
import popjava.util.Util;
/**
 * This combox implement the protocol ssl
 */
public class ComboxSecureSocket extends Combox {
	
	protected Socket peerConnection = null;
	protected byte[] receivedBuffer;
	public static final int BUFFER_LENGTH = 1024 * 1024 * 8;
	protected InputStream inputStream = null;
	protected OutputStream outputStream = null;
	private final int STREAM_BUFER_SIZE = 8 * 1024 * 1024; //8MB
	
	private final int HANDSHAKE_CHALLANGE_SIZE = 256;

	/**
	 * Create a new combox on the given socket
	 * @param socket	The socket to create the combox 
	 * @throws IOException	Thrown is any IO exception occurred during the creation
	 */
	public ComboxSecureSocket(Socket socket) throws IOException {
		peerConnection = socket;
		receivedBuffer = new byte[BUFFER_LENGTH];
		inputStream = new BufferedInputStream(peerConnection.getInputStream(), STREAM_BUFER_SIZE);
		outputStream = new BufferedOutputStream(peerConnection.getOutputStream(), STREAM_BUFER_SIZE);
		serverHandshake();
	}

	
	public ComboxSecureSocket(POPAccessPoint accesspoint, int timeout) {
		super(accesspoint, timeout);
		receivedBuffer = new byte[BUFFER_LENGTH];
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			close();
		} finally {
			super.finalize();
		}
	}

	@Override
	public void close() {
		try {
			if (peerConnection != null && !peerConnection.isClosed()) {
				/*LogWriter.writeExceptionLog(new Exception("Close connection to "+peerConnection.getInetAddress()+
						":"+peerConnection.getPort()+" remote: "+peerConnection.getLocalPort()));*/

				peerConnection.sendUrgentData(-1);
			}
		} catch (IOException e) {
		}finally{
			try {
				outputStream.close();
				inputStream.close();
				if(peerConnection != null){
				    peerConnection.close();
				}
			} catch (IOException e) {
				LogWriter.writeExceptionLog(e);
			}
		}
	}

	@Override
	public boolean connect() {
		try {
			// init SSLContext to create SSLSockets
			// https://jcalcote.wordpress.com/2010/06/22/managing-a-dynamic-java-trust-store/
			TrustManager[] trustManagers = new TrustManager[]{ POPTrustManager.getInstance() };
			SSLContext sslContext = SSLContext.getInstance(Configuration.SSL_PROTOCOL);
			sslContext.init(null, trustManagers, null);

			SSLSocketFactory factory = sslContext.getSocketFactory();

			available = false;
			int accessPointSize = accessPoint.size();
			for (int i = 0; i < accessPointSize && !available; i++) {
				AccessPoint ap = accessPoint.get(i);
				if (ap.getProtocol().compareToIgnoreCase(
						ComboxSecureSocketFactory.PROTOCOL) != 0){
					continue;
				}
				String host = ap.getHost();
				int port = ap.getPort();
				try {
					// Create an unbound socket
					if (timeOut > 0) {
						SocketAddress sockaddress = new InetSocketAddress(host,
								port);
						peerConnection = factory.createSocket();
						peerConnection.connect(sockaddress, timeOut);

						//LogWriter.writeExceptionLog(new Exception());
						//LogWriter.writeExceptionLog(new Exception("Open connection to "+host+":"+port+" remote: "+peerConnection.getLocalPort()));
					} else {
						peerConnection = factory.createSocket(host, port);
					}
					inputStream = new BufferedInputStream(peerConnection.getInputStream());
					outputStream = new BufferedOutputStream(peerConnection.getOutputStream());
					
					clientHandhsake();
					
					available = true;
				} catch (UnknownHostException e) {
					available = false;
				} catch (SocketTimeoutException e) {
					available = false;
				} catch (IOException e) {
					available = false;
				}
			}
		} catch (Exception e) {}
		
		return available;
	}
	
	@Override
	public int receive(POPBuffer buffer, int requestId) {
		
		int result = 0;
		try {
			buffer.resetToReceive();
			// Receive message length
			byte[] temp = new byte[4];
			
			boolean gotPacket = false;
			
			do{
				synchronized (inputStream) {
					inputStream.mark(8);
					
				    int read = 0;
				    //Get size
				    while(read < temp.length){
				    	int tempRead = inputStream.read(temp, read, temp.length - read);
				    	if(tempRead < 0){
					    	//System.out.println("PANIC 1 "+tempRead);
				    		close();
							return -1;
				    	}
				        read += tempRead;
				    }
					
					int messageLength = buffer.getTranslatedInteger(temp);
					
					if (messageLength <= 0) {
						//System.out.println("PANIC 3 "+messageLength);
						close();
						return -1;
					}
					
					//Get requestID
					read = 0;
				    //Get size
				    while(read < temp.length){
				    	int tempRead = inputStream.read(temp, read, temp.length - read);
				    	if(tempRead < 0){
					    	//System.out.println("PANIC 2 "+tempRead);
				    		close();
							return -1;
				    	}
				        read += tempRead;
				    }
					
					int requestIdPacket = buffer.getTranslatedInteger(temp);
					
					//A requestID of -1 (client or server) indicates that the requestID should be ignored
					if(requestId == -1 || requestIdPacket == -1 || requestIdPacket == requestId){
						gotPacket = true;
						
						result = 8;
						buffer.putInt(messageLength);
						messageLength = messageLength - 4;
						
						buffer.putInt(requestIdPacket);
						messageLength = messageLength - 4;
						
						int receivedLength = 0;
						while (messageLength > 0) {
							int count = messageLength < BUFFER_LENGTH ? messageLength : BUFFER_LENGTH;
							receivedLength = inputStream.read(receivedBuffer, 0, count);
							if (receivedLength > 0) {
								messageLength -= receivedLength;
								result += receivedLength;
								buffer.put(receivedBuffer, 0, receivedLength);
							} else {
								break;
							}
						}
					}else{
						//System.out.println("RESET "+requestIdPacket+" "+requestId);
						inputStream.reset();
						//Thread.yield();
					}
				}
			}while(!gotPacket);

			int headerLength = MessageHeader.HEADER_LENGTH;
			if (result < headerLength) {
				if (Configuration.DEBUG_COMBOBOX) {
					String logInfo = String.format(
							"%s.failed to receive header. receivedLength= %d < %d Message length %d",
							this.getClass().getName(), result, headerLength);
					LogWriter.writeDebugInfo(logInfo);
				}
				close();
			} else {
				buffer.extractHeader();				
			}
			
			return result;
		} catch (Exception e) {
			if (Configuration.DEBUG_COMBOBOX){
				LogWriter.writeDebugInfo("ComboxServerSocket Error while receiving data:"
								+ e.getMessage());
			}
			close();
			return -2;
		}
	}

	@Override
	public int send(POPBuffer buffer) {
		try {
			buffer.packMessageHeader();
			final int length = buffer.size();
			final byte[] dataSend = buffer.array();
						
			//System.out.println("Write "+length+" bytes to socket");			
			synchronized (outputStream) {
    			outputStream.write(dataSend, 0, length);
    			outputStream.flush();
			}
			
			return length;
		} catch (IOException e) {
			if (Configuration.DEBUG_COMBOBOX){
				e.printStackTrace();
				LogWriter.writeDebugInfo(this.getClass().getName()
						+ "-Send:  Error while sending data - " + e.getMessage() +" "+outputStream);
			}
			return -1;
		}
	}
	
	private boolean clientHandhsake() {
		try {
			LogWriter.writeDebugInfo("Client waiting for challange");
			// receive challange
			byte[] challangeBytes = new byte[HANDSHAKE_CHALLANGE_SIZE];
			inputStream.read(challangeBytes);
			LogWriter.writeDebugInfo(String.format("Challange is: %s...", new String(challangeBytes, 0, 10)));

			// load private key
			KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			keyStore.load(new FileInputStream(Configuration.TRUST_STORE), Configuration.TRUST_STORE_PWD.toCharArray());
			KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			keyManagerFactory.init(keyStore, Configuration.TRUST_STORE_PK_PWD.toCharArray());
			// private key
			PrivateKey pk = (PrivateKey) keyStore.getKey(Configuration.TRUST_STORE_PK_ALIAS, Configuration.TRUST_STORE_PK_PWD.toCharArray());
			Certificate ppk = keyStore.getCertificate(Configuration.TRUST_STORE_PK_ALIAS);

			// sign challange
			Signature signer = Signature.getInstance("SHA256withRSA");
			signer.initSign(pk);
			signer.update(challangeBytes);
			byte[] answer = signer.sign();
			
			LogWriter.writeDebugInfo("Challange signed, sending answer");

			// send answer
			byte[] size = ByteBuffer.allocate(4).putInt(answer.length).array();
			byte[] certHash = ByteBuffer.allocate(4).putInt(POPTrustManager.getLocalPublicCertificate().hashCode()).array();
			outputStream.write(size);
			outputStream.write(answer);
			outputStream.write(certHash);
			outputStream.flush();
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private boolean serverHandshake() {
		try {
			POPTrustManager tm = POPTrustManager.getInstance();
			// server client challange
			String challange = Util.generateRandomString(HANDSHAKE_CHALLANGE_SIZE);
			LogWriter.writeDebugInfo(String.format("Creating challange: %s...", challange.substring(0, 10)));

			// send challange
			outputStream.write(challange.getBytes(Charset.defaultCharset()));
			outputStream.flush();
			
			LogWriter.writeDebugInfo("Server waiting for answer");
			
			// receive answer
			byte[] sizeBytes = new byte[4];
			inputStream.read(sizeBytes);
			int size = ByteBuffer.wrap(sizeBytes).asIntBuffer().get();
			byte[] answerBytes = new byte[size];
			inputStream.read(answerBytes);
			byte[] certHash = new byte[4];
			inputStream.read(certHash);
			int hash = ByteBuffer.wrap(certHash).asIntBuffer().get();

			LogWriter.writeDebugInfo("Answer received, checking signature");
			
			Signature signer = Signature.getInstance("SHA256withRSA");
			signer.initVerify(POPTrustManager.getCertificate(hash));
			signer.update(challange.getBytes());
			boolean signatureStatus = signer.verify(answerBytes);

			// can't verify, kill
			if (!signatureStatus) {
				throw new SSLHandshakeException("Nodes can't trust each other, signatures missmatch.");
			}
		} catch(Exception e) {
			LogWriter.writeDebugInfo("Challange failed: " + e.getMessage());
			return false;
		}
		LogWriter.writeDebugInfo("Challange answered successfully");
		return true;
	}
}