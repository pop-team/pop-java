import popjava.annotation.POPClass;

@POPClass(isDistributable = false)
public class Main{
	public static void main(String[] args){
			
		B self = new B();
		long start = System.currentTimeMillis();
		self.test();
		
		System.out.println("Finished in "+(System.currentTimeMillis() - start)/1000+"s");
		System.out.println("Should be around 1 second");
	}
} 
