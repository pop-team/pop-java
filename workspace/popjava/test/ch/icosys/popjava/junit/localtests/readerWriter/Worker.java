package junit.localtests.readerWriter;

import popjava.annotation.POPClass;
import popjava.annotation.POPConfig;
import popjava.annotation.POPConfig.Type;
import popjava.annotation.POPSyncConc;
import popjava.base.POPObject;

@POPClass
public class Worker extends POPObject{
	private Writer writer;

	public Worker(){
		
	}
	
	public Worker(@POPConfig(Type.URL) String ip, Writer writer){
		this.writer = writer.makePermanent();
	}
	
	@POPSyncConc
	public void work(){
		writer.write();
	}
	
}
