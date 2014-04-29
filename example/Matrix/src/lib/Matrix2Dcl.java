package lib;

import java.text.DecimalFormat;

import lib.Matrix2D;

public class Matrix2Dcl extends Matrix2D {

	public Matrix2Dcl(){
		
	}
	
	public Matrix2Dcl(int line, int col){
		super(line, col);
	}
	
	public Matrix2Dcl(Matrix2D m){
		super(m);
	}
	
	@Override
	public double testMat()
	{
		if (value != null) {
		    for (int i=0; i<nbCol; i++){
		      for (int j=0; j<nbLine; j++){
		        value[i*nbCol + j] = j+1;
		      }
		    }
		    return (nbLine*(nbLine+1)*(2*nbLine+1))/6.;
		 }
		 
		return -1;
	}

	@Override
	public double get(int line, int col) {
		return value[col*nbLine+line];
	}

	@Override
	public void set(int line, int col, double v) {
		value[col * nbLine + line] = v;
	}

	public Matrix2Dcl getColsBloc(int noCol, int nbCols)
	{
	  if ((value!= null) || (nbCol>=(noCol+nbCols)))
	  {
	    Matrix2Dcl tmp = new Matrix2Dcl();
	    tmp.nbCol = nbCols;
	    tmp.nbLine = nbLine;
	    tmp.dataSize = nbCols * nbLine + 1;
	    tmp.value = new double[tmp.dataSize]; //TODO: Correctly implement this: &(value[noCol*nbLine]);
	    System.arraycopy(value, noCol*nbLine, tmp.value, 0, nbCols * nbLine);
	    if(shared==null){
	    	tmp.shared = value; 
	    }else{
	    	tmp.shared = shared;
	    }
	    if (tmp.shared != null){
	    	tmp.shared[dataSize] = tmp.shared[dataSize] + 1;
	    }
	    return tmp;
	  }
	  else
	  {
	    Matrix2Dcl tmp = new Matrix2Dcl();
	    showState("ERROR Getting columns:", false);
	    return tmp;
	  }
	}

	public void setBloc(int noLine, int noCol, Matrix2Dcl v) {
		if ((nbCol >= noCol + v.nbCol) && (nbLine >= (noLine + v.nbLine))) {
			if (value == null) {
				dataSize = nbLine * nbCol;
				value = new double[dataSize + 1];
				value[dataSize] = 0;
				shared = null;
			}

			for (int i = 0; i < v.nbCol; i++) {
				// arraycopy replaces the following for loop
				// for (int j=0; j<v.nbLine; j++)
				// value[(noCol+i)*nbLine+noLine+j]=v.value[i*v.nbLine+j];

				System.arraycopy(v.value, i * v.nbLine, value, (noCol + i)
						* nbLine + noLine, v.nbLine);
				// memcpy(&(value[(noCol+i)*nbLine+noLine]),&(v.value[i*v.nbLine]),
				// v.nbLine*sizeof(ValueType));
			}

		} else {
			System.out.println("Matrix ERROR: Non coherent bloc setting ("
					+ noLine + "," + noCol + ") !!!");
		}

	}

	public void setColsBloc(int noCol, Matrix2Dcl v) {
		if ((nbLine == v.nbLine) && (nbCol >= (noCol + v.nbCol))) {
			if (value == null) {
				dataSize = nbLine * nbCol;
				value = new double[dataSize + 1];
				value[dataSize] = 0;
				shared = null;
			}
			// memcpy (&(value[noCol*nbLine]),v.value,
			// v.nbCol*v.nbLine*sizeof(ValueType));
			// memcpy replaces the following for loop
			// for (int i=0; i<v.nbCol*v.nbLine; i++)
			// value[noCol*nbLine+i]=v.value[i];
			System.arraycopy(v.value, 0, value, noCol * nbLine, v.nbCol
					* v.nbLine);
		} else {
			System.out
					.println("Matrix ERROR: Non coherent column setting !!!\n");
		}
	}

	@Override
	public void display() {
		DecimalFormat df = new DecimalFormat("#.###");
		
		for (int i = 0; i < nbLine; i++) {
			for (int j = 0; j < nbCol; j++) {
				System.out.print(df.format(value[j * nbLine + i])+" ");
			}

			System.out.println();
		}
	}

	@Override
	public void display(int n) {
		n = Math.min(nbCol, Math.min(n, nbLine));
		DecimalFormat df = new DecimalFormat("#.###");
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				System.out.print(df.format(value[j * nbLine + i])+" ");
			}
			System.out.println(".. "+df.format(value[nbLine * (nbCol - 1) + i])+" ");
		}
		System.out.println("...................." + value[(nbLine * nbCol) - 1]
				+ " \n");
	}

}