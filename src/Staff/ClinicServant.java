package Staff;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ClinicServant{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String serverLocation;	//MTL,LVL,DDO
	private int recordIdDR;			//Records the last recordID assigned to Doctors,initialize in Constructor. 
	private int recordIdNR;			//Records the last recordID assigned to Nurses,initialize in Constructor.
	private int upLimit;			//The maximum ID for this server,initialize in Constructor.

	//The main data structure to store Doctor and Nurse's records
	private Map<String,ArrayList<staffRecord>> staffRecords = new HashMap<String,ArrayList<staffRecord>>(26,1);
	
	//To configure the servers' host ,need to be modified manually
	public static String hostMTL = "localhost";
	public static String hostLVL = "localhost";
	public static String hostDDO = "localhost";
	
	//Configure the UDP ports used by servers
	public static int udpPortMTL = 2230;
	public static int udpPortLVL = 2231;
	public static int udpPortDDO = 2232;
	
	//The UDP server(extends Thread) being used to accept 'getCount' operations from other servers
	private UDPServer myUDPServer;
	
	//The file object for logging
	public logFile mylogfile; 
	
	//Construction of ClinicServer
	public ClinicServant (String location){ //location is used to initialize serverLocation
		//indicate which server it is,MTL,LVL,DDO
		serverLocation = location;	
		
		//initialize log file object
		mylogfile = new logFile("server\\"+serverLocation+System.currentTimeMillis()+".log");
		
		//initialize ID range, portNum for exporting object and UDPserver
		if (serverLocation.equals("MTL")){	
			recordIdDR = recordIdNR = 0;
			upLimit = 33333;		//MTL Server uses 1-33333 ID resources for staffs
			myUDPServer = new UDPServer(this,udpPortMTL);
		}
		else if (serverLocation.equals("LVL")){
			recordIdDR = recordIdNR = 33333;
			upLimit = 66666;		//LVL Server uses 33334-66666 ID resources for staffs
			myUDPServer = new UDPServer(this,udpPortLVL);
		}
		else {
			recordIdDR = recordIdNR = 66666;  
			upLimit = 99999;		//DDO Server uses 66667-99999 ID resources for staffs
			myUDPServer = new UDPServer(this,udpPortDDO);
		}
		//start the UDP server
		myUDPServer.start();	
		
		//initialize main data structure with 26 keys('A'-'Z')
		//the last name with initial 'w' and 'W' will be treated as the same key and stored in the same array list
		for (char a = 'A';a <= 'Z'; a ++){		
			staffRecords.put(String.valueOf(a), new ArrayList<staffRecord>());
		}
		
		//log the create server event
		mylogfile.writeLog("["+serverLocation+" Server]: created, staff ID range is "+String.valueOf(recordIdDR+1)+" - "+upLimit+", UDP port is "+myUDPServer.UDPport);
	}
	
	//Implementation of createDRecord to create doctor's records
	public String createDRecord(String firstName, String lastName, String address, String phone, String specialization,
			String location,String ManagerID, int mode, String recordID){
		// TODO Auto-generated method stub
		try {
			int newID;
			if (mode == 0){
				newID = getRecordIdDR();  //try to get ID resource(synchronized)
				if (newID == -1){
					return mylogfile.writeLog("["+serverLocation+" Server: "+ManagerID+"] ERROR: createDRecord failed, No ID resource for a new Doctor.");
				}
			}
			else newID = Integer.parseInt(recordID.substring(2)); 
				
			//Create and initialize new Doctor record
			DoctorRecord newDR = new DoctorRecord();
			int result = newDR.init(firstName, lastName, address, phone, 
					specialization, location, String.format("%05d",newID));
			
			if (result == 0){   //0--initialize Successfully  
				//synchronized put new record into array list according to the key
				String tempKey = lastName.substring(0,1).toUpperCase();
				ArrayList<staffRecord> tempList = staffRecords.get(tempKey);
				synchronized(tempList){
					tempList.add(newDR);
				}	
				return mylogfile.writeLog("["+serverLocation+" Server: "+ManagerID+"] Succeed: createDRecord: "+newDR.getRecord());				
			}
			else {			//-1,-2 -- initialize failed
				newDR = null;
				if (result == -1){	//Data is not valid(location)
					return mylogfile.writeLog("["+serverLocation+" Server: "+ManagerID+"] ERROR: createDRecord failed, The location can only be MTL,LVL or DDO.");
				}
				else {
					return mylogfile.writeLog("["+serverLocation+" Server: "+ManagerID+"] ERROR: createDRecord failed, Internal Error.");
				}
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		return mylogfile.writeLog("["+serverLocation+" Server: "+ManagerID+"] ERROR: createDRecord failed, Other error.");
	}
	
	//Implementation of createNRecord to create nurse's records
	public String createNRecord(String firstName, String lastName, String designation, 
			String status, String statusDate,String ManagerID, int mode, String recordID){
		// TODO Auto-generated method stub
		try {
			int newID;
			if (mode == 0){
				newID = getRecordIdNR();	//try to get ID resource(synchronized)
				if (newID == -1){
					return mylogfile.writeLog("["+serverLocation+" Server: "+ManagerID+"] ERROR: createNRecord failed, No ID resource for a new Nurse.");
				}
			}
			else newID = Integer.parseInt(recordID.substring(2));
			//Create and initialize new nurse record
			NurseRecord newNR = new NurseRecord();
			int result = newNR.init(firstName, lastName, designation,
					status, statusDate, String.format("%05d",newID));
			if (result == 0){   //0--initialize Successfully 
				//synchronized put new record into array list according to the key
				String tempKey = lastName.substring(0,1).toUpperCase();
				ArrayList<staffRecord> tempList = staffRecords.get(tempKey);
				synchronized(tempList){
					tempList.add(newNR);
				}	
				return mylogfile.writeLog("["+serverLocation+" Server: "+ManagerID+"] Succeed: createNRecord: "+newNR.getRecord()+" "+String.valueOf(tempList.size()));				
			}
			else {				//-1,-2 -- initialize failed
				newNR = null;
				if (result == -1){	//Data is not valid(location)
					return mylogfile.writeLog("["+serverLocation+" Server: "+ManagerID+"] ERROR: createNRecord failed, Invalid designtion or stauts value.");
				}
				else {
					return mylogfile.writeLog("["+serverLocation+" Server: "+ManagerID+"] ERROR: createNRecord failed, Internal Error.");
				}
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		return mylogfile.writeLog("["+serverLocation+" Server: "+ManagerID+"] ERROR: createNRecord failed, Other error.");
	}

	//Implementation of getRecordCounts to get 
	public String getRecordCounts(String recordType,String ManagerID) {
		// TODO Auto-generated method stub
		String resultStr = "";
		UDPClient th1,th2;		//UDPclient threads used to getReordCount from the other servers
		
		//get record count from three servers, create thread of UDPClient as needed
		if (serverLocation.equals("MTL")){
			th1 = new UDPClient(serverLocation,"LVL",hostLVL,udpPortLVL,recordType);
			th2 = new UDPClient(serverLocation,"DDO",hostDDO,udpPortDDO,recordType);
			mylogfile.writeLog("["+serverLocation+" Server]: send getCount("+recordType+") request to LVL and DDO server.");
		}
		else if(serverLocation.equals("LVL")){
			th1 = new UDPClient(serverLocation,"MTL",hostMTL,udpPortMTL,recordType);
			th2 = new UDPClient(serverLocation,"DDO",hostDDO,udpPortDDO,recordType);
			mylogfile.writeLog("["+serverLocation+" Server]: send getCount("+recordType+") request to MTL and DDO server.");	
		}
		else{
			th1 = new UDPClient(serverLocation,"MTL",hostMTL,udpPortMTL,recordType);
			th2 = new UDPClient(serverLocation,"LVL",hostLVL,udpPortLVL,recordType);
			mylogfile.writeLog("["+serverLocation+" Server]: send getCount("+recordType+") request to MTL and LVL server.");				
		}
		
		//Start the two threads 
		th1.start();
		th2.start();
		
		try {
			//wait for the first thread end 
			th1.join();
			mylogfile.writeLog("["+serverLocation+" Server]: Got the reply of getCount("+recordType+") request from "+th1.remoteServer+" server. \""+th1.staffCount+"\"");			
			
			//wait for the second thread end
			th2.join();			
			mylogfile.writeLog("["+serverLocation+" Server]: Got the reply of getCount("+recordType+") request from "+th2.remoteServer+" server. \""+th2.staffCount+"\"");				
			
			//create return message
			if (serverLocation.equals("MTL"))
				resultStr =  "["+serverLocation+" Server: "+ManagerID+"] getRecordCounts("+recordType+") "+String.valueOf(getStaffCount(recordType))+" "+th1.staffCount+" "+th2.staffCount;
			else if (serverLocation.equals("LVL"))
				resultStr =  "["+serverLocation+" Server: "+ManagerID+"] getRecordCounts("+recordType+") "+th1.staffCount+" "+String.valueOf(getStaffCount(recordType))+" "+th2.staffCount;
			else 	
				resultStr =  "["+serverLocation+" Server: "+ManagerID+"] getRecordCounts("+recordType+") "+th1.staffCount+" "+th2.staffCount+" "+String.valueOf(getStaffCount(recordType));
			//release thread object
			th1 = null;
			th2 = null;
			
			return mylogfile.writeLog(resultStr);
		}catch (InterruptedException e)	{
			e.printStackTrace();		
			return mylogfile.writeLog("["+serverLocation+" Server: "+ManagerID+"] ERROR: getRecordCounts failed, Internal error.");		
		}
	}
	//implementation of editRecord to modify staff records
	public String editRecord(String ID,String fieldName,String newValue,String ManagerID){
		// TODO Auto-generated method stub
		
		//navigate in hash map for 26 keys
		for (char a = 'A'; a <='Z'; a ++){
			//get the array list corresponding to the key
			ArrayList<staffRecord> tempList = staffRecords.get(String.valueOf(a)); 
			synchronized(tempList) {			
			//lock the code block for further operation within the array list
				//navigate in array list to find the record
				Iterator<staffRecord> itr= tempList.iterator();
				while (itr.hasNext()){
					staffRecord tempStaff = itr.next();
				
					if (tempStaff.getID().equals(ID)){
					//find the record
						
						int result;
						
						//For Doctor record
						if (ID.substring(0,2).equals("DR")) {  
							result = ((DoctorRecord)tempStaff).editRecord(fieldName, newValue);
							if (result==0) {
								return mylogfile.writeLog("["+serverLocation+" Server: "+ManagerID+"] Succeed: Edit "+ID+"'s "+fieldName+" to "+newValue+": "+((DoctorRecord)tempStaff).getRecord());
							}
							else if (result == -1){
								return mylogfile.writeLog("["+serverLocation+" Server: "+ManagerID+"] Error: editRecord failed, Fieldname '"+fieldName+"' invalid or can't be edited.");
							}
							else if (result == -2){
								return mylogfile.writeLog("["+serverLocation+" Server: "+ManagerID+"] Error: editRecord failed, The location can only be MTL,LVL or DDO.");								
							}
							else {
								return mylogfile.writeLog("["+serverLocation+" Server: "+ManagerID+"] Error: editRecord failed, Internal error.");																
							}								
						}
						//For nurse record
						else {								
							result = ((NurseRecord)tempStaff).editRecord(fieldName, newValue);							
							if (result==0) {
								return mylogfile.writeLog("["+serverLocation+" Server: "+ManagerID+"] Succeed: Edit "+ID+"'s "+fieldName+" to "+newValue+": "+((NurseRecord)tempStaff).getRecord());
							}
							else if (result == -1){
								return mylogfile.writeLog("["+serverLocation+" Server: "+ManagerID+"] Error: editRecord failed, Fieldname '"+fieldName+"' invalid or can't be edited.");
							}
							else if (result == -2){
								return mylogfile.writeLog("["+serverLocation+" Server: "+ManagerID+"] Error: editRecord failed, Invalid '"+fieldName+"' value.");								
							}
							else {
								return mylogfile.writeLog("["+serverLocation+" Server: "+ManagerID+"] Error: editRecord failed, Internal error.");																
							}								
						}
					}//end of find the record
				}//end of one array list 
			}//end of lock array list  
		}//end of navigation throughout all hash map
		
		//if there is no this record
		return mylogfile.writeLog("["+serverLocation+" Server: "+ManagerID+"] ERROR: editRecord failed, Can't find record with recordID="+String.valueOf(ID));
	}
	
	//implementation of editRecord to modify staff records
	public String viewRecord(String ID,String ManagerID){
		// TODO Auto-generated method stub
		
		//navigate in hash map for 26 keys
		for (char a = 'A'; a <='Z'; a ++){
			//get the array list corresponding to the key
			ArrayList<staffRecord> tempList = staffRecords.get(String.valueOf(a)); 
			synchronized(tempList) {			
			//lock the code block for further operation within the array list
				//navigate in array list to find the record
				Iterator<staffRecord> itr= tempList.iterator();
				while (itr.hasNext()){
					staffRecord tempStaff = itr.next();
				
					if (tempStaff.getID().equals(ID)){
					//find the record		
						//For Doctor record
						if (ID.substring(0,2).equals("DR")) {  
							return "["+serverLocation+" Server: "+ManagerID+"] Succeed: " +((DoctorRecord)tempStaff).getRecord();
						}
						//For nurse record
						else {								
							return "["+serverLocation+" Server: "+ManagerID+"] Succeed: " +((NurseRecord)tempStaff).getRecord();								
						}
					}//end of find the record
				}//end of one array list 
			}//end of lock array list  
		}//end of navigation throughout all hash map
		
		//if there is no this record
		return "["+serverLocation+" Server: "+ManagerID+"] ERROR: viewRecord failed, Can't find record with recordID="+String.valueOf(ID);
	}	
	
	//implementation of login with managerID
	//only record the online status, to avoid invalid operation (wrong server), 
	//and multi-login with same managerID 
	public int login(String ManagerID){
		return (0);
	}

	//implementation of login with managerID
	//only record the online status, to avoid multi-login with same managerID 	
	public int logout(String ManagerID){
		return (0);
	}
	
	//synchronized get the ID resource for new doctor record
	public synchronized int getRecordIdDR() throws Exception {
		if (recordIdDR < upLimit)
			return (++ recordIdDR);
		else return (-1);     //Id is out of range
	} 
	
	//synchronized get the ID resource for new nurse record
	public synchronized int getRecordIdNR() throws Exception {
		if (recordIdNR < upLimit)
			return (++ recordIdNR);
		else return (-1);    //Id is out of range
	} 	
	
	//The real method to synchronized get staff count
	//can be referenced by object itself or the UDP Server
	public String getStaffCount(String recordType){
		int result = 0;
		//navigate in hash map for 26 keys
		for (char a = 'A';a <= 'Z';a ++){
			
			//get the array list according to key
			ArrayList<staffRecord> tempList = staffRecords.get(String.valueOf(a)); 
			//lock the code block for further operation within the array list
			synchronized(tempList) {	
				//get both Doctor and nurse, simply get the size
				if (recordType.equals("BO")){
					result += tempList.size();
				}
				//get Doctor or nurse, need to navigate through all records
				else {
					//navigate in array list
					Iterator<staffRecord> itr= tempList.iterator(); 
					while (itr.hasNext()){
						staffRecord tempStaff = itr.next();
						//Compare the first two characters of recordID with the recordType
						if (tempStaff.recordID.substring(0,2).equals(recordType)){
							result++;		
						}
					}	
				}
			}//end of lock
		}//end of hash map
		return serverLocation+" "+String.valueOf(result);
	}
	public String transferRecord(String recordID, String remoteClinicServer, String ManagerID) {
		// TODO Auto-generated method stub
		//navigate in hash map for 26 keys
		if (remoteClinicServer.equals(serverLocation))
			return ("["+serverLocation+" Server: "+ManagerID+"] ERROR: transferRecord failed, Can't transfer to the same server, may cause deadlock");
		for (char a = 'A'; a <='Z'; a ++){
			//get the array list corresponding to the key
			ArrayList<staffRecord> tempList = staffRecords.get(String.valueOf(a)); 
			synchronized(tempList) {			
			//lock the code block for further operation within the array list
				//navigate in array list to find the record
				Iterator<staffRecord> itr= tempList.iterator();
				while (itr.hasNext()){
					staffRecord tempStaff = itr.next();
					if (tempStaff.getID().equals(recordID)){
					//find the record
						String result = "";
						try { 					
							DatagramSocket aSocket = null;  
							try { 
								aSocket = new DatagramSocket();  
								String m;
								if (recordID.substring(0,2).equals("DR"))
									m ="createDRecord "+((DoctorRecord)tempStaff).firstName+" "+((DoctorRecord)tempStaff).lastName+" "
										+((DoctorRecord)tempStaff).Address+" "+((DoctorRecord)tempStaff).phoneNum+" "+((DoctorRecord)tempStaff).specialization+" "
										+((DoctorRecord)tempStaff).location+" "+ManagerID+" 1 "+recordID;
								else 	
									m = "createNRecord "+((NurseRecord)tempStaff).firstName+" "+((NurseRecord)tempStaff).lastName+" "+((NurseRecord)tempStaff).designation+
									" "+((NurseRecord)tempStaff).status+" "+ ((NurseRecord)tempStaff).statusDate+" "+ManagerID+" 1 "+recordID;
									
								InetAddress aHost;
								int UDPport;
								if (remoteClinicServer.equals("MTL")){
									aHost = InetAddress.getByName(hostMTL); 
									UDPport=udpPortMTL;
								}	
								else if (remoteClinicServer.equals("LVL")){
									aHost = InetAddress.getByName(hostLVL); 
									UDPport=udpPortLVL;
								}
								else {
									aHost = InetAddress.getByName(hostDDO); 
									UDPport=udpPortDDO;
								}
								DatagramPacket request = new DatagramPacket(m.getBytes(),m.length(),aHost,UDPport); 
								aSocket.send(request);
								byte[] buffer = new byte[1000]; 
								DatagramPacket reply = new DatagramPacket(buffer, buffer.length); 
								aSocket.setSoTimeout(5000); //set time out interval for cases
								try {
									aSocket.receive(reply); 
									result = mylogfile.writeLog(new String(reply.getData(),0,reply.getLength()));
								}catch (SocketTimeoutException e) {
					                // timeout exception.
									result = mylogfile.writeLog("time out");
					            }
							} catch (SocketException e){
								System.out.println("Socket: " + e.getMessage()); 
							} catch (IOException e){
								System.out.println("IO: " + e.getMessage());
							} finally {
								if(aSocket != null) aSocket.close();
							} 							
						}catch (Exception e){
							System.err.println("ERROR: "+e);
							e.printStackTrace(System.out);
						}
						if (result.indexOf("Succeed")==-1){
							return mylogfile.writeLog("["+serverLocation+" Server: "+ManagerID+"] ERROR: transferRecord failed, Can't create record on remote server "+remoteClinicServer);
						}
						else{
							tempList.remove(tempStaff);
							return mylogfile.writeLog("["+serverLocation+" Server: "+ManagerID+"] Succeed: transferRecord "+recordID+" to "+remoteClinicServer);							
						}
					}//end of find the record
				}//end of one array list 
			}//end of lock array list  
		}//end of navigation throughout all hash map
		
		//if there is no this record
		return mylogfile.writeLog("["+serverLocation+" Server: "+ManagerID+"] ERROR: transferRecord failed, Can't find record with recordID="+String.valueOf(recordID));
	}
	
}
