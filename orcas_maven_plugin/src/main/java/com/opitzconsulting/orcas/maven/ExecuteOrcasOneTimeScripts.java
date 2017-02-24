package com.opitzconsulting.orcas.maven;

import java.io.File;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Execute sql-scripts only once per schema. The exceution is logged in the tbale orcas_updates. This table will be created in the current schema unless the paramater orcasusername is used.
 */
@Mojo( name = "ExecuteOneTimeScripts" )
public class ExecuteOrcasOneTimeScripts extends BaseOrcasOneTimeScripts
{
  /**
   * The folder that contains the scripts to be exceutd. 
   */
  @Parameter
  private File scriptfolder;

  /**
   * The exceution of the scripts gets loged with this logname (in addition to the filnema within scriptfolder).
   */
  @Parameter
  private String logname;

  @Override
  protected String getLogname()
  {
    return logname;
  }

  @Override
  protected File getScriptfolder()
  {
    return scriptfolder;
  }
  
  @Override
  protected boolean isCheckFolderExists()
  {
    return false;
  }
}
