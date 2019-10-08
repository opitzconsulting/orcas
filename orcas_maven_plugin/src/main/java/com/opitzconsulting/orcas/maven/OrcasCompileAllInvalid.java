package com.opitzconsulting.orcas.maven;

import java.nio.charset.StandardCharsets;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.opitzconsulting.orcas.dbobjects.SqlplusDirAccessDbobjects;

import de.opitzconsulting.orcas.diff.ParametersCall;

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
      pParameters.setScriptUrl( SqlplusDirAccessDbobjects.getURL_compile_all_invalid(), "compile_all_invalid.sql", StandardCharsets.UTF_8 );
      pParameters.setIsOneTimeScriptMode( false );
      pParameters.setAdditionalParameters( null );

      new de.opitzconsulting.orcas.diff.OrcasCompileAllInvalid().mainRun( pParameters );
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
