package com.opitzconsulting.orcas.maven;

import java.io.File;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import de.opitzconsulting.orcas.diff.OrcasExtractStatics;
import de.opitzconsulting.orcas.diff.ParametersCall;

@Mojo( name = "extractStatics" )
public class OrcasExtractStaticsMojo extends BaseOrcasMojo
{
  @Parameter( defaultValue = "extract" )
  private String logname;

  @Parameter( defaultValue = "true" )
  private Boolean removeDefaultValuesFromMode;

  @Override
  protected String getLogname()
  {
    return logname;
  }

  @Parameter
  protected File extractstaticsoutfolder;

  @Parameter
  protected File extractmodelinputfolder;

  @Override
  protected void executeWithParameters( ParametersCall pParameters )
  {
    pParameters.setOrderColumnsByName( false );
    pParameters.setRemoveDefaultValuesFromModel( removeDefaultValuesFromMode );
    pParameters.setModelFile( extractmodelinputfolder == null ? null : extractmodelinputfolder.toString() );
    pParameters.setSpoolfile( null );
    if( extractstaticsoutfolder == null )
    {
      if( !staticsfolder.exists() )
      {
        extractstaticsoutfolder = staticsfolder;
      }
      else
      {
        extractstaticsoutfolder = new File( "target/extract/sql/statics" );
      }
    }
    pParameters.setSpoolfolder( "" + extractstaticsoutfolder );
 
    new OrcasExtractStatics().mainRun( pParameters );
  }
}
