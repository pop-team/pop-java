package ch.icosys.popjava.junit.localtests.readerWriter;

import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPConfig;
import ch.icosys.popjava.core.annotation.POPSyncConc;
import ch.icosys.popjava.core.annotation.POPConfig.Type;
import ch.icosys.popjava.core.base.POPObject;

@POPClass
public class Worker extends POPObject {
	private Writer writer;

	public Worker() {

	}

	public Worker(@POPConfig(Type.URL) String ip, Writer writer) {
		this.writer = writer.makePermanent();
	}

	@POPSyncConc
	public void work() {
		writer.write();
	}

}
