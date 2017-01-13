package com.opitzconsulting.orcas.diff;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.ComparisonFailure;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Parameterized.UseParametersRunnerFactory;

import de.opitzconsulting.orcas.diff.OrcasScriptRunner;
import de.opitzconsulting.orcas.diff.Parameters.FailOnErrorMode;
import de.opitzconsulting.orcas.diff.Parameters.JdbcConnectParameters;
import de.opitzconsulting.orcas.diff.ParametersCall;

@RunWith( OrcasCoreSpoolTest.MyOrcasParameterizedParallel.class )
@UseParametersRunnerFactory( OrcasParameterizedParallel.OrcasParametersRunnerFactory.class )
public class OrcasCoreSpoolTest
{
  public static class MyOrcasParameterizedParallel extends OrcasParameterizedParallel
  {
    public MyOrcasParameterizedParallel( Class<?> pKlass ) throws Throwable
    {
      super( pKlass );
    }

    @Override
    public void assumeShouldExecuteTestcase( String pTestName )
    {
      new TestSetup( pTestName ).assumeShouldExecuteTestcase();
    }
  }

  private static OrcasCoreIntegrationConfig orcasCoreIntegrationConfig = OrcasCoreIntegrationConfigSystemProperties.getOrcasCoreIntegrationConfig();

  @Parameters( name = "{0}" )
  public static Collection<Object[]> data()
  {
    List<Object[]> lReturn = new ArrayList<Object[]>();

    for( File lFile : new File( orcasCoreIntegrationConfig.getBaseDir() + "testspool/tests" ).listFiles() )
    {
      if( lFile.getName().startsWith( "test_" ) )
      {
        lReturn.add( new Object[] { lFile.getName() } );
      }
    }

    return lReturn;
  }

  @Parameter
  public String testName;

  private JdbcConnectParameters _connectParametersTargetUser;

  private void deleteRecursive( File pFile )
  {
    if( pFile.isDirectory() )
    {
      for( String lFile : pFile.list() )
      {
        deleteRecursive( new File( pFile, lFile ) );
      }
    }

    pFile.delete();
  }

  private static class TestSetup
  {
    private String testName;

    private String _required_feature_list;
    private boolean _availableFeatureRequirementMatched = true;

    public TestSetup( String pTestName )
    {
      try
      {
        testName = pTestName;

        Properties lDefaultProperties = new Properties();
        lDefaultProperties.load( new FileInputStream( orcasCoreIntegrationConfig.getBaseDir() + "/parameter.properties" ) );

        Properties lTestProperties = new Properties();
        String lTestPropertiesFileName = orcasCoreIntegrationConfig.getBaseDir() + "testspool/tests/" + pTestName + "/parameter.properties";
        if( new File( lTestPropertiesFileName ).exists() )
        {
          lTestProperties.load( new FileInputStream( lTestPropertiesFileName ) );
        }

        _required_feature_list = getProperty( "required_feature_list", lDefaultProperties, lTestProperties );

        _availableFeatureRequirementMatched = true;
        for( String lRequiredFeature : Arrays.asList( _required_feature_list.split( "," ) ) )
        {
          if( !orcasCoreIntegrationConfig.getAvailableFeatureList().contains( lRequiredFeature ) )
          {
            _availableFeatureRequirementMatched = false;
          }
        }
      }
      catch( Exception e )
      {
        throw new RuntimeException( e );
      }
    }

    private void assumeShouldExecuteTestcase()
    {
      Assume.assumeTrue( "testcase ignored: " + testName + " " + orcasCoreIntegrationConfig.getExecuteTests(), testName.matches( orcasCoreIntegrationConfig.getExecuteTests() ) );
      Assume.assumeTrue( "testcase skipped, required features not available: required: " + _required_feature_list + " available: " + orcasCoreIntegrationConfig.getAvailableFeatureList(), _availableFeatureRequirementMatched );
    }

    private String getProperty( String pKey, Properties pDefaultProperties, Properties pTestProperties )
    {
      if( pTestProperties.getProperty( pKey ) != null )
      {
        return pTestProperties.getProperty( pKey );
      }

      return pDefaultProperties.getProperty( pKey );
    }
  }

  @Before
  public void beforeTest() throws Exception
  {
    _connectParametersTargetUser = OrcasParallelConnectionHandler.createConnectionParametersForTargetUser();
  }

  @After
  public void afterTest() throws Exception
  {
    OrcasParallelConnectionHandler.returnConnectionParametersForTargetUser( _connectParametersTargetUser );
    _connectParametersTargetUser = null;
  }

  private void resetUser( JdbcConnectParameters pJdbcConnectParameters )
  {
    OrcasParallelConnectionHandler.resetUser( _connectParametersTargetUser );
  }

  private void executeScript( String pBaseDir, JdbcConnectParameters pJdbcConnectParameters, String pScriptName, String... pParameters )
  {
    ParametersCall lParametersCall = new ParametersCall();

    copyConnectionParameters( pJdbcConnectParameters, lParametersCall.getJdbcConnectParameters() );

    lParametersCall.setAdditionalParameters( Arrays.asList( pParameters ) );
    lParametersCall.setIsOneTimeScriptMode( false );
    lParametersCall.setFailOnErrorMode( FailOnErrorMode.IGNORE_DROP );

    lParametersCall.setModelFile( pBaseDir + pScriptName );

    new OrcasScriptRunner().mainRun( lParametersCall );
  }

  private void executeScript( JdbcConnectParameters pJdbcConnectParameters, String pScriptName, String... pParameters )
  {
    executeScript( orcasCoreIntegrationConfig.getBaseDir(), pJdbcConnectParameters, pScriptName, pParameters );
  }

  private void copyConnectionParameters( JdbcConnectParameters pSourceConnectParameters, JdbcConnectParameters pDestConnectParameters )
  {
    pDestConnectParameters.setJdbcDriver( pSourceConnectParameters.getJdbcDriver() );
    pDestConnectParameters.setJdbcUrl( pSourceConnectParameters.getJdbcUrl() );
    pDestConnectParameters.setJdbcUser( pSourceConnectParameters.getJdbcUser() );
    pDestConnectParameters.setJdbcPassword( pSourceConnectParameters.getJdbcPassword() );
  }

  @Test( expected = ComparisonFailure.class )
  public void test()
  {
    String lTestFolderName = "spool_" + testName;
    deleteRecursive( new File( orcasCoreIntegrationConfig.getWorkfolder() + lTestFolderName ) );

    resetUser( _connectParametersTargetUser );
    executeScript( _connectParametersTargetUser, "testspool/tests/" + testName + "/" + "a.sql", orcasCoreIntegrationConfig.getAlternateTablespace1(), orcasCoreIntegrationConfig.getAlternateTablespace2() );
    OrcasCoreIntegrationTest.extractSchema( _connectParametersTargetUser, "a", true, lTestFolderName, OrcasCoreIntegrationTest.DEFAULT_EXCLUDE, "dd.mm.yyyy" );

    resetUser( _connectParametersTargetUser );
    executeScript( _connectParametersTargetUser, "testspool/tests/" + testName + "/" + "b.sql", orcasCoreIntegrationConfig.getAlternateTablespace1(), orcasCoreIntegrationConfig.getAlternateTablespace2() );
    OrcasCoreIntegrationTest.asserSchemaEqual( "a", "b", true, lTestFolderName, OrcasCoreIntegrationTest.DEFAULT_EXCLUDE, "dd.mm.yyyy", _connectParametersTargetUser );
  }
}
