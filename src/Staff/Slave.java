package Staff;

public class Slave extends node{
	public int slaveid;
	public int port;
	public String state;
	public boolean viewReceive=true;
	public String view;
	public Slave(int i,String ipaddress,int portNum){
		slaveid = i;
		ip = ipaddress;
		state = "unknown";
		port = portNum;
		priority = i;
		view="";
	}
}
