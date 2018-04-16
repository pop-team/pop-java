package ch.icosys.popjava.core.combox;

import ch.icosys.popjava.core.baseobject.POPAccessPoint;
import ch.icosys.popjava.core.buffer.BufferFactory;
import ch.icosys.popjava.core.buffer.POPBuffer;
import ch.icosys.popjava.core.util.POPRemoteCaller;

public class ComboxConnection {

    private Combox combox;
    private int connectionID;
    
    public ComboxConnection(final Combox combox, int connectionID) {
        this.combox = combox;
        this.connectionID = connectionID;
    }
    
    public Combox getCombox() {
        return combox;
    }

    public int send(POPBuffer buffer) {
    	buffer.getHeader().setConnectionID(connectionID);
        return combox.send(buffer);
    }

    public int receive(POPBuffer buffer, int requestId) {
        return combox.receive(buffer, requestId, connectionID);
    }

    public String getNetworkUUID() {
        return combox.getNetworkUUID();
    }

    public POPRemoteCaller getRemoteCaller() {
        return combox.getRemoteCaller();
    }

    public POPAccessPoint getAccessPoint() {
        return combox.getAccessPoint();
    }

    public void close() {
    	if(combox != null) {
            combox.close(connectionID);
            combox = null;
    	}
    }

    public BufferFactory getBufferFactory() {
        return combox.getBufferFactory();
    }

    public void setBufferFactory(BufferFactory bufferFactory) {
        combox.setBufferFactory(bufferFactory);
    }
    
    public int getConnectionID() {
    	return connectionID;
    }
    
}
