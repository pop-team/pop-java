package testsuite.matrix;

import java.util.Random;

import popjava.buffer.POPBuffer;
import popjava.dataswaper.IPOPBase;

public class Matrix2D implements IPOPBase {
	protected int nbLine, nbCol, dataSize;
	protected float[] value;
	
	public Matrix2D(){
		nbLine=0;
		nbCol=0;
		dataSize=0;
		value = null;
	}
	
	public Matrix2D(int line, int col){
		nbLine = line;
		nbCol = col;
		dataSize = nbLine*nbCol;
		value = new float[dataSize];
	}
	
	public void init(){
		Random r = new Random();
		if(nbLine!=0 && nbCol!=0){
			for (int i = 0; i < value.length; i++) {
				value[i] = r.nextFloat()*10000;
			}
		}
	}
	
	public void fill(float v){
		if(nbLine!=0 && nbCol!=0){
			for (int i = 0; i < value.length; i++) {
				value[i] = v;
			}
		}
	}
	
	public void zero(){
		if(nbLine!=0 && nbCol!=0){
			for (int i = 0; i < value.length; i++) {
				value[i] = 0;
			}
		}
	}
	
	
	public float get(int line, int col){
		int pos = line * nbCol + col;
		return value[pos];
	}
	
	public int getLines(){
		return nbLine;
	}
	
	public int getCols(){
		return nbCol;
	}
	
	
	
	
	@Override
	public boolean deserialize(POPBuffer buffer) {
		nbCol = buffer.getInt();
		nbLine = buffer.getInt();
		dataSize = buffer.getInt();
		int size = buffer.getInt();
		value = buffer.getFloatArray(size);
		return false;
	}

	@Override
	public boolean serialize(POPBuffer buffer) {
		buffer.putInt(nbCol);
		buffer.putInt(nbLine);
		buffer.putInt(dataSize);
		buffer.putFloatArray(value);
		return true;
	}

}
