package ch.icosys.popjava.junit.localtests.readerWriter;

import java.util.List;

public class Reader {

	private List<Worker> workers;

	public Reader(List<Worker> workers) {
		this.workers = workers;
	}

	public void work() {
		for (Worker work : workers) {
			work.work();
		}
	}

}
