package de.opitzconsulting.orcas.diff;

import java.util.HashMap;
import java.util.Map;

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
    ORCAS_MAIN, ORCAS_LOAD_EXTRACT
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

  public Parameters( String[] pArgs, ParameterTypeMode pParameterTypeMode )
  {
    if( pArgs.length == 0 )
    {
      if( pParameterTypeMode == ParameterTypeMode.ORCAS_MAIN )
      {
      }
      if( pParameterTypeMode == ParameterTypeMode.ORCAS_LOAD_EXTRACT )
      {
      }
    }

    Map<Integer,Object> lParameterMap = new HashMap<Integer,Object>();

    for( int i = 0; i < pArgs.length; i++ )
    {
      lParameterMap.put( i, pArgs[i] );
    }

    _jdbcConnectParameters = new JdbcConnectParameters();
    _jdbcConnectParameters._jdbcDriver = getParameterString( lParameterMap.get( 0 ) );
    _jdbcConnectParameters._jdbcUrl = getParameterString( lParameterMap.get( 1 ) );
    _jdbcConnectParameters._jdbcUser = getParameterString( lParameterMap.get( 2 ) );
    _jdbcConnectParameters._jdbcPassword = getParameterString( lParameterMap.get( 3 ) );

    _modelFile = getParameterString( lParameterMap.get( 4 ) );

    if( pParameterTypeMode == ParameterTypeMode.ORCAS_MAIN )
    {
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

      if( _srcJdbcConnectParameters._jdbcUrl.equals( "" ) )
      {
        _srcJdbcConnectParameters = null;
      }
    }

    if( pParameterTypeMode == ParameterTypeMode.ORCAS_LOAD_EXTRACT )
    {
      _excludewheretable = cleanupExcludeWhere( lParameterMap.get( 5 ) );
      _excludewheresequence = cleanupExcludeWhere( lParameterMap.get( 6 ) );
      _dateformat = getParameterString( lParameterMap.get( 7 ) );
      _loglevel = getParameterString( lParameterMap.get( 8 ) );
    }

    LogManager.getRootLogger().setLevel( Level.toLevel( checkNull( _loglevel ).toUpperCase() ) );
  }

  public JdbcConnectParameters getSrcJdbcConnectParameters()
  {
    return _srcJdbcConnectParameters;
  }

  private String getParameterString( Object pArg )
  {
    if( "null".equals( pArg ) )
    {
      return "";
    }

    return (String)pArg;
  }

  public String getTargetplsql()
  {
    return checkNull( _targetplsql );
  }

  public boolean getScriptfolderrecursive()
  {
    return checkNull( _scriptfolderrecursive );
  }

  public String getScriptprefix()
  {
    return checkNull( _scriptprefix );
  }

  public String getScriptpostfix()
  {
    return checkNull( _scriptpostfix );
  }

  public String getOrcasDbUser()
  {
    return checkNull( _orcasDbUser );
  }

  private String cleanupExcludeWhere( Object pArg )
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

  private static Boolean getParameterFlag( Object pArg )
  {
    if( pArg == null )
    {
      return null;
    }

    return !pArg.equals( "false" );
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

}
