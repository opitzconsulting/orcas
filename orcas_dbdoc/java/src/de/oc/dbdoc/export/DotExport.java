// Copyright (c) 2004 OPITZ CONSULTING GmbH
package de.oc.dbdoc.export;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.oc.dbdoc.graphdata.Graph;
import de.oc.dbdoc.schemadata.Association;
import de.oc.dbdoc.schemadata.Column;
import de.oc.dbdoc.schemadata.Schema;
import de.oc.dbdoc.schemadata.Table;

/**
 * DOCUMENT ME!
 * 
 * @author FSA
 */
public class DotExport
{
  public DotExport()
  {
  }

  private void _writeSubgraphRecursiveCollapsed( Graph pGraph, Schema pSchema, DotWriter pPrintWriter, boolean pOutRefsOnly )
  {
    for( Graph lGraph : pGraph.getSubGraphs() )
    {
      if( lGraph.getSubGraphs().isEmpty() )
      {
        Map<String,Integer> lStyleToCountMap = new HashMap<String,Integer>();

        for( Table lTable : pSchema.getTables() )
        {
          if( lGraph.containsTableRecursive( lTable ) )
          {
            String lStyle = lGraph.getStyleForTable( lTable );

            if( lStyleToCountMap.containsKey( lStyle ) )
            {
              lStyleToCountMap.put( lStyle, lStyleToCountMap.get( lStyle ) + 1 );
            }
            else
            {
              lStyleToCountMap.put( lStyle, 1 );
            }
          }
        }

        String lCommonStyle = null;

        if( !lStyleToCountMap.isEmpty() )
        {
          int lCommonStyleCount = 0;

          for( String lStyleKey : lStyleToCountMap.keySet() )
          {
            if( lStyleToCountMap.get( lStyleKey ) > lCommonStyleCount )
            {
              lCommonStyle = lStyleKey;
              lCommonStyleCount = lStyleToCountMap.get( lStyleKey );
            }
          }
        }

        pPrintWriter.printGraph( lGraph, lCommonStyle, pOutRefsOnly );
      }
      else
      {
        pPrintWriter.printSubGraphStartFilled( lGraph, pOutRefsOnly );
        _writeSubgraphRecursiveCollapsed( lGraph, pSchema, pPrintWriter, pOutRefsOnly );
        pPrintWriter.printSubGraphEnd();
      }
    }
  }

  public class GraphAssociation
  {
    private Graph _graphFrom;
    private Graph _graphTo;
    private int _countTableFks;
    public boolean _bidirectional;

    public Graph getGraphFrom()
    {
      return _graphFrom;
    }

    public Graph getGraphTo()
    {
      return _graphTo;
    }

    public int getCountTableFks()
    {
      return _countTableFks;
    }

    public boolean isBidirectional()
    {
      return _bidirectional;
    }
  }

  private void _printGraphAssociation( GraphAssociation pGraphAssociation, DotWriter pPrintWriter )
  {
    pPrintWriter.printGraphAssociation( pGraphAssociation );
  }

  private List<Graph> _getEndGraphListRecursive( Graph pGraph )
  {
    if( pGraph.getSubGraphs().isEmpty() )
    {
      return Collections.singletonList( pGraph );
    }
    else
    {
      List<Graph> lReturn = new ArrayList<Graph>();

      for( Graph lGraph : pGraph.getSubGraphs() )
      {
        lReturn.addAll( _getEndGraphListRecursive( lGraph ) );
      }

      return lReturn;
    }
  }

  private List<GraphAssociation> _filterGraphAssociations( Collection<GraphAssociation> pGraphAssociations )
  {
    List<GraphAssociation> lReturn = new ArrayList<GraphAssociation>( pGraphAssociations );

    for( GraphAssociation lOuterGraphAssociation : pGraphAssociations )
    {
      for( GraphAssociation lInnerGraphAssociation : pGraphAssociations )
      {
        if( lOuterGraphAssociation._graphFrom == lInnerGraphAssociation._graphTo && lOuterGraphAssociation._graphTo == lInnerGraphAssociation._graphFrom )
        {
          if( lOuterGraphAssociation._countTableFks > lInnerGraphAssociation._countTableFks )
          {
            lReturn.remove( lInnerGraphAssociation );
            lOuterGraphAssociation._bidirectional = true;
          }
          else
          {
            lReturn.remove( lOuterGraphAssociation );
            lInnerGraphAssociation._bidirectional = true;
          }
        }
      }
    }

    return lReturn;
  }

