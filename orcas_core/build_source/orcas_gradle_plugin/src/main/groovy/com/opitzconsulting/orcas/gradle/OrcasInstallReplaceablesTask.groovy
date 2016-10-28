package com.opitzconsulting.orcas.gradle;

import de.opitzconsulting.orcas.diff.OrcasScriptRunner;
import de.opitzconsulting.orcas.diff.ParametersCall;

public class OrcasInstallReplaceablesTask extends BaseOrcasTask
{
  def logname = "install-replaceables";

  protected String getLogname()
  {
    return logname;
  }

  @Override
  protected void executeOrcasTaskWithParameters( ParametersCall pParameters )
  {
    if( project.file(project.orcasconfiguration.replaceablesfolder).exists() )
    {
      pParameters.setModelFile( project.file(project.orcasconfiguration.replaceablesfolder).toString() );
      pParameters.setIsOneTimeScriptMode( false );
      pParameters.setAdditionalParameters( null );

      new OrcasScriptRunner().mainRun( pParameters );
    }
    else
    {
      logInfo( "no replaceables found" );
    }
  }
}
