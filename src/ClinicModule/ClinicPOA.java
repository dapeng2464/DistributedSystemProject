package ClinicModule;


/**
* ClinicModule/ClinicPOA.java .
* 由IDL-to-Java 编译器 (可移植), 版本 "3.2"生成
* 从D:/Wangxt/Programs/javaworkspace/Staff_Management_CORBA/src/ClinicModule/Clinic.idl
* 2016年7月5日 星期二 下午01时37分31秒 EDT
*/

public abstract class ClinicPOA extends org.omg.PortableServer.Servant
 implements ClinicModule.ClinicOperations, org.omg.CORBA.portable.InvokeHandler
{

  // Constructors

  private static java.util.Hashtable _methods = new java.util.Hashtable ();
  static
  {
    _methods.put ("createDRecord", new java.lang.Integer (0));
    _methods.put ("createNRecord", new java.lang.Integer (1));
    _methods.put ("getRecordCounts", new java.lang.Integer (2));
    _methods.put ("editRecord", new java.lang.Integer (3));
    _methods.put ("transferRecord", new java.lang.Integer (4));
    _methods.put ("login", new java.lang.Integer (5));
    _methods.put ("logout", new java.lang.Integer (6));
  }

  public org.omg.CORBA.portable.OutputStream _invoke (String $method,
                                org.omg.CORBA.portable.InputStream in,
                                org.omg.CORBA.portable.ResponseHandler $rh)
  {
    org.omg.CORBA.portable.OutputStream out = null;
    java.lang.Integer __method = (java.lang.Integer)_methods.get ($method);
    if (__method == null)
      throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);

    switch (__method.intValue ())
    {
       case 0:  // ClinicModule/Clinic/createDRecord
       {
         String firstName = in.read_string ();
         String lastName = in.read_string ();
         String address = in.read_string ();
         String phone = in.read_string ();
         String specialization = in.read_string ();
         String location = in.read_string ();
         String ManagerID = in.read_string ();
         short mode = in.read_short ();
         String recordID = in.read_string ();
         String $result = null;
         $result = this.createDRecord (firstName, lastName, address, phone, specialization, location, ManagerID, mode, recordID);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 1:  // ClinicModule/Clinic/createNRecord
       {
         String firstName = in.read_string ();
         String lastName = in.read_string ();
         String designation = in.read_string ();
         String status = in.read_string ();
         String statusDate = in.read_string ();
         String ManagerID = in.read_string ();
         short mode = in.read_short ();
         String recordID = in.read_string ();
         String $result = null;
         $result = this.createNRecord (firstName, lastName, designation, status, statusDate, ManagerID, mode, recordID);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 2:  // ClinicModule/Clinic/getRecordCounts
       {
         String recordType = in.read_string ();
         String ManagerID = in.read_string ();
         String $result = null;
         $result = this.getRecordCounts (recordType, ManagerID);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 3:  // ClinicModule/Clinic/editRecord
       {
         String recordID = in.read_string ();
         String fieldName = in.read_string ();
         String newValue = in.read_string ();
         String ManagerID = in.read_string ();
         String $result = null;
         $result = this.editRecord (recordID, fieldName, newValue, ManagerID);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 4:  // ClinicModule/Clinic/transferRecord
       {
         String recordID = in.read_string ();
         String remoteClinicServer = in.read_string ();
         String ManagerID = in.read_string ();
         String $result = null;
         $result = this.transferRecord (recordID, remoteClinicServer, ManagerID);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 5:  // ClinicModule/Clinic/login
       {
         String ManagerID = in.read_string ();
         int $result = (int)0;
         $result = this.login (ManagerID);
         out = $rh.createReply();
         out.write_long ($result);
         break;
       }

       case 6:  // ClinicModule/Clinic/logout
       {
         String ManagerID = in.read_string ();
         int $result = (int)0;
         $result = this.logout (ManagerID);
         out = $rh.createReply();
         out.write_long ($result);
         break;
       }

       default:
         throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
    }

    return out;
  } // _invoke

  // Type-specific CORBA::Object operations
  private static String[] __ids = {
    "IDL:ClinicModule/Clinic:1.0"};

  public String[] _all_interfaces (org.omg.PortableServer.POA poa, byte[] objectId)
  {
    return (String[])__ids.clone ();
  }

  public Clinic _this() 
  {
    return ClinicHelper.narrow(
    super._this_object());
  }

  public Clinic _this(org.omg.CORBA.ORB orb) 
  {
    return ClinicHelper.narrow(
    super._this_object(orb));
  }


} // class ClinicPOA
