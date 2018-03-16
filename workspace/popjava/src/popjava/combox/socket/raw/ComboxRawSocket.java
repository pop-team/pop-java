package popjava.combox.socket.raw;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import popjava.base.MessageHeader;
import popjava.baseobject.AccessPoint;
import popjava.buffer.POPBuffer;
import popjava.combox.Combox;
import popjava.combox.ComboxFactory;
import popjava.combox.socket.ComboxSocket;
import popjava.util.LogWriter;
import popjava.util.POPRemoteCaller;

/**
 * This combox implement the protocol Socket
 */
public class ComboxRawSocket extends ComboxSocket<Socket> {
		
	/**
	 * This is used by ServerCombox (server).
	 * Create a new combox from a server.
	 * Call {@link #serverAccept(java.lang.Object)   } to let the client connect.
	 */
	public ComboxRawSocket() {
		super();
	}

	/**
	 * This is used by Combox (client).
	 * Create a combox for a client.
	 * Call {@link #connectToServer(popjava.baseobject.POPAccessPoint, int)  } to actually connect the client.
	 * @param networkUUID the id of the network
	 */
	public ComboxRawSocket(String networkUUID) {
		super(networkUUID);
	}
	
	@Override
	protected boolean connectToServer() {
		available = false;
		int accessPointSize = accessPoint.size();
		for (int i = 0; i < accessPointSize && !available; i++) {
			AccessPoint ap = accessPoint.get(i);
			if (ap.getProtocol().compareToIgnoreCase(
					ComboxSocketFactory.PROTOCOL) != 0){
				continue;
			}
			String host = ap.getHost();
			int port = ap.getPort();
			try {
				// Create an unbound socket
				if (timeOut > 0) {
					SocketAddress sockaddress = new InetSocketAddress(host,
							port);
					peerConnection = new Socket();
					peerConnection.connect(sockaddress, timeOut);
					
					//LogWriter.writeExceptionLog(new Exception());
					//LogWriter.writeExceptionLog(new Exception("Open connection to "+host+":"+port+" remote: "+peerConnection.getLocalPort()));
				} else {
					peerConnection = new Socket(host, port);
				}
				inputStream = new BufferedInputStream(peerConnection.getInputStream());
				outputStream = new BufferedOutputStream(peerConnection.getOutputStream());
				available = true;
			} catch (IOException e) {
				available = false;
				LogWriter.writeExceptionLog(e);
			}
		}
		return available;
	}

	@Override
	protected boolean sendNetworkName() {
		byte[] networkNameUTF8 = getNetworkUUID().getBytes(StandardCharsets.UTF_8);
		
		// to send buffer
		ByteBuffer intBuffer = ByteBuffer.allocate(Integer.BYTES).putInt(networkNameUTF8.length);
		
		// send it
		try {
			outputStream.write(intBuffer.array());
			outputStream.write(networkNameUTF8);
			outputStream.flush();
			
			return true;
		} catch (Exception e) {
			LogWriter.writeDebugInfo("[ComboxSocket] Couldn't send network name");
			LogWriter.writeExceptionLog(e);
			return false;
		}
	}

	@Override
	protected boolean receiveNetworkName() {
		try {
			byte[] sizeBytes = new byte[Integer.BYTES];
			inputStream.read(sizeBytes);
			int size = ByteBuffer.wrap(sizeBytes).getInt();

			byte[] networkNameBytes = new byte[size];
			inputStream.read(networkNameBytes);
			setNetworkUUID(new String(networkNameBytes, StandardCharsets.UTF_8));
			
			return true;
		} catch (Exception e) {
			LogWriter.writeDebugInfo("[ComboxSocket] Couldn't read network name");
			LogWriter.writeExceptionLog(e);
			return false;
		}
	}

	@Override
	protected boolean exportConnectionInfo() {
		remoteCaller = new POPRemoteCaller(
			peerConnection.getInetAddress(),
			MY_FACTORY.getComboxName(),
			getNetworkUUID(),
			MY_FACTORY.isSecure()
		);
		return true;
	}

}