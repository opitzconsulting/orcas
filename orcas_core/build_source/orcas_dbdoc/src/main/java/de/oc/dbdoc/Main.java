// Copyright (c) 2004 OPITZ CONSULTING GmbH
package de.oc.dbdoc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;

import de.oc.dbdoc.ant.Diagram;
import de.oc.dbdoc.ant.Styles;
import de.oc.dbdoc.ant.Tableregistry;
import de.oc.dbdoc.export.DotExport;
import de.oc.dbdoc.export.DotWriterImpl;
import de.oc.dbdoc.export.PlantUmlWriterImpl;
import de.oc.dbdoc.graphdata.Graph;
import de.oc.dbdoc.graphdata.GraphForDiagram;
import de.oc.dbdoc.graphdata.GraphForSingleTable;
import de.oc.dbdoc.graphdata.GraphForSingleTableAncestors;
import de.oc.dbdoc.graphdata.GraphForSingleTableDescendants;
import de.oc.dbdoc.schemadata.Association;
import de.oc.dbdoc.schemadata.Schema;
import de.oc.dbdoc.schemadata.Table;

/**
 * DOCUMENT ME!
 * 
 * @author FSA
 */
public class Main
{
  private static boolean isPlantuml;

  public static class GraphRef
  {
    private Graph _graph;
    private boolean _isOutref;
    private String _linkLabel;
    private List<GraphRef> _otherLinkedGraphRefs = new ArrayList<Main.GraphRef>();
    private Schema _schema;

    public List<GraphRef> getOtherLinkedGraphRefs()
    {
      return _otherLinkedGraphRefs;
    }

    public GraphRef( Graph pGraph, boolean pIsOutref, Schema pSchema )
    {
      this( pGraph, pIsOutref, pSchema, pGraph.getLabel() );
    }

    public GraphRef( Graph pGraph, boolean pIsOutref, Schema pSchema, String pLinkLabel )
    {
      _graph = pGraph;
      _isOutref = pIsOutref;
      _linkLabel = pLinkLabel;
      _schema = pSchema;
    }

    public Graph getGraph()
    {
      return _graph;
    }

    public void addLinkedGraphRef( GraphRef pGraphRef )
    {
      _otherLinkedGraphRefs.add( pGraphRef );
    }

    public boolean isOutref()
    {
      return _isOutref;
    }

    public GraphRef createForOtherGraph( Graph pGraph )
    {
      return new GraphRef( pGraph, !_isOutref ? false : _hasOutrefVersion( pGraph, _schema ), _schema );
    }

    public String getLinkLabel()
    {
      return _linkLabel;
    }
  }

  public static String readFile( File pFile )
  {
    String lReturn = "";

    try( BufferedReader lBufferedReader = new BufferedReader( new FileReader( pFile ) ) )
    {
      String lLine = null;

      while( (lLine = lBufferedReader.readLine()) != null )
      {
        lReturn += lLine + "\n";
      }

      return lReturn;
    }
    catch( Exception e )
    {
      throw new RuntimeException( e );
    }

  }

