package com.opitzconsulting.orcas.maven;

import org.apache.maven.plugins.annotations.Mojo;

/**
 * Specialized version of <a href="compileAllInvalid-mojo.html">compileAllInvalid</a> which is only executed if replaceabels folder exists.
 */
@Mojo( name = "compileAllInvalidIfReplaceablesExists" )
public class OrcasCompileAllInvalidIfReplaceablesExists extends OrcasCompileAllInvalid
{
  @Override
  protected boolean isRunOnlyIfReplaceablesExists()
  {
    return true;
  }
}
