package de.oc.dbdoc.ant;

import java.util.ArrayList;
import java.util.List;

public class Diagram extends TableGroupReferenceBase
{
  public enum SubincludeMode
  {
    TABLES, DIAGRAMS_WITH_TABLES, DIAGRAMS_ONLY
  }

  private String _label;
  private String _stylegroup;
  private SubincludeMode _subinncludeMode;
  private List<Diagram> _diagrams = new ArrayList<Diagram>();

  public Diagram( OrcasDbDoc pOrcasDbDoc )
  {
    super( pOrcasDbDoc );
  }

  public String getLabel()
  {
    return _label;
  }

  public void setLabel( String pLabel )
  {
    _label = pLabel;
  }

  public void setSubinnclude( String pSubinnclude )
  {
    if( pSubinnclude.equals( "tables" ) )
    {
      _subinncludeMode = SubincludeMode.TABLES;
    }
    if( pSubinnclude.equals( "diagrams_with_tables" ) )
    {
      _subinncludeMode = SubincludeMode.DIAGRAMS_WITH_TABLES;
    }
    if( pSubinnclude.equals( "diagrams_only" ) )
    {
      _subinncludeMode = SubincludeMode.DIAGRAMS_ONLY;
    }

    if( _subinncludeMode == null )
    {
      throw new RuntimeException( "subinclude unbekannt: " + pSubinnclude );
    }
  }

  public Diagram createDiagram()
  {
    Diagram lDiagram = new Diagram( getOrcasDbDoc() );

    _diagrams.add( lDiagram );

    return lDiagram;
  }

  public boolean isTableIncluded( String pTableName )
  {
    for( Tablegroup lTablegroup : getTableGroups() )
    {
      if( lTablegroup.isTableIncluded( pTableName ) )
      {
        return true;
      }
    }

    if( _subinncludeMode == SubincludeMode.TABLES || _subinncludeMode == SubincludeMode.DIAGRAMS_WITH_TABLES )
    {
      for( Diagram lDiagram : _diagrams )
      {
        if( lDiagram.isTableIncluded( pTableName ) )
        {
          return true;
        }
      }
    }

    return false;
  }

  public List<Diagram> getSubDiagrams()
  {
    return _diagrams;
  }

  public boolean isRenderClusterForSubgraphs()
  {
    return _subinncludeMode == SubincludeMode.DIAGRAMS_WITH_TABLES;
  }

  public boolean isCollapseSubgraphs()
  {
    return _subinncludeMode == SubincludeMode.DIAGRAMS_ONLY;
  }

  public String getStyleForDiagram()
  {
    return getOrcasDbDoc().getStyles().getStyleForStylegroup( _stylegroup );
  }

  public void setStylegroup( String pStylegroup )
  {
    _stylegroup = pStylegroup;
  }

  public String getDotExecutable()
  {
    return getOrcasDbDoc().getStyles().getDotExecutableForStylegroup( _stylegroup );
  }
}
