package com.opitzconsulting.orcas.diff;

public interface OrcasCoreIntegrationConfig
{
  String getUsernamePrefix();

  String getTablespace();

  String getDialect();

  String getWorkfolder();

  String getBaseDir();

  String getAvailableFeatureList();

  boolean isFlatTestNames();

  boolean isWithSecondRunEmptyTest();

  boolean isWithFirstRunTest();

  boolean isWithRunWithSpoolTest();

  boolean isWithRunWithExtractTest();

  String getExecuteTests();

  String getJdbcUrl();

  String getJdbcUser();

  String getJdbcPassword();

  String getAlternateTablespace1();

  String getAlternateTablespace2();

  int getParallelThreads();
}
