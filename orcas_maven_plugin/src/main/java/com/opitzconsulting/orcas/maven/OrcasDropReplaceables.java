package com.opitzconsulting.orcas.maven;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import de.opitzconsulting.orcas.diff.OrcasScriptRunner;
import de.opitzconsulting.orcas.diff.ParametersCall;
import com.opitzconsulting.orcas.dbobjects.SqlplusDirAccessDbobjects;

@Mojo( name = "dropReplaceables" )
public class OrcasDropReplaceables extends BaseOrcasMojo
{
  @Parameter( defaultValue = "drop-replaceables" )
  private String logname;

  @Override
  protected String getLogname()
  {
    return logname;
  }

  @Parameter( defaultValue = "object_name not like '%'" )
  private String excludewhereview;

  @Parameter( defaultValue = "object_name not like '%'" )
  private String excludewhereobjecttype;

  @Parameter( defaultValue = "object_name not like '%'" )
  private String excludewherepackage;

  @Parameter( defaultValue = "object_name not like '%'" )
  private String excludewheretrigger;

  @Parameter( defaultValue = "object_name not like '%'" )
  private String excludewherefunction;

  @Parameter( defaultValue = "object_name not like '%'" )
  private String excludewhereprocedure;

  @Override
  protected void executeWithParameters( ParametersCall pParameters )
  {
    if( !isRunOnlyIfReplaceablesExists() || replaceablesfolder.exists() )
    {
      pParameters.setIsOneTimeScriptMode( false );
      List<String> lAdditionalParameters = new ArrayList<String>();
      pParameters.setAdditionalParameters( lAdditionalParameters );

      pParameters.setScriptUrl( SqlplusDirAccessDbobjects.getURL_delete_replacable_objects(), "delete_replacable_objects.sql" );
      lAdditionalParameters.clear();
      lAdditionalParameters.add( excludewherepackage );
      lAdditionalParameters.add( excludewheretrigger );
      lAdditionalParameters.add( excludewhereview );
      lAdditionalParameters.add( excludewherefunction );
      lAdditionalParameters.add( excludewhereprocedure );
      new OrcasScriptRunner().mainRun( pParameters );

      pParameters.setScriptUrl( SqlplusDirAccessDbobjects.getURL_drop_all_types(), "drop_all_types.sql" );
      lAdditionalParameters.clear();
      lAdditionalParameters.add( excludewhereobjecttype );
      new OrcasScriptRunner().mainRun( pParameters );
    }
    else
    {
      getLog().info( "no replaceables found" );
    }
  }

  protected boolean isRunOnlyIfReplaceablesExists()
  {
    return false;
  }
}
