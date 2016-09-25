package com.opitzconsulting.orcas.maven;

import org.apache.maven.plugins.annotations.Mojo;

@Mojo( name = "compileAllInvalidIfReplaceablesExists" )
public class OrcasCompileAllInvalidIfReplaceablesExists extends OrcasCompileAllInvalid
{
  @Override
  protected boolean isRunOnlyIfReplaceablesExists()
  {
    return true;
  }
}
