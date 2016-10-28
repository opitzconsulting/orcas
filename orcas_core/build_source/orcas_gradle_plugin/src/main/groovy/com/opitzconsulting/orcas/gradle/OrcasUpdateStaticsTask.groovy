package com.opitzconsulting.orcas.gradle

import de.opitzconsulting.orcas.diff.OrcasMain;
import de.opitzconsulting.orcas.diff.ParametersCall;

public class OrcasUpdateStaticsTask extends BaseOrcasTask
{
  private String logname = "update-statics";


  @Override
  protected String getLogname()
  {
    return logname;
  }

  @Override
  protected void executeOrcasTaskWithParameters( ParametersCall pParameters )
  {
    if( project.file(project.orcasconfiguration.staticsfolder).exists() )
    {
      pParameters.setModelFile( project.file(project.orcasconfiguration.staticsfolder).toString() );
      pParameters.setSqlplustable( false );
      pParameters.setOrderColumnsByName( false );

      new OrcasMain().mainRun( pParameters );
    }
    else
    {
      logInfo( "no statics found" );
    }
  }
}
