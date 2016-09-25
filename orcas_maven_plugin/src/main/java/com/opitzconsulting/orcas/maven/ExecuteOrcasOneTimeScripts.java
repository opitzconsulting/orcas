package com.opitzconsulting.orcas.maven;

import java.io.File;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo( name = "ExecuteOneTimeScripts" )
public class ExecuteOrcasOneTimeScripts extends BaseOrcasOneTimeScripts
{
  @Parameter
  private File scriptfolder;

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
