package com.opitzconsulting.orcas.gradle

import de.opitzconsulting.orcas.diff.OrcasCompileAllInvalid
import de.opitzconsulting.orcas.diff.Parameters;

import java.nio.charset.StandardCharsets;
import de.opitzconsulting.orcas.diff.ParametersCall;
import com.opitzconsulting.orcas.dbobjects.SqlplusDirAccessDbobjects

public class OrcasCompileAllInvalidTask extends BaseOrcasTask
{
  def logname = "compile-all-invalid";
  def dontFailOnErrors = false;
  @Deprecated
  def boolean getCompileInfos = false;
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

  protected boolean isRunOnlyIfReplaceablesExists()
  {
    return false;
  }
}
