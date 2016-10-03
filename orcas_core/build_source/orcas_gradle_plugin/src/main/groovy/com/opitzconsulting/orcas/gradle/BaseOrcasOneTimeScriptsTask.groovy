package com.opitzconsulting.orcas.gradle

import java.io.File;

import de.opitzconsulting.orcas.diff.OrcasScriptRunner;
import de.opitzconsulting.orcas.diff.Parameters.JdbcConnectParameters;
import de.opitzconsulting.orcas.diff.ParametersCall;

public abstract class BaseOrcasOneTimeScriptsTask extends BaseOrcasTask
{
  @Override
  protected void executeOrcasTaskWithParameters( ParametersCall pParameters )
  {
    if( !isCheckFolderExists() || getScriptfolder().exists() )
    {
      if( project.orcasconfiguration.orcasusername != null )
      {
        JdbcConnectParameters lOrcasJdbcConnectParameters = new JdbcConnectParameters();
        lOrcasJdbcConnectParameters.setJdbcDriver( project.orcasconfiguration.orcasjdbcdriver );
        lOrcasJdbcConnectParameters.setJdbcUrl( project.orcasconfiguration.orcasjdbcurl == null ? pParameters.getJdbcConnectParameters().getJdbcUrl() : project.orcasconfiguration.orcasjdbcurl );
        lOrcasJdbcConnectParameters.setJdbcUser( project.orcasconfiguration.orcasusername );
        lOrcasJdbcConnectParameters.setJdbcPassword( project.orcasconfiguration.orcaspassword );
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
