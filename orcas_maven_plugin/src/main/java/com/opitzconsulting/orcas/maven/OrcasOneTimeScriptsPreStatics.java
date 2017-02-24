package com.opitzconsulting.orcas.maven;

import java.io.File;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Specialized version of <a href="ExecuteOneTimeScripts-mojo.html">ExecuteOneTimeScripts</a> which is added at the beginning of the build cycle.
 */
@Mojo( name = "oneTimeScriptsPreStatics" )
public class OrcasOneTimeScriptsPreStatics extends BaseOrcasOneTimeScripts
{
  /**
   * The folder conataining the one-time-sql-scripts.
   */
  @Parameter( defaultValue = "src/main/scripts/pre-statics" )
  private File scriptfolder;

  @Override
  protected File getScriptfolder()
  {
    return scriptfolder;
  }

  /**
   * The logname for spooling.
   */
  @Parameter( defaultValue = "pre-statics" )
  private String logname;

  @Override
  protected String getLogname()
  {
    return logname;
  }
}
