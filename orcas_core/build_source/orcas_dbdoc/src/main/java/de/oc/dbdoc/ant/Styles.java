package de.oc.dbdoc.ant;

public class Styles
{
  private Tables _tables;
  private Diagrams _diagrams;

  public void setTables( Tables pTables )
  {
    if( _tables != null )
    {
      throw new RuntimeException( "tables darf nicht mehrfach vorkommen" );
    }

    _tables = pTables;
  }

  public void setDiagrams( Diagrams pDiagrams )
  {
    if( _diagrams != null )
    {
      throw new RuntimeException( "diagrams darf nicht mehrfach vorkommen" );
    }

    _diagrams = pDiagrams;
  }

  public String getStyleForTable( String pTableName, Tableregistry pTableregistry )
  {
    return _tables.getStyleForTable( pTableName, pTableregistry );
  }

  public String getStyleForStylegroup( String pStylegroupName )
  {
    return _diagrams.getStyleForStylegroup( pStylegroupName );
  }

  public String getDotExecutableForStylegroup( String pStylegroupName )
  {
    return _diagrams.getDotExecutableForStylegroup( pStylegroupName );
  }

  public Tables createTables()
  {
    setTables( new Tables() );

    return _tables;
  }

  public Diagrams createDiagrams()
  {
    setDiagrams( new Diagrams() );

    return _diagrams;
  }
}
