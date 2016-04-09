package de.opitzconsulting.orcas.ot;

public class ClassDataPrimitive extends ClassData
{
  private String _sqlName;

  public String getSqlName()
  {
    return _sqlName;
  }

  public ClassDataPrimitive( String pSqlName )
  {
    _sqlName = pSqlName;
  }

  @Override
  public String getDiffSqlName() {
	throw new UnsupportedOperationException();
  }

  @Override
  public boolean isAtomicValue() {	
	return true;
  }
}
