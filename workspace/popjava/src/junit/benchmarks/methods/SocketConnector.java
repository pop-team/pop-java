package junit.benchmarks.methods;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class SocketConnector {
	
	private final Socket socket;
	private final OutputStream out;
	private final InputStream in;
	
	public SocketConnector() throws UnknownHostException, IOException{
		socket = new Socket("localhost", SocketServer.PORT);
		out = socket.getOutputStream();
		in = socket.getInputStream();
	}
	
	public void noParamNoReturn() throws IOException{
		out.write(1);
	}
	
	public int noParamSimple() throws IOException{
		out.write(2);
		return in.read();
	}
	
	public String [] noParamComplex() throws IOException{
		out.write(3);
		int strings = in.read();
		String [] temp = new String[strings];
		
		for(int i = 0; i < temp.length; i++){
			int l = in.read();
			
			byte [] t = new byte[l];
			
			while(l > 0){
				l -= in.read(t, l, t.length - l);
			}
			
			temp[i] = new String(t);
		}
		
		return temp;
	}
	
	public void close() throws IOException{
		socket.close();
	}

}
