package de.oc.dbdoc.graphdata;

import java.util.ArrayList;
import java.util.List;

import de.oc.dbdoc.ant.Styles;
import de.oc.dbdoc.schemadata.Association;
import de.oc.dbdoc.schemadata.Schema;
import de.oc.dbdoc.schemadata.Table;

public abstract class Graph
{
  private List<Graph> _parentGraphs;
  private Styles _styles;

  public Graph( List<Graph> pParentGraphs, Styles pStyles )
  {
    _parentGraphs = pParentGraphs;
    _styles = pStyles;
  }

  public abstract boolean containsTableRecursive( Table pTable );

  public abstract String getLabel();

  public abstract boolean isRenderClusterForSubgraphs();

  public abstract boolean isForSingleTable( Table pTable );

  public abstract List<Graph> getSubGraphs();

  public String getStyleForTable( Table pTable )
  {
    return _styles.getStyleForTable( pTable.getName() );
  }

  public boolean isRoot()
  {
    return _parentGraphs.isEmpty();
  }

  public abstract boolean isSingleTable();

  public Graph getParentGraph()
  {
    return isRoot() ? null : _parentGraphs.get( 0 );
  }

  protected Styles getStyles()
  {
    return _styles;
  }

  public abstract boolean isCollapseSubgraphs();

  public abstract String getStyleForGraph();

  public abstract String getDotExecutable();

  public List<Association> getVisibleAssociation( Table pTable, Schema pSchema, boolean pOutRefsOnly )
  {
    List<Association> lReturn = new ArrayList<Association>();

    for( Association lAssociation : pSchema.getAssociations() )
    {
      if( (lAssociation.getTableFrom().equals( pTable ) && !pOutRefsOnly && containsTableRecursive( lAssociation.getTableTo() ))
          || (lAssociation.getTableTo().equals( pTable ) && containsTableRecursive( lAssociation.getTableFrom() )) )
      {
        lReturn.add( lAssociation );
      }
    }

    return lReturn;
  }

  public boolean allAssociations()
  {
    return true;
  }

  public boolean isAllAncestors()
  {
    return false;
  }

  public boolean isAllDescendants()
  {
    return false;
  }
}
