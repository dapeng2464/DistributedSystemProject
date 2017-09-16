package Staff;

import java.net.*; 
import java.io.*; 

//Thread class of UDPServer used by ClinicServer to accept getCount request from other server
public class UDPServer extends Thread{
	private ClinicServant ClinicObj; //reference the server who creates this UDP server thread
	public int UDPport;	//port to listen request
	
	//construction method
	public UDPServer (ClinicServant Obj,int port){
		this.ClinicObj = Obj;
		this.UDPport = port;
	}
	
	//Body of thread
	public void run(){ 
		DatagramSocket aSocket = null;    
		try{    
			//Create socket and buffer
			aSocket = new DatagramSocket(UDPport); 
			byte[] buffer = new byte[1000];
			//loop infinitely
			while(true){    
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);     
				aSocket.receive(request); //listen to request
				//analyze request
				String requestStr = new String(request.getData(),0,request.getLength());
				String result;
				String recordType = requestStr.substring(3,5);
				if (requestStr.substring(5).equals("getCount")){
					//right request
					result = ClinicObj.getStaffCount(recordType);
					ClinicObj.mylogfile.writeLog("["+ClinicObj.serverLocation+" Server]: Received getCount("+recordType+") request from "+(new String(request.getData(),0,request.getLength())).substring(0,3)+" server and replied \""+result+"\"");
				}
				else if (requestStr.substring(0,15).equals("getRecordCounts")){
					String[] inputs = requestStr.split(" ");
					result = ClinicObj.getRecordCounts(inputs[1],ClinicObj.serverLocation+"0000");
				}
				else if (requestStr.substring(0,10).equals("viewRecord")){
					String[] inputs = requestStr.split(" ");
					result = ClinicObj.viewRecord(inputs[1],ClinicObj.serverLocation+"0000");					
				}
				else if (requestStr.substring(0,13).equals("createDRecord")){
					String[] inputs = requestStr.split(" ");		
					result = ClinicObj.createDRecord(inputs[1], inputs[2], inputs[3], inputs[4], inputs[5], inputs[6],inputs[7],Integer.parseInt(inputs[8]),inputs[9]);
				}
				else if (requestStr.substring(0,13).equals("createNRecord")){
					String[] inputs = requestStr.split(" ");		
					result = ClinicObj.createNRecord(inputs[1], inputs[2], inputs[3], inputs[4],inputs[5],inputs[6],Integer.parseInt(inputs[7]),inputs[8]);
				}				
				else {
					//invalid request
					result = "Invalid request";
				}  
				//send result
				DatagramPacket reply = new DatagramPacket(result.getBytes(),result.length(), request.getAddress(), request.getPort());   					
				aSocket.send(reply);
			}    
		} catch (SocketException e){
			System.out.println("Socket: " + e.getMessage());   
		} catch (IOException e) {
			System.out.println("IO: " + e.getMessage());
		} finally {
			if(aSocket != null) aSocket.close();
		}
	} 
} 
