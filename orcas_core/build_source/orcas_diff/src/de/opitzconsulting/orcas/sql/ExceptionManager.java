package de.opitzconsulting.orcas.sql;
import java.sql.SQLException;

public class ExceptionManager
{
  public static RuntimeException createException( SQLException pE )
  {
    return new RuntimeException( pE );
  }
}
