package de.oc.dbdoc.test;

import static junit.framework.Assert.*;

import org.junit.Test;

import de.oc.dbdoc.ant.Diagram;

public class TestDiagram extends BaseTableRegistrySetup 
{
 
  @Test
  public void test()
  {
    Diagram lRootDiagram = _orcasDbDoc.createDiagram();
    lRootDiagram.setTablegroup( "ABC.*" );

    assertTrue( lRootDiagram.isTableIncluded( "ABC", tableregistry ) );
    assertFalse( lRootDiagram.isTableIncluded( "ALBI", tableregistry ) );
    assertTrue( lRootDiagram.isTableIncluded( "GH", tableregistry ) );
    assertFalse( lRootDiagram.isTableIncluded( "DEF", tableregistry ) );
  }

  @Test
  public void testSubinclude()
  {
    Diagram lRootDiagram = _orcasDbDoc.createDiagram();
    Diagram lDiagramLevel1 = lRootDiagram.createDiagram();
    Diagram lDiagramLevel2 = lDiagramLevel1.createDiagram();
    lDiagramLevel1.setSubinnclude( "tables" );
    lDiagramLevel2.setTablegroup( "ABC.*" );

    assertTrue( lDiagramLevel2.isTableIncluded( "ABC", tableregistry ) );
    assertTrue( lDiagramLevel1.isTableIncluded( "ABC", tableregistry ) );
    assertFalse( lRootDiagram.isTableIncluded( "ABC", tableregistry ) );
  }
}
