package de.oc.dbdoc.export;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.oc.dbdoc.Main;
import de.oc.dbdoc.graphdata.Graph;
import de.oc.dbdoc.schemadata.Association;
import de.oc.dbdoc.schemadata.Column;
import de.oc.dbdoc.schemadata.Table;

public class PlantUmlWriterImpl implements DotWriter
{
  private PrintWriter lPrintWriter;

  public PlantUmlWriterImpl( Writer pWriter )
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
    println( "@startuml" );
  }

  public void printHeaderEnd()
  {
    println( "@enduml" );
  }

  public void printGraph( Graph pGraph, String pCommonStyle, boolean pIsOutref )
  {
    println( "package \""+pGraph.getLabel()+"\" {" );
    println("}" );
  }

  public void printSubGraphStartFilled( Graph pGraph, boolean pOutRefsOnly )
  {
    printSubGraphStart( pGraph, "filled", pOutRefsOnly );
  }

  private void printSubGraphStart( Graph pGraph, String pStyle, boolean pIsOutref )
  {
    print( "package \""+pGraph.getLabel()+"\" " );
    Map<String, String> lStyleMap = parseStyle(pGraph.getStyleForGraph());
    if(lStyleMap.containsKey("package")){
        print( lStyleMap.get("package") + " " );
    }
    println( "{" );
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
      if( pGraphAssociation._bidirectional )
      {
          print( " - " );
      }
      else
      {
          print( " <-- " );
      }
    println( Main.getNameFromLabel( pGraphAssociation.getGraphFrom().getLabel() ) );
    /*print( " [label=\"\", taillabel=\"\", headlabel=\"\", style=\"dashed\"" );*/
  }

  private Map<String,String> parseStyle(String pStyle){
      Map<String,String> lReturn = new HashMap<>();

      String lLastKey = null;
    String lLastValue = null;

    String[] lStyles = pStyle.split(",");
    for( int i=0;i<lStyles.length;i++ ) {
        String lStyle =lStyles[i];
        String lName ;
        String lValue;

        if(lStyle.contains("=")) {
          lName = lStyle.split("=")[0];
          lValue = lStyle.substring(lName.length() + 1, lStyle.length());
          lLastValue = lValue;
        }
        else {
          lName = lLastKey;
          lValue = lLastValue +","+ lStyle;
        }
        lLastKey = lName;
        lReturn.put(lName,lValue.substring(1,lValue.length()-1));
      }

      return lReturn;
  }

  public void printTable( Table pTable, List<Association> pVisibleAssociation, boolean pIsOutref, String pStyle, List<Column> pFilteredColumns )
  {
    print("class "+ pTable.getName() );
      Map<String, String> lStyleMap = parseStyle(pStyle);
      if(lStyleMap.containsKey("class")){
          print( " " + lStyleMap.get("class") );
      }

      println( " {" );

    boolean lColumnsFiltered = pFilteredColumns.size() != pTable.getColumns().size();

    Iterator<Column> lColumnIterator = pFilteredColumns.iterator();
    while( lColumnIterator.hasNext() )
    {
      Column lColumn = lColumnIterator.next();

      print( lColumn.getColumnType() );
      print( " " );
      println( lColumn.getColumnName() );
    }

    if( lColumnsFiltered )
    {
      println( "\"...\"" );
    }

    println( "}" );
  }

  public void printAssociation( Association pAssociation, Graph pGraph ) {
    print(pAssociation.getTableTo().getName());
    if (pAssociation.getMultiplicityTextTo().length() > 0) {
      print(" \"" + pAssociation.getMultiplicityTextTo() + "\" ");
    }
    if(pAssociation.isDirected()) {
      print(" <-- ");
    } else {
      print(" - ");
    }
    if (pAssociation.getMultiplicityTextFrom().length() > 0){
      print(" \"" + pAssociation.getMultiplicityTextFrom() + "\" ");
    }
    print( pAssociation.getTableFrom().getName() );
    if( pAssociation.getAssociationName().length() <= 20 )
    {
      print( " : "+  pAssociation.getAssociationName() );
    }
    println("");
  }
}
