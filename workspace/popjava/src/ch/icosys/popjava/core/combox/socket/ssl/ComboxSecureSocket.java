package ch.icosys.popjava.core.combox.socket.ssl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
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

import ch.icosys.popjava.core.baseobject.AccessPoint;
import ch.icosys.popjava.core.combox.ComboxFactory;
import ch.icosys.popjava.core.combox.socket.ComboxSocket;
import ch.icosys.popjava.core.system.POPSystem;
import ch.icosys.popjava.core.util.LogWriter;
import ch.icosys.popjava.core.util.POPRemoteCaller;
import ch.icosys.popjava.core.util.ssl.SSLUtils;

/**
 * This combox implement the protocol ssl
 */
public class ComboxSecureSocket extends ComboxSocket<SSLSocket> {

	protected static final ComboxFactory MY_FACTORY = new ComboxSecureSocketFactory();

	/**
	 * This is used by ServerCombox (server). Create a new combox from a server.
	 * Call {@link #serverAccept(java.lang.Object) } to let the client connect.
	 */
	public ComboxSecureSocket() {
		super();
	}

	/**
	 * This is used by Combox (client). Create a combox for a client. Call
	 * {@link #connectToServer(ch.icosys.popjava.core.baseobject.POPAccessPoint, int) }
	 * to actually connect the client.
	 * 
	 * @param networkUUID
	 *            the id of the network
	 */
	public ComboxSecureSocket(String networkUUID) {
		super(networkUUID);
	}

	@Override
	protected boolean connectToServer() {
		
		List<IOException> exceptions = new ArrayList<>();
		
		try {
			SSLContext sslContext = SSLUtils.getSSLContext();
			SSLSocketFactory factory = sslContext.getSocketFactory();

			available = false;

			List<AccessPoint> aps = getSortedAccessPoints(POPSystem.getHostIP(), accessPoint,
					ComboxSecureSocketFactory.PROTOCOL);

			for (int i = 0; i < aps.size() && !available; i++) {
				AccessPoint ap = aps.get(i);

				String host = ap.getHost();
				int port = ap.getPort();

				try {
					// Create an unbound socket
					SocketAddress sockaddress = new InetSocketAddress(host, port);
					if (timeOut > 0) {
						peerConnection = (SSLSocket) factory.createSocket();

						// LogWriter.writeExceptionLog(new Exception());
						// LogWriter.writeExceptionLog(new Exception("Open
						// connection to "+host+":"+port+" remote:
						// "+peerConnection.getLocalPort()));
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
					exceptions.add(e);
					available = false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(!available) {
			for(IOException e : exceptions) {
				e.printStackTrace();
			}
		}

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

					remoteCaller = new POPRemoteCaller(peerConnection.getInetAddress(), MY_FACTORY.getComboxName(),
							getNetworkUUID(), MY_FACTORY.isSecure(), fingerprint);
					return true;
				}
			}
		} catch (Exception e) {
			LogWriter.writeExceptionLog(e);
		}
		return false;
	}
}
