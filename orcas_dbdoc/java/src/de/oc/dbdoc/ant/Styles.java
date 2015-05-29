package de.oc.dbdoc.ant;

public class Styles
{
  private OrcasDbDoc _orcasDbDoc;
  private Tables _tables;
  private Diagrams _diagrams;

  public Styles( OrcasDbDoc pOrcasDbDoc )
  {
    _orcasDbDoc = pOrcasDbDoc;
  }

  public String getStyleForTable( String pTableName )
  {
    return _tables.getStyleForTable( pTableName );
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
    if( _tables != null )
    {
      throw new RuntimeException( "tables darf nicht mehrfach vorkommen" );
    }

    _tables = new Tables( _orcasDbDoc );

    return _tables;
  }

  public Diagrams createDiagrams()
  {
    if( _diagrams != null )
    {
      throw new RuntimeException( "diagrams darf nicht mehrfach vorkommen" );
    }

    _diagrams = new Diagrams();

    return _diagrams;
  }
}
