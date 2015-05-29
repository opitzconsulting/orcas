package de.oc.dbdoc.ant;

import java.util.ArrayList;
import java.util.List;

public class Diagrams
{
  private List<Stylegroup> _stylegroups = new ArrayList<Stylegroup>();

  public Stylegroup createStylegroup()
  {
    Stylegroup lStylegroup = new Stylegroup();

    _stylegroups.add( lStylegroup );

    return lStylegroup;
  }

  private Stylegroup _getStylegroup( String pStylegroupName )
  {
    Stylegroup lStylegroup = null;

    for( Stylegroup lTestStylegroup : _stylegroups )
    {
      if( lTestStylegroup.getName().equals( pStylegroupName ) )
      {
        lStylegroup = lTestStylegroup;
      }
    }

    if( lStylegroup == null )
    {
      lStylegroup = new Stylegroup();
    }

    return lStylegroup;
  }

  String getStyleForStylegroup( String pStylegroupName )
  {
    return _getStylegroup( pStylegroupName ).getStyleForStylegroup();
  }

  public String getDotExecutableForStylegroup( String pStylegroupName )
  {
    return _getStylegroup( pStylegroupName ).getDotExecutable();
  }
}
