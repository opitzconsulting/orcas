package com.opitzconsulting.orcas.gradle

import de.opitzconsulting.orcas.diff.Parameters
import org.gradle.api.tasks.Internal;

import java.io.File;
import java.util.List;

import de.opitzconsulting.orcas.diff.OrcasScriptRunner;
import de.opitzconsulting.orcas.diff.ParametersCall;

public class ExecuteOrcasScriptTask extends BaseOrcasTask
{
  @Internal
  def scriptfile;

  @Internal
  def logname;

  protected String getLogname()
  {
    return logname;
  }

  @Internal
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
