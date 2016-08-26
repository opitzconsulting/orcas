package de.opitzconsulting.orcas.diff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

public class Parameters
{
  public class JdbcConnectParameters
  {
    private String _jdbcDriver;
    private String _jdbcUrl;
    private String _jdbcUser;
    private String _jdbcPassword;

    public String getJdbcDriver()
    {
      return _jdbcDriver;
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
  }

  public static enum ParameterTypeMode
  {
    ORCAS_MAIN, ORCAS_LOAD_EXTRACT, ORCAS_EXTRACT_VIEWS, ORCAS_CHECK_CONNECTION, ORCAS_SCRIPT
  }

  public static enum FailOnErrorMode
  {
    NEVER, ALWAYS, IGNORE_DROP
  }

  private JdbcConnectParameters _jdbcConnectParameters;
  private JdbcConnectParameters _srcJdbcConnectParameters;
  private Boolean _logonly;
  private Boolean _dropmode;
  private Boolean _indexparallelcreate;
  private Boolean _indexmovetablespace;
  private Boolean _tablemovetablespace;
  private Boolean _createmissingfkindexes;
  private String _modelFile;
  private String _spoolfile;
  private String _excludewheretable;
  private String _excludewheresequence;
  private String _dateformat;
  private String _orcasDbUser;
  private Boolean _scriptfolderrecursive;
  private String _scriptprefix;
  private String _scriptpostfix;
  private String _loglevel;
  private String _targetplsql;
  private Boolean _sqlplustable;
  private Boolean _orderColumnsByName;
  private Boolean _removeDefaultValuesFromModel;
  private String _viewExtractMode;
  private List<String> _additionalParameters;
  private String _logname;
  private String _spoolfolder;
  private String _prefix;
  private String _failonerror;

