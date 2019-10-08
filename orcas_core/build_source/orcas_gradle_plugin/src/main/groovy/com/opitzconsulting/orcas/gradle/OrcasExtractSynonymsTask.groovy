package com.opitzconsulting.orcas.gradle

import de.opitzconsulting.orcas.diff.OrcasExtractSynonyms
import de.opitzconsulting.orcas.diff.ParametersCall

public class OrcasExtractSynonymsTask extends BaseOrcasTask
{
  @Override
  protected String getLogname()
  {
    return "extract";
  }

  @Override
  protected void executeOrcasTaskWithParameters( ParametersCall pParameters )
  {
    String lExtractsynonymsfile;
    if( project.orcasconfiguration.extractsynonymsfile == null )
    {
      lExtractsynonymsfile = convertOutFile( "extract/scripts/synonyms/synonyms.sql" );
    }
    else
    {
      lExtractsynonymsfile = project.orcasconfiguration.extractsynonymsfile;
    }
    pParameters.setSpoolfile( lExtractsynonymsfile );
    pParameters.setExcludewheresynonym( project.orcasconfiguration.excludewheresynonym );
 
    new OrcasExtractSynonyms().mainRun( modifyParameters( pParameters ) );
  }
}
