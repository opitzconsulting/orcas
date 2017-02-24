package com.opitzconsulting.orcas.maven;

import java.io.File;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import de.opitzconsulting.orcas.diff.OrcasScriptRunner;
import de.opitzconsulting.orcas.diff.ParametersCall;

/**
 * Execute sql-scripts.
 */
@Mojo( name = "ExecuteScripts" )
public class ExecuteOrcasScripts extends BaseOrcasMojo
{
  /**
   * The folder that contains the scripts to be exceutd. 
   */
  @Parameter
  private File scriptfolder;

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

  @Override
  protected void executeWithParameters( ParametersCall pParameters )
  {
    pParameters.setModelFile( scriptfolder.toString() );
    pParameters.setIsOneTimeScriptMode( false );
    pParameters.setAdditionalParameters( null );

    new OrcasScriptRunner().mainRun( pParameters );
  }
}