  public static void writeDiagramsRecursive( Diagram pRootDiagram, Styles pStyles, Schema pSchema, String pHtmlOutDir, String pDotOutDir, String pTableSourcefileFolder, Tableregistry pTableregistry, boolean pIsSvg, boolean pIsPlantuml )
  {
    isPlantuml = pIsPlantuml;
    try
    {
      GraphForDiagram lRootGraph = new GraphForDiagram( pRootDiagram, pStyles, null, pTableregistry );

      _writeGraphRecursive( lRootGraph, pSchema, pHtmlOutDir, pDotOutDir, pTableSourcefileFolder, pTableregistry, pIsSvg );

      for( Table lTable : pSchema.getTables() )
      {
        Graph lGraphNormal = new GraphForSingleTable( lTable, Collections.singletonList( (Graph) lRootGraph ), pStyles, pTableregistry );

        boolean lHasOutref = _hasOutrefVersion( lGraphNormal, pSchema );

        List<GraphRef> lGraphRefs = new ArrayList<Main.GraphRef>();

        if( lHasOutref )
        {
          lGraphRefs.add( new GraphRef( lGraphNormal, true, pSchema, "only outgoing references" ) );
          lGraphRefs.add( new GraphRef( lGraphNormal, false, pSchema, "all references" ) );
          lGraphRefs.add( new GraphRef( new GraphForSingleTableDescendants( lTable, Collections.singletonList( (Graph) lRootGraph ), pStyles, pTableregistry ), false, pSchema, "all descendants" ) );
        }
        else
        {
          lGraphRefs.add( new GraphRef( lGraphNormal, false, pSchema, "normal view" ) );
        }

        if( _hasInrefVersion( lGraphNormal, pSchema ) )
        {
          lGraphRefs.add( new GraphRef( new GraphForSingleTableAncestors( lTable, Collections.singletonList( (Graph) lRootGraph ), pStyles, pTableregistry ), false, pSchema, "all ancestors" ) );
        }

        for( GraphRef lGraphRef : lGraphRefs )
        {
          for( GraphRef lGraphRefOther : lGraphRefs )
          {
            if( lGraphRef != lGraphRefOther )
            {
              lGraphRef.addLinkedGraphRef( lGraphRefOther );
            }
          }
        }

        for( GraphRef lGraphRef : lGraphRefs )
        {
          _writeSingleGraph( lGraphRef, pSchema, pHtmlOutDir, pDotOutDir, pTableSourcefileFolder, pTableregistry, pIsSvg );
        }
      }
    }
    catch( Exception e )
    {
      e.printStackTrace();

      throw new RuntimeException( e );
    }
  }

  private static void _writeGraphRecursive( Graph pGraph, Schema pSchema, String pHtmlOutDir, String pDotOutDir, String pTableSourcefileFolder, Tableregistry pTableregistry, boolean pIsSvg ) throws Exception
  {
    boolean lHasOutref = _hasOutrefVersion( pGraph, pSchema );

    GraphRef lGraphRefAllref = new GraphRef( pGraph, false, pSchema, "all references" );

    if( lHasOutref )
    {
      GraphRef lGraphRefOutref = new GraphRef( pGraph, true, pSchema, "only outgoing references" );

      lGraphRefAllref.addLinkedGraphRef( lGraphRefOutref );
      lGraphRefOutref.addLinkedGraphRef( lGraphRefAllref );

      _writeSingleGraph( lGraphRefOutref, pSchema, pHtmlOutDir, pDotOutDir, pTableSourcefileFolder, pTableregistry, pIsSvg );
    }

    _writeSingleGraph( lGraphRefAllref, pSchema, pHtmlOutDir, pDotOutDir, pTableSourcefileFolder, pTableregistry, pIsSvg );

    for( Graph lSubGraph : pGraph.getSubGraphs() )
    {
      _writeGraphRecursive( lSubGraph, pSchema, pHtmlOutDir, pDotOutDir, pTableSourcefileFolder, pTableregistry, pIsSvg );
    }
  }

  public static String getNameFromLabel( String pLabel )
  {
    String lReturn = "";

    for( int i = 0; i < pLabel.length(); i++ )
    {
      char lChar = pLabel.charAt( i );

      if( Character.isLetterOrDigit( lChar ) || lChar == '_' )
      {
        lReturn += lChar;
      }
      else
      {
        if( lReturn.length() > 0 && lReturn.charAt( lReturn.length() - 1 ) != '_' )
        {
          lReturn += '_';
        }
      }
    }

    return lReturn.toLowerCase();
  }

  private static void _writeSingleGraph( GraphRef pGraphRef, Schema pSchema, String pHtmlOutDir, String pDotOutDir, String pTableSourcefileFolder, Tableregistry pTableregistry, boolean pIsSvg ) throws Exception
  {
    if(isPlantuml){
      System.out.println( "writing graph: " + getFileNameForGraph( pGraphRef, "plantuml" ) );
      FileWriter lPlantUmlFileWriter = new FileWriter( pHtmlOutDir + "/" + getFileNameForGraph( pGraphRef, "plantuml" ) );
      new DotExport().export( pGraphRef.getGraph(), pSchema, new PlantUmlWriterImpl( lPlantUmlFileWriter ), pGraphRef.isOutref() );
      lPlantUmlFileWriter.close();
    }
    else {
      System.out.println( "writing graph: " + getFileNameForGraph( pGraphRef, pGraphRef.getGraph().getDotExecutable() ) );
      _writeHTML(pHtmlOutDir, pGraphRef, pSchema, pTableSourcefileFolder, pTableregistry, pIsSvg);
      FileWriter lFileWriter = new FileWriter(pDotOutDir + "/" + getFileNameForGraph(pGraphRef, pGraphRef.getGraph().getDotExecutable()));
      new DotExport().export(pGraphRef.getGraph(), pSchema, new DotWriterImpl(lFileWriter), pGraphRef.isOutref());
      lFileWriter.close();
    }
  }

