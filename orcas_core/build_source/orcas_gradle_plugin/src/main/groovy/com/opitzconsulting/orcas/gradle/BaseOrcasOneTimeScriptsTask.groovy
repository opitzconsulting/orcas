package com.opitzconsulting.orcas.gradle

import de.opitzconsulting.orcas.diff.ParameterDefaults
import de.opitzconsulting.orcas.diff.Parameters
import org.gradle.api.tasks.Internal

import java.io.File;

import de.opitzconsulting.orcas.diff.OrcasScriptRunner;
import de.opitzconsulting.orcas.diff.ParametersCall;

public abstract class BaseOrcasOneTimeScriptsTask extends BaseOrcasTask
{
  @Override
  protected void executeOrcasTaskWithParameters( ParametersCall pParameters )
  {
    if( !isCheckFolderExists() || getScriptfolder().exists() )
    {
      pParameters.setModelFile( getScriptfolder().toString() );
      pParameters.setIsOneTimeScriptMode( true );
      pParameters.setAdditionalParameters( null );

      new OrcasScriptRunner().mainRun( modifyParameters( pParameters ) );
    }
    else
    {
      logInfo( "no " + getLogname() + " found" );
    }
  }

  @Internal
  protected boolean isCheckFolderExists()
  {
    return true;
  }

  @Internal
  protected abstract File getScriptfolder();
}
