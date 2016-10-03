package com.opitzconsulting.orcas.gradle;

import de.opitzconsulting.orcas.diff.OrcasCheckConnection;
import de.opitzconsulting.orcas.diff.ParametersCall;

public class OrcasCheckConnectionTask extends BaseOrcasTask
{
  def logname = "check-connection";

  @Override
  protected String getLogname()
  {
    return logname;
  }

  @Override
  protected void executeOrcasTaskWithParameters( ParametersCall pParameters )
  {
    new OrcasCheckConnection().mainRun( pParameters );
  }
}
