package ch.icosys.popjava.core.combox;

import ch.icosys.popjava.core.baseobject.POPAccessPoint;
import ch.icosys.popjava.core.buffer.BufferFactory;
import ch.icosys.popjava.core.buffer.POPBuffer;
import ch.icosys.popjava.core.util.POPRemoteCaller;

public class ComboxConnection<T> {

	private Combox<T> combox;

	protected T peerConnection;
	private int connectionID;

	public ComboxConnection(final Combox<T> combox, int connectionID) {
		this.combox = combox;
		this.connectionID = connectionID;
		this.peerConnection = combox.peerConnection;
	}

	public Combox<T> getCombox() {
		return combox;
	}

	public int send(POPBuffer buffer) {
		throwIfClosed();
		
		buffer.getHeader().setConnectionID(connectionID);
		return combox.send(buffer);
	}

	public int receive(POPBuffer buffer, int requestId) {
		throwIfClosed();
		return combox.receive(buffer, requestId, connectionID);
	}

	public String getNetworkUUID() {
		throwIfClosed();
		return combox.getNetworkUUID();
	}

	public POPRemoteCaller getRemoteCaller() {
		throwIfClosed();
		return combox.getRemoteCaller();
	}

	public POPAccessPoint getAccessPoint() {
		throwIfClosed();
		return combox.getAccessPoint();
	}

	public void close() {
		if (combox != null) {
			combox.close(connectionID);
			combox = null;
		}
	}

	public BufferFactory getBufferFactory() {
		throwIfClosed();
		return combox.getBufferFactory();
	}

	public void setBufferFactory(BufferFactory bufferFactory) {
		throwIfClosed();
		combox.setBufferFactory(bufferFactory);
	}

	public int getConnectionID() {
		return connectionID;
	}
	
	private void throwIfClosed() {
		if(combox == null) {
			throw new RuntimeException("Combox closed");
		}
	}

}
