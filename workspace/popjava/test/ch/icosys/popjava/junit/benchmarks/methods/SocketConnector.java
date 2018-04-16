package ch.icosys.popjava.junit.benchmarks.methods;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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
		out = new BufferedOutputStream(socket.getOutputStream());
		in = new BufferedInputStream(socket.getInputStream());
	}
	
	public void noParamNoReturn() throws IOException{
		out.write(1);
		out.flush();
	}
	
	public int noParamSimple() throws IOException{
		out.write(2);
		out.flush();
		return in.read();
	}
	
	public String [] noParamComplex() throws IOException{
		out.write(3);
		out.flush();
		
		int strings = in.read();
		String [] temp = new String[strings];
		
		for(int i = 0; i < temp.length; i++){
			int l = in.read();
			byte [] t = new byte[l];
			
			while(l > 0){
				int dataRead = in.read(t, t.length - l, l);
				l -= dataRead;
			}
			
			temp[i] = new String(t);
		}
		
		return temp;
	}
	
	public void close() throws IOException{
		socket.close();
	}

}
