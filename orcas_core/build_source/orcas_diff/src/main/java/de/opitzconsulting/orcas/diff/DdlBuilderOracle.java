package de.opitzconsulting.orcas.diff;

import de.opitzconsulting.origOrcasDsl.DataType;

public class DdlBuilderOracle extends DdlBuilder
{
  public DdlBuilderOracle( Parameters pParameters, DatabaseHandler pDatabaseHandler )
  {
    super( pParameters, pDatabaseHandler );
  }

  protected String getDatatypeName( DataType pData_typeNew )
  {
    switch (pData_typeNew) {
    case INT:
    case SMALLINT:
    case TINYINT:
    case MEDIUMINT:
    case BIGINT:
      return "integer";
    default:
      return super.getDatatypeName( pData_typeNew );
    }
  }
}
