package com.opitzconsulting.orcas.maven;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import de.opitzconsulting.orcas.diff.OrcasScriptRunner;
import de.opitzconsulting.orcas.diff.ParametersCall;
import com.opitzconsulting.orcas.dbobjects.SqlplusDirAccessDbobjects;

/**
 * Compiles pl/sql code in the database.
 */
@Mojo( name = "compileAllInvalid" )
public class OrcasCompileAllInvalid extends BaseOrcasMojo
{
  /**
   * The logname for spooling.
   */
  @Parameter( defaultValue = "compile-all-invalid" )
  private String logname;

  @Override
  protected String getLogname()
  {
    return logname;
  }

  @Override
  protected void executeWithParameters( ParametersCall pParameters )
  {
    if( !isRunOnlyIfReplaceablesExists() || replaceablesfolder.exists() )
    {
      pParameters.setScriptUrl( SqlplusDirAccessDbobjects.getURL_compile_all_invalid(), "compile_all_invalid.sql" );
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
