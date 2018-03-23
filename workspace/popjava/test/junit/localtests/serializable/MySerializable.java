package junit.localtests.serializable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author Davide Mazzoleni
 */
public class MySerializable implements Serializable {
	
	private static final long serialVersionUID = 43L;
	
	private String a;
	private int b;
	private double c;

	public MySerializable(String a, int b, double c) {
		this.a = a;
		this.b = b;
		this.c = c;
	}

	public String getA() {
		return a;
	}

	public int getB() {
		return b;
	}

	public double getC() {
		return c;
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeUTF(a);
		out.writeInt(b);
		out.writeDouble(c);
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		a = in.readUTF();
		b = in.readInt();
		c = in.readDouble();
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 53 * hash + Objects.hashCode(this.a);
		hash = 53 * hash + this.b;
		hash = 53 * hash + (int) (Double.doubleToLongBits(this.c) ^ (Double.doubleToLongBits(this.c) >>> 32));
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final MySerializable other = (MySerializable) obj;
		if (this.b != other.b) {
			return false;
		}
		if (Double.doubleToLongBits(this.c) != Double.doubleToLongBits(other.c)) {
			return false;
		}
		if (!Objects.equals(this.a, other.a)) {
			return false;
		}
		return true;
	}
}
