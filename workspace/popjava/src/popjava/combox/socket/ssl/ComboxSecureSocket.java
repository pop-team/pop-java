package popjava.combox.socket.ssl;

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
import java.util.Collections;
import java.util.Comparator;
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
import popjava.combox.socket.ComboxSocket;
import popjava.system.POPSystem;
import popjava.util.LogWriter;
import popjava.util.POPRemoteCaller;

/**
 * This combox implement the protocol ssl
 */
public class ComboxSecureSocket extends ComboxSocket<SSLSocket> {
	
	
	/**
	 * This is used by ServerCombox (server).
	 * Create a new combox from a server.
	 * Call {@link #serverAccept(java.lang.Object)   } to let the client connect.
	 */
	public ComboxSecureSocket() {
		super();
	}

	/**
	 * This is used by Combox (client).
	 * Create a combox for a client.
	 * Call {@link #connectToServer(popjava.baseobject.POPAccessPoint, int)  } to actually connect the client.
	 * @param networkUUID the id of the network
	 */
	public ComboxSecureSocket(String networkUUID) {
		super(networkUUID);
	}

	@Override
	protected boolean connectToServer() {
		try {			
			SSLContext sslContext = SSLUtils.getSSLContext();
			SSLSocketFactory factory = sslContext.getSocketFactory();

			available = false;
			int accessPointSize = accessPoint.size();
			
			List<AccessPoint> aps = getSortedAccessPoints(POPSystem.getHostIP(), accessPoint, ComboxSecureSocketFactory.PROTOCOL);
			
			for (int i = 0; i < aps.size() && !available; i++) {
				AccessPoint ap = aps.get(i);
				
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
