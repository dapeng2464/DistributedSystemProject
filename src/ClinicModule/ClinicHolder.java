package ClinicModule;

/**
* ClinicModule/ClinicHolder.java .
* 由IDL-to-Java 编译器 (可移植), 版本 "3.2"生成
* 从D:/Wangxt/Programs/javaworkspace/Staff_Management_CORBA/src/ClinicModule/Clinic.idl
* 2016年7月5日 星期二 下午03时01分49秒 EDT
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
