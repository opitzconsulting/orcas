package com.opitzconsulting.orcas.maven;

import java.io.File;

import org.apache.maven.plugins.annotations.Mojo;

import de.opitzconsulting.orcas.diff.ParametersCall;
import de.opitzconsulting.orcas.diff.Orcas;

@Mojo( name = "cleanLog" )
public class OrcasCleanLog extends BaseOrcasMojo
{
  @Override
  protected void executeWithParameters( ParametersCall pParameters )
  {
    if( pParameters.getSpoolfolder().length() != 0 && new File( pParameters.getSpoolfolder() ).exists() )
    {
      Orcas.deleteRecursive( new File( pParameters.getSpoolfolder() ) );
    }
  }

  @Override
  protected String getLogname()
  {
    return "clean-log";
  }
}
