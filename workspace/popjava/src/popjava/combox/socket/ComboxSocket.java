package popjava.combox.socket;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import popjava.base.MessageHeader;
import popjava.baseobject.AccessPoint;
import popjava.buffer.POPBuffer;
import popjava.combox.Combox;
import popjava.combox.ComboxFactory;
import popjava.util.LogWriter;
import popjava.util.POPRemoteCaller;

/**
 * This combox implement the protocol Socket
 */
public class ComboxSocket extends Combox<Socket> {
	
	protected byte[] receivedBuffer;
	public static final int BUFFER_LENGTH = 1024 * 1024 * 8;
	protected InputStream inputStream = null;
	protected OutputStream outputStream = null;
	private final int STREAM_BUFFER_SIZE = 8 * 1024 * 1024; //8MB

	private static final ComboxFactory MY_FACTORY = new ComboxSocketFactory();
	
	@Override
	public String toString(){
		if(peerConnection != null){
			return peerConnection.toString();
		}
		
		return "Closed";
	}
	
	/**
	 * This is used by ServerCombox (server).
	 * Create a new combox from a server.
	 * Call {@link #serverAccept(java.lang.Object)   } to let the client connect.
	 * @throws IOException	Thrown is any IO exception occurred during the creation
	 */
	public ComboxSocket() throws IOException {
		super();
		receivedBuffer = new byte[BUFFER_LENGTH];
	}

	/**
	 * This is used by Combox (client).
	 * Create a combox for a client.
	 * Call {@link #connectToServer(popjava.baseobject.POPAccessPoint, int)  } to actually connect the client.
	 * @param networkUUID 
	 */
	public ComboxSocket(String networkUUID) {
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
		receivedBuffer = null;
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
				//LogWriter.writeExceptionLog(e);
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
			LogWriter.writeDebugInfo("[ComboxSocket] Couldn't open streams on the server side.");
			return false;
		}
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
			}
		}
		return available;
	}

	@Override
	protected boolean sendNetworkName() {
		byte[] networkNameUTF8 = networkUUID.getBytes(StandardCharsets.UTF_8);
		
		// to send buffer
		ByteBuffer intBuffer = ByteBuffer.allocate(Integer.BYTES);
		intBuffer.putInt(networkNameUTF8.length);
		
		// send it
		try {
			outputStream.write(intBuffer.array());
			outputStream.write(networkNameUTF8);
			outputStream.flush();
			
			return true;
		} catch (IOException e) {
			LogWriter.writeDebugInfo("[ComboxSocket] Couldn't send network name");
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
			networkUUID = new String(networkNameBytes, StandardCharsets.UTF_8);
			
			return true;
		} catch (IOException e) {
			LogWriter.writeDebugInfo("[ComboxSocket] Couldn't read network name");
			return false;
		}
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
				if (conf.isDebugCombox()) {
					String logInfo = String.format(
							"%s. failed to receive header. receivedLength= %d, Message length %d",
							this.getClass().getName(), result, headerLength);
					LogWriter.writeDebugInfo(logInfo);
				}
				close();
			} else {
				buffer.extractHeader();				
			}
			
			return result;
		} catch (Exception e) {
			if (conf.isDebugCombox()){
				LogWriter.writeDebugInfo("ComboxSocket Error while receiving data:"
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
			if (conf.isDebugCombox()){
				LogWriter.writeDebugInfo(this.getClass().getName()
						+ "-Send:  Error while sending data - " + e.getMessage() +" "+outputStream);
				LogWriter.writeExceptionLog(e);
			}
			return -1;
		}
	}

	@Override
	protected boolean exportConnectionInfo() {
		remoteCaller = new POPRemoteCaller(
			peerConnection.getInetAddress(),
			MY_FACTORY.getComboxName(),
			MY_FACTORY.isSecure()
		);
		return true;
	}

}