package lib;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

import popjava.buffer.POPBuffer;
import popjava.dataswaper.IPOPBase;

public abstract class Matrix2D implements IPOPBase {
	
	protected int nbLine;
	protected int nbCol;
	protected int dataSize;
	protected double [] value;
	protected double [] shared;
	
	public Matrix2D(){
		
	}
	
	public Matrix2D(int line, int col){
		nbLine = line;
		nbCol= col;
		dataSize = nbLine*nbCol;
		value = new double[dataSize + 1];
		
		value[dataSize] = 0;
		shared = null;
	}
	
	public Matrix2D(Matrix2D m){
		nbLine = m.nbLine;
		nbCol = m.nbCol;
		dataSize = m.dataSize;
		value = m.value;
		
		if (m.shared == null){
			shared = value;
		}else {
			shared = m.shared;
		}
		
		if (shared != null){
			shared[dataSize] = shared[dataSize] + 1;
		}
	}
	
	public void showState(String s, boolean all)
	{
		System.out.print(s+" M"+nbLine+"x"+nbCol+" at "+value.hashCode()+", data size = "+dataSize+" ");
	  if (shared != null){
		  System.out.print("ref = %f, "+shared[dataSize]);
	  }else if (value != null){
		  System.out.print("ref = %f, "+ value[dataSize]);
	  }
	  System.out.print("shared = "+ shared.hashCode());
	  if (value != null){
		  System.out.print(" at " + value[dataSize]);
	  }
	  System.out.println();
	  
	  if (all && value != null){
		  display();
	  }
	}

	public void init()   //random [0 -> 199/7]
	{
		Random random = new Random();
		if(value != null){
		  for (int i=0; i < nbCol * nbLine; i++){
		      value[i] = random.nextInt(200)/7.;
		  }
	  }
	}
	
	public void initInc(){
		if(value != null){
		  for (int i=0; i < nbCol * nbLine; i++){
		      value[i] = i + 1;
		  }
	  }
	}

	/**
	 * Inits the matrix with the content specified in a certain file
	 */
	public void init(String filename) throws IOException{ 
		
	}

	/**
	 * Fills the matrix with a certain value
	 * @param v
	 */
	public void fill(double v) // fill with v
	{
	  if (value!=null){
		  for (int i=0; i<nbCol*nbLine; i++){
		      value[i] = v;
		  }
	  }
	}

	/**
	 * Fills the matrix with the value 0
	 */
	public void zero() // fill with 0
	{
	  if (value != null){
		  for (int i=0; i<nbCol*nbLine; i++){
			  value[i] = 0;
		  }
	  }
	}
	
	/**
	 * Transforms the matrix in 0x0 matrix
	 */
	public void reset()  // if possible Free memory space used by data 
	{
		value = null;
		shared = null;
		nbCol = 0;
		nbLine = 0;
		dataSize = 0;
	}
	
	public abstract double testMat();
	
	public abstract double get(int line, int col);

	public abstract void set(int line, int col, double v);
	
	public int getLines(){
		return nbLine;
	}

	public int getCols(){
		return nbCol;
	}
	
	/**
	 * Assigns the content of another matrix to this matrix
	 * @param m
	 */
	public void assign(Matrix2D m)
	{
	  nbLine = m.nbLine;
	  nbCol = m.nbCol;
	  dataSize = m.dataSize;
	  value = m.value;
	  
	  if(m.shared == null){
		  shared = m.value; 
	  }else {
		  shared = m.shared;
	  }
	  
	  if (shared != null) {
		  shared[dataSize] = shared[dataSize] + 1;
	  }
	}

	public void display(){
		
	}

	public void display(int n){
		
	}

	@Override
	public boolean deserialize(POPBuffer buffer) {
		nbCol = buffer.getInt();
		nbLine = buffer.getInt();
		dataSize = buffer.getInt() - 1;
		if(dataSize > 0){
			value = buffer.getDoubleArray(dataSize + 1);
		}else{
			value = null;
		}
		
		return true;
	}

	@Override
	public boolean serialize(POPBuffer buffer) {
		buffer.putInt(nbCol);
		buffer.putInt(nbLine);
		buffer.putDoubleArray(value);
		return true;
	}
}