package com.opitzconsulting.orcas.maven;

import java.io.File;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo( name = "oneTimeScriptsPostCompile" )
public class OrcasOneTimeScriptsPostCompile extends BaseOrcasOneTimeScripts
{
  @Parameter( defaultValue = "src/main/scripts/post-compile" )
  private File scriptfolder;

  @Override
  protected File getScriptfolder()
  {
    return scriptfolder;
  }

  @Parameter( defaultValue = "post-compile" )
  private String logname;

  @Override
  protected String getLogname()
  {
    return logname;
  }
}
