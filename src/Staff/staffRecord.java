package Staff;

//Base class of DoctorRecord and NurseRecord
public class staffRecord {
	protected String firstName;
	protected String lastName;
	protected String recordID;
	
	protected static boolean isBelongList(String str,String [] validStrings){  
		for (String temp : validStrings) {  
			if (temp.equals(str)) {  
				return (true);   
			}  
		}
		return (false);
	}  
	
	public String getID(){
		return this.recordID;
	}
}
