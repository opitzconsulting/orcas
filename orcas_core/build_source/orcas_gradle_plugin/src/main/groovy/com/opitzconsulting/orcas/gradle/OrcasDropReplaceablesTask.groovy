package com.opitzconsulting.orcas.gradle;

import java.util.ArrayList;
import java.util.List;

import de.opitzconsulting.orcas.diff.OrcasScriptRunner;
import de.opitzconsulting.orcas.diff.ParametersCall;

public class OrcasDropReplaceablesTask extends BaseOrcasTask
{
  def logname = "drop-replaceables";

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
      pParameters.setIsOneTimeScriptMode( false );
      List<String> lAdditionalParameters = new ArrayList<String>();
      pParameters.setAdditionalParameters( lAdditionalParameters );

      pParameters.setScriptUrl( getClass().getResource( "/delete_replacable_objects.sql" ), "delete_replacable_objects.sql" );
      lAdditionalParameters.clear();
      lAdditionalParameters.add( project.orcasconfiguration.excludewherepackage );
      lAdditionalParameters.add( project.orcasconfiguration.excludewheretrigger );
      lAdditionalParameters.add( project.orcasconfiguration.excludewhereview );
      lAdditionalParameters.add( project.orcasconfiguration.excludewherefunction );
      lAdditionalParameters.add( project.orcasconfiguration.excludewhereprocedure );
      new OrcasScriptRunner().mainRun( pParameters );

      pParameters.setScriptUrl( getClass().getResource( "/drop_all_types.sql" ), "drop_all_types.sql" );
      lAdditionalParameters.clear();
      lAdditionalParameters.add( project.orcasconfiguration.excludewhereobjecttype );
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
