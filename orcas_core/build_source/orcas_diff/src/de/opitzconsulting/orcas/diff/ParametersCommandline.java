package de.opitzconsulting.orcas.diff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

public class ParametersCommandline extends Parameters
{
  public static enum ParameterTypeMode
  {
    ORCAS_MAIN, ORCAS_LOAD_EXTRACT, ORCAS_EXTRACT_VIEWS, ORCAS_CHECK_CONNECTION, ORCAS_SCRIPT
  }

  private String _prefix;

  public static Parameters parseFromCommandLine( String[] pArgs, ParameterTypeMode pParameterTypeMode )
  {
    return new ParametersCommandline( pArgs, pParameterTypeMode );
  }

  private ParametersCommandline( String[] pArgs, ParameterTypeMode pParameterTypeMode )
  {
    Map<Integer,String> lParameterMap = new HashMap<Integer,String>();

    for( int i = 0; i < pArgs.length; i++ )
    {
      lParameterMap.put( i, pArgs[i] );
    }

    if( pParameterTypeMode == ParameterTypeMode.ORCAS_SCRIPT || pParameterTypeMode == ParameterTypeMode.ORCAS_MAIN )
    {
      _prefix = "prefix_";
    }

    int lParameterIndex = 0;

    _jdbcConnectParameters._jdbcDriver = getParameterString( lParameterMap.get( lParameterIndex++ ) );
    _jdbcConnectParameters._jdbcUrl = getParameterString( lParameterMap.get( lParameterIndex++ ) );
    _jdbcConnectParameters._jdbcUser = getParameterString( lParameterMap.get( lParameterIndex++ ) );
    _jdbcConnectParameters._jdbcPassword = getParameterString( lParameterMap.get( lParameterIndex++ ) );

    if( pParameterTypeMode == ParameterTypeMode.ORCAS_MAIN )
    {
      _modelFile = getParameterString( lParameterMap.get( lParameterIndex++ ) );

      _spoolfile = getParameterString( lParameterMap.get( lParameterIndex++ ) );

      _logonly = getParameterFlag( lParameterMap.get( lParameterIndex++ ) );
      _dropmode = getParameterFlag( lParameterMap.get( lParameterIndex++ ) );
      _indexparallelcreate = getParameterFlag( lParameterMap.get( lParameterIndex++ ) );
      _indexmovetablespace = getParameterFlag( lParameterMap.get( lParameterIndex++ ) );
      _tablemovetablespace = getParameterFlag( lParameterMap.get( lParameterIndex++ ) );
      _createmissingfkindexes = getParameterFlag( lParameterMap.get( lParameterIndex++ ) );

      _excludewheretable = cleanupExcludeWhere( lParameterMap.get( lParameterIndex++ ) );
      _excludewheresequence = cleanupExcludeWhere( lParameterMap.get( lParameterIndex++ ) );

      _dateformat = getParameterString( lParameterMap.get( lParameterIndex++ ) );

      _orcasDbUser = getParameterString( lParameterMap.get( lParameterIndex++ ) );

      _scriptfolderrecursive = getParameterFlag( lParameterMap.get( lParameterIndex++ ) );
      _scriptprefix = getParameterString( lParameterMap.get( lParameterIndex++ ) );
      _scriptpostfix = getParameterString( lParameterMap.get( lParameterIndex++ ) );

      _loglevel = getParameterString( lParameterMap.get( lParameterIndex++ ) );

      _targetplsql = getParameterString( lParameterMap.get( lParameterIndex++ ) );

      _srcJdbcConnectParameters = new JdbcConnectParameters();
      _srcJdbcConnectParameters._jdbcDriver = _jdbcConnectParameters._jdbcDriver;
      _srcJdbcConnectParameters._jdbcUrl = getParameterString( lParameterMap.get( lParameterIndex++ ) );
      _srcJdbcConnectParameters._jdbcUser = getParameterString( lParameterMap.get( lParameterIndex++ ) );
      _srcJdbcConnectParameters._jdbcPassword = getParameterString( lParameterMap.get( lParameterIndex++ ) );
      if( _srcJdbcConnectParameters._jdbcUrl.equals( "" ) )
      {
        _srcJdbcConnectParameters = null;
      }

      _sqlplustable = getParameterFlag( lParameterMap.get( lParameterIndex++ ) );

      _orderColumnsByName = getParameterFlag( lParameterMap.get( lParameterIndex++ ) );
      _extensionParameter = getParameterString( lParameterMap.get( lParameterIndex++ ) );
    }

    if( pParameterTypeMode == ParameterTypeMode.ORCAS_LOAD_EXTRACT )
    {
      _modelFile = getParameterString( lParameterMap.get( lParameterIndex++ ) );

      _excludewheretable = cleanupExcludeWhere( lParameterMap.get( lParameterIndex++ ) );
      _excludewheresequence = cleanupExcludeWhere( lParameterMap.get( lParameterIndex++ ) );
      _dateformat = getParameterString( lParameterMap.get( lParameterIndex++ ) );
      _loglevel = getParameterString( lParameterMap.get( lParameterIndex++ ) );

      _spoolfile = getParameterString( lParameterMap.get( lParameterIndex++ ) );

      _scriptfolderrecursive = getParameterFlag( lParameterMap.get( lParameterIndex++ ) );
      _scriptprefix = getParameterString( lParameterMap.get( lParameterIndex++ ) );
      _scriptpostfix = getParameterString( lParameterMap.get( lParameterIndex++ ) );

      _orcasDbUser = getParameterString( lParameterMap.get( lParameterIndex++ ) );

      _orderColumnsByName = getParameterFlag( lParameterMap.get( lParameterIndex++ ) );

      _removeDefaultValuesFromModel = getParameterFlag( lParameterMap.get( lParameterIndex++ ) );
    }

    if( pParameterTypeMode == ParameterTypeMode.ORCAS_EXTRACT_VIEWS )
    {
      _viewExtractMode = getParameterString( lParameterMap.get( lParameterIndex++ ) );
      _loglevel = "info";
    }

    if( pParameterTypeMode == ParameterTypeMode.ORCAS_CHECK_CONNECTION )
    {
      _loglevel = "info";
    }

    if( pParameterTypeMode == ParameterTypeMode.ORCAS_SCRIPT )
    {
      _modelFile = getParameterString( lParameterMap.get( lParameterIndex++ ) );
      _scriptfolderrecursive = getParameterFlag( lParameterMap.get( lParameterIndex++ ) );
      _scriptprefix = getParameterString( lParameterMap.get( lParameterIndex++ ) );
      _scriptpostfix = getParameterString( lParameterMap.get( lParameterIndex++ ) );
      _loglevel = getParameterString( lParameterMap.get( lParameterIndex++ ) );
      _logname = getParameterString( lParameterMap.get( lParameterIndex++ ) );
      _spoolfolder = getParameterString( lParameterMap.get( lParameterIndex++ ) );
      setFailOnError( getParameterString( lParameterMap.get( lParameterIndex++ ) ) );
      _isOneTimeScriptMode = getParameterFlag( lParameterMap.get( lParameterIndex++ ) );
      _isOneTimeScriptLogonlyMode = getParameterFlag( lParameterMap.get( lParameterIndex++ ) );

      _orcasJdbcConnectParameters = new JdbcConnectParameters();
      _orcasJdbcConnectParameters._jdbcDriver = _jdbcConnectParameters._jdbcDriver;
      _orcasJdbcConnectParameters._jdbcUrl = getParameterString( lParameterMap.get( lParameterIndex++ ) );
      _orcasJdbcConnectParameters._jdbcUser = getParameterString( lParameterMap.get( lParameterIndex++ ) );
      _orcasJdbcConnectParameters._jdbcPassword = getParameterString( lParameterMap.get( lParameterIndex++ ) );
      if( _orcasJdbcConnectParameters._jdbcUrl.equals( "" ) )
      {
        _orcasJdbcConnectParameters = null;
      }

      _additionalParameters = new ArrayList<String>();
      String lAdditionalParameters = getParameterString( lParameterMap.get( lParameterIndex++ ) );

      if( lAdditionalParameters != null )
      {
        StringTokenizer lStringTokenizer = new StringTokenizer( lAdditionalParameters, " " );
        while( lStringTokenizer.hasMoreTokens() )
        {
          _additionalParameters.add( lStringTokenizer.nextToken() );
        }
      }
    }
  }

  private void setFailOnError( String pFailonerror )
  {
    if( checkNull( pFailonerror ).equals( "true" ) )
    {
      _failOnErrorMode = FailOnErrorMode.ALWAYS;
    }
    if( checkNull( pFailonerror ).equals( "false" ) )
    {
      _failOnErrorMode = FailOnErrorMode.NEVER;
    }
    if( checkNull( pFailonerror ).equals( "ignore_drop" ) || checkNull( pFailonerror ).equals( "default" ) )
    {
      _failOnErrorMode = FailOnErrorMode.IGNORE_DROP;
    }

    throw new IllegalArgumentException( "unknown failonerror param: " + checkNull( pFailonerror ) );
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
}
