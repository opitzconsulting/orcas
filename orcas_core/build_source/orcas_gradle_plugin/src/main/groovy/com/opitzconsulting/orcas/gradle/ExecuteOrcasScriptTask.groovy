package com.opitzconsulting.orcas.gradle

import de.opitzconsulting.orcas.diff.Parameters;

import java.io.File;
import java.util.List;

import de.opitzconsulting.orcas.diff.OrcasScriptRunner;
import de.opitzconsulting.orcas.diff.ParametersCall;

public class ExecuteOrcasScriptTask extends BaseOrcasTask
{
  def Parameters.FailOnErrorMode failOnErrorMode = null;

  def scriptfile;

  def logname;

  protected String getLogname()
  {
    return logname;
  }

  List<String> scriptParameters;

  @Override
  protected void executeOrcasTaskWithParameters( ParametersCall pParameters )
  {
    pParameters.setModelFile( project.file(scriptfile) );
    pParameters.setIsOneTimeScriptMode( false );

    pParameters.setAdditionalParameters( scriptParameters );
    if( failOnErrorMode != null ) {
      pParameters.setFailOnErrorMode(failOnErrorMode);
    }

    new OrcasScriptRunner().mainRun( modifyParameters( pParameters ) );
  }
}
