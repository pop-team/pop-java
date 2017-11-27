package testsuite.matrix;

import java.text.DecimalFormat;

public class Matrix2Dlc extends Matrix2D {
	
	
	public Matrix2Dlc(){
		
	}
	
	public Matrix2Dlc(int line, int col){
		super(line, col);
	}
	
	public float get(int line, int col){
		return value[line*nbCol+col];
	}
	
	public void set(int line, int col, float v){
		value[line*nbCol+col] = v;
	}
	
	public Matrix2Dlc getLinesBloc(int noLine, int nbLines){
		if(dataSize > 0 && nbLine >= (noLine+nbLines)){
			//TODO: implement
		}
		return null;
	}
	
	public void display(){
		if(dataSize>0){
			DecimalFormat df = new DecimalFormat("#.###");
			
			for (int i = 0; i < nbLine; i++) {
				for (int j = 0; j < nbCol; j++) {
					System.out.print(df.format(value[i*nbCol+j])+" ");
				}
				System.out.println();
			}
			System.out.println(".....................");
		}
	}
	
	public void display(int n){
		if(n>nbLine) n=nbLine;
		if(n>nbCol) n=nbCol;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				System.out.print(value[i*nbCol+j]+" ");
			}
			System.out.println("..");
		}
		System.out.println(".....................");
	}
}
