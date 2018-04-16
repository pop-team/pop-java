package ch.icosys.popjava.core.combox.socket;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import ch.icosys.popjava.core.base.MessageHeader;
import ch.icosys.popjava.core.baseobject.AccessPoint;
import ch.icosys.popjava.core.baseobject.POPAccessPoint;
import ch.icosys.popjava.core.buffer.POPBuffer;
import ch.icosys.popjava.core.combox.Combox;
import ch.icosys.popjava.core.util.LogWriter;

public abstract class ComboxSocket<T extends Socket> extends Combox<T> {

	public static final int BUFFER_LENGTH = 1024 * 1024 * 8;
	protected static final int STREAM_BUFFER_SIZE = 8 * 1024 * 1024; //8MB
	
	protected final byte[] receivedBuffer = new byte[BUFFER_LENGTH];
	protected InputStream inputStream = null;
	protected OutputStream outputStream = null;
	
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
					
					registerCommuncation();
					
					//System.out.println("GOT "+requestIdPacket+" "+packetConnectionID+" "+messageLength+" on "+connectionID);
                    
                    //HANDLE SPECIAL COMBOX packet
                    //TODO: Make this cleaner. 
					if(packetConnectionID == 0 && requestIdPacket == 2 && requestId != 2) {
                        POPBuffer tempBuffer = getBufferFactory().createBuffer();
                        tempBuffer.resetToReceive();
                        int length = readPacket(tempBuffer, messageLength, requestIdPacket, packetConnectionID);
                        
                        if(length >= MessageHeader.HEADER_LENGTH) {
                        	tempBuffer.extractHeader();
                        }

                        if(tempBuffer.getHeader().getRequestType() == MessageHeader.RESPONSE) {
                            //System.out.println("Got rebind response " +connectionID+" "+requestId);
                            inputStream.reset();
                            yieldThread = true;
                            continue;
                        }else {
                        	handleComboxMessages(tempBuffer);
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
    	registerCommuncation();
    	
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
	
	public static List<AccessPoint> getSortedAccessPoints(String myHost, POPAccessPoint accessPoint, String protocol){
		List<AccessPoint> aps = new ArrayList<>();
		for (int i = 0; i < accessPoint.size(); i++) {
			AccessPoint ap = accessPoint.get(i);
			if (ap.getProtocol().compareToIgnoreCase(protocol) != 0){
				continue;
			}
			aps.add(ap);
		}
		
		int countPoints = myHost.length() - myHost.replace(".", "").length();
		
		if(countPoints == 3) {
			final String subnet = myHost.substring(0, myHost.indexOf("."));
			
			Collections.sort(aps, new Comparator<AccessPoint>() {

				@Override
				public int compare(AccessPoint o1, AccessPoint o2) {
					
					int countPoints1 = o1.getHost().length() - o1.getHost().replace(".", "").length();
					int countPoints2 = o2.getHost().length() - o2.getHost().replace(".", "").length();
					
					if(countPoints1 == countPoints2) {
						//If both hosts are in my subnet, sort by string
						if(o1.getHost().startsWith(subnet) && o2.getHost().startsWith(subnet)) {
							return o1.getHost().compareTo(o2.getHost());
						}
						
						//if first host is in my subnet, priority to that
						if(o1.getHost().startsWith(subnet)) {
							return -1;
						}
						
						//Same for second
						if(o2.getHost().startsWith(subnet)) {
							return 1;
						}
						
						boolean privateSubnet1 = isHostInPrivateSubnet(o1.getHost());
						boolean privateSubnet2 = isHostInPrivateSubnet(o2.getHost());
						
						if(privateSubnet1 && !privateSubnet2) {
							return 1;
						}
						
						if(!privateSubnet1 && privateSubnet2) {
							return -1;
						}
						
						return o1.getHost().compareTo(o2.getHost());
					}
					
					return countPoints1 - countPoints2;
				}
			});
		}
		
		return aps;
	}
	
	private static boolean isHostInPrivateSubnet(String host) {
		return host.startsWith("10.") || host.startsWith("172.16.") || host.startsWith("192.168.");
	}
}

