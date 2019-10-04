package com.opitzconsulting.orcas.gradle

import de.opitzconsulting.orcas.diff.ParameterDefaults
import de.opitzconsulting.orcas.diff.Parameters

import java.io.File;

import de.opitzconsulting.orcas.diff.OrcasScriptRunner;
import de.opitzconsulting.orcas.diff.ParametersCall;

public abstract class BaseOrcasOneTimeScriptsTask extends BaseOrcasTask
{
  def Parameters.FailOnErrorMode failOnErrorMode = null;

  @Override
  protected void executeOrcasTaskWithParameters( ParametersCall pParameters )
  {
    if( !isCheckFolderExists() || getScriptfolder().exists() )
    {
      pParameters.setModelFile( getScriptfolder().toString() );
      pParameters.setIsOneTimeScriptMode( true );
      pParameters.setAdditionalParameters( null );
      pParameters.setIsOneTimeScriptLogonlyMode( false );
      if( failOnErrorMode != null ) {
        pParameters.setFailOnErrorMode(failOnErrorMode);
      }

      new OrcasScriptRunner().mainRun( modifyParameters( pParameters ) );
    }
    else
    {
      logInfo( "no " + getLogname() + " found" );
    }
  }

  protected boolean isCheckFolderExists()
  {
    return true;
  }

  protected abstract File getScriptfolder();
}
