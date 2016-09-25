package com.opitzconsulting.orcas.maven;

import java.io.File;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import de.opitzconsulting.orcas.diff.OrcasScriptRunner;
import de.opitzconsulting.orcas.diff.ParametersCall;

@Mojo( name = "ExecuteScripts" )
public class ExecuteOrcasScripts extends BaseOrcasMojo
{
  @Parameter
  private File scriptfolder;

  @Parameter
  private String logname;

  @Override
  protected String getLogname()
  {
    return logname;
  }

  @Override
  protected void executeWithParameters( ParametersCall pParameters )
  {
    pParameters.setModelFile( scriptfolder.toString() );
    pParameters.setIsOneTimeScriptMode( false );
    pParameters.setAdditionalParameters( null );

    new OrcasScriptRunner().mainRun( pParameters );
  }
}
