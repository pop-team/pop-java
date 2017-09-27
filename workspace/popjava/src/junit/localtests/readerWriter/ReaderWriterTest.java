package junit.localtests.readerWriter;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import popjava.PopJava;
import popjava.system.POPSystem;

public class ReaderWriterTest {

	@Test
	public void test(){
		POPSystem.initialize();
		
		int workerCount = 5;
		
		Writer writer = PopJava.newActive(Writer.class, "localhost");
		
		List<Worker> workers = new ArrayList<>();
		
		for(int i = 0; i < workerCount; i++){
			workers.add(PopJava.newActive(Worker.class, "localhost", writer));
		}
		
		Reader reader = new Reader(workers);
		reader.work();
		assertEquals(workerCount, writer.getWritten());
		POPSystem.end();
		
	}
}
