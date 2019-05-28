package de.oc.dbdoc.test;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.oc.dbdoc.ant.Diagram;
import de.oc.dbdoc.ant.Include;
import de.oc.dbdoc.ant.OrcasDbDoc;
import de.oc.dbdoc.ant.Styles;
import de.oc.dbdoc.ant.Tablegroup;
import de.oc.dbdoc.export.DotExport;
import de.oc.dbdoc.export.DotExport.GraphAssociation;
import de.oc.dbdoc.export.DotWriter;
import de.oc.dbdoc.export.DotWriterImpl;
import de.oc.dbdoc.graphdata.Graph;
import de.oc.dbdoc.graphdata.GraphForDiagram;
import de.oc.dbdoc.graphdata.GraphForSingleTable;
import de.oc.dbdoc.graphdata.GraphForSingleTableAncestors;
import de.oc.dbdoc.graphdata.GraphForSingleTableDescendants;
import de.oc.dbdoc.schemadata.Association;
import de.oc.dbdoc.schemadata.Column;
import de.oc.dbdoc.schemadata.Schema;
import de.oc.dbdoc.schemadata.Table;

public class TestGraphPrinting extends BaseTableRegistrySetup
{
  private static final String COLUMN_NAME_3 = "C3";
  private static final String COLUMN_NAME_ID = "C_ID";
  private static final String COLUMN_NAME_REF = "C_REF";

  private enum Mode
  {
    ALL_ANCESTORS, NORMAL, ALL_DESCENDANTS
  }

  private Table _table1;
  private Table _table2;
  private Table _table3;
  private Table _table5;
  private Schema _schema;
  private Styles _styles;
  private DotWriterTestImpl _dotWriterTestImpl;
  private Association _association12;
  private Association _association23;
  private OrcasDbDoc _orcasDbDoc;
  private Association _association35;

  private Table createTable( String pTableName )
  {
    Table lTable = new Table( pTableName );
    lTable.addColumn( new Column( COLUMN_NAME_ID, "type" ) );
    lTable.addColumn( new Column( COLUMN_NAME_REF, "type" ) );
    lTable.addColumn( new Column( COLUMN_NAME_3, "type" ) );

    _schema.addTable( lTable );

    return lTable;
  }

  @Before
  public void setup()
  {
    _schema = new Schema();

    _table1 = createTable( "T1" );
    _table2 = createTable( "T2" );
    _table3 = createTable( "T3" );
    createTable( "T4" );
    _table5 = createTable( "T5" );

    _association12 = createAssociation( _table1, _table2 );
    _association23 = createAssociation( _table2, _table3 );
    _association35 = createAssociation( _table3, _table5 );

    _orcasDbDoc = new OrcasDbDoc();
    _styles = _orcasDbDoc.createStyles();
    _styles.createTables();
    _styles.createDiagrams();

    _dotWriterTestImpl = new DotWriterTestImpl();
  }

  private Association createAssociation( Table pTableFrom, Table pTableTo )
  {
    Association lAssociation = new Association( pTableFrom.getName() + "_TO_" + pTableTo.getName(), pTableFrom, pTableTo, true, 0, Association.MULTIPLICITY_N, 1, 1 );
    lAssociation.addColumnFrom( COLUMN_NAME_REF );
    lAssociation.addColumnTo( COLUMN_NAME_ID );

    _schema.addAssociation( lAssociation );

    return lAssociation;
  }

  @Test
  public void testSingleTable1()
  {
    expectTableFull( _table1 );
    expectTableRefonly( _table2, COLUMN_NAME_ID );
    expectAssociation( _association12 );

    runExporSingleTable( _table1, false );
  }

  @Test
  public void testSingleTable1WithAllAncestors()
  {
    expectTableFull( _table1 );
    expectTableRefonly( _table2, COLUMN_NAME_ID, COLUMN_NAME_REF );
    expectTableRefonly( _table3, COLUMN_NAME_ID, COLUMN_NAME_REF );
    expectTableRefonly( _table5, COLUMN_NAME_ID );
    expectAssociation( _association12 );
    expectAssociation( _association23 );
    expectAssociation( _association35 );

    runExporSingleTable( _table1, false, Mode.ALL_ANCESTORS );
  }

  @Test
  public void testSingleTable2WithAllAncestors()
  {
    expectTableFull( _table2 );
    expectTableRefonly( _table3, COLUMN_NAME_ID, COLUMN_NAME_REF );
    expectTableRefonly( _table5, COLUMN_NAME_ID );
    expectAssociation( _association23 );
    expectAssociation( _association35 );

    runExporSingleTable( _table2, false, Mode.ALL_ANCESTORS );
  }

  @Test
  public void testSingleTable3WithAllDescendants()
  {
    expectTableFull( _table3 );
    expectTableRefonly( _table2, COLUMN_NAME_ID, COLUMN_NAME_REF );
    expectTableRefonly( _table1, COLUMN_NAME_REF );
    expectAssociation( _association12 );
    expectAssociation( _association23 );

    runExporSingleTable( _table3, false, Mode.ALL_DESCENDANTS );
  }

