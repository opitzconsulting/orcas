package de.opitzconsulting.orcas.ot;

public class EnumData
{
  private String _literal;
  private String _name;
  private int _value;
  private String _javaName;

  public String getJavaName()
  {
    return _javaName;
  }

  public EnumData( String pLiteral, String pName, int pValue, String pJavaName )
  {
    _literal = pLiteral;
    _name = pName;
    _value = pValue;
    _javaName = pJavaName;
  }

  public String getLiteral()
  {
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
