package com.opitzconsulting.orcas.gradle;

import de.opitzconsulting.orcas.diff.OrcasScriptRunner;
import de.opitzconsulting.orcas.diff.ParametersCall;

public class OrcasCompileAllInvalidTask extends BaseOrcasTask
{
  def logname = "compile-all-invalid";

  @Override
  protected String getLogname()
  {
    return logname;
  }

  @Override
  protected void executeOrcasTaskWithParameters( ParametersCall pParameters )
  {
    if( !isRunOnlyIfReplaceablesExists() || project.file(project.orcasconfiguration.replaceablesfolder).exists() )
    {
      pParameters.setScriptUrl( getClass().getResource( "/compile_all_invalid.sql" ), "compile_all_invalid.sql" );
      pParameters.setIsOneTimeScriptMode( false );
      pParameters.setAdditionalParameters( null );

      new OrcasScriptRunner().mainRun( pParameters );
    }
    else
    {
      getLog().info( "no replaceables found" );
    }
  }

  protected boolean isRunOnlyIfReplaceablesExists()
  {
    return false;
  }
}
