package de.opitzconsulting.orcas.diff;

import de.opitzconsulting.orcas.sql.CallableStatementProvider;
import de.opitzconsulting.origOrcasDsl.CharType;

public abstract class DatabaseHandler
{
  public abstract void createOrcasUpdatesTable( String pOrcasUpdatesTableName, CallableStatementProvider pOrcasCallableStatementProvider );

  public abstract void insertIntoOrcasUpdatesTable( String pOrcasUpdatesTableName, CallableStatementProvider pOrcasCallableStatementProvider, String pFilePart, String pLogname );

  public abstract LoadIst createLoadIst( CallableStatementProvider pCallableStatementProvider, Parameters pParameters );

  public abstract CharType getDefaultCharType( CallableStatementProvider pCallableStatementProvider );

  public abstract String getDefaultTablespace( CallableStatementProvider pCallableStatementProvider );

  public abstract DdlBuilder createDdlBuilder( Parameters pParameters );

  public abstract void executeDiffResultStatement( String pStatementToExecute, CallableStatementProvider pCallableStatementProvider );
}
