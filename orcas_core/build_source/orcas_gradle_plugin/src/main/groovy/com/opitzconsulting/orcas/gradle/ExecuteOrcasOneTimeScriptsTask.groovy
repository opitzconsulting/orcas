package com.opitzconsulting.orcas.gradle

import org.gradle.api.tasks.Internal;

import java.io.File;

public class ExecuteOrcasOneTimeScriptsTask extends BaseOrcasOneTimeScriptsTask
{
  @Internal
  def scriptfolder;

  @Internal
  def logname;

  protected String getLogname()
  {
    return logname;
  }

  protected File getScriptfolder()
  {
    return project.file(scriptfolder);
  }
  
  @Override
  protected boolean isCheckFolderExists()
  {
    return false;
  }
}
