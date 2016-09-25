package com.opitzconsulting.orcas.maven;

import org.apache.maven.plugins.annotations.Mojo;

@Mojo( name = "dropReplaceablesIfReplaceablesExists" )
public class OrcasDropReplaceablesIfReplaceablesExists extends OrcasDropReplaceables
{
  @Override
  protected boolean isRunOnlyIfReplaceablesExists()
  {
    return true;
  }
}
