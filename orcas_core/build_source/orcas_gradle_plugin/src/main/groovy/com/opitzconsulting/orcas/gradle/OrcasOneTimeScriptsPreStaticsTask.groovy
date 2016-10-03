package com.opitzconsulting.orcas.gradle;

import java.io.File;

public class OrcasOneTimeScriptsPreStaticsTask extends BaseOrcasOneTimeScriptsTask
{
  private String scriptfolder = "src/main/scripts/pre-statics";

  @Override
  protected File getScriptfolder()
  {
    return project.file(scriptfolder);
  }

  private String logname = "pre-statics";

  @Override
  protected String getLogname()
  {
    return logname;
  }
}
