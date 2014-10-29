package de.oc.dbdoc.ant;

public class Style extends TableGroupReferenceBase
{
  private String _name;
  private String _value;

  public Style( OrcasDbDoc pOrcasDbDoc )
  {
    super( pOrcasDbDoc );
  }

  String getName()
  {
    return _name;
  }

  String getValue()
  {
    return _value;
  }

  public void setName( String pName )
  {
    _name = pName;
  }

  public void setValue( String pValue )
  {
    _value = pValue;
  }

  boolean isForTable( String pTableName )
  {
    for( Tablegroup lTablegroup : getTableGroups() )
    {
      if( lTablegroup.isTableIncluded( pTableName ) )
      {
        return true;
      }
    }

    return false;
  }
}
