package junit.benchmarks.readerWriter;

import java.util.List;

public class Reader {
	
	private List<Worker> workers;
	private int total = 0;
	public Reader(List<Worker> workers, int total){
		this.workers = workers;
		this.total = total;
	}

	public void work(){
		int workerIndex = 0;
		while(total > 0){
			Worker worker = workers.get(workerIndex);
			worker.work();
			workerIndex++;
			workerIndex = workerIndex % workers.size();
			total--;
			
			//System.out.println("Sent "+total);
		}
	}
	
}
