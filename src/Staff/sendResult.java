package Staff;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class sendResult extends Thread{
	DatagramPacket reply=null;
	String requestID;
	int priorityID;
	public sendResult (String request,DatagramPacket re,int id){
		this.reply = re;
		this.requestID = request;
		this.priorityID = id;
	}
	
	public void run(){
		DatagramSocket rSocket = null;
		MulticastSocket mSocket =null;
		InetAddress group = null;
		int retrytimes =0;
		try {
			rSocket = new DatagramSocket();
			mSocket = new MulticastSocket();
			group = InetAddress.getByName("239.1.2.7");
			mSocket.joinGroup(group);	
			byte[] buffer = new byte[1000];
			DatagramPacket ack = new DatagramPacket(buffer, buffer.length);
			while(retrytimes<50){
				try{
					mSocket.setSoTimeout(2000);
					mSocket.receive(ack);			
					
					String requestStr = new String(ack.getData(),0,ack.getLength());
					System.out.println(requestStr);
					String[] inputs = requestStr.split(" ");
					if (inputs[4].equals(requestID)&&(Integer.parseInt(inputs[2])!=priorityID)){
						rSocket.send(reply);
						break;
					}
					retrytimes++;
				}catch (SocketTimeoutException e) {
					retrytimes++;
				}
			}//end of while
		} catch (SocketException e){
			System.out.println("Socket: " + e.getMessage());   
		} catch (IOException e) {
			System.out.println("IO: " + e.getMessage());
		} finally {
			if(rSocket != null) rSocket.close();
			if(mSocket != null) mSocket.close();
		}		
	}

}
