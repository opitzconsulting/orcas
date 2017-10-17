package de.opitzconsulting.orcas.diff;

import java.nio.charset.StandardCharsets;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;

import com.opitzconsulting.orcas.dbobjects.SqlplusDirAccessDbobjects;
import com.opitzconsulting.orcas.extenions.SqlplusDirAccessExtenions;
import com.opitzconsulting.orcas.syex.SqlplusDirAccessSyex;

import de.opitzconsulting.orcas.diff.JdbcConnectionHandler.RunWithCallableStatementProvider;
import de.opitzconsulting.orcas.diff.Parameters.FailOnErrorMode;
import de.opitzconsulting.orcas.extensions.PlSqlExtensionInfo;
import de.opitzconsulting.orcas.sql.CallableStatementProvider;
import de.opitzconsulting.orcas.sql.WrapperIteratorResultSet;

public class OrcasInitializeOrcasDbImpl extends BaseParameterBasedHandler
{
  public OrcasInitializeOrcasDbImpl( Parameters pParameters )
  {
    setParametersInternal( pParameters );
  }

  private String getInitializeChecksumExtension()
  {
    return getParameters().getInitializeChecksumExtension() == null ? PlSqlExtensionInfo.getOrcasVersion() : getParameters().getInitializeChecksumExtension();
  }

  private String getInitializeChecksumTotal()
  {
    return getParameters().getInitializeChecksumTotal() == null ? PlSqlExtensionInfo.getOrcasVersion() : getParameters().getInitializeChecksumTotal();
  }

  public void initOrcasDb( Log pLog )
  {
    if( PlSqlExtensionInfo.hasExtensions() )
    {
      getParameters()._failOnErrorMode = FailOnErrorMode.ALWAYS;

      try
      {
        JdbcConnectionHandler.runWithCallableStatementProvider( getParameters(), getParameters().getOrcasJdbcConnectParameters(), new RunWithCallableStatementProvider()
        {
          public void run( CallableStatementProvider pOrcasCallableStatementProvider ) throws Exception
          {
            OrcasScriptRunner lOrcasScriptRunner = new OrcasScriptRunner();
            final boolean[] lIsFullInstallNeeded = new boolean[] { true };
            final boolean[] lIsExtensionInstallNeeded = new boolean[] { true };

            boolean lIsSnapshot = getInitializeChecksumTotal().endsWith( "SNAPSHOT" ) || getInitializeChecksumExtension().endsWith( "SNAPSHOT" );
            if( !lIsSnapshot )
            {
              try
              {
                new WrapperIteratorResultSet( "select pa_orcas_checksum.get_total_checksum() total_checksum, pa_orcas_checksum.get_extension_checksum() extension_checksum from dual", pOrcasCallableStatementProvider )
                {
                  @Override
                  protected void useResultSetRow( ResultSet pResultSet ) throws SQLException
                  {
                    lIsFullInstallNeeded[0] = !pResultSet.getString( "total_checksum" ).equals( getInitializeChecksumTotal() );
                    lIsExtensionInstallNeeded[0] = !pResultSet.getString( "extension_checksum" ).equals( getInitializeChecksumExtension() );
                  }
                }.execute();
              }
              catch( Exception e )
              {
                pLog.debug( e, e );
              }
            }

            if( lIsFullInstallNeeded[0] )
            {
              logInfo( "initialize orcas-db" + (lIsSnapshot ? " (SNAPSHOT-version always needs to be updated)" : "") );

              installFull( lOrcasScriptRunner, pOrcasCallableStatementProvider );
            }
            else
            {
              if( lIsExtensionInstallNeeded[0] )
              {
                try
                {
                  logInfo( "initialize orcas-db extensions only" );

                  installExtensionsOnly( lOrcasScriptRunner, pOrcasCallableStatementProvider );
                }
                catch( Exception e )
                {
                  pLog.debug( e, e );

                  logInfo( "extension-only initialize failed retrying full-initialize" );

                  installFull( lOrcasScriptRunner, pOrcasCallableStatementProvider );
                }
              }
              else
              {
                logInfo( "orcas-db is up to date: skipping initialize orcas-db" );
              }
            }
          }
        } );
      }
      catch( Exception e )
      {
        throw new RuntimeException( e );
      }
    }
    else
    {
      logInfo( "no pl/sql extensions: skipping initialize orcas-db" );
    }
  }

  private void installExtensionsOnly( OrcasScriptRunner pOrcasScriptRunner, CallableStatementProvider pOrcasCallableStatementProvider ) throws Exception
  {
    for( URL lURL : SqlplusDirAccessExtenions.getFileURLs() )
    {
      pOrcasScriptRunner.runURL( lURL, pOrcasCallableStatementProvider, getParameters(), StandardCharsets.UTF_8 );
    }

    pOrcasScriptRunner.runURL( SqlplusDirAccessDbobjects.getURL_compile_all_invalid(), pOrcasCallableStatementProvider, getParameters(), StandardCharsets.UTF_8 );

    pOrcasScriptRunner.runURL( SqlplusDirAccessDbobjects.getURL_update_checksum(), pOrcasCallableStatementProvider, getParameters(), StandardCharsets.UTF_8, getInitializeChecksumTotal(), getInitializeChecksumExtension() );
  }

  private void installFull( OrcasScriptRunner pOrcasScriptRunner, CallableStatementProvider pOrcasCallableStatementProvider ) throws Exception
  {
    pOrcasScriptRunner.runURL( SqlplusDirAccessDbobjects.getURL_delete_replacable_objects(), pOrcasCallableStatementProvider, getParameters(), StandardCharsets.UTF_8, "object_name not like '%'", "object_name not like '%'", "object_name not like '%'", "object_name not like '%'", "object_name not like '%'" );
    pOrcasScriptRunner.runURL( SqlplusDirAccessDbobjects.getURL_drop_all_types(), pOrcasCallableStatementProvider, getParameters(), StandardCharsets.UTF_8, "object_name not like '%'" );

    List<URL> lURLs = new ArrayList<URL>();
    lURLs.addAll( SqlplusDirAccessSyex.getFileURLs() );
    lURLs.addAll( SqlplusDirAccessExtenions.getFileURLs() );
    lURLs.addAll( SqlplusDirAccessDbobjects.getFileURLs() );
    for( URL lURL : lURLs )
    {
      pOrcasScriptRunner.runURL( lURL, pOrcasCallableStatementProvider, getParameters(), StandardCharsets.UTF_8 );
    }

    pOrcasScriptRunner.runURL( SqlplusDirAccessDbobjects.getURL_compile_all_invalid(), pOrcasCallableStatementProvider, getParameters(), StandardCharsets.UTF_8 );

    pOrcasScriptRunner.runURL( SqlplusDirAccessDbobjects.getURL_update_checksum(), pOrcasCallableStatementProvider, getParameters(), StandardCharsets.UTF_8, getInitializeChecksumTotal(), getInitializeChecksumExtension() );
  }
}
