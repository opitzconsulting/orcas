package de.oc.dbdoc.ant;

public class Style extends TableGroupReferenceBase
{
  private String _name;
  private String _value;

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
  
  public void setStylename( String pName )
  {
    _name = pName;
  }

  public void setValue( String pValue )
  {
    _value = pValue;
  }

  boolean isForTable( String pTableName, Tableregistry pTableregistry )
  {
    for( Tablegroup lTablegroup : getTableGroups( pTableregistry ) )
    {
      if( lTablegroup.isTableIncluded( pTableName, pTableregistry ) )
      {
        return true;
      }
    }

    return false;
  }
}
