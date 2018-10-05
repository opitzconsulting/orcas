package de.oc.dbdoc.test;

import static junit.framework.Assert.*;

import org.junit.Before;
import org.junit.Test;

import de.oc.dbdoc.ant.Diagram;
import de.oc.dbdoc.ant.OrcasDbDoc;
import de.oc.dbdoc.ant.Tablegroup;
import de.oc.dbdoc.ant.Tableregistry;

public class TestTablegroup
{
  private Tablegroup _tablegroup;
  private Tablegroup _excludeTablegroup;
  private Diagram _rootDiagram;
  private final String _excludeTableGroupName = "XY1";

  @Before
  public void setup()
  {
    OrcasDbDoc lOrcasDbDoc = new OrcasDbDoc();

    Tableregistry lTableregistry = lOrcasDbDoc.createTableregistry();

    String lTableGroupName = "XY";

    _tablegroup = lTableregistry.createTablegroup();
    _tablegroup.setName( lTableGroupName );

    _excludeTablegroup = lTableregistry.createTablegroup();
    _excludeTablegroup.setName( _excludeTableGroupName );

    _rootDiagram = lOrcasDbDoc.createDiagram();
    _rootDiagram.setTablegroup( lTableGroupName );
  }

  @Test
  public void testSimple()
  {
    assertTrue( _rootDiagram.isTableIncluded( "ABC" ) );
    assertTrue( _rootDiagram.isTableIncluded( "DEF" ) );
  }

  @Test
  public void testIncludes()
  {
    _tablegroup.setIncludes( "ABC" );

    assertTrue( _rootDiagram.isTableIncluded( "ABC" ) );
    assertFalse( _rootDiagram.isTableIncluded( "DEF" ) );
  }

  @Test
  public void testExcludes()
  {
    _tablegroup.setIncludes( "AB.*" );
    _tablegroup.setExcludes( "ABC" );

    assertFalse( _rootDiagram.isTableIncluded( "ABC" ) );
    assertTrue( _rootDiagram.isTableIncluded( "ABE" ) );
  }

  @Test
  public void testIncludeExclude()
  {
    _tablegroup.createInclude().setName( "AB.*" );
    _tablegroup.createExclude().setName( "ABC.*" );
    _tablegroup.createInclude().setName( "ABC" );

    assertTrue( _rootDiagram.isTableIncluded( "ABE" ) );
    assertFalse( _rootDiagram.isTableIncluded( "ABCD" ) );
    assertTrue( _rootDiagram.isTableIncluded( "ABC" ) );
    assertFalse( _rootDiagram.isTableIncluded( "DEF" ) );
  }

  @Test
  public void testIncludesExclude()
  {
    _tablegroup.setIncludes( "AB.*" );
    _tablegroup.createExclude().setName( "ABC.*" );

    assertTrue( _rootDiagram.isTableIncluded( "ABE" ) );
    assertFalse( _rootDiagram.isTableIncluded( "ABC" ) );
  }

  @Test
  public void testIncludeExcludes()
  {
    _tablegroup.setExcludes( "ABC.*" );
    _tablegroup.createInclude().setName( "AB.*" );

    assertTrue( _rootDiagram.isTableIncluded( "ABE" ) );
    assertFalse( _rootDiagram.isTableIncluded( "ABC" ) );
  }

  @Test
  public void testExcludeTableGroup()
  {
    _tablegroup.createExclude().setTablegroup( _excludeTableGroupName );
    _excludeTablegroup.setIncludes( "ABC.*" );

    assertTrue( _rootDiagram.isTableIncluded( "ABE" ) );
    assertFalse( _rootDiagram.isTableIncluded( "ABC" ) );
  }
}
