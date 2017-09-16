package Staff;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;

import ClinicModule.*;

public class FEServant extends _ClinicImplBase{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public String leaderHost;
	public int leaderPort;
	
	//The file object for logging
	public logFile mylogfile; 
	
	//Construction of ClinicServer
	public FEServant (String leaderIP,int leaderUDP){ //location is used to initialize serverLocation
		//indicate which server it is,MTL,LVL,DDO
		leaderHost = leaderIP;
		leaderPort = leaderUDP;
		//initialize log file object
		mylogfile = new logFile("FE\\FE"+System.currentTimeMillis()+".log");
		
		//log the create server event
		System.out.println(mylogfile.writeLog("[FE is ready]"));
	}
	
	//Implementation of createDRecord to create doctor's records
	public String createDRecord(String firstName, String lastName, String address, String phone, String specialization,
			String location,String ManagerID, int mode, String recordID){
		String mPure = ManagerID.substring(0,3)+" createDRecord "+firstName+" "+lastName+" "+address+" "+phone+" "+specialization+" "+location+" "+ManagerID+" 0 z";
		String result = sendToLeader(mPure);
		if (result.equals("-1")) 
			return mylogfile.writeLog("[FE: "+ManagerID+"] ERROR: createDRecord failed, Socket error.");
		else return result;
	}
	
	//Implementation of createNRecord to create nurse's records
	public String createNRecord(String firstName, String lastName, String designation, 
			String status, String statusDate,String ManagerID, int mode, String recordID){
		String mPure = ManagerID.substring(0,3)+" createNRecord "+firstName+" "+lastName+" "+designation+" "+status+" "+statusDate+" "+ManagerID+" 0 z"; 
		String result = sendToLeader(mPure);
		if (result.equals("-1")) 
			return mylogfile.writeLog("[FE: "+ManagerID+"] ERROR: createNRecord failed, Socket error.");
		else return result;
	}

	//Implementation of getRecordCounts to get 
	public String getRecordCounts(String recordType,String ManagerID) {
		// TODO Auto-generated method stub
		String mPure = ManagerID.substring(0,3)+" getRecordCounts "+recordType+" "+ManagerID; 
		String result = sendToLeader(mPure);
		if (result.equals("-1")) 
			return mylogfile.writeLog("[FE: "+ManagerID+"] ERROR: getRecordCounts failed, Socket error.");
		else return result;
	}

	//implementation of editRecord to modify staff records
	public String editRecord(String ID,String fieldName,String newValue,String ManagerID){
		String mPure = ManagerID.substring(0,3)+" editRecord "+ID+" "+fieldName+" "+newValue+" "+ManagerID; 
		String result = sendToLeader(mPure);
		if (result.equals("-1")) 
			return mylogfile.writeLog("[FE: "+ManagerID+"] ERROR: editRecord failed, Socket error.");
		else return result;
	}
	
	//implementation of login with managerID
	//only record the online status, to avoid invalid operation (wrong server), 
	//and multi-login with same managerID 
	public int login(String ManagerID){
		return 0;
	}

	//implementation of login with managerID
	//only record the online status, to avoid multi-login with same managerID 	
	public int logout(String ManagerID){
		return (0);
	}
	
	public String transferRecord(String recordID, String remoteClinicServer, String ManagerID) {
		String mPure = ManagerID.substring(0,3)+" transferRecord "+recordID+" "+remoteClinicServer+" "+ManagerID; 
		String result = sendToLeader(mPure);
		if (result.equals("-1")) 
			return mylogfile.writeLog("[FE: "+ManagerID+"] ERROR: transferRecord failed, Socket error.");
		else return result;
	}
	
	public String sendToLeader(String mPure) {
		DatagramSocket aSocket = null;  
		try { 
			aSocket = new DatagramSocket();    
			String requestID = String.valueOf(Thread.currentThread().getId())+System.currentTimeMillis();
			String m = requestID+" 0 "+mPure;
			InetAddress aHost = InetAddress.getByName(leaderHost); 
			DatagramPacket request = new DatagramPacket(m.getBytes(),m.length(),aHost,leaderPort); 
			m = requestID+" 1 "+mPure;
			DatagramPacket request2 = new DatagramPacket(m.getBytes(),m.length(),aHost,leaderPort); 
				
			byte[] buffer = new byte[1000]; 
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length); 
			int received =0;
			int resend = 0;
			while (received == 0){
				if (resend==0)
					aSocket.send(request);
				else if (resend<10) aSocket.send(request2);
				else return "-1";
				aSocket.setSoTimeout(10000); //set time out interval for cases
				try {
					aSocket.receive(reply); 
					received = 1;
				}catch (SocketTimeoutException e) {
					// timeout exception.
					System.out.println("Time out and resend");
					resend ++;
				}
			} //end of while	
			return (new String(reply.getData(),0,reply.getLength()));
		} catch (SocketException e){
			System.out.println("Socket: " + e.getMessage()); 
		} catch (IOException e){
			System.out.println("IO: " + e.getMessage());
		} finally {
			if(aSocket != null) aSocket.close();
		}
		return "-1";
	}

	
	public static void main(String argv[]){
		try {
			ORB orb = ORB.init(new String[]{"-ORBInitialPort",argv[1],"-ORBInitialHost",argv[0]},null);
			FEServant FE = new FEServant(argv[2],Integer.parseInt(argv[3]));

			orb.connect(FE);
			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			NamingContext ncRef = NamingContextHelper.narrow(objRef);
			
			NameComponent nc = new NameComponent("FE","");
			NameComponent path[] = {nc};
			
			ncRef.rebind(path, FE);
			System.out.println("FE are up and running, registered to name service on "+argv[0]+":"+argv[1]+",leader server is on "+argv[2]+": "+argv[3]+"!");						
			java.lang.Object sync = new java.lang.Object();
			synchronized(sync) {
				sync.wait();
			}
		}catch (Exception e){
			System.err.println("ERROR:"+e);
			e.printStackTrace(System.out);
		}
	}
}
