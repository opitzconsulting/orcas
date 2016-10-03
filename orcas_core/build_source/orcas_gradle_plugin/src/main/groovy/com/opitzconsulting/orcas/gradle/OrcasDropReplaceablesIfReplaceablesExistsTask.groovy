package com.opitzconsulting.orcas.gradle;

public class OrcasDropReplaceablesIfReplaceablesExistsTask extends OrcasDropReplaceablesTask
{
  @Override
  protected boolean isRunOnlyIfReplaceablesExists()
  {
    return true;
  }
}
