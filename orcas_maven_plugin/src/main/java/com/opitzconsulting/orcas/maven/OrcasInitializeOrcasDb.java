package com.opitzconsulting.orcas.maven;

import java.io.File;

import org.apache.maven.plugins.annotations.Mojo;

import de.opitzconsulting.orcas.diff.Parameters.JdbcConnectParameters;
import de.opitzconsulting.orcas.diff.ParametersCall;

@Mojo( name = "initializeOrcasDb" )
public class OrcasInitializeOrcasDb extends BaseOrcasMojo
{
  @Override
  protected void executeWithParameters( ParametersCall pParameters )
  {
    pParameters.setAdditionalParameters( null );

    new de.opitzconsulting.orcas.diff.OrcasInitializeOrcasDb().mainRun( pParameters );
  }

  @Override
  protected String getLogname()
  {
    return "initialize-orcas-db";
  }
}
