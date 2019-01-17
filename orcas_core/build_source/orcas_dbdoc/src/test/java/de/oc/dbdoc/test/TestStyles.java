package de.oc.dbdoc.test;

import static junit.framework.Assert.*;

import org.junit.Test;

import de.oc.dbdoc.ant.Diagrams;
import de.oc.dbdoc.ant.Style;
import de.oc.dbdoc.ant.Stylegroup;
import de.oc.dbdoc.ant.StylegroupStyle;
import de.oc.dbdoc.ant.Styles;
import de.oc.dbdoc.ant.Tables;

public class TestStyles extends BaseTableRegistrySetup
{
  @Test
  public void testTableStyles()
  {
    Styles lStyles = _orcasDbDoc.createStyles();
    Tables lTables = lStyles.createTables();

    {
      Style lStyle = lTables.createStyle();
      lStyle.setTablegroup( "ABC.*" );
      lStyle.setName( "fillcolor" );
      lStyle.setValue( "ff4040" );
    }
    {
      Style lStyle = lTables.createStyle();
      lStyle.setTablegroup( ".*" );
      lStyle.setName( "style" );
      lStyle.setValue( "filled" );
    }

    assertEquals( "fillcolor=\"ff4040\", style=\"filled\"", lStyles.getStyleForTable( "ABC", tableregistry ) );
    assertEquals( "style=\"filled\"", lStyles.getStyleForTable( "ALBI", tableregistry ) );
  }

  @Test
  public void testDiagramStyles()
  {
    Styles lStyles = _orcasDbDoc.createStyles();
    Diagrams lDiagrams = lStyles.createDiagrams();
    Stylegroup lStylegroup = lDiagrams.createStylegroup();
    lStylegroup.setName( "HUHU" );

    {
      StylegroupStyle lStyle = lStylegroup.createStyle();
      lStyle.setName( "center" );
      lStyle.setValue( "true" );
    }
    {
      StylegroupStyle lStyle = lStylegroup.createStyle();
      lStyle.setName( "overlap" );
      lStyle.setValue( "true" );
    }

    assertEquals( "nodesep=\"1\", overlap=\"false\", ranksep=\"2\", splines=\"polyline\"", lStyles.getStyleForStylegroup( null ) );
    assertEquals( "center=\"true\", nodesep=\"1\", overlap=\"true\", ranksep=\"2\", splines=\"polyline\"", lStyles.getStyleForStylegroup( "HUHU" ) );
  }
}
