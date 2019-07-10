package com.opitzconsulting.orcas.gradle;

import java.io.File;
import org.gradle.api.file.FileCollection;

import de.opitzconsulting.orcas.diff.OrcasScriptRunner;
import de.opitzconsulting.orcas.diff.ParametersCall;


public class ExecuteOrcasScriptsTask extends BaseOrcasTask
{
  def scriptfolder;

  FileCollection scriptFiles;

  def logname;

  protected String getLogname()
  {
    return logname;
  }

  @Override
  protected void executeOrcasTaskWithParameters( ParametersCall pParameters )
  {
    if( scriptFiles == null )
    {
      pParameters.setModelFile( project.file(scriptfolder).toString() );
    }
    else
    {
      pParameters.setModelFiles( scriptFiles as List );
    }
    pParameters.setIsOneTimeScriptMode( false );
    pParameters.setAdditionalParameters( null );

    new OrcasScriptRunner().mainRun( modifyParameters( pParameters ) );
  }
}
