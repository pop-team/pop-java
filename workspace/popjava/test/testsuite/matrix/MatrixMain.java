package testsuite.matrix;

import popjava.system.POPSystem;



public class MatrixMain {
	
	public static void main(String... argvs){
		int alines, acols, bcols, nbWorker, nbBlocB;
		int toDisplay = 5;
		
		alines = acols = bcols = Integer.parseInt(argvs[0]);
		nbWorker = Integer.parseInt(argvs[1]);
		nbBlocB = Integer.parseInt(argvs[2]);
		
		System.out.println("Matrxi test started ...");
		
		Matrix2Dlc a = new Matrix2Dlc(alines, acols);
		Matrix2Dcl b = new Matrix2Dcl(acols, bcols);
		
		a.init();
		b.init();
		
		a.display(toDisplay);
		b.display(toDisplay);
		POPSystem.end();
	}
}
