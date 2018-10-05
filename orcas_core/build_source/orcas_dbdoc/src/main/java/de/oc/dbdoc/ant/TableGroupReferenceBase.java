package de.oc.dbdoc.ant;

import java.util.ArrayList;
import java.util.List;

public class TableGroupReferenceBase
{
  private String _tablegroup;

  public TableGroupReferenceBase()
  {
  }

  public void setTablegroup( String pTablegroup )
  {
    _tablegroup = pTablegroup;
  }

  protected List<Tablegroup> getTableGroups( Tableregistry pTableregistry )
  {
    List<Tablegroup> lReturn = new ArrayList<Tablegroup>();

    if( _tablegroup != null )
    {
      for( Tablegroup lTablegroup : pTableregistry.getTablesgroups() )
      {
        if( lTablegroup.getName().matches( _tablegroup ) )
        {
          lReturn.add( lTablegroup );
        }
      }
    }

    return lReturn;
  }
}
