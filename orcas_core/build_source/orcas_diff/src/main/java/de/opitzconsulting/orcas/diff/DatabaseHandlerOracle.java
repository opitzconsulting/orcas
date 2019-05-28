package de.opitzconsulting.orcas.diff;

import java.util.ArrayList;
import java.util.List;

import de.opitzconsulting.orcas.sql.CallableStatementProvider;
import de.opitzconsulting.orcas.sql.WrapperExecuteStatement;
import de.opitzconsulting.orcas.sql.WrapperReturnFirstValue;
import de.opitzconsulting.origOrcasDsl.CharType;

public class DatabaseHandlerOracle extends DatabaseHandler
{
  @Override
  public void createOrcasUpdatesTable( String pOrcasUpdatesTableName, CallableStatementProvider pOrcasCallableStatementProvider )
  {
    String lSql = "create table " + pOrcasUpdatesTableName + " ( scup_id number(22) not null, scup_script_name varchar2(4000 byte) not null, scup_logname varchar2(100 byte) not null, scup_date date not null, scup_schema varchar2(30 byte) not null)";
    new WrapperExecuteStatement( lSql, pOrcasCallableStatementProvider ).execute();
  }

  @Override
  public void insertIntoOrcasUpdatesTable( String pOrcasUpdatesTableName, CallableStatementProvider pOrcasCallableStatementProvider, String pFilePart, String pLogname )
  {
    String lSql = "" + //
                  " insert into " + pOrcasUpdatesTableName + "(" + //
                  "        scup_id," + //
                  "        scup_script_name," + //
                  "        scup_date," + //
                  "        scup_schema," + //
                  "        scup_logname" + //
                  "        )" + //
                  " values (" + //
                  "        nvl" + //
                  "          (" + //
                  "            (" + //
                  "            select max( scup_id ) + 1" + //
                  "              from orcas_updates" + //
                  "            )," + //
                  "          1" + //
                  "          )," + //
                  "        ?," + //
                  "        sysdate," + //
                  "        user," + //
                  "        ?" + //
                  "        )" + //
                  "";
    List<Object> lInsertParameters = new ArrayList<Object>();
    lInsertParameters.add( pFilePart );
    lInsertParameters.add( pLogname );
    new WrapperExecuteStatement( lSql, pOrcasCallableStatementProvider, lInsertParameters ).execute();
    new WrapperExecuteStatement( "commit", pOrcasCallableStatementProvider ).execute();
  }

  @Override
  public LoadIst createLoadIst( CallableStatementProvider pCallableStatementProvider, Parameters pParameters )
  {
    return new LoadIstOracle( pCallableStatementProvider, pParameters );
  }

  @Override
  public CharType getDefaultCharType( CallableStatementProvider pCallableStatementProvider )
  {
    if( new WrapperReturnFirstValue( "select value from nls_instance_parameters where parameter = 'NLS_LENGTH_SEMANTICS'", pCallableStatementProvider ).executeForValue().equals( "BYTE" ) )
    {
      return CharType.BYTE;
    }
    else
    {
      return CharType.CHAR;
    }
  }

  @Override
  public String getDefaultTablespace( CallableStatementProvider pCallableStatementProvider )
  {
    return (String) new WrapperReturnFirstValue( "select default_tablespace from user_users", pCallableStatementProvider ).executeForValue();
  }

  @Override
  public DdlBuilder createDdlBuilder( Parameters pParameters )
  {
    return new DdlBuilderOracle( pParameters, this );
  }

  @Override
  public void executeDiffResultStatement( String pStatementToExecute, CallableStatementProvider pCallableStatementProvider )
  {
    new WrapperExecuteStatement( pStatementToExecute, pCallableStatementProvider ).execute();
  }

  @Override
  public boolean isRenamePrimaryKey()
  {
    return true;
  }

  @Override
  public boolean isRenameIndex()
  {
    return true;
  }

  @Override
  public boolean isRenameMView()
  {
    return true;
  }

  @Override
  public boolean isRenameForeignKey()
  {
    return true;
  }

  @Override
  public boolean isRenameUniqueKey()
  {
    return true;
  }

  @Override
  public boolean isRenameConstraint()
  {
    return true;
  }

  @Override
  public boolean isUpdateIdentity()
  {
    return false;
  }
}
