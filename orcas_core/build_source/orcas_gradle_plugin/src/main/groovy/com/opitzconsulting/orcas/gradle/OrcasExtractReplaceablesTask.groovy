package com.opitzconsulting.orcas.gradle;

import java.io.File;

import de.opitzconsulting.orcas.diff.OrcasExtractReplaceables;
import de.opitzconsulting.orcas.diff.ParametersCall;

public class OrcasExtractReplaceablesTask extends BaseOrcasTask
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
    String lExtractreplaceablesoutfolder;
    if( project.orcasconfiguration.extractreplaceablesoutfolder == null )
    {
      if( !new File(project.orcasconfiguration.replaceablesfolder).exists() )
      {
        lExtractreplaceablesoutfolder = project.orcasconfiguration.replaceablesfolder;
      }
      else
      {
        lExtractreplaceablesoutfolder = ".gradle/extract/sql/replaceables";
      }
    }
    else
    {
      lExtractreplaceablesoutfolder = project.orcasconfiguration.extractreplaceablesoutfolder;
    }
    pParameters.setSpoolfolder( lExtractreplaceablesoutfolder );
    pParameters.setViewExtractMode( project.orcasconfiguration.viewextractmode );
 
    new OrcasExtractReplaceables().mainRun( modifyParameters( pParameters ) );
  }
}
