package testsuite.matrix;

import popjava.base.POPObject;
import popjava.base.Semantic;

public class MatrixWorker extends POPObject {
	
	private int id;
	
	private Matrix2Dlc resMatrix;
	private Matrix2Dcl bMatrix;
	private int nbLinesA, nbColsA, nbColsB, sizeB;
	private float[][] linesA, colsB;
	private boolean nextBbloc;
	private double computeTime;
	
	public MatrixWorker(){		
		Class<?> c = MatrixWorker.class;
		initializePOPObject();
		addSemantic(c, "setId", Semantic.Asynchronous | Semantic.Sequence);
	}
	
	public MatrixWorker(int i, int nbLineA, int nbColA, int nbColB, String machine){
		nbLinesA = nbLineA;
		nbColsA = nbColA;
		nbColsB = nbColB;
		id = i;
		resMatrix = new Matrix2Dlc(nbLineA, nbColB);
		resMatrix.zero();
		sizeB=0;
		nextBbloc = false;
		Class<?> c = MatrixWorker.class;
		od.setHostname(machine);
		initializePOPObject();
		addSemantic(c, "solve", Semantic.Asynchronous | Semantic.Concurrent);
		addSemantic(c, "putB", Semantic.Asynchronous | Semantic.Sequence);
		addSemantic(c, "getResult", Semantic.Synchronous | Semantic.Mutex);
	}
	
	public void solve(Matrix2Dlc a, Matrix2Dcl b){
				
	}
	
	public void putB(Matrix2Dcl b){
		
	}
	
	public Matrix2Dlc getResult(double t){
		
		return null;
	}

}
