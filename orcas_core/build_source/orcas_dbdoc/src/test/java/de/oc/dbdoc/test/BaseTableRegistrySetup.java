package de.oc.dbdoc.test;

import org.junit.Before;

import de.oc.dbdoc.ant.OrcasDbDoc;
import de.oc.dbdoc.ant.Tablegroup;
import de.oc.dbdoc.ant.Tableregistry;

public class BaseTableRegistrySetup
{
  protected OrcasDbDoc _orcasDbDoc;
  protected Tableregistry tableregistry;

  @Before
  public void setup()
  {
    _orcasDbDoc = new OrcasDbDoc();

    tableregistry = _orcasDbDoc.createTableregistry();

    {
      Tablegroup lTablegroup = tableregistry.createTablegroup();
      lTablegroup.setName( "ABC_1" );
      lTablegroup.setIncludes( "ABC" );
    }
    {
      Tablegroup lTablegroup = tableregistry.createTablegroup();
      lTablegroup.setName( "ABC_2" );
      lTablegroup.setIncludes( "G.*" );
    }
    {
      Tablegroup lTablegroup = tableregistry.createTablegroup();
      lTablegroup.setName( "OTHER" );

      lTablegroup.createInclude().setName( "ALBI" );
    }
  }
}
