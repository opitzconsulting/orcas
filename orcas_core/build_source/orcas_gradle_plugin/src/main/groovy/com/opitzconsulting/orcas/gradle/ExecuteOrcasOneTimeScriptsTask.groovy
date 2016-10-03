package com.opitzconsulting.orcas.gradle;

import java.io.File;

public class ExecuteOrcasOneTimeScriptsTask extends BaseOrcasOneTimeScriptsTask
{
  def scriptfolder;

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
