package com.opitzconsulting.orcas.maven;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import de.opitzconsulting.orcas.diff.OrcasCheckConnection;
import de.opitzconsulting.orcas.diff.ParametersCall;

/**
 * Simple connection test.
 */
@Mojo( name = "checkConnection" )
public class OrcasCheckConnectionMojo extends BaseOrcasMojo
{
  /**
   * The logname for spooling.
   */
  @Parameter( defaultValue = "check-connection" )
  private String logname;

  @Override
  protected String getLogname()
  {
    return logname;
  }

  @Override
  protected void executeWithParameters( ParametersCall pParameters )
  {
    new OrcasCheckConnection().mainRun( pParameters );
  }
}
