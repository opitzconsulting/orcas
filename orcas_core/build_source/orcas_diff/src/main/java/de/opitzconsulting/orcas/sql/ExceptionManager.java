package de.opitzconsulting.orcas.sql;

import java.sql.SQLException;

public class ExceptionManager
{
  public static RuntimeException createException( SQLException pE, String pSql )
  {
    return new RuntimeException( pE.getMessage() + " " + pSql, pE );
  }
}
