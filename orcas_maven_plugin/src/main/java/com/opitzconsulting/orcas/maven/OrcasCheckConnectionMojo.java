package com.opitzconsulting.orcas.maven;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import de.opitzconsulting.orcas.diff.OrcasCheckConnection;
import de.opitzconsulting.orcas.diff.ParametersCall;

@Mojo( name = "checkConnection" )
public class OrcasCheckConnectionMojo extends BaseOrcasMojo
{
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
