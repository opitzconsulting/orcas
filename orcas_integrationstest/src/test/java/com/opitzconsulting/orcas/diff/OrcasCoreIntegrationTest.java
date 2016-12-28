package com.opitzconsulting.orcas.diff;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.Assume;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Parameterized.UseParametersRunnerFactory;

import de.opitzconsulting.orcas.diff.OrcasExtractStatics;
import de.opitzconsulting.orcas.diff.OrcasMain;
import de.opitzconsulting.orcas.diff.OrcasScriptRunner;
import de.opitzconsulting.orcas.diff.Parameters.FailOnErrorMode;
import de.opitzconsulting.orcas.diff.Parameters.JdbcConnectParameters;
import de.opitzconsulting.orcas.diff.ParametersCall;

@RunWith( OrcasCoreIntegrationTest.MyOrcasParameterizedParallel.class )
@FixMethodOrder( MethodSorters.NAME_ASCENDING )
@UseParametersRunnerFactory( OrcasParameterizedParallel.OrcasParametersRunnerFactory.class )
public class OrcasCoreIntegrationTest
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

  private static final String EXTRACT_FOLDER_NAME = "extract";
  private static final String REFERENCE_NAME = "reference";
  static final String DEFAULT_EXCLUDE = "object_name like '%$%'";

  @Parameters( name = "{0}" )
  public static Collection<Object[]> data()
  {
    List<Object[]> lReturn = new ArrayList<Object[]>();

    for( File lFile : new File( orcasCoreIntegrationConfig.getBaseDir() + "tests" ).listFiles() )
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

  private TestSetup _testSetup;

  private String _lognameFirstRun = "first_run";

  private static Map<String,JdbcConnectParameters> _connectParametersTargetUserMap = new HashMap<String,JdbcConnectParameters>();

  private static List<String> parseReaderToLines( File pFile )
  {
    try
    {
      BufferedReader lBufferedReader = new BufferedReader( new InputStreamReader( new FileInputStream( pFile ) ) );

      List<String> lLines = new ArrayList<String>();

      String lFileLine;
      while( (lFileLine = lBufferedReader.readLine()) != null )
      {
        lLines.add( lFileLine );
      }
      lBufferedReader.close();

      return lLines;
    }
    catch( Exception e )
    {
      throw new RuntimeException( e );
    }
  }

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

    private boolean _dropmode;
    private boolean _indexparallelcreate;
    private boolean _createmissingfkindexes;
    private boolean _test_extract;
    private boolean _tablemovetablespace;
    private boolean _indexmovetablespace;
    private String _extensionfolder;
    private String _required_feature_list;
    private String _dateformat;
    private String _excludewheresequence;
    private boolean _customExtensionFolder;
    private boolean _availableFeatureRequirementMatched = true;

    public TestSetup( String pTestName )
    {
      try
      {
        testName = pTestName;

        Properties lDefaultProperties = new Properties();
        lDefaultProperties.load( new FileInputStream( orcasCoreIntegrationConfig.getBaseDir() + "/parameter.properties" ) );

        Properties lTestProperties = new Properties();
        String lTestPropertiesFileName = orcasCoreIntegrationConfig.getBaseDir() + "tests/" + pTestName + "/parameter.properties";
        if( new File( lTestPropertiesFileName ).exists() )
        {
          lTestProperties.load( new FileInputStream( lTestPropertiesFileName ) );
        }

        _dropmode = getBooleanProperty( "dropmode", lDefaultProperties, lTestProperties );
        _indexparallelcreate = getBooleanProperty( "indexparallelcreate", lDefaultProperties, lTestProperties );
        _createmissingfkindexes = getBooleanProperty( "createmissingfkindexes", lDefaultProperties, lTestProperties );
        _test_extract = getBooleanProperty( "test_extract", lDefaultProperties, lTestProperties );
        _tablemovetablespace = getBooleanProperty( "tablemovetablespace", lDefaultProperties, lTestProperties );
        _indexmovetablespace = getBooleanProperty( "indexmovetablespace", lDefaultProperties, lTestProperties );
        _extensionfolder = getProperty( "extensionfolder", lDefaultProperties, lTestProperties );
        _required_feature_list = getProperty( "required_feature_list", lDefaultProperties, lTestProperties );
        _dateformat = getProperty( "dateformat", lDefaultProperties, lTestProperties );
        _excludewheresequence = getProperty( "excludewheresequence", lDefaultProperties, lTestProperties );
        _excludewheresequence = _excludewheresequence.replaceAll( "''", "'" );

        _availableFeatureRequirementMatched = true;
        for( String lRequiredFeature : Arrays.asList( _required_feature_list.split( "," ) ) )
        {
          if( !orcasCoreIntegrationConfig.getAvailableFeatureList().contains( lRequiredFeature ) )
          {
            _availableFeatureRequirementMatched = false;
          }
        }

        if( !_extensionfolder.equals( "extensions" ) )
        {
          _customExtensionFolder = true;
        }
      }
      catch( Exception e )
      {
        throw new RuntimeException( e );
      }
    }

    private void assumeShouldExecuteTestcase()
    {
      Assume.assumeTrue( "testcase ignored custom extensionfolder not supported yet " + _extensionfolder, !_customExtensionFolder );
      Assume.assumeTrue( "testcase ignored: " + testName + " " + orcasCoreIntegrationConfig.getExecuteTests(), testName.matches( orcasCoreIntegrationConfig.getExecuteTests() ) );
      Assume.assumeTrue( "testcase skipped, required features not available: required: " + _required_feature_list + " available: " + orcasCoreIntegrationConfig.getAvailableFeatureList(), _availableFeatureRequirementMatched );
    }

    private boolean getBooleanProperty( String pKey, Properties pDefaultProperties, Properties pTestProperties )
    {
      String lValue = getProperty( pKey, pDefaultProperties, pTestProperties );

      return lValue.equals( "true" );
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
    synchronized( _connectParametersTargetUserMap )
    {
      if( !_connectParametersTargetUserMap.containsKey( testName ) )
      {
        _connectParametersTargetUserMap.put( testName, OrcasParallelConnectionHandler.createConnectionParametersForTargetUser() );
      }
    }

    _testSetup = new TestSetup( testName );
  }

  public void afterAllTests()
  {
    synchronized( _connectParametersTargetUserMap )
    {
      OrcasParallelConnectionHandler.returnConnectionParametersForTargetUser( getConnectParametersTargetUser() );
      _connectParametersTargetUserMap.put( testName, null );
    }
  }

  private JdbcConnectParameters getConnectParametersTargetUser()
  {
    synchronized( _connectParametersTargetUserMap )
    {
      return _connectParametersTargetUserMap.get( testName );
    }
  }

  private void resetUser( JdbcConnectParameters pJdbcConnectParameters )
  {
    OrcasParallelConnectionHandler.resetUser( pJdbcConnectParameters );
  }

  private static void executeScript( String pBaseDir, JdbcConnectParameters pJdbcConnectParameters, String pScriptName, String... pParameters )
  {
    ParametersCall lParametersCall = new ParametersCall();

    copyConnectionParameters( pJdbcConnectParameters, lParametersCall.getJdbcConnectParameters() );

    lParametersCall.setAdditionalParameters( Arrays.asList( pParameters ) );
    lParametersCall.setIsOneTimeScriptMode( false );
    lParametersCall.setFailOnErrorMode( FailOnErrorMode.IGNORE_DROP );

    lParametersCall.setModelFile( pBaseDir + pScriptName );

    new OrcasScriptRunner().mainRun( lParametersCall );
  }

  private static void executeScript( JdbcConnectParameters pJdbcConnectParameters, String pScriptName, String... pParameters )
  {
    executeScript( orcasCoreIntegrationConfig.getBaseDir(), pJdbcConnectParameters, pScriptName, pParameters );
  }

  private static void copyConnectionParameters( JdbcConnectParameters pSourceConnectParameters, JdbcConnectParameters pDestConnectParameters )
  {
    pDestConnectParameters.setJdbcDriver( pSourceConnectParameters.getJdbcDriver() );
    pDestConnectParameters.setJdbcUrl( pSourceConnectParameters.getJdbcUrl() );
    pDestConnectParameters.setJdbcUser( pSourceConnectParameters.getJdbcUser() );
    pDestConnectParameters.setJdbcPassword( pSourceConnectParameters.getJdbcPassword() );
  }

  private void asserSchemaEqual( String pName, boolean pIncludeData )
  {
    asserSchemaEqual( REFERENCE_NAME, pName, pIncludeData, testName, _testSetup._excludewheresequence, _testSetup._dateformat, getConnectParametersTargetUser() );
  }

  static void asserSchemaEqual( String pNameExpected, String pNameActual, boolean pIncludeData, String pTestName, String pExcludeWhereSequence, String pDateformat, JdbcConnectParameters pJdbcConnectParameters )
  {
    extractSchema( pJdbcConnectParameters, pNameActual, pIncludeData, pTestName, pExcludeWhereSequence, pDateformat );

    assertFilesEqual( pNameActual, getSchemaExtarctFileName( pNameExpected, pIncludeData, true, pTestName ), getSchemaExtarctFileName( pNameActual, pIncludeData, true, pTestName ), pIncludeData );
    assertFilesEqual( pNameActual, getSchemaExtarctFileName( pNameExpected, pIncludeData, false, pTestName ), getSchemaExtarctFileName( pNameActual, pIncludeData, false, pTestName ), pIncludeData );
  }

  private static void assertFilesEqual( String pName, String pExpectedFile, String pActualFile, boolean pIncludeData )
  {
    List<String> lExpectedLines = parseReaderToLines( new File( pExpectedFile ) );
    List<String> lActualLines = parseReaderToLines( new File( pActualFile ) );

    String lMessage = "schema-files not equal: " + pName + " " + pExpectedFile + " " + pActualFile;

    int lMinLines = Math.min( lExpectedLines.size(), lActualLines.size() );
    for( int i = 0; i < lMinLines; i++ )
    {
      if( pIncludeData || !lExpectedLines.get( i ).contains( "max_value_select" ) )
      {
        assertEquals( lMessage, lExpectedLines.get( i ), lActualLines.get( i ) );
      }
    }

    assertEquals( lMessage, lExpectedLines.size(), lActualLines.size() );
  }

  private void extractSchema( JdbcConnectParameters pConnectParametersTargetUser, String pName, boolean pIncludeData )
  {
    extractSchema( pConnectParametersTargetUser, pName, pIncludeData, testName, _testSetup._excludewheresequence, _testSetup._dateformat );
  }

  static void extractSchema( JdbcConnectParameters pConnectParametersTargetUser, String pName, boolean pIncludeData, String pTestName, String pExcludeWhereSequence, String pDateformat )
  {
    executeScript( pConnectParametersTargetUser, "spool.sql", getSchemaExtarctFileName( pName, pIncludeData, false, pTestName ), orcasCoreIntegrationConfig.getWorkfolder() + pTestName + "/tmp_extrcat_table_data.log", pIncludeData ? "1=1" : "1=2" );

    ParametersCall lParametersCall = new ParametersCall();

    copyConnectionParameters( pConnectParametersTargetUser, lParametersCall.getJdbcConnectParameters() );

    lParametersCall.setExcludewheresequence( pExcludeWhereSequence );
    lParametersCall.setExcludewheretable( DEFAULT_EXCLUDE );
    lParametersCall.setDateformat( pDateformat );

    lParametersCall.setSpoolfile( getSchemaExtarctFileName( pName, pIncludeData, true, pTestName ) );
    lParametersCall.setModelFile( null );
    lParametersCall.setScriptprefix( null );
    lParametersCall.setScriptpostfix( null );
    lParametersCall.setOrderColumnsByName( true );
    lParametersCall.setRemoveDefaultValuesFromModel( false );

    new OrcasExtractStatics().mainRun( lParametersCall );
  }

  private static String getSchemaExtarctFileName( String pString, boolean pIncludeData, boolean pOrcas, String pTestName )
  {
    return orcasCoreIntegrationConfig.getWorkfolder() + pTestName + "/" + pString + (pIncludeData ? "_data" : "_nodata") + (pOrcas ? "_orcas" : "_spool") + ".txt";
  }

  private void orcasExtract( JdbcConnectParameters p_connectParametersTargetUser, String pString )
  {
    String lSpoolFolder = getSpoolFolder( pString );

    ParametersCall lParametersCall = new ParametersCall();

    copyConnectionParameters( p_connectParametersTargetUser, lParametersCall.getJdbcConnectParameters() );

    lParametersCall.setExcludewheresequence( _testSetup._excludewheresequence );
    lParametersCall.setExcludewheretable( DEFAULT_EXCLUDE );
    lParametersCall.setDateformat( _testSetup._dateformat );

    lParametersCall.setSpoolfile( null );
    lParametersCall.setSpoolfolder( lSpoolFolder );
    lParametersCall.setModelFile( null );
    lParametersCall.setScriptprefix( null );
    lParametersCall.setScriptpostfix( null );
    lParametersCall.setOrderColumnsByName( false );
    lParametersCall.setRemoveDefaultValuesFromModel( true );

    new OrcasExtractStatics().mainRun( lParametersCall );
  }

  private void executeOrcasStatics( JdbcConnectParameters pJdbcConnectParameters, String pSpoolName )
  {
    executeOrcasStatics( pJdbcConnectParameters, pSpoolName, orcasCoreIntegrationConfig.getBaseDir() + "tests/" + testName + "/tabellen" );
  }

  private void executeOrcasStatics( JdbcConnectParameters pJdbcConnectParameters, String pSpoolName, String pModelFolder )
  {
    ParametersCall lParametersCall = new ParametersCall();

    copyConnectionParameters( pJdbcConnectParameters, lParametersCall.getJdbcConnectParameters() );

    lParametersCall.setModelFile( pModelFolder );
    lParametersCall.setSqlplustable( false );
    lParametersCall.setTargetplsql( null );
    lParametersCall.setScriptprefix( null );
    lParametersCall.setScriptpostfix( "sql" );
    lParametersCall.setExcludewheresequence( _testSetup._excludewheresequence );
    lParametersCall.setExcludewheretable( DEFAULT_EXCLUDE );
    lParametersCall.setOrderColumnsByName( false );
    lParametersCall.setCreatemissingfkindexes( _testSetup._createmissingfkindexes );
    lParametersCall.setDropmode( _testSetup._dropmode );
    lParametersCall.setLogonly( false );
    lParametersCall.setLogname( pSpoolName );
    lParametersCall.setSpoolfolder( getSpoolFolder( pSpoolName ) );
    lParametersCall.setTablemovetablespace( _testSetup._tablemovetablespace );
    lParametersCall.setIndexmovetablespace( _testSetup._indexmovetablespace );
    lParametersCall.setIndexparallelcreate( _testSetup._indexparallelcreate );

    new OrcasMain().mainRun( lParametersCall );
  }

  private String getSpoolFolder( String pSpoolName )
  {
    return orcasCoreIntegrationConfig.getWorkfolder() + testName + "/" + pSpoolName;
  }

  private void assumeTestNotSkipped( boolean pNoSkipTest )
  {
    Assume.assumeTrue( "test skipped", pNoSkipTest );
  }

  @Test
  public void test_00_setup_reference()
  {
    deleteRecursive( new File( orcasCoreIntegrationConfig.getWorkfolder() + testName ) );

    resetUser( getConnectParametersTargetUser() );

    executeScript( getConnectParametersTargetUser(), "tests/" + testName + "/" + "erzeuge_zielzustand.sql" );

    extractSchema( getConnectParametersTargetUser(), REFERENCE_NAME, true );

    if( orcasCoreIntegrationConfig.isWithRunWithExtractTest() )
    {
      extractSchema( getConnectParametersTargetUser(), REFERENCE_NAME, false );
      orcasExtract( getConnectParametersTargetUser(), EXTRACT_FOLDER_NAME );
    }
  }

  @Test
  public void test_01_update_statics()
  {
    resetUser( getConnectParametersTargetUser() );
    executeScript( getConnectParametersTargetUser(), "tests/" + testName + "/" + "erzeuge_ausgangszustand.sql" );

    executeOrcasStatics( getConnectParametersTargetUser(), _lognameFirstRun );
    asserSchemaEqual( "orcas_run", true );
  }

  @Test
  public void test_02_second_run_empty()
  {
    assumeTestNotSkipped( orcasCoreIntegrationConfig.isWithSecondRunEmptyTest() );

    String lLognameSecondRun = "spool_second_run";
    executeOrcasStatics( getConnectParametersTargetUser(), lLognameSecondRun );
    assertFalse( "second run not empty", new File( getSpoolFolder( lLognameSecondRun ) ).exists() );
  }

  @Test
  public void test_03_run_with_spool()
  {
    assumeTestNotSkipped( orcasCoreIntegrationConfig.isWithRunWithSpoolTest() );

    resetUser( getConnectParametersTargetUser() );
    executeScript( getConnectParametersTargetUser(), "tests/" + testName + "/" + "erzeuge_ausgangszustand.sql" );
    executeScript( orcasCoreIntegrationConfig.getWorkfolder() + testName + "/" + _lognameFirstRun + "/", getConnectParametersTargetUser(), "master_install.sql" );
    asserSchemaEqual( "protocol_run", true );
  }

  @Test
  public void test_04_run_with_extract()
  {
    try
    {
      assumeTestNotSkipped( orcasCoreIntegrationConfig.isWithRunWithExtractTest() );
      Assume.assumeTrue( "extract test not possible for testcase", _testSetup._test_extract );

      resetUser( getConnectParametersTargetUser() );
      executeOrcasStatics( getConnectParametersTargetUser(), "spool_extract", orcasCoreIntegrationConfig.getWorkfolder() + testName + "/" + EXTRACT_FOLDER_NAME );
      asserSchemaEqual( "extract_run", false );
    }
    finally
    {
      afterAllTests();
    }
  }
}
