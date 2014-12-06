package bcby.rutgers.parkinghelper;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class SocketClient{
	int portNum;
	InetAddress serverAddr;
	
	public SocketClient(String _hostIp,int port){
		portNum = port;
		
		try {
			serverAddr = InetAddress.getByName(_hostIp);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public String communicate(String command) throws IOException{
		String msg = new String();
		try{
			
			Socket c_sock = new Socket(serverAddr,portNum);
			BufferedReader in = new BufferedReader(new InputStreamReader(c_sock.getInputStream()));
			PrintWriter out = new PrintWriter(new OutputStreamWriter(c_sock.getOutputStream()),true);
			BufferedReader userEntry = new BufferedReader(new InputStreamReader(System.in));
		
			out.println(command);

			String tmp = in.readLine();
			while(tmp!=null){
				msg += tmp;
				msg += "\n";
				tmp = in.readLine();
			}
			c_sock.close();
		}catch(IOException ex){ex.printStackTrace();}
		return msg;
	}
	public Bitmap getBitMap(String command) throws IOException{
		String msg = new String();
		Bitmap img = null;
		try{
			
			Socket c_sock = new Socket(serverAddr,portNum);
			BufferedReader in = new BufferedReader(new InputStreamReader(c_sock.getInputStream()));
			PrintWriter out = new PrintWriter(new OutputStreamWriter(c_sock.getOutputStream()),true);
			BufferedReader userEntry = new BufferedReader(new InputStreamReader(System.in));
		
			out.println(command);
			
			InputStream ins = c_sock.getInputStream();
			DataInputStream dis=new DataInputStream(ins);
			
			int len = dis.readInt();
		    byte[] data = new byte[len];
		    if (len > 0) {
		        dis.readFully(data);
		    }
		    
		    img = BitmapFactory.decodeByteArray(data, 0, data.length);
			
			c_sock.close();
		}catch(IOException ex){ex.printStackTrace();}
		return img;
	}
}