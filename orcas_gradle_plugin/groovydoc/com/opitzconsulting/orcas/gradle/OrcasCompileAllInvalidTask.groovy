package com.opitzconsulting.orcas.gradle;

import java.nio.charset.StandardCharsets;
import de.opitzconsulting.orcas.diff.OrcasScriptRunner;
import de.opitzconsulting.orcas.diff.ParametersCall;
import com.opitzconsulting.orcas.dbobjects.SqlplusDirAccessDbobjects;

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
    if( !isMariadb() )
    {
      if( !isRunOnlyIfReplaceablesExists() || project.file(project.orcasconfiguration.replaceablesfolder).exists() )
      {
        pParameters.setScriptUrl( SqlplusDirAccessDbobjects.getURL_compile_all_invalid(), "compile_all_invalid.sql", StandardCharsets.UTF_8 );
        pParameters.setIsOneTimeScriptMode( false );
        pParameters.setAdditionalParameters( null );
  
        new OrcasScriptRunner().mainRun( modifyParameters( pParameters ) );
      }
      else
      {
        logInfo( "no replaceables found" );
      }
    }
  }

  protected boolean isRunOnlyIfReplaceablesExists()
  {
    return false;
  }
}
