package de.oc.dbdoc.ant;

import java.util.ArrayList;
import java.util.List;

public class Tableregistry
{
  private List<Tablegroup> _tablesgroups = new ArrayList<Tablegroup>();

  public void addTablegroup( Tablegroup pTablegroup )
  {
    _tablesgroups.add( pTablegroup );
  }

  public Tablegroup createTablegroup()
  {
    Tablegroup lTablegroup = new Tablegroup();

    _tablesgroups.add( lTablegroup );

    return lTablegroup;
  }

  List<Tablegroup> getTablesgroups()
  {
    return _tablesgroups;
  }
}
