package com.opitzconsulting.orcas.maven;

import java.io.File;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Specialized version of <a href="ExecuteOneTimeScripts-mojo.html">ExecuteOneTimeScripts</a> which is added at the end of the build cycle.
 */
@Mojo( name = "oneTimeScriptsPostCompile" )
public class OrcasOneTimeScriptsPostCompile extends BaseOrcasOneTimeScripts
{
  /**
   * The folder conataining the one-time-sql-scripts.
   */
  @Parameter( defaultValue = "src/main/scripts/post-compile" )
  private File scriptfolder;

  @Override
  protected File getScriptfolder()
  {
    return scriptfolder;
  }

  /**
   * The logname for spooling.
   */
  @Parameter( defaultValue = "post-compile" )
  private String logname;

  @Override
  protected String getLogname()
  {
    return logname;
  }
}
