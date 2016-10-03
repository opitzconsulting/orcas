package com.opitzconsulting.orcas.gradle;

public class OrcasCompileAllInvalidIfReplaceablesExistsTask extends OrcasCompileAllInvalidTask
{
  @Override
  protected boolean isRunOnlyIfReplaceablesExists()
  {
    return true;
  }
}