  public Parameters( String[] pArgs, ParameterTypeMode pParameterTypeMode )
  {
    if( pArgs.length == 0 )
    {
      _jdbcConnectParameters = new JdbcConnectParameters();
      _jdbcConnectParameters._jdbcDriver = "oracle.jdbc.OracleDriver";
      _srcJdbcConnectParameters = null;
      _logonly = true;
      _dropmode = false;
      _indexparallelcreate = true;
      _indexmovetablespace = true;
      _tablemovetablespace = true;
      _createmissingfkindexes = true;
      _modelFile = null;
      _spoolfile = "";
      _excludewheretable = "object_name like '%$%'";
      _excludewheresequence = "object_name like '%$%'";
      _dateformat = "dd.mm.yy";
      _orcasDbUser = "";
      _scriptfolderrecursive = false;
      _scriptprefix = "";
      _scriptpostfix = "";
      _loglevel = "debug";
      _targetplsql = "";
      _sqlplustable = false;
      _orderColumnsByName = false;

      if( pParameterTypeMode == ParameterTypeMode.ORCAS_MAIN )
      {
        _jdbcConnectParameters._jdbcUrl = "jdbc:oracle:thin:@10.1.40.36:1521:ORCL";
        _jdbcConnectParameters._jdbcUser = "ORCAS_ITT_UEBERFUEHRUNG";
        _jdbcConnectParameters._jdbcPassword = "xxx";
        _orcasDbUser = "ORCAS_ITT_SVW_USER";
        _modelFile = "D:\\2_orcas\\orcas\\orcas_integrationstest\\tests\\test_comment\\tabellen";
      }

      return;
    }

    Map<Integer,String> lParameterMap = new HashMap<Integer,String>();

    for( int i = 0; i < pArgs.length; i++ )
    {
      lParameterMap.put( i, pArgs[i] );
    }

    if( pParameterTypeMode == ParameterTypeMode.ORCAS_SCRIPT )
    {
      _prefix = "prefix_";
    }

    _jdbcConnectParameters = new JdbcConnectParameters();
    _jdbcConnectParameters._jdbcDriver = getParameterString( lParameterMap.get( 0 ) );
    _jdbcConnectParameters._jdbcUrl = getParameterString( lParameterMap.get( 1 ) );
    _jdbcConnectParameters._jdbcUser = getParameterString( lParameterMap.get( 2 ) );
    _jdbcConnectParameters._jdbcPassword = getParameterString( lParameterMap.get( 3 ) );

    if( pParameterTypeMode == ParameterTypeMode.ORCAS_MAIN )
    {
      _modelFile = getParameterString( lParameterMap.get( 4 ) );

      _spoolfile = getParameterString( lParameterMap.get( 5 ) );

      _logonly = getParameterFlag( lParameterMap.get( 6 ) );
      _dropmode = getParameterFlag( lParameterMap.get( 7 ) );
      _indexparallelcreate = getParameterFlag( lParameterMap.get( 8 ) );
      _indexmovetablespace = getParameterFlag( lParameterMap.get( 9 ) );
      _tablemovetablespace = getParameterFlag( lParameterMap.get( 10 ) );
      _createmissingfkindexes = getParameterFlag( lParameterMap.get( 11 ) );

      _excludewheretable = cleanupExcludeWhere( lParameterMap.get( 12 ) );
      _excludewheresequence = cleanupExcludeWhere( lParameterMap.get( 13 ) );

      _dateformat = getParameterString( lParameterMap.get( 14 ) );

      _orcasDbUser = getParameterString( lParameterMap.get( 15 ) );

      _scriptfolderrecursive = getParameterFlag( lParameterMap.get( 16 ) );
      _scriptprefix = getParameterString( lParameterMap.get( 17 ) );
      _scriptpostfix = getParameterString( lParameterMap.get( 18 ) );

      _loglevel = getParameterString( lParameterMap.get( 19 ) );

      _targetplsql = getParameterString( lParameterMap.get( 20 ) );

      _srcJdbcConnectParameters = new JdbcConnectParameters();
      _srcJdbcConnectParameters._jdbcDriver = _jdbcConnectParameters._jdbcDriver;
      _srcJdbcConnectParameters._jdbcUrl = getParameterString( lParameterMap.get( 21 ) );
      _srcJdbcConnectParameters._jdbcUser = getParameterString( lParameterMap.get( 22 ) );
      _srcJdbcConnectParameters._jdbcPassword = getParameterString( lParameterMap.get( 23 ) );

      _sqlplustable = getParameterFlag( lParameterMap.get( 24 ) );

      _orderColumnsByName = getParameterFlag( lParameterMap.get( 25 ) );

      if( _srcJdbcConnectParameters._jdbcUrl.equals( "" ) )
      {
        _srcJdbcConnectParameters = null;
      }
    }

    if( pParameterTypeMode == ParameterTypeMode.ORCAS_LOAD_EXTRACT )
    {
      _modelFile = getParameterString( lParameterMap.get( 4 ) );

      _excludewheretable = cleanupExcludeWhere( lParameterMap.get( 5 ) );
      _excludewheresequence = cleanupExcludeWhere( lParameterMap.get( 6 ) );
      _dateformat = getParameterString( lParameterMap.get( 7 ) );
      _loglevel = getParameterString( lParameterMap.get( 8 ) );

      _spoolfile = getParameterString( lParameterMap.get( 9 ) );

      _scriptfolderrecursive = getParameterFlag( lParameterMap.get( 10 ) );
      _scriptprefix = getParameterString( lParameterMap.get( 11 ) );
      _scriptpostfix = getParameterString( lParameterMap.get( 12 ) );

      _orcasDbUser = getParameterString( lParameterMap.get( 13 ) );

      _orderColumnsByName = getParameterFlag( lParameterMap.get( 14 ) );

      _removeDefaultValuesFromModel = getParameterFlag( lParameterMap.get( 15 ) );
    }

    if( pParameterTypeMode == ParameterTypeMode.ORCAS_EXTRACT_VIEWS )
    {
      _viewExtractMode = getParameterString( lParameterMap.get( 4 ) );
      _loglevel = "info";
    }

    if( pParameterTypeMode == ParameterTypeMode.ORCAS_CHECK_CONNECTION )
    {
      _loglevel = "info";
    }

    if( pParameterTypeMode == ParameterTypeMode.ORCAS_SCRIPT )
    {
      _modelFile = getParameterString( lParameterMap.get( 4 ) );
      _scriptfolderrecursive = getParameterFlag( lParameterMap.get( 5 ) );
      _scriptprefix = getParameterString( lParameterMap.get( 6 ) );
      _scriptpostfix = getParameterString( lParameterMap.get( 7 ) );
      _loglevel = getParameterString( lParameterMap.get( 8 ) );
      _logname = getParameterString( lParameterMap.get( 9 ) );
      _spoolfolder = getParameterString( lParameterMap.get( 10 ) );
      _failonerror = getParameterString( lParameterMap.get( 11 ) );

      _additionalParameters = new ArrayList<String>();
      String lAdditionalParameters = (String)lParameterMap.get( 12 );

      if( lAdditionalParameters != null )
      {
        StringTokenizer lStringTokenizer = new StringTokenizer( lAdditionalParameters, " " );
        while( lStringTokenizer.hasMoreTokens() )
        {
          _additionalParameters.add( lStringTokenizer.nextToken() );
        }
      }
    }

    if( "nologging".equals( _loglevel ) )
    {
      LogManager.getRootLogger().setLevel( Level.ERROR );
    }
    else
    {
      LogManager.getRootLogger().setLevel( Level.toLevel( checkNull( _loglevel ).toUpperCase() ) );
    }
  }

