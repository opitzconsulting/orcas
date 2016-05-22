package de.opitzconsulting.orcas.diff;

import java.util.HashMap;
import java.util.Map;

public class Parameters
{
  public static enum ParameterTypeMode
  {
    ORCAS_MAIN, ORCAS_RUN_PL_SQL_EXTENSIONS, ORCAS_LOAD_EXTRACT
  }

  private Boolean _logonly;
  private Boolean _dropmode;
  private Boolean _indexparallelcreate;
  private Boolean _indexmovetablespace;
  private Boolean _tablemovetablespace;
  private Boolean _createmissingfkindexes;
  private String _modelFile;
  private String _spoolfile;
  private String _jdbcDriver;
  private String _jdbcUrl;
  private String _jdbcUser;
  private String _jdbcPassword;
  private String _excludewheretable;
  private String _excludewheresequence;
  private String _dateformat;
  private String _orcasDbUser;

  public Parameters( String[] pArgs, ParameterTypeMode pParameterTypeMode )
  {
    if( pArgs.length == 0 )
    {
      if( pParameterTypeMode == ParameterTypeMode.ORCAS_MAIN )
      {
        pArgs = new String[] { "oracle.jdbc.OracleDriver", "jdbc:oracle:thin:@localhost:1522:XE", "orcas_orderentry", "orcas_orderentry", "D:\\2_orcas\\orcas\\examples\\orderentry\\distribution/../../../../bin_orderentry/tmp/orcas/statics/all.xml", "D:/2_orcas/sql.sql", "true", "true", "true", "true", "true", "true", "object_name like ''%$%''", "object_name in (''SEQ_IGNORE'')", "dd.mm.yy" };
        pArgs = new String[] { "oracle.jdbc.OracleDriver", "jdbc:oracle:thin:@localhost:1522:XE", "ORCAS_ITT_UEBERFUEHRUNG", "xxx", "D:\\2_orcas\\orcas\\examples\\orderentry\\distribution/../../../../bin_integrationstest/orcas_dir/statics/all.xml", "D:/2_orcas/sql.sql", "true", "true", "true", "true", "true", "true", "object_name like ''%$%''", "object_name in (''SEQ_IGNORE'')", "dd.mm.yy" };
      }
      if( pParameterTypeMode == ParameterTypeMode.ORCAS_RUN_PL_SQL_EXTENSIONS )
      {
        pArgs = new String[] { "oracle.jdbc.OracleDriver", "jdbc:oracle:thin:@localhost:1522:XE", "ORCAS_ITT_UEBERFUEHRUNG", "xxx", "D:\\2_orcas\\orcas\\examples\\orderentry\\distribution/../../../../bin_integrationstest/orcas_dir/statics/all.xml", "orcas_itt_svw_user" };
      }
      if( pParameterTypeMode == ParameterTypeMode.ORCAS_LOAD_EXTRACT )
      {
        pArgs = new String[] { "oracle.jdbc.OracleDriver", "jdbc:oracle:thin:@localhost:1522:XE", "ORCAS_ITT_UEBERFUEHRUNG", "xxx", "D:\\2_orcas\\extract.xml", "object_name like ''%$%''", "object_name like ''%$%''" };
      }
    }

    Map<Integer,String> lParameterMap = new HashMap<Integer,String>();

    for( int i = 0; i < pArgs.length; i++ )
    {
      lParameterMap.put( i, pArgs[i] );
    }

    _jdbcDriver = lParameterMap.get( 0 );
    _jdbcUrl = lParameterMap.get( 1 );
    _jdbcUser = lParameterMap.get( 2 );
    _jdbcPassword = lParameterMap.get( 3 );

    _modelFile = lParameterMap.get( 4 );

    if( pParameterTypeMode == ParameterTypeMode.ORCAS_MAIN )
    {
      _spoolfile = lParameterMap.get( 5 );

      _logonly = getParameterFlag( lParameterMap.get( 6 ) );
      _dropmode = getParameterFlag( lParameterMap.get( 7 ) );
      _indexparallelcreate = getParameterFlag( lParameterMap.get( 8 ) );
      _indexmovetablespace = getParameterFlag( lParameterMap.get( 9 ) );
      _tablemovetablespace = getParameterFlag( lParameterMap.get( 10 ) );
      _createmissingfkindexes = getParameterFlag( lParameterMap.get( 11 ) );

      _excludewheretable = cleanupExcludeWhere( lParameterMap.get( 12 ) );
      _excludewheresequence = cleanupExcludeWhere( lParameterMap.get( 13 ) );

      _dateformat = lParameterMap.get( 14 );
    }

    if( pParameterTypeMode == ParameterTypeMode.ORCAS_RUN_PL_SQL_EXTENSIONS )
    {
      _orcasDbUser = lParameterMap.get( 5 );
    }

    if( pParameterTypeMode == ParameterTypeMode.ORCAS_LOAD_EXTRACT )
    {
      _excludewheretable = cleanupExcludeWhere( lParameterMap.get( 5 ) );
      _excludewheresequence = cleanupExcludeWhere( lParameterMap.get( 6 ) );
    }
  }

  public String getOrcasDbUser()
  {
    return checkNull( _orcasDbUser );
  }

  private String cleanupExcludeWhere( String pArg )
  {
    if( pArg == null )
    {
      return null;
    }
    return pArg.replace( "''", "'" );
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

  private static Boolean getParameterFlag( String pArg )
  {
    if( pArg == null )
    {
      return null;
    }

    return !pArg.equals( "false" );
  }

  public String getJdbcDriver()
  {
    return checkNull( _jdbcDriver );
  }

  public String getJdbcUrl()
  {
    return checkNull( _jdbcUrl );
  }

  public String getJdbcUser()
  {
    return checkNull( _jdbcUser );
  }

  public String getJdbcPassword()
  {
    return checkNull( _jdbcPassword );
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
