package Staff;
import java.net.*; 
import java.io.*;

//Thread class of UDPClient used by ClinicServer to send getCount request to other server
public class HeartbeatClient extends Thread{
	private Slave slave;
	//Construction method
	public HeartbeatClient (Slave obj){
		this.slave = obj;
	}	
	
	//Body of thread
	public void run(){ 
		DatagramSocket aSocket = null;
		try { 
			String m = "1 I'm here"; 
			aSocket = new DatagramSocket(); 
			InetAddress aHost = InetAddress.getByName(slave.ip);
			DatagramPacket request = new DatagramPacket(m.getBytes(),m.length(),aHost,slave.port); 
			byte[] buffer = new byte[20]; 
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length); 
			int interval = 1000;
			int failtimes = 0;
			while (true) {
				if (!slave.viewReceive){
					DatagramPacket view = new DatagramPacket(slave.view.getBytes(),slave.view.length(),aHost,slave.port); 
					aSocket.send(view);
					aSocket.setSoTimeout(10000);
				}
				else{ 
					aSocket.send(request);
					aSocket.setSoTimeout(interval); //set time out interval for cases
				}	
				try {
					aSocket.receive(reply); 
					interval=1000;
					failtimes=0;
					if (slave.state.equals("unknown")){
						System.out.println("Found Slave["+slave.slaveid+"] on "+slave.ip+ ":"+slave.port);
						slave.state="OK";
					}
					String replyStr = new String(reply.getData(),0,reply.getLength());
					int messageType = Integer.parseInt(replyStr.substring(0, 1));
					switch(messageType) {	
					case 2:
						slave.viewReceive=true;
						break;
					}

					try {
						sleep(2000);
					}catch (Exception e){}
				}catch (SocketTimeoutException e) {
					// timeout exception.
					failtimes++;
					if (failtimes>=3){
						if (slave.state.equals("unknown"))
							System.out.println("Slave["+ slave.slaveid +"] on "+slave.ip+":"+slave.port+" is unreachable.");
						else 
							System.out.println("Lost connection with Slave["+ slave.slaveid +"] on "+slave.ip+":"+slave.port);						
						slave.state = "failed";
						break;
					}
					else{
						//System.out.println("Slave["+ slave.slaveid +"] on "+slave.ip+":"+slave.port+" lost a beat");
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