  public boolean isOrderColumnsByName()
  {
    return checkNull( _orderColumnsByName );
  }

  public boolean isRemoveDefaultValuesFromModel()
  {
    return checkNull( _removeDefaultValuesFromModel );
  }

  public Boolean getSqlplustable()
  {
    return checkNull( _sqlplustable );
  }

  public JdbcConnectParameters getSrcJdbcConnectParameters()
  {
    return _srcJdbcConnectParameters;
  }

  private String getParameterString( String pArg )
  {
    String lArg = removePrefix( pArg );

    if( "null".equals( lArg ) )
    {
      return "";
    }

    return lArg;
  }

  public String getTargetplsql()
  {
    return checkNull( _targetplsql );
  }

  public List<String> getAdditionalParameters()
  {
    return checkNull( _additionalParameters );
  }

  public String getViewExtractMode()
  {
    return checkNull( _viewExtractMode );
  }

  public boolean getScriptfolderrecursive()
  {
    return checkNull( _scriptfolderrecursive );
  }

  public String getScriptprefix()
  {
    return checkNull( _scriptprefix );
  }

  public String getLogname()
  {
    return checkNull( _logname );
  }

  public boolean isLognameSet()
  {
    return _logname != null && !getLogname().equals( "" );
  }

  public String getSpoolfolder()
  {
    return checkNull( _spoolfolder );
  }

  public boolean isSpoolfolderSet()
  {
    return _spoolfolder != null && !getSpoolfolder().equals( "" );
  }

  public String getScriptpostfix()
  {
    return checkNull( _scriptpostfix );
  }

  public String getOrcasDbUser()
  {
    return checkNull( _orcasDbUser );
  }

  private String cleanupExcludeWhere( String pArg )
  {
    return getParameterString( pArg ).replace( "''", "'" );
  }

  private <T> T checkNull( T pValue )
  {
    if( pValue == null )
    {
      throw new IllegalArgumentException( "Parameter not set" );
    }

    return pValue;
  }

  public String getExcludewheretable()
  {
    return checkNull( _excludewheretable );
  }

  public String getExcludewheresequence()
  {
    return checkNull( _excludewheresequence );
  }

  public String getDateformat()
  {
    return checkNull( _dateformat );
  }

  public String getModelFile()
  {
    return checkNull( _modelFile );
  }

  public String getSpoolfile()
  {
    return checkNull( _spoolfile );
  }

  private Boolean getParameterFlag( String pArg )
  {
    if( pArg == null )
    {
      return null;
    }

    return !removePrefix( pArg ).equals( "false" );
  }

  private String removePrefix( String pArg )
  {
    if( _prefix != null && pArg != null && pArg.startsWith( _prefix ) )
    {
      return pArg.substring( _prefix.length() );
    }

    return pArg;
  }

  public JdbcConnectParameters getJdbcConnectParameters()
  {
    return _jdbcConnectParameters;
  }

  public boolean isLogonly()
  {
    return checkNull( _logonly );
  }

  public boolean isDropmode()
  {
    return checkNull( _dropmode );
  }

  public boolean isIndexparallelcreate()
  {
    return checkNull( _indexparallelcreate );
  }

  public boolean isIndexmovetablespace()
  {
    return checkNull( _indexmovetablespace );
  }

  public boolean isTablemovetablespace()
  {
    return checkNull( _tablemovetablespace );
  }

  public boolean isCreatemissingfkindexes()
  {
    return checkNull( _createmissingfkindexes );
  }

  public FailOnErrorMode getFailOnErrorMode()
  {
    if( checkNull( _failonerror ).equals( "true" ) )
    {
      return FailOnErrorMode.ALWAYS;
    }
    if( checkNull( _failonerror ).equals( "false" ) )
    {
      return FailOnErrorMode.NEVER;
    }
    if( checkNull( _failonerror ).equals( "ignore_drop" ) || checkNull( _failonerror ).equals( "default" ) )
    {
      return FailOnErrorMode.IGNORE_DROP;
    }

    throw new IllegalArgumentException( "unknown failonerror param: " + checkNull( _failonerror ) );
  }
}
