package de.oc.dbdoc.ant;

import java.util.ArrayList;
import java.util.List;

public class Tableregistry
{
  private OrcasDbDoc _orcasDbDoc;
  private String _tablesrcfolder;
  private List<Tablegroup> _tablesgroups = new ArrayList<Tablegroup>();

  public Tableregistry( OrcasDbDoc pOrcasDbDoc )
  {
    _orcasDbDoc = pOrcasDbDoc;
  }

  public void setTablesrcfolder( String pTablesrcfolder )
  {
    _tablesrcfolder = pTablesrcfolder;
  }

  public Tablegroup createTablegroup()
  {
    Tablegroup lTablegroup = new Tablegroup( _orcasDbDoc );

    _tablesgroups.add( lTablegroup );

    return lTablegroup;
  }

  List<Tablegroup> getTablesgroups()
  {
    return _tablesgroups;
  }

  String getTablesrcfolder()
  {
    return _tablesrcfolder;
  }
}