  private static boolean _hasOutrefVersion( Graph pGraph, Schema pSchema )
  {
    if( !pGraph.allAssociations() )
    {
      return false;
    }

    for( Association lAssociation : pSchema.getAssociations() )
    {
      if( !pGraph.containsTableRecursive( lAssociation.getTableFrom() ) )
      {
        if( pGraph.containsTableRecursive( lAssociation.getTableTo() ) )
        {
          return true;
        }
      }
    }

    return false;
  }

  private static boolean _hasInrefVersion( Graph pGraph, Schema pSchema )
  {
    if( !pGraph.allAssociations() )
    {
      return false;
    }

    for( Association lAssociation : pSchema.getAssociations() )
    {
      if( !pGraph.containsTableRecursive( lAssociation.getTableTo() ) )
      {
        if( pGraph.containsTableRecursive( lAssociation.getTableFrom() ) )
        {
          return true;
        }
      }
    }

    return false;
  }

  public static final String TAB_FILE_PREFIX = "tab_";

  public static String getFileNameForName( String pName, boolean pIsOutref, String pExtension )
  {
    return getNameFromLabel( pName ) + (pIsOutref ? "_outref" : "") + "." + pExtension;
  }

  public static String getFileNameForGraph( GraphRef pGraphRef, String pExtension )
  {
    return getFileNameForName( pGraphRef.getGraph().isRoot() ? "index" : (pGraphRef.getGraph().isSingleTable() ? TAB_FILE_PREFIX : "sg_") + pGraphRef.getGraph().getLabel(), pGraphRef.isOutref(), pExtension );
  }

  public static String getHtmlFileNameForGraph( GraphRef pGraphRef )
  {
    return getFileNameForGraph( pGraphRef, "html" );
  }

  public static String _getImgFileNameForGraph( GraphRef pGraphRef, boolean pIsSvg )
  {
    if( pIsSvg )
    {
      return getFileNameForGraph( pGraphRef, "svg" );
    }
    else
    {
      return getFileNameForGraph( pGraphRef, "png" );
    }
  }

  private static String _getHtmlLinkForGraph( GraphRef pGraphRef )
  {
    return "<a href=\"" + getHtmlFileNameForGraph( pGraphRef ) + "\">" + pGraphRef.getLinkLabel() + "</a>";
  }

  private static void _createSubgraphListRecursive( ArrayList<String> pSubGraphLinks, GraphRef pGraphRef, int pLevel )
  {
    if( !pGraphRef.getGraph().isSingleTable() )
    {
      for( Graph lSubGraph : pGraphRef.getGraph().getSubGraphs() )
      {
        if( !lSubGraph.isSingleTable() )
        {
          String lLink = _getHtmlLinkForGraph( pGraphRef.createForOtherGraph( lSubGraph ) );

          for( int i = 0; i < pLevel; i++ )
          {
            if( i == pLevel - 1 )
            {
              lLink = "->" + lLink;
            }
            else
            {
              lLink = "&nbsp;&nbsp;" + lLink;
            }
          }

          pSubGraphLinks.add( lLink );

          _createSubgraphListRecursive( pSubGraphLinks, pGraphRef.createForOtherGraph( lSubGraph ), pLevel + 1 );
        }
      }
    }
  }

