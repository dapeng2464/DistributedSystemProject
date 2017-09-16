package Staff;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;


//The implementation of ClinicInterface
public class ClinicServer extends Thread {
	public static int runMode=0;//0-leader,1-slave
	public static int queueLength = 1000;
	public static requestMessage [] deliveryQueue;
	public static requestMessage [] holdbackQueue;	
	public static int deliveryHead = 0,deliveryEnd = 0;
	public static int holdbackHead = 0,holdbackEnd = 0;	
	public static ClinicServant MTL;
	public static ClinicServant LVL;
	public static ClinicServant DDO;	
	public static int slaveNum;
	public static Slave [] slaves=null; 
	public static Leader leader=null;
	public static node[] systemView=null;
	public static node me=null;

	public void run() {
		requestMessage currenRequest;
		DatagramSocket rSocket = null;
		MulticastSocket mSocket =null;
		InetAddress group = null;
		try {
			if (runMode==0){//leader		
				rSocket = new DatagramSocket();
				mSocket = new MulticastSocket();
				group = InetAddress.getByName("239.1.2.7");
				mSocket.joinGroup(group);
				System.out.println("Multicast sub-system initialized.");	
				System.out.println("Dispatcher starts up in leader mode!");
			}	
			else {//Slave
				System.out.println("Dispatcher starts up in slave mode!");
			}
			while(true){
				if (deliveryHead!=deliveryEnd){ //there are requests in the queue
				//System.out.println("new request");
					currenRequest = deliveryQueue[deliveryHead];

					String requestStr = currenRequest.mainRequest;
					String [] inputs = requestStr.split(" ");
					String result;
					ClinicServant ClinicObj;
					if (inputs[0].equals("MTL"))
						ClinicObj = MTL;
					else if (inputs[0].equals("LVL")) 
						ClinicObj = LVL;
					else ClinicObj = DDO; 
					
					if (inputs[1].equals("createDRecord")){
						result = ClinicObj.createDRecord(inputs[2], inputs[3], inputs[4], inputs[5], inputs[6], inputs[7],inputs[8],Integer.parseInt(inputs[9]),inputs[10]);
					}
					else if (inputs[1].equals("createNRecord")){
						result = ClinicObj.createNRecord(inputs[2], inputs[3], inputs[4], inputs[5],inputs[6],inputs[7],Integer.parseInt(inputs[8]),inputs[9]);
					}		
					else if (inputs[1].equals("getRecordCounts")){
						result = ClinicObj.getRecordCounts(inputs[2], inputs[3]);
					}	
					else if (inputs[1].equals("editRecord")){
						result = ClinicObj.editRecord(inputs[2], inputs[3],inputs[4],inputs[5]);
					}	
					else if (inputs[1].equals("transferRecord")){
						result = ClinicObj.transferRecord(inputs[2], inputs[3],inputs[4]);
					}					
					else {
						//invalid request
						result = "Invalid request";
					}  

					//send result
					currenRequest.result=result;
					if (runMode==0){
						DatagramPacket reply = new DatagramPacket(result.getBytes(),result.length(), currenRequest.hostFE, currenRequest.portFE);   					
						rSocket.send(reply);
						//sendResult se = new sendResult(currenRequest.requestId,reply,leader.priority);
						//se.run();
						//multicast to slaves
						String originalMsg = currenRequest.sequenceNum+" "+me.priority+" "+(me.seq++)+" "+currenRequest.requestId+" "+currenRequest.hostFE.getHostAddress()+" "+currenRequest.portFE+" "+currenRequest.mainRequest;
						DatagramPacket multicast = new DatagramPacket(originalMsg.getBytes(),originalMsg.length(),group,4446);   					
						mSocket.send(multicast);
						System.out.println("send msg");
					}	
					synchronized(deliveryQueue){
						//System.out.println(String.valueOf(deliveryHead)+String.valueOf(deliveryEnd));
						int tempHead = deliveryHead;
						tempHead++;
						if (tempHead == queueLength) 
							tempHead=0;
						deliveryHead = tempHead;
					}
				}
				try{
					sleep(1);
				}catch(Exception e)
				{}
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
	
	public static void main(String argv[]){
		//argvs: 0 - runmode; 1- udpport for FE; The rest: slaves' IP portnum
		runMode = Integer.parseInt(argv[0]);
		
		//negotiate process
		leader = new Leader();
		if (runMode==0) {//leader
			leader.ip= "localhost";
			leader.priority=9;
			leader.seq=0;
			leader.state=true;
			//try to find all slaves
			slaveNum = (argv.length-2)/2; 
			slaves = new Slave[slaveNum];
			systemView = new node[slaveNum+1];
			systemView[0]=leader;
			me=leader;
			for (int i=0;i<slaveNum;i++){
				slaves[i] = new Slave(i,argv[2+i*2],Integer.parseInt(argv[3+i*2]));
				systemView[i+1]=slaves[i];
			}	
			if (slaveNum>0) {
				System.out.println("Finding slaves ...");	
				for (int i=0;i<slaveNum;i++){
					HeartbeatClient t = new HeartbeatClient(slaves[i]);
					t.start();
					while (slaves[i].state.equals("unknown")){
						try{
							sleep(500);
						}catch (Exception e){}	
					}
				}
				//send systemView to each 
				String viewStr ="2 "+String.valueOf(leader.priority)+" "+String.valueOf(slaveNum)+" ";
				for (int i=0;i<slaveNum;i++){
					viewStr+=String.valueOf(slaves[i].slaveid)+" "+slaves[i].ip+" "+String.valueOf(slaves[i].port)+" "+slaves[i].state+" ";
				}
				for (int i=0;i<slaveNum;i++){
					if (slaves[i].state.equals("OK")){
						slaves[i].view=viewStr+String.valueOf(i);
						slaves[i].viewReceive=false;
						while (!slaves[i].viewReceive){
							try{
								sleep(500);
							}catch (Exception e){}	
						}						
					}	
				}
				System.out.println("View of the whole system synchronized!");			
			}
		}	
		else { //slave mode
			System.out.println("Waiting for leader on "+argv[1]+" ...");	
			
			HeartbeatServer t =new HeartbeatServer(Integer.parseInt(argv[1]),leader,slaves,me);
			t.start();
			while ((!leader.state)||(HeartbeatServer.systemView.equals(""))){
				try {
					sleep(500);
				}catch (Exception e){}
			}
			String[] inputs = HeartbeatServer.systemView.split(" ");
			leader.priority = Integer.parseInt(inputs[1]);
			int slaveNum = Integer.parseInt(inputs[2]);
			slaves = new Slave[slaveNum];
			for (int i=0;i<slaveNum;i++){
				slaves[i]=new Slave(Integer.parseInt(inputs[3+i*4]), inputs[4+i*4], Integer.parseInt(inputs[5+i*4]));
				slaves[i].state = inputs[6+i*4];
			}
			me = slaves[Integer.parseInt(inputs[3+slaveNum*4])];

			systemView = new node[slaveNum+1];
			systemView[0]=leader;
			for (int i=0;i<slaveNum;i++)
				systemView[i+1]=slaves[i];
			if (argv.length>2){
				ClinicServant.udpPortMTL = Integer.parseInt(argv[2]);
				ClinicServant.udpPortLVL = ClinicServant.udpPortMTL+1;
				ClinicServant.udpPortDDO = ClinicServant.udpPortLVL+1;
			}
		}
		
		//start clinic servers
		MTL = new ClinicServant("MTL");
		LVL = new ClinicServant("LVL");
		DDO = new ClinicServant("DDO");				
		System.out.println("Three servers (MTL:"+ClinicServant.udpPortMTL+",LVL:"+ClinicServant.udpPortLVL+",DDO:"+ClinicServant.udpPortDDO+") are up and running.");						

		
		//start dispatcher
		ClinicServer a = new ClinicServer();
		a.start();
		
		//initialize delivery queue and hold-back queue
		deliveryQueue = new requestMessage [queueLength];
		for (int i=0;i<queueLength;i++)
			deliveryQueue[i]= new requestMessage();
		int globalSequence =0; //initialize sequence number
		System.out.println("Delivery queue initialized.");	
		if (runMode==1){
			holdbackQueue = new requestMessage [queueLength/2];
			for (int i=0;i<queueLength/2;i++)
				holdbackQueue[i]= new requestMessage();
			System.out.println("Hold-back queue initialized.");			
		}	
		
		DatagramSocket aSocket = null;
		MulticastSocket mSocket =null;
		try{    
			//Create socket and buffer
			byte[] buffer = new byte[1000];
			if (runMode==0){
				aSocket = new DatagramSocket(Integer.parseInt(argv[1])); 
				System.out.println("Run as leader, priority is "+me.priority+", Listening request from FE on "+argv[1]);									
			}
			else {
				mSocket = new MulticastSocket(4446);
				InetAddress group = InetAddress.getByName("239.1.2.7");	
				mSocket.joinGroup(group);
				System.out.println("Multicast sub-system initialized.");				
				System.out.println("Run as Slave, priority is "+me.priority+", listening request from Leader through multicast group "+group.getHostAddress());								
			}	
			//loop infinitely
			int tolerent=4;
			int missCount=0;
			while(true){     
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);     
				if (runMode==0) {
					aSocket.receive(request); //listen to request
					//analyze request
					String requestStr = new String(request.getData(),0,request.getLength());
					String[] inputs = requestStr.split(" ");
					if (inputs[1].equals("0")){ //new message
						//put into delivery queue
						synchronized (deliveryQueue){
							int tempEnd = deliveryEnd+1;
							if (tempEnd==queueLength) tempEnd=0;
							if (tempEnd!=deliveryHead){
								//queue is not full
								deliveryQueue[deliveryEnd].requestId=inputs[0];
								deliveryQueue[deliveryEnd].mainRequest=requestStr.substring(inputs[0].length()+inputs[1].length()+2);
								deliveryQueue[deliveryEnd].sequenceNum=++globalSequence;
								deliveryQueue[deliveryEnd].hostFE=request.getAddress();
								deliveryQueue[deliveryEnd].portFE=request.getPort();
								deliveryEnd = tempEnd;
								//System.out.println(String.valueOf(deliveryHead)+String.valueOf(deliveryEnd));
							}
							else
								//queue is full, simply discard the request
								System.out.println("Queue is full, discard the request!");
						}//end of synchronize			.
					} 
					else {	//resend message
						Boolean found=false;
						for (int i=0;i<queueLength;i++){
							if (inputs[0].equals(deliveryQueue[i].requestId)){
								found=true;
								synchronized (deliveryQueue){
									if (inputs[0].equals(deliveryQueue[i].requestId)){
										if (((deliveryEnd>deliveryHead)&&(i>=deliveryHead)&&(i<deliveryEnd))||
										   ((deliveryEnd<deliveryHead)&&((i>=deliveryHead)||(i<deliveryEnd)))){
											//in valid queue,not handled yet
											//Discard it
											//System.out.println(String.valueOf(deliveryHead)+String.valueOf(deliveryEnd));
											System.out.println("Repeated request, discard!");
										}
										else {
											//already been handled,send the result again
											String result = deliveryQueue[i].result;
											aSocket.send(new DatagramPacket(result.getBytes(),result.length(), deliveryQueue[i].hostFE, deliveryQueue[i].portFE)); 
											//System.out.println(String.valueOf(deliveryHead)+String.valueOf(deliveryEnd));
											System.out.println("Handled request, resend result!");
										}
									}
									else {//been modified at the last second,the result lost
										String result = "Succeed: Succeed with the lost result!";
										aSocket.send(new DatagramPacket(result.getBytes(),result.length(), deliveryQueue[i].hostFE, deliveryQueue[i].portFE));   
										//System.out.println(String.valueOf(deliveryHead)+String.valueOf(deliveryEnd));
										System.out.println("Handled request, result lost,send succeed responce!");
									}
								}//end of synchronize
								break;
							}					
						}//end of for
						if (!found){//add to queue as a new message;
							synchronized (deliveryQueue){
								int tempEnd = deliveryEnd+1;
								if (tempEnd==queueLength) tempEnd=0;
								if (tempEnd!=deliveryHead){
								//queue is not full
									deliveryQueue[deliveryEnd].requestId=inputs[0];
									deliveryQueue[deliveryEnd].mainRequest=requestStr.substring(inputs[0].length()+inputs[1].length()+2);
									deliveryQueue[deliveryEnd].sequenceNum=globalSequence++;
									deliveryQueue[deliveryEnd].hostFE=request.getAddress();
									deliveryQueue[deliveryEnd].portFE=request.getPort();
									deliveryEnd = tempEnd;
									//System.out.println(String.valueOf(deliveryHead)+String.valueOf(deliveryEnd));
								}
								else
									//queue is full, simply discard the request
									System.out.println("Queue is full, discard the request!");
							}//end of synchronize			.
						}
					}//end of resend message
				}//leader mode
				
				else{//slave mode
					if (missCount==0) mSocket.setSoTimeout(0);
					else mSocket.setSoTimeout(1000);
					try{
						mSocket.receive(request); //listen to multicast messages
						//analyze request
						String requestStr = new String(request.getData(),0,request.getLength());
						String[] inputs = requestStr.split(" ");
						int seq=Integer.parseInt(inputs[0]);
						int R = Integer.parseInt(inputs[2]);
						int foundNode=-1;
						for (int i=0;i<systemView.length;i++){
							if (systemView[i].priority==Integer.parseInt(inputs[1])){
								foundNode=i;
								break;
							}	
						}
						int Rq=99999;
						if (foundNode>0){
							if (((Slave)systemView[foundNode]).state!="OK"){
								Rq=systemView[foundNode].seq;
							}
						}
						else if (foundNode==0) Rq=systemView[foundNode].seq;
						else {
							if (missCount>0) missCount++; 
							continue;
						}
						if (seq==(Rq+1)){
							//put into deliverry queue
							synchronized (deliveryQueue){
								int tempEnd = deliveryEnd+1;
								if (tempEnd==queueLength) tempEnd=0;
								if (tempEnd!=deliveryHead){
									//queue is not full
									deliveryQueue[deliveryEnd].requestId=inputs[3];
									deliveryQueue[deliveryEnd].mainRequest=requestStr.substring(inputs[0].length()+
										inputs[1].length()+inputs[2].length()+inputs[3].length()+inputs[4].length()+inputs[5].length()+6);
									deliveryQueue[deliveryEnd].sequenceNum=Integer.parseInt(inputs[0]);
									deliveryQueue[deliveryEnd].hostFE=InetAddress.getByName(inputs[4]);
									deliveryQueue[deliveryEnd].portFE=Integer.parseInt(inputs[5]);
									deliveryEnd = tempEnd;
										//System.out.println(String.valueOf(deliveryHead)+String.valueOf(deliveryEnd));
									
									//multicast ack
									String originalMsg = inputs[0]+" "+systemView[foundNode].priority+" "+systemView[foundNode].seq+" "+requestStr.substring(inputs[0].length()+
											inputs[1].length()+inputs[2].length()+3);
									InetAddress group = InetAddress.getByName("239.1.2.7");	
									DatagramPacket multicast = new DatagramPacket(originalMsg.getBytes(),originalMsg.length(),group,4446);   					
									mSocket.send(multicast);
									
									System.out.println("send ack1");
									systemView[foundNode].seq++;
									missCount=0;
									while (holdbackHead!=holdbackEnd){
										if (holdbackQueue[holdbackHead].sequenceNum ==
												(systemView[holdbackQueue[holdbackHead].qInViewMap].seq+1)){//holdback has data
											//put into deliverry queue
											tempEnd = deliveryEnd+1;
											if (tempEnd==queueLength) tempEnd=0;
											if (tempEnd!=deliveryHead){
												//queue is not full
												deliveryQueue[deliveryEnd].requestId=holdbackQueue[holdbackHead].requestId;
												deliveryQueue[deliveryEnd].mainRequest=holdbackQueue[holdbackHead].mainRequest;
												deliveryQueue[deliveryEnd].sequenceNum=holdbackQueue[holdbackHead].sequenceNum;
												deliveryQueue[deliveryEnd].hostFE=holdbackQueue[holdbackHead].hostFE;
												deliveryQueue[deliveryEnd].portFE=holdbackQueue[holdbackHead].portFE;
												deliveryEnd = tempEnd;
												//multicast ack
												originalMsg = holdbackQueue[holdbackHead].sequenceNum+" "+systemView[holdbackQueue[holdbackHead].qInViewMap].priority+" "+systemView[holdbackQueue[holdbackHead].qInViewMap].seq+" "
												+holdbackQueue[holdbackHead].hostFE+" "+holdbackQueue[holdbackHead].portFE+" "+holdbackQueue[holdbackHead].mainRequest;
												multicast = new DatagramPacket(originalMsg.getBytes(),originalMsg.length(),group,4446);   					
												mSocket.send(multicast);
												System.out.println("send ack2");
												systemView[holdbackQueue[holdbackHead].qInViewMap].seq++;	
												int tempHead = holdbackHead;
												tempHead++;
												if (tempHead == queueLength/2) 
													tempHead=0;
												holdbackHead = tempHead;
											}
											else{
												//queue is full, simply discard the request
												System.out.println("Queue is full, discard the request!");
												break;
											}
										}
										else if(holdbackQueue[holdbackHead].sequenceNum <
												(systemView[holdbackQueue[holdbackHead].qInViewMap].seq+1)){
											int tempHead = holdbackHead;
											tempHead++;
											if (tempHead == queueLength/2) 
												tempHead=0;
											holdbackHead = tempHead;
										}
										else {
											missCount++;
											break;
										}
									}									
								}
								else
									//queue is full, simply discard the request
									System.out.println("Queue is full, discard the request!");
							}//end of synchronize	
						}
						else if ((seq>(Rq+1))||(R>Rq)){
							//put into holdback queue

							int tempEnd = holdbackEnd+1;
							if (tempEnd==queueLength/2) tempEnd=0;
							if (tempEnd!=holdbackHead){
								//queue is not full
								holdbackQueue[holdbackEnd].requestId=inputs[3];
								holdbackQueue[holdbackEnd].mainRequest=requestStr.substring(inputs[0].length()+
									inputs[1].length()+inputs[2].length()+inputs[3].length()+inputs[4].length()+inputs[5].length()+6);
								holdbackQueue[holdbackEnd].sequenceNum=Integer.parseInt(inputs[0]);
								holdbackQueue[holdbackEnd].hostFE=InetAddress.getByName(inputs[4]);
								holdbackQueue[holdbackEnd].portFE=Integer.parseInt(inputs[5]);
								holdbackQueue[holdbackEnd].qInViewMap=foundNode;
								holdbackEnd = tempEnd;
								//System.out.println(String.valueOf(deliveryHead)+String.valueOf(deliveryEnd));
							}
							else{
								//queue is full, simply discard the request
								System.out.println("holdback Queue is full, discard the request!");
							}
							missCount++;
						}
					}catch(SocketTimeoutException e) {
						missCount++;
					}
					if (missCount>=tolerent){ //sendrequst;}
						
					}
				}//end of slave				
			}  //end of while  
		} catch (SocketException e){
			System.out.println("Socket: " + e.getMessage()); 
		} catch (IOException e){
			System.out.println("IO: " + e.getMessage());
		} finally {
			if(aSocket != null) aSocket.close();
			if(mSocket != null) mSocket.close();
		} 
	}
}
