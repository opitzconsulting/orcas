package com.opitzconsulting.orcas.gradle;

import java.io.File;


import de.opitzconsulting.orcas.diff.ParametersCall;

public class OrcasCleanLogTask extends BaseOrcasTask
{
  @Override
  protected void executeOrcasTaskWithParameters( ParametersCall pParameters )
  {
    modifyParameters( pParameters );
    if( pParameters.getSpoolfolder().length() != 0 && project.file( pParameters.getSpoolfolder() ).exists() )
    {
      project.file( pParameters.getSpoolfolder() ).deleteDir();
    }
  }

  @Override
  protected String getLogname()
  {
    return "clean-log";
  }
}
