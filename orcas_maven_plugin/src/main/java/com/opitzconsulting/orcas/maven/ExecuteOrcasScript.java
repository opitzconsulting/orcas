package com.opitzconsulting.orcas.maven;

import java.io.File;
import java.util.List;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import de.opitzconsulting.orcas.diff.OrcasScriptRunner;
import de.opitzconsulting.orcas.diff.ParametersCall;

@Mojo( name = "ExecuteScript" )
public class ExecuteOrcasScript extends BaseOrcasMojo
{
  @Parameter
  private File scriptfile;

  @Parameter
  private String logname;

  @Override
  protected String getLogname()
  {
    return logname;
  }

  @Parameter
  private List<String> scriptParameters;

  @Override
  protected void executeWithParameters( ParametersCall pParameters )
  {
    pParameters.setModelFile( scriptfile.toString() );
    pParameters.setIsOneTimeScriptMode( false );

    pParameters.setAdditionalParameters( scriptParameters );

    new OrcasScriptRunner().mainRun( pParameters );
  }
}
