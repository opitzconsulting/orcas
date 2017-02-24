package com.opitzconsulting.orcas.maven;

import java.io.File;
import java.util.List;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import de.opitzconsulting.orcas.diff.OrcasScriptRunner;
import de.opitzconsulting.orcas.diff.ParametersCall;

/**
 * Execute a single sql-script.
 */
@Mojo( name = "ExecuteScript" )
public class ExecuteOrcasScript extends BaseOrcasMojo
{
  /**
   * The file to exceute.
   */
  @Parameter
  private File scriptfile;

  /**
   * The logname for spooling.
   */
  @Parameter
  private String logname;

  @Override
  protected String getLogname()
  {
    return logname;
  }

  /**
   * Parameters for the sql-script if needed.
   */
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