  @Test
  public void testSingleTable1OutrefOnly()
  {
    expectTableFull( _table1 );
    expectTableRefonly( _table2, COLUMN_NAME_ID );
    expectAssociation( _association12 );

    runExporSingleTable( _table1, true );
  }

  @Test
  public void testSingleTable2()
  {
    expectTableFull( _table2 );
    expectTableRefonly( _table1, COLUMN_NAME_REF );
    expectTableRefonly( _table3, COLUMN_NAME_ID );
    expectAssociation( _association12 );
    expectAssociation( _association23 );

    runExporSingleTable( _table2, false );
  }

  @Test
  public void testSingleTable2OutrefOnly()
  {
    expectTableFull( _table2 );
    expectTableRefonly( _table3, COLUMN_NAME_ID );
    expectAssociation( _association23 );

    runExporSingleTable( _table2, true );
  }

  @Test
  public void testGraphTable1Table3()
  {
    Diagram lDiagram = new Diagram();
    lDiagram.setLabel( "D1" );
    lDiagram.setTablegroup( createTableGroup( _table1, _table3 ) );

    expectTableFull( _table1 );
    expectTableFull( _table3 );
    expectTableRefonly( _table2, COLUMN_NAME_ID, COLUMN_NAME_REF );
    expectTableRefonly( _table5, COLUMN_NAME_ID );
    expectAssociation( _association12 );
    expectAssociation( _association23 );
    expectAssociation( _association35 );

    runExportForDiagram( lDiagram, false );
  }

  @Test
  public void testGraphTable1Table3OutrefOnly()
  {
    Diagram lDiagram = new Diagram();
    lDiagram.setLabel( "D1" );
    lDiagram.setTablegroup( createTableGroup( _table1, _table3 ) );

    expectTableFull( _table1 );
    expectTableFull( _table3 );
    expectTableRefonly( _table2, COLUMN_NAME_ID );
    expectTableRefonly( _table5, COLUMN_NAME_ID );
    expectAssociation( _association12 );
    expectAssociation( _association35 );

    runExportForDiagram( lDiagram, true );
  }

  @Test
  public void testGraphTable1Table2()
  {
    Diagram lDiagram = new Diagram();
    lDiagram.setLabel( "D1" );
    lDiagram.setTablegroup( createTableGroup( _table1, _table2 ) );

    expectTableFull( _table1 );
    expectTableFull( _table2 );
    expectTableRefonly( _table3, COLUMN_NAME_ID );
    expectAssociation( _association12 );
    expectAssociation( _association23 );

    runExportForDiagram( lDiagram, false );
  }

  private String createTableGroup( Table... pTables )
  {
    Tablegroup lTablegroup = _orcasDbDoc.createTableregistry().createTablegroup();

    String lName = "tg1";
    lTablegroup.setName( lName );

    for( Table lTable : pTables )
    {
      Include lInclude = lTablegroup.createInclude();
      lInclude.setName( lTable.getName() );
    }

    return lName;
  }

  private void runExporSingleTable( Table pTable, boolean pOutRefsOnly, Mode pAllAncestors )
  {
    GraphForSingleTable lGraphForSingleTable;

    switch( pAllAncestors )
    {
      case ALL_ANCESTORS:
        lGraphForSingleTable = new GraphForSingleTableAncestors( pTable, new ArrayList<Graph>(), _styles, tableregistry );
        break;
      case ALL_DESCENDANTS:
        lGraphForSingleTable = new GraphForSingleTableDescendants( pTable, new ArrayList<Graph>(), _styles, tableregistry );
        break;
      case NORMAL:
        lGraphForSingleTable = new GraphForSingleTable( pTable, new ArrayList<Graph>(), _styles, tableregistry );
        break;
      default:
        throw new RuntimeException();
    }

    runExport( lGraphForSingleTable, pOutRefsOnly );
  }

  private void runExporSingleTable( Table pTable, boolean pOutRefsOnly )
  {
    runExporSingleTable( pTable, pOutRefsOnly, Mode.NORMAL );
  }

  private void runExportForDiagram( Diagram pDiagram, boolean pOutRefsOnly )
  {
    runExport( new GraphForDiagram( pDiagram, _styles, null, tableregistry ), pOutRefsOnly );
  }

  private void runExport( Graph pGraph, boolean pOutRefsOnly )
  {
    new DotExport().export( pGraph, _schema, _dotWriterTestImpl, pOutRefsOnly );

    StringWriter lStringWriter = new StringWriter();
    new DotExport().export( pGraph, _schema, new DotWriterImpl( lStringWriter ), pOutRefsOnly );

    System.out.println( lStringWriter );

    _dotWriterTestImpl.assertExpectedResults();
  }

  public void expectTableRefonly( Table pTable, String... pColumns )
  {
    _dotWriterTestImpl.expectTableRefonly( pTable, pColumns );
  }

  public void expectTableFull( Table pTable )
  {
    _dotWriterTestImpl.expectTableFull( pTable );
  }

  public void expectAssociation( Association pAssociation )
  {
    _dotWriterTestImpl.expectAssociation( pAssociation );
  }

