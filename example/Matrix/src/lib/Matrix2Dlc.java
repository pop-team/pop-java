package lib;

import java.text.DecimalFormat;

import lib.Matrix2D;

public class Matrix2Dlc extends Matrix2D{
	
	public Matrix2Dlc(){
		
	}
	
	public Matrix2Dlc(int line, int col){
		super(line, col);
	}
	
	public Matrix2Dlc(Matrix2D m){
		super(m);
	}
	
	@Override
	public double testMat()
	{
		if (value!=null) {
		    for (int i=0; i<nbLine; i++){
		      for (int j=0; j<nbCol; j++){
		        value[i*nbLine+j]= j+1;
		      }
		    }
		    return (nbCol*(nbCol+1)*(2*nbCol+1))/6.;
		  }
		  return -1;
	}
	
	@Override
	public double get(int line, int col){
		return value[line*nbCol+col];
	}

	@Override
	public void  set(int line, int col, double v){
		value[line*nbCol+col]=v;
	}
	
	public Matrix2Dlc getLinesBloc(int noLine, int nbLines)
	{
	  if ( (value!=null) || (nbLine >= (noLine+nbLines)) )
	  {
		
		Matrix2Dlc tmp = new Matrix2Dlc();
	    tmp.nbCol = nbCol;
	    tmp.nbLine = nbLines;
	    tmp.dataSize = nbLines*nbCol + 1;
	    tmp.value = new double[tmp.dataSize];
	    System.arraycopy(value, noLine*nbCol, tmp.value, 0, nbLines*nbCol);
	    //tmp.value = value ;//TODO: Correctly implement this: &(value[noLine*nbCol]);
		  if(shared==null){
			  tmp.shared = value; 
		  }else {
			  tmp.shared=shared;
		  }
	    if (tmp.shared!=null) {
	    	tmp.shared[dataSize]= tmp.shared[dataSize] + 1;
	    }
	    return tmp;
	  }
	  else
	  {
		  Matrix2Dlc tmp = new Matrix2Dlc();
	    showState("ERROR Getting lines:", false);
	    return tmp;
	  }
	}

	public void setBloc(int noLine, int noCol, Matrix2Dlc v)
	{
	   if ((nbCol>=noCol+v.nbCol) && (nbLine>=(noLine+v.nbLine)) )
	   {
	     if (value==null)
	     {
	       dataSize = nbLine*nbCol;
	       value = new double[dataSize+1];
	       value[dataSize]=0;
	       shared = null;
	     }
	     
	     for (int i=0; i<v.nbLine; i++){
	    	 //memcpy(&(value[(noLine+i)*nbCol+noCol]),&(v.value[i*v.nbCol]), v.nbCol*sizeof(ValueType));
		      // memcpy replaces the following for loop
		      //for (int j=0; j<v.nbCol; j++)
			    //  value[(noLine+i)*nbCol+noCol+j]=v.value[i*v.nbCol+j];
	    	// System.out.println("** "+i*v.nbCol+" "+((noLine+i)*nbCol+noCol)+" "+v.nbCol);
	    	 System.arraycopy(v.value, i*v.nbCol, value, (noLine+i)*nbCol+noCol, v.nbCol);
	     }
	 }
	 else{
		 System.out.println("Matrix ERROR: Non coherent bloc setting ("+noLine+","+noCol+") !!!");
	 }
	}

	public void setLinesBloc(int noLine, Matrix2Dlc v)
	{
	   if ((nbCol==v.nbCol) && (nbLine>=(noLine+v.nbLine)) )
	   {
	     if (value==null)
	     {
	       dataSize = nbLine * nbCol;
	       value = new double[dataSize+1];
	       value[dataSize]=0;
	       shared = null;
	     }
	     // memcpy(&(value[noLine*nbCol]), v.value, v.nbCol*v.nbLine*sizeof(ValueType));
	     // memcpy replaces the following for loop
	     //for (int i=0; i<v.nbCol*v.nbLine; i++)
		   //  value[noLine*nbCol+i]=v.value[i];
	     System.arraycopy(v.value, 0, value, noLine * nbCol, v.nbCol*v.nbLine);
		 }
		 else {
			 System.out.println("Matrix ERROR: Non coherent line setting !!!");
		 }
	}

	public void display()
	{
	  if (value != null){
		  DecimalFormat df = new DecimalFormat("#.###");
		  for (int i=0; i<nbLine; i++)
		  {
		    for (int j=0; j<nbCol; j++){
		    	System.out.print(df.format(value[i*nbCol+j])+" ");
		    }
		    System.out.println();
		  }
		  System.out.println("....................");
	  }
	}

	public void display(int n)
	{
		n = Math.min(nbCol, Math.min(n, nbLine));
		DecimalFormat df = new DecimalFormat("#.###");
	 for (int i=0; i<n; i++)
	  {
	    for (int j=0; j<n; j++){
	    	System.out.print(df.format(value[i*nbCol+j])+" ");
	    }
	      
	    System.out.println(".. "+df.format(value[nbCol*(i+1)-1])+" ");
	  }
	 System.out.println("....................");
	}
}