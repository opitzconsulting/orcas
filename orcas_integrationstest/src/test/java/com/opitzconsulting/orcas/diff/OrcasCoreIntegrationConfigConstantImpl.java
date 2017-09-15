package com.opitzconsulting.orcas.diff;

public class OrcasCoreIntegrationConfigConstantImpl extends OrcasCoreIntegrationConfigDefaults
{
  private String _workfolder = "./build/ide_tests/";
  private String _availableFeatureList = "";

  private String _jdbcUrl = "jdbc:oracle:thin:@localhost:1521:XE";
  private String _jdbcUser = "ORCAS_INTEGRATION_TEST_DBA";
  private String _jdbcPassword = _jdbcUser;

  private boolean _withSecondRunEmptyTest = true;
  private boolean _withRunWithSpoolTest = true;
  private boolean _withRunWithExtractTest = true;
  private String _executeTests = ".*column";

  private int _parallelThreads = 1;

  public String getWorkfolder()
  {
    return _workfolder;
  }

  public String getAvailableFeatureList()
  {
    return _availableFeatureList;
  }

  public boolean isWithSecondRunEmptyTest()
  {
    return _withSecondRunEmptyTest;
  }

  public boolean isWithRunWithSpoolTest()
  {
    return _withRunWithSpoolTest;
  }

  public boolean isWithRunWithExtractTest()
  {
    return _withRunWithExtractTest;
  }

  public String getExecuteTests()
  {
    return _executeTests;
  }

  public String getJdbcUrl()
  {
    return _jdbcUrl;
  }

  public String getJdbcUser()
  {
    return _jdbcUser;
  }

  public String getJdbcPassword()
  {
    return _jdbcPassword;
  }

  public int getParallelThreads()
  {
    return _parallelThreads;
  }

  public String getAlternateTablespace1()
  {
    return null;
  }

  public String getAlternateTablespace2()
  {
    return null;
  }
}
