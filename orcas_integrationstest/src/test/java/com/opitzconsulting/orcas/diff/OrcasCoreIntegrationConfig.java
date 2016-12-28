package com.opitzconsulting.orcas.diff;

public interface OrcasCoreIntegrationConfig
{
  String getUsernamePrefix();

  String getTablespace();

  String getWorkfolder();

  String getBaseDir();

  String getAvailableFeatureList();

  boolean isFlatTestNames();

  boolean isWithSecondRunEmptyTest();

  boolean isWithRunWithSpoolTest();

  boolean isWithRunWithExtractTest();

  String getExecuteTests();

  String getJdbcUrl();

  String getJdbcUser();

  String getJdbcPassword();

  int getParallelThreads();
}