  private class DotWriterTestImpl implements DotWriter
  {
    private List<Table> _tablesFull = new ArrayList();
    private List<Table> _tablesRefonly = new ArrayList();
    private Map<Table,String[]> _tablesRefonlyColumns = new HashMap();
    private List<Association> _associations = new ArrayList();

    private List<Table> _expectedTablesFull = new ArrayList();
    private List<Table> _expectedTablesRefonly = new ArrayList();
    private Map<Table,String[]> _expectedTablesRefonlyColumns = new HashMap();
    private List<Association> _expectedAssociations = new ArrayList();

    public void expectTableRefonly( Table pTable, String... pColumns )
    {
      _expectedTablesRefonly.add( pTable );
      _expectedTablesRefonlyColumns.put( pTable, pColumns );
    }

    public void expectTableFull( Table pTable )
    {
      _expectedTablesFull.add( pTable );
    }

    public void expectAssociation( Association pAssociation )
    {
      _expectedAssociations.add( pAssociation );
    }

    public void assertExpectedResults()
    {
      Assert.assertEquals( buildCompareString( _expectedTablesFull, _expectedTablesRefonly, _expectedTablesRefonlyColumns, _expectedAssociations ),
          buildCompareString( _tablesFull, _tablesRefonly, _tablesRefonlyColumns, _associations ) );

    }

    private String buildCompareString( List<Table> pTablesFull, List<Table> pTablesRefonly, Map<Table,String[]> pTablesRefonlyColumns, List<Association> pAssociations )
    {
      String lCompare = "";
      lCompare += getTablesString( "full", pTablesFull, null );
      lCompare += getTablesString( "refonly", pTablesRefonly, pTablesRefonlyColumns );

      List<Table> lAllTables = new ArrayList( _schema.getTables() );
      lAllTables.removeAll( pTablesFull );
      lAllTables.removeAll( pTablesRefonly );

      lCompare += getTablesString( "none", lAllTables, null );
      lCompare += getAssociationString( "associations", pAssociations );

      List<Association> lAllAssociations = new ArrayList( _schema.getAssociations() );
      lAllAssociations.removeAll( pAssociations );

      lCompare += getAssociationString( "none-associations", lAllAssociations );

      return lCompare;
    }

    private String getTablesString( String pString, List<Table> pTables, Map<Table,String[]> pTablesColumns )
    {
      List<String> lTableNames = new ArrayList<String>();
      Map<String,Table> lTableToNAmeMap = new HashMap();

      for( Table lTable : pTables )
      {
        lTableNames.add( lTable.getName() );
        lTableToNAmeMap.put( lTable.getName(), lTable );
      }

      Collections.sort( lTableNames );

      String lReturn = pString + ": ";

      for( String lTableName : lTableNames )
      {
        lReturn += ",";
        lReturn += lTableName;

        if( pTablesColumns != null )
        {
          lReturn += "(";
          List lColumnNames = new ArrayList( Arrays.asList( pTablesColumns.get( lTableToNAmeMap.get( lTableName ) ) ) );

          Collections.sort( lColumnNames );

          lReturn += lColumnNames;
          lReturn += ")";
        }
      }

      lReturn += "\n";

      return lReturn;
    }

    private String getAssociationString( String pString, List<Association> pAssociations )
    {
      List<String> lTableNames = new ArrayList<String>();

      for( Association lAssociation : pAssociations )
      {
        lTableNames.add( lAssociation.getAssociationName() );
      }

      Collections.sort( lTableNames );

      String lReturn = pString + ": ";

      for( String lTableName : lTableNames )
      {
        lReturn += ",";
        lReturn += lTableName;
      }

      lReturn += "\n";

      return lReturn;
    }

    public void printHeaderStart( String pStyleForGraph )
    {
    }

    public void printHeaderEnd()
    {
    }

    public void printGraph( Graph pGraph, String pCommonStyle, boolean pOutRefsOnly )
    {
    }

    public void printSubGraphStartFilled( Graph pGraph, boolean pOutRefsOnly )
    {
    }

    public void printSubGraphEnd()
    {
    }

    public void printSubGraphStartDashed( Graph pGraph, boolean pOutRefsOnly )
    {
    }

    public void printGraphAssociation( GraphAssociation pGraphAssociation )
    {
    }

    public void printTable( Table pTable, List<Association> pVisibleAssociation, boolean pIsOutref, String pStyle, List<Column> pFilteredColumns )
    {
      if( pVisibleAssociation == null )
      {
        _tablesFull.add( pTable );
      }
      else
      {
        String[] lColumnNames = new String[pFilteredColumns.size()];

        for( int i = 0; i < lColumnNames.length; i++ )
        {
          lColumnNames[i] = pFilteredColumns.get( i ).getColumnName();
        }

        _tablesRefonly.add( pTable );
        _tablesRefonlyColumns.put( pTable, lColumnNames );
      }
    }

    public void printAssociation( Association pAssociation, Graph pGraph )
    {
      _associations.add( pAssociation );
    }
  }
}
