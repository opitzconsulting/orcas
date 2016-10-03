package com.opitzconsulting.orcas.gradle;

import java.io.File;

import de.opitzconsulting.orcas.diff.OrcasScriptRunner;
import de.opitzconsulting.orcas.diff.ParametersCall;

public class ExecuteOrcasScriptsTask extends BaseOrcasTask
{
  def scriptfolder;

  def logname;

  protected String getLogname()
  {
    return logname;
  }

  @Override
  protected void executeOrcasTaskWithParameters( ParametersCall pParameters )
  {
    pParameters.setModelFile( project.file(scriptfolder) );
    pParameters.setIsOneTimeScriptMode( false );
    pParameters.setAdditionalParameters( null );

    new OrcasScriptRunner().mainRun( pParameters );
  }
}
