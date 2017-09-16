package Staff;

//inherit base class staffRecord
public class NurseRecord extends staffRecord{
	public String designation;
	public String status;
	public String statusDate;
	private static String [] validFields = {"designation","status","statusDate"};
	private static String [] validDesigs = {"JUNIOR","SENIOR"};
	private static String [] validStatus = {"ACTIVE","TERMINATED"};
	
	//initialize a new nurse record	
	public int init(String first,String last,String desig,
				String st,String stDate,String ID) {  
		if ((!isBelongList(desig.toUpperCase(),validDesigs))||(!isBelongList(st.toUpperCase(),validStatus))){
			return -1;						//invalid designation ors status
		}									 
		if (ID.length()!=5) {
			return -2;						//invalid ID
		}									
		this.firstName = first;
		this.lastName = last;
		this.designation = desig.toLowerCase();
		this.status = st.toLowerCase();
		this.statusDate = stDate;
		this.recordID = "NR"+ID;
		return 0;
	}
	
	//referenced by ClinicServer.editRecord	
	public int editRecord (String fieldName,String newValue) {
		if (!isBelongList(fieldName,validFields)) { 
			return -1;		//field invalid or can't be edited
		}
		if (fieldName.equals("designation")){
			if (!isBelongList(newValue.toUpperCase(),validDesigs))
				return -2;		//designation or status value is invalid
			else newValue=newValue.toLowerCase();  //designation will be modified to lower case automatically
		}		
		else if (fieldName.equals("status")) {
			if (!isBelongList(newValue.toUpperCase(),validStatus))
				return -2;      //designation or status value is invalid
			else newValue=newValue.toLowerCase();	//status will be modified to lower case automatically
		}
		try{
			this.getClass().getDeclaredField(fieldName).set(this, newValue);
		}catch (Exception e){
			return -3;
		}
		return 0;
	}
	
	public String getRecord (){
		return recordID+" "+firstName+" "+lastName+" "+designation+" "+status+" "+statusDate;
	}	

}
