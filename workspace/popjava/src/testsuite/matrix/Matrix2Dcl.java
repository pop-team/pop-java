package testsuite.matrix;

public class Matrix2Dcl extends Matrix2D {
	
	public Matrix2Dcl(){
		
	}
	
	public Matrix2Dcl(int line, int col){
		super(line, col);
	}
	
	public float get(int line, int col){
		return value[col*nbLine+line];
	}
	
	public void set(int line, int col, float v){
		value[col*nbLine+line]=v;
	}
	
	public void setBloc(int noLine, int noCol, Matrix2Dcl v){
		if((nbCol>=noCol+v.nbCol) && (nbLine>=(noLine+v.nbLine))){
			dataSize = nbLine*nbCol;
			value = new float[dataSize];
			for (int i = 0; i < v.nbCol; i++) {
				for (int j = 0; j < v.nbLine; j++) {
					value[(noCol+i)*nbLine+noLine+j]=v.value[i*v.nbLine+j];
				}
			}
		}
		
	}
	
	public void setColsBloc(int noCol, Matrix2Dcl v){
		if((nbLine == v.nbLine) && (nbCol>=(noCol+v.nbCol))){
			dataSize = nbLine * nbCol;
			value = new float[dataSize];
//			shared = null;
			for (int i = 0; i < v.nbCol*v.nbLine; i++) {
				value[noCol*nbLine+i] = v.value[i];
			}
		}	
	}
	
	public void display(){
		if(dataSize>0){
			for (int i = 0; i < nbLine; i++) {
				for (int j = 0; j < nbCol; j++) {
					System.out.print(value[j*nbLine+i]+" ");
				}
				System.out.println("");
			}
			System.out.println(".....................");
		}
	}
	
	public void display(int n){
		if(n>nbLine) n=nbLine;
		if(n>nbCol) n=nbCol;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				System.out.print(value[j*nbLine+i]+" ");
			}
			System.out.println("..");
		}
		System.out.println(".....................");
	}

}
