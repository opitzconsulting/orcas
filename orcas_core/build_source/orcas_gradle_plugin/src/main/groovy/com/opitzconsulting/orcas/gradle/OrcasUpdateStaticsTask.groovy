package com.opitzconsulting.orcas.gradle

import de.opitzconsulting.orcas.diff.OrcasMain;
import de.opitzconsulting.orcas.diff.ParametersCall;
import org.gradle.api.file.FileCollection;

public class OrcasUpdateStaticsTask extends BaseOrcasTask
{
  public String logname = "update-statics";

  FileCollection scriptFiles;

  @Override
  protected String getLogname()
  {
    return logname;
  }

  @Override
  protected void executeOrcasTaskWithParameters( ParametersCall pParameters )
  {
    if( scriptFiles == null )
    {
      pParameters.setModelFile( project.file(project.orcasconfiguration.staticsfolder).toString() );
    }
    else
    {
      pParameters.setModelFile( "" );
      pParameters.setModelFiles( scriptFiles as List );
    }

    if( project.file(project.orcasconfiguration.staticsfolder).exists() || scriptFiles != null )
    {
      pParameters.setSqlplustable( false );
      pParameters.setOrderColumnsByName( false );

      new OrcasMain().mainRun( modifyParameters( pParameters ) );
    }
    else
    {
      logInfo( "no statics found" );
    }
  }
}
