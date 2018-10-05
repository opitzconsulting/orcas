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
  private SubincludeMode _subinncludeMode = SubincludeMode.DIAGRAMS_WITH_TABLES;
  private List<Diagram> _diagrams = new ArrayList<Diagram>();

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
    if( pSubinnclude.equals( "diagramswithtables" ) )
    {
      _subinncludeMode = SubincludeMode.DIAGRAMS_WITH_TABLES;
    }
    if( pSubinnclude.equals( "diagramsonly" ) )
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
    Diagram lDiagram = new Diagram();

    addDiagram( lDiagram );

    return lDiagram;
  }

  public void addDiagram( Diagram pDiagram )
  {
    _diagrams.add( pDiagram );
  }

  public boolean isTableIncluded( String pTableName, Tableregistry pTableregistry )
  {
    for( Tablegroup lTablegroup : getTableGroups( pTableregistry ) )
    {
      if( lTablegroup.isTableIncluded( pTableName, pTableregistry ) )
      {
        return true;
      }
    }

    if( _subinncludeMode == SubincludeMode.TABLES || _subinncludeMode == SubincludeMode.DIAGRAMS_WITH_TABLES )
    {
      for( Diagram lDiagram : _diagrams )
      {
        if( lDiagram.isTableIncluded( pTableName, pTableregistry ) )
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

  public String getStyleForDiagram( Styles pStyles )
  {
    return pStyles.getStyleForStylegroup( _stylegroup );
  }

  public void setStylegroup( String pStylegroup )
  {
    _stylegroup = pStylegroup;
  }

  public String getDotExecutable( Styles pStyles )
  {
    return pStyles.getDotExecutableForStylegroup( _stylegroup );
  }
}
