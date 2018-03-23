package popjava.combox.socket;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

import popjava.base.MessageHeader;
import popjava.baseobject.POPAccessPoint;
import popjava.buffer.POPBuffer;
import popjava.combox.Combox;
import popjava.combox.ComboxFactory;
import popjava.combox.socket.ssl.ComboxSecureSocketFactory;
import popjava.util.LogWriter;

public abstract class ComboxSocket<T extends Socket> extends Combox<T> {

	public static final int BUFFER_LENGTH = 1024 * 1024 * 8;
	protected static final int STREAM_BUFFER_SIZE = 8 * 1024 * 1024; //8MB
	
	protected final byte[] receivedBuffer = new byte[BUFFER_LENGTH];
	protected InputStream inputStream = null;
	protected OutputStream outputStream = null;
	protected static final ComboxFactory MY_FACTORY = new ComboxSecureSocketFactory();
	
	public ComboxSocket() {
		super();
	}
	
	public ComboxSocket(String networkUUID) {
		super(networkUUID);
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
	public int receive(POPBuffer buffer, int requestId, int connectionID) {
		
		int result = 0;
		try {
			buffer.resetToReceive();
			// Receive message length
			byte[] temp = new byte[4];
			
			boolean gotPacket = false;

			boolean yieldThread = false;
			do {
				if(yieldThread) {
                    Thread.yield();
                    Thread.sleep(1);
				}
				yieldThread = false;
				
				synchronized (inputStream) {
					inputStream.mark(12);
					
					if(peakHeadInteger(temp)) {
						return -1;
					}
					
					int messageLength = buffer.getTranslatedInteger(temp);
					
					if (messageLength <= 0) {
						closeInternal();
						return -1;
					}
					
					if(peakHeadInteger(temp)) {
						return -1;
					}
                    
                    int packetConnectionID = buffer.getTranslatedInteger(temp);
					
                    if(peakHeadInteger(temp)) {
						return -1;
					}
                    
					int requestIdPacket = buffer.getTranslatedInteger(temp);
					
					//System.out.println("GOT "+requestIdPacket+" "+packetConnectionID+" "+messageLength+" on "+connectionID);
                    
                    //HANDLE SPECIAL COMBOX packet
                    //TODO: Make this cleaner. 
					if(packetConnectionID == 0 && requestIdPacket == 2 && requestId != 2) {
                        POPBuffer tempBuffer = getBufferFactory().createBuffer();
                        tempBuffer.resetToReceive();
                        int length = readPacket(tempBuffer, messageLength, requestIdPacket, packetConnectionID);
                        
                        if(length >= MessageHeader.HEADER_LENGTH) {
                        	tempBuffer.extractHeader();
                        }else {
                            System.out.println("THIS SHOULD NOT HAPPEN");
                        }

                        if(tempBuffer.getHeader().getRequestType() == MessageHeader.RESPONSE) {
                            //System.out.println("Got rebind response " +connectionID+" "+requestId);
                            inputStream.reset();
                            yieldThread = true;
                            continue;
                        }else {
                        	switch(tempBuffer.getHeader().getMethodId()) {
                        	case OPEN_BIDIRECTIONAL:{//Handle the opening of a bidirectional channel
                        		if(tempBuffer.getBoolean()) {
                                	POPAccessPoint otherAP = (POPAccessPoint) tempBuffer.getValue(POPAccessPoint.class);                  		
                            		
                                	if(remoteCaller.getBrokerAP() == null) {
                                		remoteCaller.setBrokerAP(otherAP);
                                	}
                                }
                                
                                int newConnectionID = registerNewConnection();
                                
                                bindToBroker(newConnectionID);
                                
                                tempBuffer.reset();
                                MessageHeader header = new MessageHeader();
                                header.setRequestType(MessageHeader.RESPONSE);
                                header.setRequestID(requestIdPacket);
                                header.setConnectionID(0);
                                tempBuffer.setHeader(header);
                                tempBuffer.putInt(newConnectionID);
                                
                                send(tempBuffer);
                        	}
                        		break;
                        	case CLOSE_SUBCONNECTION:{
                        		System.out.println("Got closing packet");
                        		int closedConnectionID = buffer.getInt();
                        		
                        		close(closedConnectionID, false);
                        	}
                        		break;
                    		default:
                                System.out.println("Unknown internal method id "+tempBuffer.getHeader().getMethodId());

                        	}
                        }
                        
                        continue;
                    }
					                    
                    if(packetConnectionID != connectionID) {
                        inputStream.reset();
                        yieldThread = true;
                        continue;
                    }
					
					//A requestID of -1 (client or server) indicates that the requestID should be ignored
					if(requestId == -1 || requestIdPacket == -1 || requestIdPacket == requestId){
						gotPacket = true;

						result = readPacket(buffer, messageLength, requestIdPacket, packetConnectionID);
					}else{
				        //System.out.println("RESET got "+requestIdPacket+" instead of "+requestId);
                        inputStream.reset();
                        yieldThread = true;
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
				closeInternal();
			} else {
				buffer.extractHeader();
			}
			
			return result;
		} catch (Exception e) {
			if (conf.isDebugCombox()){
				LogWriter.writeDebugInfo("[ComboxSocket] Error while receiving data:"
								+ e.getMessage());
			}
			closeInternal();
			return -2;
		}
	}

	/**
	 * Reads one integer from the header.
	 * Return true if we reached the end of the stream.
	 * @param temp
	 * @return
	 * @throws IOException
	 */
	private boolean peakHeadInteger(byte[] temp) throws IOException {
		int read = 0;
		
		//Get size
		while(read < temp.length){
			int tempRead = inputStream.read(temp, read, temp.length - read);
			if(tempRead < 0){
				closeInternal();
				return true;
			}
		    read += tempRead;
		}
		
		return false;
	}

    private int readPacket(POPBuffer buffer, int messageLength, int requestIdPacket, int packetConnectionID) throws IOException {
        int result = 12;
        buffer.putInt(messageLength);
        messageLength = messageLength - 4;
        
        buffer.putInt(packetConnectionID);
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
        		
        		/*for(int i = 0; i < receivedLength; i++) {
        		    System.out.print(receivedBuffer[i]+" ");
        		}
        		System.out.println();*/
        	} else {
        		break;
        	}
        }
        return result;
    }
	
	@Override
	public int send(POPBuffer buffer) {
		
		try {
			buffer.packMessageHeader();
			final int length = buffer.size();
			final byte[] dataSend = buffer.array();
			
			//new Exception().printStackTrace();
			//System.out.println("SEND ID "+buffer.getHeader().getRequestID()+" " +buffer.size()+" con : "+buffer.getHeader().getConnectionID()+" method "+buffer.getHeader()+" combox "+this);
			
			//System.out.println("Write "+length+" bytes to socket");
			synchronized (outputStream) {
    			outputStream.write(dataSend, 0, length);
    			
    			outputStream.flush();
			}
			
			return length;
		} catch (Exception e) {
			if (conf.isDebugCombox()){
				LogWriter.writeDebugInfo(
					"[ComboxSocket] -Send:  Error while sending data - " + e.getMessage() +" "+outputStream);
			}
			closeInternal();
			return -1;
		}
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			closeInternal();
		} finally {
			super.finalize();
		}
	}
	
	@Override
	public void closeInternal() {
		//new Exception("CLOSING COMBOX SOCKET "+this).printStackTrace();
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
	public String toString(){
		if(peerConnection != null){
			return peerConnection.toString();
		}
		
		return "Closed";
	}
}

