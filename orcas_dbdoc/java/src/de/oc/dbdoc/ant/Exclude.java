package de.oc.dbdoc.ant;

public class Exclude
{
  private String _name;
  private String _tablegroup;

  String getTablegroup()
  {
    return _tablegroup;
  }

  public void setTablegroup( String pTablegroup )
  {
    _tablegroup = pTablegroup;
  }

  public void setName( String pName )
  {
    _name = pName;
  }

  String getName()
  {
    return _name;
  }
}
