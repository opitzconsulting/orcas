package com.opitzconsulting.orcas.gradle

import de.opitzconsulting.orcas.diff.OrcasMain;
import de.opitzconsulting.orcas.diff.ParametersCall;
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Internal;

public class OrcasUpdateStaticsTask extends BaseOrcasTask
{
  @Internal
  public String logname = "update-statics";

  @Internal
  FileCollection scriptFiles;

  @Internal
  FileCollection relevantScriptFiles;

  @Internal
  FileCollection schemaFiles;

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

    if( relevantScriptFiles != null )
    {
      pParameters.setRelevantModelFiles( relevantScriptFiles as List  );
    }

    if( relevantScriptFiles != null )
    {
      pParameters.setSchemaFiles( schemaFiles as List  );
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
