package com.opitzconsulting.orcas.maven;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import de.opitzconsulting.orcas.diff.OrcasMain;
import de.opitzconsulting.orcas.diff.ParametersCall;

@Mojo( name = "updateStatics" )
public class OrcasUpdateStatics extends BaseOrcasMojo
{
  @Parameter( defaultValue = "update-statics" )
  private String logname;


  @Override
  protected String getLogname()
  {
    return logname;
  }

  @Override
  protected void executeWithParameters( ParametersCall pParameters )
  {
    if( staticsfolder.exists() )
    {
      pParameters.setModelFile( staticsfolder.toString() );
      pParameters.setSqlplustable( false );
      pParameters.setOrderColumnsByName( false );

      new OrcasMain().mainRun( pParameters );
    }
    else
    {
      getLog().info( "no statics found" );
    }
  }
}
