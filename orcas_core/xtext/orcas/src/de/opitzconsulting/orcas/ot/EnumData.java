package de.opitzconsulting.orcas.ot;

public class EnumData
{
  private String _literal;
  private String _name;
  private int _value;

  public EnumData( String pLiteral, String pName, int pValue )
  {
    _literal = pLiteral;
    _name = pName;
    _value = pValue;
  }

  public String getLiteral()
  {      
      
     if ("all_operations".equals(_literal)) {
          return "all operations";
      }
      if ("direct_load_operations".equals(_literal)) {
          return "direct_load operations";
      }
      if ("query_low".equals(_literal)) {
          return "query low";
      }
      if ("query_high".equals(_literal)) {
          return "query high";
      }
      if ("archive_low".equals(_literal)) {
          return "archive low";
      }
      if ("archive_high".equals(_literal)) {
          return "archive high";
      }
      if ("refresh_complete".equals(_literal)) {
          return "refresh complete";
      }
      if ("refresh_force".equals(_literal)) {
          return "refresh force";
      }
      if ("refresh_fast".equals(_literal)) {
          return "refresh fast";
      }
      if ("never_refresh".equals(_literal)) {
          return "never refresh";
      }
    return _literal;
  }

  public String getName()
  {
    return _name;
  }

  public int getValue()
  {
    return _value;
  }
}
