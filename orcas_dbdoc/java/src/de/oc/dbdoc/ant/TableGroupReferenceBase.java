package de.oc.dbdoc.ant;

import java.util.ArrayList;
import java.util.List;

public class TableGroupReferenceBase
{
  private OrcasDbDoc _orcasDbDoc;
  private String _tablegroup;

  public TableGroupReferenceBase( OrcasDbDoc pOrcasDbDoc )
  {
    _orcasDbDoc = pOrcasDbDoc;
  }

  protected OrcasDbDoc getOrcasDbDoc()
  {
    return _orcasDbDoc;
  }

  public void setTablegroup( String pTablegroup )
  {
    _tablegroup = pTablegroup;
  }

  protected List<Tablegroup> getTableGroups()
  {
    List<Tablegroup> lReturn = new ArrayList<Tablegroup>();

    if( _tablegroup != null )
    {
      for( Tablegroup lTablegroup : _orcasDbDoc.getTableregistry().getTablesgroups() )
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
