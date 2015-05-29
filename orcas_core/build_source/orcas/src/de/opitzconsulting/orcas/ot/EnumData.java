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
