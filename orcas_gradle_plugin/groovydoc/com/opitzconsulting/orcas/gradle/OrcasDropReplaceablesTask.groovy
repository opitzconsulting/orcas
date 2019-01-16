package com.opitzconsulting.orcas.gradle;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import de.opitzconsulting.orcas.diff.OrcasScriptRunner;
import de.opitzconsulting.orcas.diff.ParametersCall;
import com.opitzconsulting.orcas.dbobjects.SqlplusDirAccessDbobjects;

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

      if( isMariadb() )
      {
        pParameters.setScriptUrl( SqlplusDirAccessDbobjects.getURL_mariadb_delete_replacable_objects(), "mariadb_delete_replacable_objects.sql", StandardCharsets.UTF_8 );
      }
      else
      {
        pParameters.setScriptUrl( SqlplusDirAccessDbobjects.getURL_delete_replacable_objects(), "delete_replacable_objects.sql", StandardCharsets.UTF_8 );
      }
      lAdditionalParameters.clear();
      lAdditionalParameters.add( project.orcasconfiguration.excludewherepackage );
      lAdditionalParameters.add( project.orcasconfiguration.excludewheretrigger );
      lAdditionalParameters.add( project.orcasconfiguration.excludewhereview );
      lAdditionalParameters.add( project.orcasconfiguration.excludewherefunction );
      lAdditionalParameters.add( project.orcasconfiguration.excludewhereprocedure );
      new OrcasScriptRunner().mainRun( modifyParameters( pParameters ) );

      if( !isMariadb() )
      {
        pParameters.setScriptUrl( SqlplusDirAccessDbobjects.getURL_drop_all_types(), "drop_all_types.sql", StandardCharsets.UTF_8 );
        lAdditionalParameters.clear();
        lAdditionalParameters.add( project.orcasconfiguration.excludewhereobjecttype );
        new OrcasScriptRunner().mainRun( pParameters );
      }
    }
    else
    {
      logInfo( "no replaceables found" );
    }
  }

  protected boolean isRunOnlyIfReplaceablesExists()
  {
    return false;
  }
}