  /**
   * Creates a dot File for the Schema.
   */
  public void export( Graph pGraph, Schema pSchema, DotWriter pDotWriter, boolean pOutRefsOnly )
  {
    pDotWriter.printHeaderStart( pGraph.getStyleForGraph() );

    if( pGraph.isCollapseSubgraphs() )
    {
      _writeSubgraphRecursiveCollapsed( pGraph, pSchema, pDotWriter, pOutRefsOnly );

      List<Graph> lEndGraphs = _getEndGraphListRecursive( pGraph );

      List<GraphAssociation> lGraphAssociations = new ArrayList<GraphAssociation>();

      for( Graph lGraphFrom : lEndGraphs )
      {
        for( Graph lGraphTo : lEndGraphs )
        {
          GraphAssociation lGraphAssociation = new GraphAssociation();
          lGraphAssociation._graphFrom = lGraphFrom;
          lGraphAssociation._graphTo = lGraphTo;

          for( Table lTableFrom : pSchema.getTables() )
          {
            if( lGraphFrom.containsTableRecursive( lTableFrom ) )
            {
              for( Table lTableTo : pSchema.getTables() )
              {
                if( lGraphTo.containsTableRecursive( lTableTo ) )
                {
                  for( Association lAssociation : pSchema.getAssociations() )
                  {
                    if( lAssociation.getTableFrom() == lTableFrom && lAssociation.getTableTo() == lTableTo )
                    {
                      lGraphAssociation._countTableFks += 1;
                    }
                  }
                }
              }
            }
          }

          if( lGraphAssociation._countTableFks > 0 )
          {
            lGraphAssociations.add( lGraphAssociation );
          }
        }
      }

      for( GraphAssociation lGraphAssociation : _filterGraphAssociations( lGraphAssociations ) )
      {
        _printGraphAssociation( lGraphAssociation, pDotWriter );
      }
    }
    else
    {
      List<Association> lExtraAssociations = new ArrayList<Association>();
      boolean lOtherTablesFound = false;

      for( Table lTable : pSchema.getTables() )
      {
        if( !pGraph.containsTableRecursive( lTable ) )
        {
          List<Association> lVisibleAssociation = pGraph.getVisibleAssociation( lTable, pSchema, pOutRefsOnly );

          if( !lVisibleAssociation.isEmpty() )
          {
            lExtraAssociations.addAll( lVisibleAssociation );
            lOtherTablesFound = true;
            _printTable( lTable, pDotWriter, lVisibleAssociation, pOutRefsOnly, pGraph.getStyleForTable( lTable ) );
          }
        }
      }

      _writeSubgraphRecursive( pGraph, pSchema, pDotWriter, pOutRefsOnly, new ArrayList<Table>(), lOtherTablesFound, pGraph.isRenderClusterForSubgraphs() );

      for( Association lAssociation : pSchema.getAssociations() )
      {
        if( lExtraAssociations.contains( lAssociation )
            || (pGraph.allAssociations() && (pGraph.containsTableRecursive( lAssociation.getTableFrom() ) || (!pOutRefsOnly && pGraph.containsTableRecursive( lAssociation.getTableTo() )))) )
        {
          _printAssociation( lAssociation, pDotWriter, pGraph );
        }
      }
    }

    pDotWriter.printHeaderEnd();
  }

  private void _writeSubgraphRecursive( Graph pGraph, Schema pSchema, DotWriter pPrintWriter, boolean pOutRefsOnly, List<Table> pOutTablesRendered, boolean pRenderSubgraph,
      boolean pRenderSubgraphsRecursive )
  {
    boolean lRenderSubgraph = pRenderSubgraph || pRenderSubgraphsRecursive;

    if( lRenderSubgraph )
    {
      pPrintWriter.printSubGraphStartDashed( pGraph, pOutRefsOnly );
    }

    if( !pGraph.isSingleTable() )
    {
      for( Graph lSubGraph : pGraph.getSubGraphs() )
      {
        _writeSubgraphRecursive( lSubGraph, pSchema, pPrintWriter, pOutRefsOnly, pOutTablesRendered, false, pRenderSubgraphsRecursive );
      }
    }

    for( Table lTable : pSchema.getTables() )
    {
      if( pGraph.containsTableRecursive( lTable ) && !pOutTablesRendered.contains( lTable ) )
      {
        pOutTablesRendered.add( lTable );

        _printTable( lTable, pPrintWriter, null, pOutRefsOnly, pGraph.getStyleForTable( lTable ) );
      }
    }

    if( lRenderSubgraph )
    {
      pPrintWriter.printSubGraphEnd();
    }
  }

  private List<Column> _filterColumnsByAssociations( Table pTable, List<Association> pVisibleAssociation )
  {
    List<Column> lReturn = new ArrayList<Column>();

    for( Column lColumn : pTable.getColumns() )
    {
      if( _isColumnsInAssociations( lColumn, pTable, pVisibleAssociation ) )
      {
        lReturn.add( lColumn );
      }
    }

    return lReturn;
  }

  private boolean _isColumnsInAssociations( Column pColumn, Table pTable, List<Association> pVisibleAssociation )
  {
    if( pVisibleAssociation == null )
    {
      return true;
    }

    for( Association lAssociation : pVisibleAssociation )
    {
      if( lAssociation.getTableFrom().equals( pTable ) && lAssociation.isFromColumn( pColumn ) )
      {
        return true;
      }
      if( lAssociation.getTableTo().equals( pTable ) && lAssociation.isToColumn( pColumn ) )
      {
        return true;
      }
    }

    return false;
  }

  private void _printTable( Table pTable, DotWriter pPrintWriter, List<Association> pVisibleAssociation, boolean pIsOutref, String pStyle )
  {
    pPrintWriter.printTable( pTable, pVisibleAssociation, pIsOutref, pStyle, _filterColumnsByAssociations( pTable, pVisibleAssociation ) );
  }

  private void _printAssociation( Association pAssociation, DotWriter pPrintWriter, Graph pGraph )
  {
    pPrintWriter.printAssociation( pAssociation, pGraph );
  }
}
