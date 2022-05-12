package com.opitzconsulting.orcas.gradle;

import de.opitzconsulting.orcas.diff.OrcasScriptRunner;
import de.opitzconsulting.orcas.diff.ParametersCall;
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Internal;

public class OrcasInstallReplaceablesTask extends BaseOrcasTask
{
  @Internal
  def logname = "install-replaceables";

  @Internal
  FileCollection scriptFiles;

  protected String getLogname()
  {
    return logname;
  }

  @Override
  protected void executeOrcasTaskWithParameters( ParametersCall pParameters )
  {
    if( scriptFiles == null )
    {
      pParameters.setModelFile( project.file(project.orcasconfiguration.replaceablesfolder).toString() );
    }
    else
    {
      pParameters.setModelFiles( scriptFiles as List );
    }

    if( project.file(project.orcasconfiguration.replaceablesfolder).exists() || scriptFiles != null )
    {
      pParameters.setIsOneTimeScriptMode( false );
      pParameters.setAdditionalParameters( null );

      new OrcasScriptRunner().mainRun( modifyParameters( pParameters ) );
    }
    else
    {
      logInfo( "no replaceables found" );
    }
  }
}
