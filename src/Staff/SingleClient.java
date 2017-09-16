package Staff;

import ClinicModule.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Random;
import java.util.Scanner;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;

public class SingleClient{
	//The server hosts of three clinic server ,need to be configured according to real situation
	//private static String hostMTL = "localhost";	
	//private static String hostLVL = "localhost";
	//private static String hostDDO = "localhost";
	private static String serverName;
	private static String myServerHost; //to store the name server host
	private static int myServerPort; //to store the name server port
	
	
	public static boolean checkServer(String serverName){
		return ((serverName.equals("MTL"))||(serverName.equals("LVL"))||(serverName.equals("DDO")));
	}
	
	public static void showMenu(String grade)
	{
		if (grade.equals("0")){
			System.out.println("\n****Welcome to SingleClient****\n");
			System.out.println("Please select an option (1-3)");
			System.out.println("1. View a record.");
			System.out.println("2. Get records count");
			System.out.println("3. Exit");
		}	
		else if (grade.equals("02")){
			System.out.println("	Please select a recordType' (1-4)");
			System.out.println("	1. Doctor records");
			System.out.println("	2. Nurser records");
			System.out.println("	3. Both records");
			System.out.println("	4. Back to upper menu.");
		}	
	}	
	public static String getRecordCounts(String recordType) {
		// TODO Auto-generated method stub
		String mPure = "getRecordCounts "+recordType; 
		String result = sendToLeader(mPure);
		if (result.equals("-1")) 
			return ("ERROR: getRecordCounts failed, Socket error.");
		else return result;
	}
	public static String viewRecord(String recordID) {
		// TODO Auto-generated method stub
		String mPure = "viewRecord "+recordID; 
		String result = sendToLeader(mPure);
		if (result.equals("-1")) 
			return ("ERROR: getRecordCounts failed, Socket error.");
		else return result;
	}
	
	public static String sendToLeader(String mPure) {
		DatagramSocket aSocket = null; 
		int port;
		try { 
			aSocket = new DatagramSocket();    			
			InetAddress aHost = InetAddress.getByName(myServerHost);
			if (serverName.equals("MTL")) port= myServerPort;
			else if (serverName.equals("LVL")) port=myServerPort+1;
			else port=myServerPort+2;
					
			DatagramPacket request = new DatagramPacket(mPure.getBytes(),mPure.length(),aHost,port); 
				
			byte[] buffer = new byte[1000]; 
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length); 
			int received =0;
			int resend = 0;
			while (received == 0){
				if (resend<3)
					aSocket.send(request);
				else return "-1";
				aSocket.setSoTimeout(5000); //set time out interval for cases
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
	
	public static void main(String[] argv) {
		myServerHost = argv[0];	
		myServerPort = Integer.parseInt(argv[1]);
		
		//Begin the main part
		try {
			int userChoice=0;
			Scanner keyboard = new Scanner(System.in);
				
			showMenu("0");	//Display main menu					
				
			while(true)
			{
				Boolean valid = false;
				// Enforces a valid integer input.
				while(!valid)
				{
					try{
						userChoice=keyboard.nextInt();
						valid=true;
					}
					catch(Exception e)
					{
						System.out.println("Invalid Input, please enter an Integer");
						valid=false;
						keyboard.nextLine();
					}
				}
				// Manage user selection.
				switch(userChoice)
				{
				case 1: { //view exiting records
					System.out.println("RecordID(DR/UR+5 digits):");
					String recordID=keyboard.next();
					System.out.println("Server(MTL/LVL/DDO):");
					serverName=keyboard.next();
					//Begin to validate ManagerID
					if ((serverName.length()!=3)||(!checkServer(serverName))){  
						System.out.println("ServerName "+serverName+" is invalid");
					}
					else {
						System.out.println(viewRecord(recordID));
					}	
					showMenu("0");
					break;}
				
				case 2:{	//get record counts
					showMenu("02"); //show sub menu to choose record type of get count
									//1-only DR,2-only NR,3-Both
					Boolean valid2 = false;
					int userChoice2 = 0;
					// Enforces a valid integer input.
					while(!valid2)
					{
						try{
							userChoice2=keyboard.nextInt();
							valid2=true;
						}
						catch(Exception e)
						{
							System.out.println("Invalid Input, please enter an Integer");
							valid2=false;
							keyboard.nextLine();
						}
					}	
					String recordType="invalid";
					switch(userChoice2){
					case 1: recordType="DR";
							break;
					case 2: recordType="NR";
							break;
					case 3: recordType="BO";
							break;
					case 4: break;
					default:
						System.out.println("Invalid Input, please try again.");									
					}							
					if (!recordType.equals("invalid")) {
						serverName ="MTL";
						System.out.println(getRecordCounts(recordType));
					}
					showMenu("0");
					break;}
				case 3:	//exit
					System.out.println("Have a nice day!");
					keyboard.close();
					System.exit(0);
				default:
					System.out.println("Invalid Input, please try again.");
				}//end of switch	
			}//end of while
		}catch (Exception e){
			System.err.println("ERROR: "+e);
			e.printStackTrace(System.out);
		}
			
	}//end of main		
}//end of class
