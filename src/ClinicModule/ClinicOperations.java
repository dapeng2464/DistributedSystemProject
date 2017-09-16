package ClinicModule;


/**
* ClinicModule/ClinicOperations.java .
* ��IDL-to-Java ������ (����ֲ), �汾 "3.2"����
* ��D:/Wangxt/Programs/javaworkspace/Staff_Management_CORBA/src/ClinicModule/Clinic.idl
* 2016��7��5�� ���ڶ� ����03ʱ01��49�� EDT
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
