package de.opitzconsulting.orcas.ot;

public class ClassDataPrimitive extends ClassData
{
  private String _sqlName;
  private String _javaName;

  public String getSqlName()
  {
    return _sqlName;
  }

  public ClassDataPrimitive( String pSqlName, String pJavaName )
  {
    _sqlName = pSqlName;
    _javaName = pJavaName;
  }

  @Override
  public String getDiffSqlName()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isAtomicValue()
  {
    return true;
  }

  @Override
  public String getPlainSqlName()
  {
    if( _sqlName.indexOf( '(' ) != -1 )
    {
      return _sqlName.substring( 0, _sqlName.indexOf( '(' ) );
    }

    return _sqlName;
  }

  @Override
  public String getJavaName()
  {
    return _javaName;
  }
}
