package com.opitzconsulting.orcas.gradle

import de.opitzconsulting.orcas.diff.OrcasExtractGrants
import de.opitzconsulting.orcas.diff.ParametersCall

public class OrcasExtractGrantsTask extends BaseOrcasTask
{
  @Override
  protected String getLogname()
  {
    return "extract";
  }

  @Override
  protected void executeOrcasTaskWithParameters( ParametersCall pParameters )
  {
    String lExtractgrantsfile;
    if( project.orcasconfiguration.extractgrantsfile == null )
    {
      lExtractgrantsfile = convertOutFile( "extract/scripts/grants/grants.sql" );
    }
    else
    {
      lExtractgrantsfile = project.orcasconfiguration.extractgrantsfile;
    }
    pParameters.setSpoolfile( lExtractgrantsfile );
    pParameters.setExcludewheregrant( project.orcasconfiguration.excludewheregrant );
 
    new OrcasExtractGrants().mainRun( modifyParameters( pParameters ) );
  }
}
