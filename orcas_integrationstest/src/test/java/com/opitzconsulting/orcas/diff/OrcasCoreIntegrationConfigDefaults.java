package com.opitzconsulting.orcas.diff;

public abstract class OrcasCoreIntegrationConfigDefaults implements OrcasCoreIntegrationConfig
{
  public String getUsernamePrefix()
  {
    return "orcas_ut_";
  }

  public String getTablespace()
  {
    return "USERS";
  }

  public String getBaseDir()
  {
    return "./";
  }

  public String getAvailableFeatureList()
  {
    return "";
  }

  public boolean isFlatTestNames()
  {
    return false;
  }

  public boolean isWithSecondRunEmptyTest()
  {
    return true;
  }

  public boolean isWithRunWithSpoolTest()
  {
    return true;
  }

  public boolean isWithRunWithExtractTest()
  {
    return true;
  }

  public String getExecuteTests()
  {
    return ".*";
  }
}
