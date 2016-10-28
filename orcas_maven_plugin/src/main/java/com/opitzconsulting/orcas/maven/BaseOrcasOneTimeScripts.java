package com.opitzconsulting.orcas.maven;

import java.io.File;

import org.apache.maven.plugins.annotations.Parameter;

import de.opitzconsulting.orcas.diff.OrcasScriptRunner;
import de.opitzconsulting.orcas.diff.ParametersCall;

public abstract class BaseOrcasOneTimeScripts extends BaseOrcasMojo
{
  @Override
  protected void executeWithParameters( ParametersCall pParameters )
  {
    if( !isCheckFolderExists() || getScriptfolder().exists() )
    {
      pParameters.setModelFile( getScriptfolder().toString() );
      pParameters.setIsOneTimeScriptMode( true );
      pParameters.setAdditionalParameters( null );
      pParameters.setIsOneTimeScriptLogonlyMode( false );

      new OrcasScriptRunner().mainRun( pParameters );
    }
    else
    {
      getLog().info( "no " + getLogname() + " found" );
    }
  }

  protected boolean isCheckFolderExists()
  {
    return true;
  }

  protected abstract File getScriptfolder();
}
