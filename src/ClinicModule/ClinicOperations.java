package ClinicModule;


/**
* ClinicModule/ClinicOperations.java .
* 由IDL-to-Java 编译器 (可移植), 版本 "3.2"生成
* 从D:/Wangxt/Programs/javaworkspace/Staff_Management_CORBA/src/ClinicModule/Clinic.idl
* 2016年7月5日 星期二 下午03时01分49秒 EDT
*/

public interface ClinicOperations 
{
  String createDRecord (String firstName, String lastName, String address, String phone, String specialization, String location, String ManagerID, int mode, String recordID);
  String createNRecord (String firstName, String lastName, String designation, String status, String statusDate, String ManagerID, int mode, String recordID);
  String getRecordCounts (String recordType, String ManagerID);
  String editRecord (String recordID, String fieldName, String newValue, String ManagerID);
  String transferRecord (String recordID, String remoteClinicServer, String ManagerID);
  int login (String ManagerID);
  int logout (String ManagerID);
} // interface ClinicOperations
