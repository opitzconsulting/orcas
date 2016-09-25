package com.opitzconsulting.orcas.maven;

import java.io.File;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo( name = "oneTimeScriptsPreStatics" )
public class OrcasOneTimeScriptsPreStatics extends BaseOrcasOneTimeScripts
{
  @Parameter( defaultValue = "src/main/scripts/pre-statics" )
  private File scriptfolder;

  @Override
  protected File getScriptfolder()
  {
    return scriptfolder;
  }

  @Parameter( defaultValue = "pre-statics" )
  private String logname;

  @Override
  protected String getLogname()
  {
    return logname;
  }
}
