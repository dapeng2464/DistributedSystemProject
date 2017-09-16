package Staff;
import java.net.*; 
import java.io.*;

//Thread class of UDPClient used by ClinicServer to send getCount request to other server
public class HeartbeatServer extends Thread{
	//Body of thread
	public Leader leader;
	public int portNum;
	public static String systemView="";
	public HeartbeatServer(int port,Leader obj,Slave[] sl,node m){
		portNum =port;
		leader =obj;
	}
	
	
	public void run(){ 
		DatagramSocket aSocket = null;
		try { 
			String m = "1 I know you are there"; 
			aSocket = new DatagramSocket(portNum); 
			byte[] buffer = new byte[200]; 
			DatagramPacket receive = new DatagramPacket(buffer, buffer.length); 
			int	interval = 3000;
			int failtimes = 0;
			while (true) {
				try{
					if (leader.state) aSocket.setSoTimeout(interval); 
					aSocket.receive(receive); 
					if (!leader.state) {
						leader.state=true;
						leader.ip = receive.getAddress().getHostAddress();
						System.out.println("Leader Found on "+leader.ip+".");									
					}
					failtimes=0;
					interval = 3000;
					
					String receiveStr = new String(receive.getData(),0,receive.getLength());
					int messageType = Integer.parseInt(receiveStr.substring(0, 1));
					DatagramPacket reply=null;
					switch(messageType) {	
					case 1:
						reply = new DatagramPacket(m.getBytes(),m.length(),receive.getAddress(),receive.getPort()); 
						break;
					case 2:
						//System.out.println("View of the whole system received! "+receiveStr);
						systemView = receiveStr; 
						String m2="2 view received";
						reply = new DatagramPacket(m2.getBytes(),m2.length(),receive.getAddress(),receive.getPort()); 
						break;
					}										
					aSocket.send(reply);
				}catch (SocketTimeoutException e) {
					// timeout exception.
					//leader is dead
					failtimes++;
					if (failtimes>=3) {
						System.out.println("leader is dead.");
						leader.state = false;
						break;
					}
					else {
						System.out.println("leader lost a beat.");
						interval = 500;
					}
				}
			}	
		} catch (SocketException e){
			System.out.println("Socket: " + e.getMessage()); 
		} catch (IOException e){
			System.out.println("IO: " + e.getMessage());
		} finally {
			if(aSocket != null) aSocket.close();
		} 
	} 
}
