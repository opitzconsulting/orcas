package com.opitzconsulting.orcas.gradle

import de.opitzconsulting.orcas.diff.ModelLoader;
import de.opitzconsulting.orcas.diff.OrcasMain;
import de.opitzconsulting.orcas.diff.ParametersCall;
import org.gradle.api.file.FileCollection;

public class OrcasUpdateStaticsTask extends BaseOrcasTask
{
  private String logname = "update-statics";

  FileCollection scriptFiles;

  ModelLoader modelLoader;

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
      pParameters.setModelFiles( scriptFiles as List );
    }

    if( project.file(project.orcasconfiguration.staticsfolder).exists() || scriptFiles != null )
    {
      pParameters.setSqlplustable( false );
      pParameters.setOrderColumnsByName( false );
      if( modelLoader != null )
      {
        pParameters.setModelLoader( modelLoader );
      }

      new OrcasMain().mainRun( modifyParameters( pParameters ) );
    }
    else
    {
      logInfo( "no statics found" );
    }
  }
}
