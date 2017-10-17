package com.opitzconsulting.orcas.maven;

import java.io.File;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import de.opitzconsulting.orcas.diff.OrcasExtractReplaceables;
import de.opitzconsulting.orcas.diff.ParametersCall;

/**
 * Cretaes sql-scripts for replace objects existing in the database-schema.
 */
@Mojo( name = "extractReplaceables" )
public class OrcasExtractReplaceablesMojo extends BaseOrcasMojo
{
  @Parameter( defaultValue = "extract" )
  private String logname;

  @Override
  protected String getLogname()
  {
    return logname;
  }

  @Parameter
  protected File extractreplaceablesoutfolder;

  @Parameter
  protected File extractmodelinputfolder;

  /**
   * View extract-mode. "full" or "text". "full" includes schema-name and column
   * definitions.
   */
  @Parameter( defaultValue = "text" )
  private String viewextractmode;

  @Override
  protected void executeWithParameters( ParametersCall pParameters )
  {
    if( extractreplaceablesoutfolder == null )
    {
      if( !replaceablesfolder.exists() )
      {
        extractreplaceablesoutfolder = replaceablesfolder;
      }
      else
      {
        extractreplaceablesoutfolder = new File( "target/extract/sql/replaceables" );
      }
    }
    pParameters.setSpoolfolder( "" + extractreplaceablesoutfolder );
    pParameters.setViewExtractMode( viewextractmode );

    new OrcasExtractReplaceables().mainRun( pParameters );
  }
}
