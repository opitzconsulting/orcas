package com.opitzconsulting.orcas.maven;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import de.opitzconsulting.orcas.diff.OrcasScriptRunner;
import de.opitzconsulting.orcas.diff.ParametersCall;

/**
 * Exceutes sql-script that contain database-objects.
 */
@Mojo( name = "installReplaceables" )
public class OrcasInstallReplaceables extends BaseOrcasMojo
{
  /**
   * The logname for spooling.
   */
  @Parameter( defaultValue = "install-replaceables" )
  private String logname;

  @Override
  protected String getLogname()
  {
    return logname;
  }

  @Override
  protected void executeWithParameters( ParametersCall pParameters )
  {
    if( replaceablesfolder.exists() )
    {
      pParameters.setModelFile( replaceablesfolder.toString() );
      pParameters.setIsOneTimeScriptMode( false );
      pParameters.setAdditionalParameters( null );

      new OrcasScriptRunner().mainRun( pParameters );
    }
    else
    {
      getLog().info( "no replaceables found" );
    }
  }
}
