package com.opitzconsulting.orcas.gradle;

import java.io.File;

import de.opitzconsulting.orcas.diff.OrcasExtractStatics;
import de.opitzconsulting.orcas.diff.ParametersCall;

public class OrcasExtractStaticsTask extends BaseOrcasTask
{
  @Override
  protected String getLogname()
  {
    return "extract";
  }

  @Override
  protected void executeOrcasTaskWithParameters( ParametersCall pParameters )
  {
    pParameters.setOrderColumnsByName( false );
    pParameters.setRemoveDefaultValuesFromModel( project.orcasconfiguration.extractremovedefaultvaluesfrommodel );
    pParameters.setModelFile( project.orcasconfiguration.extractmodelinputfolder );
    pParameters.setSpoolfile( null );
    String lExtractstaticsoutfolder;
    if( project.orcasconfiguration.extractstaticsoutfolder == null )
    {
      if( !new File(project.orcasconfiguration.staticsfolder).exists() )
      {
        lExtractstaticsoutfolder = project.orcasconfiguration.staticsfolder;
      }
      else
      {
        lExtractstaticsoutfolder = ".gradle/extract/sql/statics";
      }
    }
    else
    {
      lExtractstaticsoutfolder = project.orcasconfiguration.extractstaticsoutfolder;
    }
    pParameters.setSpoolfolder( lExtractstaticsoutfolder );
 
    new OrcasExtractStatics().mainRun( modifyParameters( pParameters ) );
  }
}
