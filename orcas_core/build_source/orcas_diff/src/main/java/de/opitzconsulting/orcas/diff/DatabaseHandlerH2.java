package de.opitzconsulting.orcas.diff;

import java.util.ArrayList;
import java.util.List;

import de.opitzconsulting.orcas.sql.CallableStatementProvider;
import de.opitzconsulting.orcas.sql.WrapperExecutePreparedStatement;
import de.opitzconsulting.origOrcasDsl.CharType;

public class DatabaseHandlerH2 extends DatabaseHandler
{
  @Override
  public void createOrcasUpdatesTable( String pOrcasUpdatesTableName, CallableStatementProvider pOrcasCallableStatementProvider )
  {
    String lSql = "create table " + pOrcasUpdatesTableName + " ( scup_id INTEGER IDENTITY PRIMARY KEY, scup_script_name varchar(4000) not null, scup_logname varchar(100) not null, scup_date date not null, scup_schema varchar(30) not null)";
    new WrapperExecutePreparedStatement( lSql, pOrcasCallableStatementProvider ).execute();
  }

  @Override
  public void insertIntoOrcasUpdatesTable( String pOrcasUpdatesTableName, CallableStatementProvider pOrcasCallableStatementProvider, String pFilePart, String pLogname )
  {
    String lSql = "" + //
                  " insert into " + pOrcasUpdatesTableName + "(" + //
                  "        scup_script_name," + //
                  "        scup_date," + //
                  "        scup_schema," + //
                  "        scup_logname" + //
                  "        )" + //
                  " values (" + //
                  "        ?," + //
                  "        current_timestamp," + //
                  "        SCHEMA()," + //
                  "        ?" + //
                  "        )" + //
                  "";
    List<Object> lInsertParameters = new ArrayList<Object>();
    lInsertParameters.add( pFilePart );
    lInsertParameters.add( pLogname );
    new WrapperExecutePreparedStatement( lSql, pOrcasCallableStatementProvider, lInsertParameters ).execute();
    new WrapperExecutePreparedStatement( "commit", pOrcasCallableStatementProvider ).execute();
  }

  @Override
  public LoadIst createLoadIst( CallableStatementProvider pCallableStatementProvider, Parameters pParameters )
  {
    return new LoadIstHsqlDb( pCallableStatementProvider, pParameters );
  }

  @Override
  public CharType getDefaultCharType( CallableStatementProvider pCallableStatementProvider )
  {
    return CharType.CHAR;
  }

  @Override
  public String getDefaultTablespace( CallableStatementProvider pCallableStatementProvider )
  {
    return null;
  }

  @Override
  public DdlBuilder createDdlBuilder( Parameters pParameters )
  {
    return new DdlBuilderHsqlDb( pParameters );
  }

  @Override
  public void executeDiffResultStatement( String pStatementToExecute, CallableStatementProvider pCallableStatementProvider )
  {
    new WrapperExecutePreparedStatement( pStatementToExecute, pCallableStatementProvider ).execute();
  }

  @Override
  public boolean isRenamePrimaryKey()
  {
    return false;
  }

  @Override
  public boolean isRenameIndex()
  {
    return false;
  }

  @Override
  public boolean isRenameMView()
  {
    return false;
  }

  @Override
  public boolean isRenameForeignKey()
  {
    return false;
  }

  @Override
  public boolean isRenameUniqueKey()
  {
    return false;
  }

  @Override
  public boolean isRenameConstraint()
  {
    return false;
  }

  @Override
  public boolean isUpdateIdentity()
  {
    return false;
  }
}
