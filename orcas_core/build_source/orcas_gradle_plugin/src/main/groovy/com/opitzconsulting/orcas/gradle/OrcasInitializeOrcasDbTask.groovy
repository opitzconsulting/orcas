package com.opitzconsulting.orcas.gradle

import java.io.File;

import de.opitzconsulting.orcas.diff.OrcasInitializeOrcasDb;
import de.opitzconsulting.orcas.diff.Parameters.JdbcConnectParameters;
import de.opitzconsulting.orcas.diff.ParametersCall;

public class OrcasInitializeOrcasDbTask extends BaseOrcasTask
{
  @Override
  protected void executeOrcasTaskWithParameters( ParametersCall pParameters )
  {
    pParameters.setAdditionalParameters( null );

    new OrcasInitializeOrcasDb().mainRun( pParameters );
  }

  @Override
  protected String getLogname()
  {
    return "initialize-orcas-db";
  }
}
