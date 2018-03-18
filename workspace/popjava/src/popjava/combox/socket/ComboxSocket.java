package popjava.combox.socket;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

import popjava.base.MessageHeader;
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
	
	private int connectionCounter = 10;
	
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
			
			do {
				synchronized (inputStream) {
					inputStream.mark(12);
					
					int read = 0;
                    //Get connectionID
                    while(read < temp.length){
                        int tempRead = inputStream.read(temp, read, temp.length - read);
                        if(tempRead < 0){
                            close();
                            return -1;
                        }
                        read += tempRead;
                    }
                    
                    int packetConnectionID = ByteBuffer.wrap(temp).getInt();
					
				    read = 0;
				    //Get size
				    while(read < temp.length){
				    	int tempRead = inputStream.read(temp, read, temp.length - read);
				    	if(tempRead < 0){
				    		close();
							return -1;
				    	}
				        read += tempRead;
				    }
					
					int messageLength = buffer.getTranslatedInteger(temp);
					
					if (messageLength <= 0) {
						close();
						return -1;
					}
					
					//Get requestID
					read = 0;
				    //Get size
				    while(read < temp.length){
				    	int tempRead = inputStream.read(temp, read, temp.length - read);
				    	if(tempRead < 0){
				    		close();
							return -1;
				    	}
				        read += tempRead;
				    }
					
					int requestIdPacket = buffer.getTranslatedInteger(temp);
					
					System.out.println("GOT "+requestIdPacket+" "+packetConnectionID+" "+messageLength);
                    
                    //HANDLE SPECIAL COMBOX packet
                    //TODO: Make this cleaner. 
					if(packetConnectionID == 0 && requestIdPacket == 2 && requestId != 2) {
                        POPBuffer tempBuffer = getBufferFactory().createBuffer();
                        tempBuffer.resetToReceive();
                        int length = readPacket(tempBuffer, messageLength, requestIdPacket);
                        
                        if(length >= MessageHeader.HEADER_LENGTH) {
                            tempBuffer.extractHeader();
                        }else {
                            System.out.println("THIS SHOULD NOT HAPPEN");
                        }

                        if(tempBuffer.getHeader().getRequestType() == MessageHeader.RESPONSE) {
                            System.out.println("Got rebind response " +connectionID+" "+requestId);
                            inputStream.reset();
                            Thread.yield();
                            continue;
                        }else {
                            if(tempBuffer.getHeader().getMethodId() != 1234) {
                                System.out.println("GOT WRONG METHOD ID, PLEASE FIX: "+tempBuffer.getHeader().getMethodId()+" "+length+" "+this);
                                System.out.println(tempBuffer.getHeader()+" "+length);
                                System.exit(0);
                            }
                            
                            System.out.println("Rebinding this combox to broker"+this);
                            
                            int newConnectionID = connectionCounter++;
                            
                            bindToBroker(newConnectionID);
                            
                            tempBuffer.reset();
                            MessageHeader header = new MessageHeader();
                            header.setRequestType(MessageHeader.RESPONSE);
                            header.setRequestID(requestIdPacket);
                            tempBuffer.setHeader(header);
                            tempBuffer.putInt(newConnectionID);
                            
                            send(tempBuffer, 0);
                        }
                        
                        continue;
                    }
					                    
                    if(packetConnectionID != connectionID) {
                        inputStream.reset();
                        System.out.println("WRONG CONNECTION "+packetConnectionID+" instead of "+connectionID +" "+this);
                        Thread.yield();
                        System.exit(0);
                        continue;
                    }
					
					//A requestID of -1 (client or server) indicates that the requestID should be ignored
					if(requestId == -1 || requestIdPacket == -1 || requestIdPacket == requestId){
						gotPacket = true;

						result = readPacket(buffer, messageLength, requestIdPacket);
					}else{
				        System.out.println("RESET got "+requestIdPacket+" instead of "+requestId);
                        inputStream.reset();
                        Thread.yield();
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

    private int readPacket(POPBuffer buffer, int messageLength, int requestIdPacket) throws IOException {
        int result = 8;
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
	public int send(POPBuffer buffer, int connectionID) {
		try {
			buffer.packMessageHeader();
			final int length = buffer.size();
			final byte[] dataSend = buffer.array();
			
			//new Exception().printStackTrace();
			System.out.println("SEND ID "+buffer.getHeader().getRequestID()+" con : "+connectionID+" method "+buffer.getHeader()+" combox "+this);
			
			//System.out.println("Write "+length+" bytes to socket");
			synchronized (outputStream) {
			    outputStream.write(ByteBuffer.allocate(4).putInt(connectionID).array(),0 ,4);
    			outputStream.write(dataSend, 0, length);
    			
    			outputStream.flush();
			}
			
			return length;
		} catch (Exception e) {
			if (conf.isDebugCombox()){
				LogWriter.writeDebugInfo(
					"[ComboxSocket] -Send:  Error while sending data - " + e.getMessage() +" "+outputStream);
			}
			close();
			return -1;
		}
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
	public String toString(){
		if(peerConnection != null){
			return peerConnection.toString();
		}
		
		return "Closed";
	}
}

