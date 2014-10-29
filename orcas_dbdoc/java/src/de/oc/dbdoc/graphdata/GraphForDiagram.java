package de.oc.dbdoc.graphdata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.oc.dbdoc.ant.Diagram;
import de.oc.dbdoc.ant.Styles;
import de.oc.dbdoc.schemadata.Table;

public class GraphForDiagram extends Graph
{
  private Diagram _diagram;
  private List<Graph> _subGraphs;

  public GraphForDiagram( Diagram pDiagram, Styles pStyles, Graph pParentGraph )
  {
    super( pParentGraph == null ? Collections.EMPTY_LIST : Collections.singletonList( pParentGraph ), pStyles );

    _subGraphs = new ArrayList<Graph>();
    _diagram = pDiagram;

    for( Diagram lDiagram : _diagram.getSubDiagrams() )
    {
      _subGraphs.add( new GraphForDiagram( lDiagram, getStyles(), this ) );
    }
  }

  @Override
  public boolean containsTableRecursive( Table pTable )
  {
    return _diagram.isTableIncluded( pTable.getName() );
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
    return _diagram.getStyleForDiagram();
  }

  @Override
  public String getDotExecutable()
  {
    return _diagram.getDotExecutable();
  }
}
