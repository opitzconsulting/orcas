package com.opitzconsulting.orcas.maven;

import org.apache.maven.plugins.annotations.Mojo;

/**
 * Specialized version of <a href="dropReplaceables-mojo.html">dropReplaceables</a> which is only executed if replaceabels folder exists.
 */
@Mojo( name = "dropReplaceablesIfReplaceablesExists" )
public class OrcasDropReplaceablesIfReplaceablesExists extends OrcasDropReplaceables
{
  @Override
  protected boolean isRunOnlyIfReplaceablesExists()
  {
    return true;
  }
}
