package de.oc.dbdoc.export;

import java.util.List;

import de.oc.dbdoc.graphdata.Graph;
import de.oc.dbdoc.schemadata.Association;
import de.oc.dbdoc.schemadata.Column;
import de.oc.dbdoc.schemadata.Table;

public interface DotWriter
{
  public void printHeaderStart( String pStyleForGraph );

  public void printHeaderEnd();

  public void printGraph( Graph pGraph, String pCommonStyle, boolean pOutRefsOnly );

  public void printSubGraphStartFilled( Graph pGraph, boolean pOutRefsOnly );

  public void printSubGraphEnd();

  public void printSubGraphStartDashed( Graph pGraph, boolean pOutRefsOnly );

  public void printGraphAssociation( DotExport.GraphAssociation pGraphAssociation );

  public void printTable( Table pTable, List<Association> pVisibleAssociation, boolean pIsOutref, String pStyle, List<Column> pFilteredColumns );

  public void printAssociation( Association pAssociation, Graph pGraph );
}
