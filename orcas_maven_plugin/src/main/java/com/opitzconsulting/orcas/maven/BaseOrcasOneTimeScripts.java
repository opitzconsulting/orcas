package com.opitzconsulting.orcas.maven;

import java.io.File;

import org.apache.maven.plugins.annotations.Parameter;

import de.opitzconsulting.orcas.diff.OrcasScriptRunner;
import de.opitzconsulting.orcas.diff.Parameters.JdbcConnectParameters;
import de.opitzconsulting.orcas.diff.ParametersCall;

public abstract class BaseOrcasOneTimeScripts extends BaseOrcasMojo
{
  @Parameter( defaultValue = "oracle.jdbc.OracleDriver" )
  private String orcasjdbcdriver;

  @Parameter
  private String orcasjdbcurl;

  @Parameter
  private String orcasusername;

  @Parameter
  private String orcaspassword;

  @Override
  protected void executeWithParameters( ParametersCall pParameters )
  {
    if( !isCheckFolderExists() || getScriptfolder().exists() )
    {
      if( orcasusername != null )
      {
        JdbcConnectParameters lOrcasJdbcConnectParameters = new JdbcConnectParameters();
        lOrcasJdbcConnectParameters.setJdbcDriver( orcasjdbcdriver );
        lOrcasJdbcConnectParameters.setJdbcUrl( orcasjdbcurl == null ? pParameters.getJdbcConnectParameters().getJdbcUrl() : orcasjdbcurl );
        lOrcasJdbcConnectParameters.setJdbcUser( orcasusername );
        lOrcasJdbcConnectParameters.setJdbcPassword( orcaspassword );
        pParameters.setOrcasJdbcConnectParameters( lOrcasJdbcConnectParameters );
      }
      else
      {
        pParameters.setOrcasJdbcConnectParameters( pParameters.getJdbcConnectParameters() );
      }

      pParameters.setModelFile( getScriptfolder().toString() );
      pParameters.setIsOneTimeScriptMode( true );
      pParameters.setAdditionalParameters( null );
      pParameters.setIsOneTimeScriptLogonlyMode( false );

      new OrcasScriptRunner().mainRun( pParameters );
    }
    else
    {
      getLog().info( "no " + getLogname() + " found" );
    }
  }

  protected boolean isCheckFolderExists()
  {
    return true;
  }

  protected abstract File getScriptfolder();
}
