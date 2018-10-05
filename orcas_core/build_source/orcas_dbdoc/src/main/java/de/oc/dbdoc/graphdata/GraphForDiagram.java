package de.oc.dbdoc.graphdata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.oc.dbdoc.ant.Diagram;
import de.oc.dbdoc.ant.Styles;
import de.oc.dbdoc.ant.Tableregistry;
import de.oc.dbdoc.schemadata.Table;

public class GraphForDiagram extends Graph
{
  private Diagram _diagram;
  private List<Graph> _subGraphs;

  public GraphForDiagram( Diagram pDiagram, Styles pStyles, Graph pParentGraph, Tableregistry pTableregistry )
  {
    super( pParentGraph == null ? Collections.emptyList() : Collections.singletonList( pParentGraph ), pStyles, pTableregistry );

    _subGraphs = new ArrayList<Graph>();
    _diagram = pDiagram;

    for( Diagram lDiagram : _diagram.getSubDiagrams() )
    {
      _subGraphs.add( new GraphForDiagram( lDiagram, getStyles(), this, pTableregistry ) );
    }
  }

  @Override
  public boolean containsTableRecursive( Table pTable )
  {
    return _diagram.isTableIncluded( pTable.getName(), getTableregistry() );
  }

  @Override
  public String getLabel()
  {
    return _diagram.getLabel();
  }

  @Override
  public boolean isForSingleTable( Table pTable )
  {
    return false;
  }

  @Override
  public List<Graph> getSubGraphs()
  {
    return _subGraphs;
  }

  @Override
  public boolean isSingleTable()
  {
    return false;
  }

  @Override
  public boolean isRenderClusterForSubgraphs()
  {
    return _diagram.isRenderClusterForSubgraphs();
  }

  @Override
  public boolean isCollapseSubgraphs()
  {
    return _diagram.isCollapseSubgraphs();
  }

  @Override
  public String getStyleForGraph()
  {
    return _diagram.getStyleForDiagram( getStyles() );
  }

  @Override
  public String getDotExecutable()
  {
    return _diagram.getDotExecutable( getStyles() );
  }
}
