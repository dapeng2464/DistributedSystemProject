package ClinicModule;


/**
* ClinicModule/ClinicHelper.java .
* 由IDL-to-Java 编译器 (可移植), 版本 "3.2"生成
* 从D:/Wangxt/Programs/javaworkspace/Staff_Management_CORBA/src/ClinicModule/Clinic.idl
* 2016年7月5日 星期二 下午03时01分49秒 EDT
*/

abstract public class ClinicHelper
{
  private static String  _id = "IDL:ClinicModule/Clinic:1.0";

  public static void insert (org.omg.CORBA.Any a, ClinicModule.Clinic that)
  {
    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
  }

  public static ClinicModule.Clinic extract (org.omg.CORBA.Any a)
  {
    return read (a.create_input_stream ());
  }

  private static org.omg.CORBA.TypeCode __typeCode = null;
  synchronized public static org.omg.CORBA.TypeCode type ()
  {
    if (__typeCode == null)
    {
      __typeCode = org.omg.CORBA.ORB.init ().create_interface_tc (ClinicModule.ClinicHelper.id (), "Clinic");
    }
    return __typeCode;
  }

  public static String id ()
  {
    return _id;
  }

  public static ClinicModule.Clinic read (org.omg.CORBA.portable.InputStream istream)
  {
    return narrow (istream.read_Object (_ClinicStub.class));
  }

  public static void write (org.omg.CORBA.portable.OutputStream ostream, ClinicModule.Clinic value)
  {
    ostream.write_Object ((org.omg.CORBA.Object) value);
  }

  public static ClinicModule.Clinic narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof ClinicModule.Clinic)
      return (ClinicModule.Clinic)obj;
    else if (!obj._is_a (id ()))
      throw new org.omg.CORBA.BAD_PARAM ();
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      ClinicModule._ClinicStub stub = new ClinicModule._ClinicStub ();
      stub._set_delegate(delegate);
      return stub;
    }
  }

  public static ClinicModule.Clinic unchecked_narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof ClinicModule.Clinic)
      return (ClinicModule.Clinic)obj;
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      ClinicModule._ClinicStub stub = new ClinicModule._ClinicStub ();
      stub._set_delegate(delegate);
      return stub;
    }
  }

}