  private static void _writeHTML( String pHtmlOutDir, GraphRef pGraphRef, Schema pSchema, String pTableSourcefileFolder, Tableregistry pTableregistry, boolean pIsSvg ) throws Exception
  {
    ArrayList<String> lSubGraphLinks = new ArrayList<String>();
    ArrayList<String> lSubTableLinks = new ArrayList<String>();
    ArrayList<String> lParentGraphLinks = new ArrayList<String>();
    ArrayList<Table> lContainedTables = new ArrayList<Table>();

    _createSubgraphListRecursive( lSubGraphLinks, pGraphRef, 0 );

    for( Table lTable : pSchema.getTables() )
    {
      if( pGraphRef.getGraph().containsTableRecursive( lTable ) )
      {
        lContainedTables.add( lTable );
        lSubTableLinks.add( _getHtmlLinkForGraph( pGraphRef.createForOtherGraph( new GraphForSingleTable( lTable, Collections.singletonList( pGraphRef.getGraph() ), null, pTableregistry ) ) ) );
      }
    }

    // Wurzelgraphnamen in Link umwandeln
    String lParentGraphLink;

    if( pGraphRef.getGraph().isRoot() || pGraphRef.getGraph().isSingleTable() )
    {
      lParentGraphLink = null;

      if( pGraphRef.getGraph().isSingleTable() )
      {
        _getHtmlLinksForDiagramsContainingTables( lParentGraphLinks, pGraphRef.createForOtherGraph( pGraphRef.getGraph().getParentGraph() ), lContainedTables.get( 0 ) );
        lSubTableLinks.clear();
      }
    }
    else
    {
      lParentGraphLink = "";
      Graph lParent = pGraphRef.getGraph().getParentGraph();

      do
      {
        lParentGraphLink = _getHtmlLinkForGraph( pGraphRef.createForOtherGraph( lParent ) ) + (lParentGraphLink.length() == 0 ? "" : " -> " + lParentGraphLink);

        if( lParent.isRoot() )
        {
          break;
        }

        lParent = lParent.getParentGraph();
      } while( true );
    }

    String lGraphChangeLink = "";

    for( GraphRef lGraphRef : pGraphRef.getOtherLinkedGraphRefs() )
    {
      lGraphChangeLink += " " + _getHtmlLinkForGraph( lGraphRef );
    }

    lGraphChangeLink = lGraphChangeLink.trim();

    if( lGraphChangeLink.length() == 0 )
    {
      lGraphChangeLink = null;
    }

    String lTableSource = null;

    if( pGraphRef.getGraph().isSingleTable() && pTableSourcefileFolder != null && pTableSourcefileFolder.length() > 0 )
    {
      File lFile = new File( pTableSourcefileFolder + "/" + pGraphRef.getGraph().getLabel().toLowerCase() + ".sql" );

      if( lFile.exists() )
      {
        lTableSource = readFile( lFile );
      }
    }

    VelocityEngine lVelocityEngine = new VelocityEngine();

    lVelocityEngine.init();
    Template lTemplate = Velocity.getTemplate( "graphtemplate.vm" );

    VelocityContext lVelocityContext = new VelocityContext();
    lVelocityContext.put( "pImageName", _getImgFileNameForGraph( pGraphRef, pIsSvg ) );
    if( !pIsSvg )
    {
      lVelocityContext.put( "pSvgName", getFileNameForGraph( pGraphRef, "svg" ) );
    }
    lVelocityContext.put( "pGraphChangeLink", lGraphChangeLink );
    lVelocityContext.put( "pTitle", pGraphRef.getGraph().getLabel() );
    lVelocityContext.put( "pParentGraphLink", lParentGraphLink );
    lVelocityContext.put( "pParentGraphLinks", lParentGraphLinks.isEmpty() ? null : lParentGraphLinks );
    lVelocityContext.put( "pSubGraphLinks", lSubGraphLinks.isEmpty() ? null : lSubGraphLinks );
    lVelocityContext.put( "pSubTableLinks", lSubTableLinks.isEmpty() ? null : lSubTableLinks );
    lVelocityContext.put( "pTableSource", lTableSource );
    FileWriter lFileWriter = new FileWriter( pHtmlOutDir + "/" + getHtmlFileNameForGraph( pGraphRef ) );
    lTemplate.merge( lVelocityContext, lFileWriter );
    lFileWriter.close();
  }

  private static void _getHtmlLinksForDiagramsContainingTables( ArrayList<String> pDiagramGraphLinks, GraphRef pGraphRef, Table pTable )
  {
    if( pGraphRef.getGraph().containsTableRecursive( pTable ) )
    {
      pDiagramGraphLinks.add( _getHtmlLinkForGraph( pGraphRef ) );
    }

    for( Graph lGraph : pGraphRef.getGraph().getSubGraphs() )
    {
      _getHtmlLinksForDiagramsContainingTables( pDiagramGraphLinks, pGraphRef.createForOtherGraph( lGraph ), pTable );
    }
  }

  public static void log( String pString )
  {
    System.out.println( pString );
  }
}
