package popjava.combox.ssl;

import popjava.util.ssl.SSLUtils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.ExtendedSSLSession;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.StandardConstants;

import popjava.base.MessageHeader;
import popjava.baseobject.AccessPoint;
import popjava.buffer.POPBuffer;
import popjava.combox.Combox;
import popjava.combox.ComboxFactory;
import popjava.util.LogWriter;
import popjava.util.POPRemoteCaller;

/**
 * This combox implement the protocol ssl
 */
public class ComboxSecureSocket extends Combox<SSLSocket> {
	
	protected final byte[] receivedBuffer;
	public static final int BUFFER_LENGTH = 1024 * 1024 * 8;
	protected InputStream inputStream = null;
	protected OutputStream outputStream = null;
	private final int STREAM_BUFFER_SIZE = 8 * 1024 * 1024; //8MB
	
	private static final ComboxFactory MY_FACTORY = new ComboxSecureSocketFactory();
	
	/**
	 * This is used by ServerCombox (server).
	 * Create a new combox from a server.
	 * Call {@link #serverAccept(java.lang.Object)   } to let the client connect.
	 * @throws IOException	Thrown is any IO exception occurred during the creation
	 */
	public ComboxSecureSocket() {
		super();
		receivedBuffer = new byte[BUFFER_LENGTH];
	}

	/**
	 * This is used by Combox (client).
	 * Create a combox for a client.
	 * Call {@link #connectToServer(popjava.baseobject.POPAccessPoint, int)  } to actually connect the client.
	 * @param networkUUID the id of the network
	 */
	public ComboxSecureSocket(String networkUUID) {
		super(networkUUID);
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
		} catch (IOException e) {}
		finally {
			try {
				outputStream.close();
			} catch (IOException e) {}
			try {
				inputStream.close();
			} catch (IOException e) {}
			if(peerConnection != null){
				try {
				    peerConnection.close();
				} catch (IOException e) {}
			}
		}
	}

	@Override
	protected boolean serverAccept() {
		try {
			inputStream = new BufferedInputStream(peerConnection.getInputStream(), STREAM_BUFFER_SIZE);
			outputStream = new BufferedOutputStream(peerConnection.getOutputStream(), STREAM_BUFFER_SIZE);
			return true;
		} catch(IOException e) {
			LogWriter.writeDebugInfo("[ComboxSecureSocket] Couldn't open streams on the server side.");
			return false;
		}
	}

	@Override
	protected boolean connectToServer() {
		try {			
			SSLContext sslContext = SSLUtils.getSSLContext();
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
					SocketAddress sockaddress = new InetSocketAddress(host, port);
					if (timeOut > 0) {
						peerConnection = (SSLSocket) factory.createSocket();

						//LogWriter.writeExceptionLog(new Exception());
						//LogWriter.writeExceptionLog(new Exception("Open connection to "+host+":"+port+" remote: "+peerConnection.getLocalPort()));
					} else {
						peerConnection = (SSLSocket) factory.createSocket();
						timeOut = 0;
					}
					peerConnection.setUseClientMode(true);
					
					// setup SNI
					SNIServerName network = new SNIHostName(getNetworkUUID());
					List<SNIServerName> nets = new ArrayList<>(1);
					nets.add(network);

					// set SNI as part of the parameters
					SSLParameters parameters = peerConnection.getSSLParameters();
					parameters.setServerNames(nets);
					peerConnection.setSSLParameters(parameters);

					// connect and start handshake
					peerConnection.connect(sockaddress);
					
					// setup communication buffers
					inputStream = new BufferedInputStream(peerConnection.getInputStream());
					outputStream = new BufferedOutputStream(peerConnection.getOutputStream());
					
					available = true;
				} catch (IOException e) {
					available = false;
				}
			}
		} catch (Exception e) {}
		
		return available;
	}

	@Override
	protected boolean sendNetworkName() {
		try {
			peerConnection.startHandshake();
			return true;
		} catch (Exception e) {
			LogWriter.writeDebugInfo("[ComboxSecureSocket] Client handshake failed. Message: %s", e.getMessage());
			return false;
		}
	}

	@Override
	protected boolean receiveNetworkName() {
		// extract network from handshake
		ExtendedSSLSession handshakeSession = (ExtendedSSLSession) peerConnection.getSession();

		// we need that the handshake is there
		if (handshakeSession != null) {
			// extract the SNI from the extended handshake
			for (SNIServerName sniNetwork : handshakeSession.getRequestedServerNames()) {
				if (sniNetwork.getType() == StandardConstants.SNI_HOST_NAME) {
					setNetworkUUID(((SNIHostName) sniNetwork).getAsciiName());
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public int receive(POPBuffer buffer, int requestId) {
		
		int result = 0;
		try {
			buffer.resetToReceive();
			// Receive message length
			byte[] temp = new byte[4];
			
			boolean gotPacket = false;
			
			do {
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
						System.out.println("RESET "+requestIdPacket+" "+requestId);
						inputStream.reset();
						//Thread.yield();
					}
				}
			}while(!gotPacket);

			int headerLength = MessageHeader.HEADER_LENGTH;
			if (result < headerLength) {
				if (conf.isDebugCombox()) {
					String logInfo = String.format(
							"[ComboxSecureSocket] failed to receive header. receivedLength= %d, Message length %d",
							result, headerLength);
					LogWriter.writeDebugInfo(logInfo);
				}
				close();
			} else {
				buffer.extractHeader();				
			}
			
			return result;
		} catch (Exception e) {
			if (conf.isDebugCombox()){
				LogWriter.writeDebugInfo("[ComboxSecureSocket] Error while receiving data:"
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
		} catch (Exception e) {
			if (conf.isDebugCombox()){
				LogWriter.writeDebugInfo(
					"[ComboxSecureSocket] -Send:  Error while sending data - " + e.getMessage() +" "+outputStream);
			}
			close();
			return -1;
		}
	}

	@Override
	protected boolean exportConnectionInfo() {		
		try {			
			// set the fingerprint in the accesspoint for all to know
			// this time we have to look which it is
			Certificate[] certs = peerConnection.getSession().getPeerCertificates();
			for (Certificate cert : certs) {
				if (SSLUtils.isCertificateKnown(cert)) {
					String fingerprint = SSLUtils.certificateFingerprint(cert);
					accessPoint.setFingerprint(fingerprint);
					
					if (getNetworkUUID() == null) {
						setNetworkUUID(SSLUtils.getNetworkFromFingerprint(fingerprint));
					}
					
					remoteCaller = new POPRemoteCaller(
						peerConnection.getInetAddress(),
						MY_FACTORY.getComboxName(),
						getNetworkUUID(),
						MY_FACTORY.isSecure(),
						fingerprint
					);
					return true;
				}
			}
		} catch (Exception e) {
			LogWriter.writeExceptionLog(e);
		}
		return false;
	}
}
