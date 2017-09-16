package Staff;

//inherit base class staffRecord
public class DoctorRecord extends staffRecord{

	public String Address;
	public String phoneNum;
	public String specialization;
	public String location;

	private static String [] validFields = {"Address","phoneNum","location"};
	public static String [] validLocations = {"MTL","LVL","DDO"};
	
	//initialize a new doctor record
	public int init(String first,String last,String Add,
			String phone,String spec,String loc,String ID) {
		
		if (!isBelongList(loc.toUpperCase(),validLocations)){
			return -1;						//invalid location
		}									
		if (ID.length()!=5) {
			return -2;						//invalid ID
		}			
		
		this.firstName = first;
		this.lastName = last;
		this.Address = Add;
		this.phoneNum = phone;
		this.specialization = spec;
		this.location = loc.toUpperCase();	//location will be modified to upper case automatically
		this.recordID = "DR"+ID;
		return 0;
	}
	
	//referenced by ClinicServer.editRecord
	public int editRecord (String fieldName,String newValue){
		if (!isBelongList(fieldName,validFields)) { 
			return -1;		//field invalid or can't be edited
		}
		if (fieldName.equals("location")) { 
			if (!isBelongList(newValue.toUpperCase(),validLocations))
				return -2;      //location value is invalid
			else newValue=newValue.toUpperCase();
		}
		try{
			this.getClass().getDeclaredField(fieldName).set(this, newValue);
		}catch (Exception e){
			return -3;
		}
		return 0;
	}
	
	public String getRecord (){
		return recordID+" "+firstName+" "+lastName+" "+Address+" "+phoneNum+" "+specialization+" "+location;
	}
	
}
