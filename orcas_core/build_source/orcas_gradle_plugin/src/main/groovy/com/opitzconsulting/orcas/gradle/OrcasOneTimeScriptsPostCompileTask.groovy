package com.opitzconsulting.orcas.gradle;

import java.io.File;

public class OrcasOneTimeScriptsPostCompileTask extends BaseOrcasOneTimeScriptsTask
{
  private String scriptfolder = "src/main/scripts/post-compile";

  @Override
  protected File getScriptfolder()
  {
    return project.file(scriptfolder);
  }

  private String logname = "post-compile";

  @Override
  protected String getLogname()
  {
    return logname;
  }
}
