package ClinicModule;

/**
* ClinicModule/ClinicHolder.java .
* ��IDL-to-Java ������ (����ֲ), �汾 "3.2"����
* ��D:/Wangxt/Programs/javaworkspace/Staff_Management_CORBA/src/ClinicModule/Clinic.idl
* 2016��7��5�� ���ڶ� ����03ʱ01��49�� EDT
*/

public final class ClinicHolder implements org.omg.CORBA.portable.Streamable
{
  public ClinicModule.Clinic value = null;

  public ClinicHolder ()
  {
  }

  public ClinicHolder (ClinicModule.Clinic initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = ClinicModule.ClinicHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    ClinicModule.ClinicHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return ClinicModule.ClinicHelper.type ();
  }

}
