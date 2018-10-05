package de.oc.dbdoc.export;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

import de.oc.dbdoc.Main;
import de.oc.dbdoc.Main.GraphRef;
import de.oc.dbdoc.graphdata.Graph;
import de.oc.dbdoc.schemadata.Association;
import de.oc.dbdoc.schemadata.Column;
import de.oc.dbdoc.schemadata.Table;

public class DotWriterImpl implements DotWriter
{
  private PrintWriter lPrintWriter;

  public DotWriterImpl( Writer pWriter )
  {
    lPrintWriter = new PrintWriter( pWriter );
  }

  private void println( String pString )
  {
    lPrintWriter.println( pString );
  }

  private void print( String pString )
  {
    lPrintWriter.print( pString );
  }

  public void printHeaderStart( String pStyleForGraph )
  {
    println( "digraph G {" );

    println( "graph [" + pStyleForGraph + "];" );
    println( "edge [fontname=\"Sans\",fontsize=10,labelfontname=\"Sans\",labelfontsize=10];" );
    println( "node [fontname=\"Sans\",fontsize=10,shape=record,style=filled];" );
  }

  public void printHeaderEnd()
  {
    println( "}" );
  }

  public void printGraph( Graph pGraph, String pCommonStyle, boolean pIsOutref )
  {
    print( Main.getNameFromLabel( pGraph.getLabel() ) );
    print( " [" );
    print( "style=\"filled\"," );
    if( pCommonStyle != null && !pCommonStyle.trim().isEmpty() )
    {
      print( pCommonStyle );
      print( ", " );
    }
    print( "URL=\"" + Main.getHtmlFileNameForGraph( new GraphRef( pGraph, pIsOutref, null ) ) + "\"," );
    print( "label=\"" );
    print( pGraph.getLabel() );
    println( "\"]" );
  }

  public void printSubGraphStartFilled( Graph pGraph, boolean pOutRefsOnly )
  {
    printSubGraphStart( pGraph, "filled", pOutRefsOnly );
  }

  private void printSubGraphStart( Graph pGraph, String pStyle, boolean pIsOutref )
  {
    println( "subgraph cluster_" + Main.getNameFromLabel( pGraph.getLabel() ) + " { label=\"" + pGraph.getLabel() + "\" style=\"" + pStyle + "\" fontsize=\"20\" URL=\"" + Main.getHtmlFileNameForGraph( new GraphRef( pGraph, pIsOutref, null ) ) + "\"" );
  }

  public void printSubGraphEnd()
  {
    println( "}" );
  }

  public void printSubGraphStartDashed( Graph pGraph, boolean pOutRefsOnly )
  {
    printSubGraphStart( pGraph, "dashed", pOutRefsOnly );
  }

  public void printGraphAssociation( DotExport.GraphAssociation pGraphAssociation )
  {
    print( Main.getNameFromLabel( pGraphAssociation.getGraphTo().getLabel() ) );
    print( " -> " );
    print( Main.getNameFromLabel( pGraphAssociation.getGraphFrom().getLabel() ) );
    print( " [label=\"\", taillabel=\"\", headlabel=\"\", style=\"dashed\"" );
    if( pGraphAssociation._bidirectional )
    {
      print( ",dir=both" );
      print( ",arrowhead=open" );
      print( ",arrowtail=open" );
    }
    else
    {
      print( ",dir=back" );
      print( ",arrowhead=none" );
      print( ",arrowtail=open" );
    }
    println( "]" );
  }

  public void printTable( Table pTable, List<Association> pVisibleAssociation, boolean pIsOutref, String pStyle, List<Column> pFilteredColumns )
  {
    print( pTable.getName() );
    print( " [" );
    if( pStyle != null && pStyle.length() != 0 )
    {
      print( pStyle );
      print( ", " );
    }
    print( "URL=\"" + Main.getFileNameForName( Main.TAB_FILE_PREFIX + pTable.getName(), pIsOutref, "html" ) + "\"," );
    print( "tooltip=\"" + pTable.getName() + "\"," );
    print( "label=\"{" );
    print( pTable.getName() );
    print( "\\l|" );

    boolean lColumnsFiltered = pFilteredColumns.size() != pTable.getColumns().size();

    Iterator<Column> lColumnIterator = pFilteredColumns.iterator();
    while( lColumnIterator.hasNext() )
    {
      Column lColumn = lColumnIterator.next();

      print( lColumn.getColumnName() );

      if( lColumnIterator.hasNext() || lColumnsFiltered )
      {
        print( "," );
      }

      print( "\\l" );
    }

    if( lColumnsFiltered )
    {
      print( "...\\l" );
    }

    println( "}\"]" );
  }

  public void printAssociation( Association pAssociation, Graph pGraph )
  {
    print( pAssociation.getTableTo().getName() );
    print( " -> " );
    print( pAssociation.getTableFrom().getName() );
    print( " [label=\"" );
    if( pAssociation.getAssociationName().length() <= 20 )
    {
      print( pAssociation.getAssociationName() );
    }
    print( "\", taillabel=\"" );
    print( pAssociation.getMultiplicityTextTo() );
    print( "\", headlabel=\"" );
    print( pAssociation.getMultiplicityTextFrom() );
    print( "\"" );
    print( ",dir=" );
    print( pAssociation.isDirected() ? "back" : "none" );
    print( ",arrowhead=none" );
    print( ",arrowtail=none" );
    if( !pAssociation.isDirected() )
    {
      // pPrintWriter.print( ",constraint=false" );
    }
    println( "]" );
  }
}
