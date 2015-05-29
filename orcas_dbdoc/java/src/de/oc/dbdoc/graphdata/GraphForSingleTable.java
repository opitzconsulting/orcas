package de.oc.dbdoc.graphdata;

import java.util.List;

import de.oc.dbdoc.ant.Stylegroup;
import de.oc.dbdoc.ant.Styles;
import de.oc.dbdoc.schemadata.Table;

public class GraphForSingleTable extends Graph
{
  private Table _singleTable;

  public GraphForSingleTable( Table pSingleTable, List<Graph> pParentGraphs, Styles pStyles )
  {
    super( pParentGraphs, pStyles );

    _singleTable = pSingleTable;
  }

  @Override
  public String getLabel()
  {
    return _singleTable.getName();
  }

  @Override
  public boolean isForSingleTable( Table pTable )
  {
    return pTable == _singleTable;
  }

  @Override
  public boolean containsTableRecursive( Table pTable )
  {
    return isForSingleTable( pTable );
  }

  @Override
  public List<Graph> getSubGraphs()
  {
    return null;
  }

  @Override
  public boolean isSingleTable()
  {
    return true;
  }

  @Override
  public boolean isRenderClusterForSubgraphs()
  {
    return false;
  }

  @Override
  public boolean isCollapseSubgraphs()
  {
    return false;
  }

  @Override
  public String getStyleForGraph()
  {
    return new Stylegroup().getStyleForStylegroup();
  }

  @Override
  public String getDotExecutable()
  {
    return "dot";
  }

  public Table getSingleTable()
  {
    return _singleTable;
  }
}
