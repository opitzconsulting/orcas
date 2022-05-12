package com.opitzconsulting.orcas.gradle

import de.opitzconsulting.orcas.diff.OrcasCompileAllInvalid
import de.opitzconsulting.orcas.diff.Parameters
import org.gradle.api.tasks.Internal;

import java.nio.charset.StandardCharsets;
import de.opitzconsulting.orcas.diff.ParametersCall;
import com.opitzconsulting.orcas.dbobjects.SqlplusDirAccessDbobjects

public class OrcasCompileAllInvalidTask extends BaseOrcasTask
{
  @Internal
  def logname = "compile-all-invalid";
  @Internal
  def dontFailOnErrors = false;
  @Internal
  @Deprecated
  def boolean getCompileInfos = false;

  @Internal
  def List<OrcasCompileAllInvalid.CompileInfo> compileInfos;

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
        pParameters.setAdditionalParameters(null);

        if (dontFailOnErrors) {
          pParameters.setFailOnErrorMode(Parameters.FailOnErrorMode.NEVER)
        }

        def orcasCompileAllInvalid = new OrcasCompileAllInvalid()
        orcasCompileAllInvalid.mainRun( modifyParameters( pParameters ) );
        compileInfos = orcasCompileAllInvalid.getCompileInfos()
      }
      else
      {
        logInfo( "no replaceables found" );
      }
    }
  }

  @Internal
  protected boolean isRunOnlyIfReplaceablesExists()
  {
    return false;
  }
}
