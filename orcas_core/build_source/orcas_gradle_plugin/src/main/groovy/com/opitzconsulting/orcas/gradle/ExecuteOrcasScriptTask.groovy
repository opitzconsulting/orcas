package com.opitzconsulting.orcas.gradle

import de.opitzconsulting.orcas.diff.Parameters;

import java.io.File;
import java.util.List;

import de.opitzconsulting.orcas.diff.OrcasScriptRunner;
import de.opitzconsulting.orcas.diff.ParametersCall;

public class ExecuteOrcasScriptTask extends BaseOrcasTask
{
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
    pParameters.setModelFile( scriptfile);
    pParameters.setIsOneTimeScriptMode( false );

    pParameters.setAdditionalParameters( scriptParameters );

    new OrcasScriptRunner().mainRun( modifyParameters( pParameters ) );
  }
}
