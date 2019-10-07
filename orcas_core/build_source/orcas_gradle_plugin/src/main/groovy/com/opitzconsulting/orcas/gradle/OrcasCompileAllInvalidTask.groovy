package com.opitzconsulting.orcas.gradle

import de.opitzconsulting.orcas.diff.OrcasCompileAllInvalid
import de.opitzconsulting.orcas.diff.Parameters;

import java.nio.charset.StandardCharsets;
import de.opitzconsulting.orcas.diff.OrcasScriptRunner;
import de.opitzconsulting.orcas.diff.ParametersCall;
import com.opitzconsulting.orcas.dbobjects.SqlplusDirAccessDbobjects

import java.util.stream.Collectors
import java.util.stream.Stream;

public class OrcasCompileAllInvalidTask extends BaseOrcasTask
{
  def logname = "compile-all-invalid";
  def dontFailOnErrors = false;
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
        if(getCompileInfos) {
          orcasCompileAllInvalid.setGetCompileInfos()
        }
        orcasCompileAllInvalid.mainRun( modifyParameters( pParameters ) );
        if(getCompileInfos) {
          compileInfos = orcasCompileAllInvalid.getCompileInfos()
        }
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
