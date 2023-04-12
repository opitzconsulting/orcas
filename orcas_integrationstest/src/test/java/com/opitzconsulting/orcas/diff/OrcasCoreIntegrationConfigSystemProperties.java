package com.opitzconsulting.orcas.diff;

public class OrcasCoreIntegrationConfigSystemProperties extends OrcasCoreIntegrationConfigDefaults
{
  public static OrcasCoreIntegrationConfig getOrcasCoreIntegrationConfig()
  {
    OrcasCoreIntegrationConfigSystemProperties lOrcasCoreIntegrationConfigSystemProperties = new OrcasCoreIntegrationConfigSystemProperties();

    try
    {
      lOrcasCoreIntegrationConfigSystemProperties.getJdbcUrl();

      return lOrcasCoreIntegrationConfigSystemProperties;
    }
    catch( Exception e )
    {
      return new OrcasCoreIntegrationConfigConstantImpl();
    }
  }

  private String getStringProperty( String pString )
  {
    String lProperty = System.getProperty( pString );

    if( lProperty == null )
    {
      throw new IllegalArgumentException( pString );
    }

    return lProperty;
  }

  private boolean getBooleanSystemProperty( String pString )
  {
    return getStringProperty( pString ).equals( "true" );
  }

  public String getDialect() {
    return getStringProperty( "orcas.integrationtest.dialect" );
  }

  public String getWorkfolder()
  {
    return getStringProperty( "orcas.integrationtest.workfolder" );
  }

  public String getAvailableFeatureList()
  {
    return getStringProperty( "orcas.integrationtest.available_feature_list" );
  }

  public boolean isWithSecondRunEmptyTest()
  {
    return getBooleanSystemProperty( "orcas.integrationtest.with_second_run_empty_test" );
  }

  public boolean isWithFirstRunTest()
  {
    return getBooleanSystemProperty( "orcas.integrationtest.with_first_run_test" );
  }

  public boolean isWithRunWithSpoolTest()
  {
    return getBooleanSystemProperty( "orcas.integrationtest.with_run_with_spool_test" );
  }

  public boolean isWithRunWithExtractTest()
  {
    return getBooleanSystemProperty( "orcas.integrationtest.with_run_with_extract_test" );
  }

  public String getExecuteTests()
  {
    return getStringProperty( "orcas.integrationtest.execute_tests" );
  }

  public String getJdbcUrl()
  {
    return getStringProperty( "orcas.integrationtest.jdbc_url" );
  }

  public String getJdbcUser()
  {
    return getStringProperty( "orcas.integrationtest.jdbc_user" );
  }

  public String getJdbcPassword()
  {
    return getStringProperty( "orcas.integrationtest.jdbc_password" );
  }

  public int getParallelThreads()
  {
    return Integer.parseInt( getStringProperty( "junit.parallel.threads" ) );
  }

  @Override
  public boolean isFlatTestNames()
  {
    return true;
  }

  public String getAlternateTablespace1()
  {
    return getStringProperty( "orcas.integrationtest.alternate_tablespace_1" );
  }

  public String getAlternateTablespace2()
  {
    return getStringProperty( "orcas.integrationtest.alternate_tablespace_2" );
  }
}
