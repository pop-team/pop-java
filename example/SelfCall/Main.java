import ch.icosys.popjava.core.annotation.POPClass;

@POPClass(isDistributable = false)
public class Main{
	public static void main(String[] args){
	
		long start = System.currentTimeMillis();
		
		Self self = new Self();
		self.test();
		System.out.println("1) Finished in "+(System.currentTimeMillis() - start)/1000+"s "+self.getValue());
		
		start = System.currentTimeMillis();
		self.test2();
		System.out.println("2) Finished in "+(System.currentTimeMillis() - start)/1000+"s "+self.getValue());
	}
} 
