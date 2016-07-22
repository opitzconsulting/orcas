package de.opitzconsulting.orcas.diff;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.emf.common.util.EList;

import de.opitzconsulting.orcas.orig.diff.DiffRepository;
import de.opitzconsulting.orcas.sql.CallableStatementProvider;
import de.opitzconsulting.orcas.sql.WrapperCallableStatement;
import de.opitzconsulting.orcas.sql.WrapperIteratorResultSet;
import de.opitzconsulting.orcas.sql.WrapperReturnFirstValue;
import de.opitzconsulting.origOrcasDsl.BuildModeType;
import de.opitzconsulting.origOrcasDsl.CharType;
import de.opitzconsulting.origOrcasDsl.Column;
import de.opitzconsulting.origOrcasDsl.ColumnIdentity;
import de.opitzconsulting.origOrcasDsl.ColumnRef;
import de.opitzconsulting.origOrcasDsl.CommentObjectType;
import de.opitzconsulting.origOrcasDsl.CompressForType;
import de.opitzconsulting.origOrcasDsl.CompressType;
import de.opitzconsulting.origOrcasDsl.Constraint;
import de.opitzconsulting.origOrcasDsl.CycleType;
import de.opitzconsulting.origOrcasDsl.DataType;
import de.opitzconsulting.origOrcasDsl.DeferrType;
import de.opitzconsulting.origOrcasDsl.EnableType;
import de.opitzconsulting.origOrcasDsl.FkDeleteRuleType;
import de.opitzconsulting.origOrcasDsl.ForeignKey;
import de.opitzconsulting.origOrcasDsl.HashPartition;
import de.opitzconsulting.origOrcasDsl.HashPartitions;
import de.opitzconsulting.origOrcasDsl.HashSubParts;
import de.opitzconsulting.origOrcasDsl.HashSubSubPart;
import de.opitzconsulting.origOrcasDsl.Index;
import de.opitzconsulting.origOrcasDsl.IndexGlobalType;
import de.opitzconsulting.origOrcasDsl.IndexOrUniqueKey;
import de.opitzconsulting.origOrcasDsl.InlineComment;
import de.opitzconsulting.origOrcasDsl.ListPartition;
import de.opitzconsulting.origOrcasDsl.ListPartitionValue;
import de.opitzconsulting.origOrcasDsl.ListPartitions;
import de.opitzconsulting.origOrcasDsl.ListSubPart;
import de.opitzconsulting.origOrcasDsl.ListSubParts;
import de.opitzconsulting.origOrcasDsl.ListSubSubPart;
import de.opitzconsulting.origOrcasDsl.LobStorage;
import de.opitzconsulting.origOrcasDsl.LoggingType;
import de.opitzconsulting.origOrcasDsl.Model;
import de.opitzconsulting.origOrcasDsl.ModelElement;
import de.opitzconsulting.origOrcasDsl.Mview;
import de.opitzconsulting.origOrcasDsl.MviewLog;
import de.opitzconsulting.origOrcasDsl.NewValuesType;
import de.opitzconsulting.origOrcasDsl.OrderType;
import de.opitzconsulting.origOrcasDsl.ParallelType;
import de.opitzconsulting.origOrcasDsl.PermanentnessTransactionType;
import de.opitzconsulting.origOrcasDsl.PermanentnessType;
import de.opitzconsulting.origOrcasDsl.PrimaryKey;
import de.opitzconsulting.origOrcasDsl.RangePartition;
import de.opitzconsulting.origOrcasDsl.RangePartitionValue;
import de.opitzconsulting.origOrcasDsl.RangePartitions;
import de.opitzconsulting.origOrcasDsl.RangeSubPart;
import de.opitzconsulting.origOrcasDsl.RangeSubParts;
import de.opitzconsulting.origOrcasDsl.RangeSubSubPart;
import de.opitzconsulting.origOrcasDsl.RefPartition;
import de.opitzconsulting.origOrcasDsl.RefPartitions;
import de.opitzconsulting.origOrcasDsl.RefreshMethodType;
import de.opitzconsulting.origOrcasDsl.RefreshModeType;
import de.opitzconsulting.origOrcasDsl.Sequence;
import de.opitzconsulting.origOrcasDsl.SubSubPart;
import de.opitzconsulting.origOrcasDsl.SynchronousType;
import de.opitzconsulting.origOrcasDsl.Table;
import de.opitzconsulting.origOrcasDsl.TablePartitioning;
import de.opitzconsulting.origOrcasDsl.TableSubPart;
import de.opitzconsulting.origOrcasDsl.UniqueKey;
import de.opitzconsulting.origOrcasDsl.impl.ColumnIdentityImpl;
import de.opitzconsulting.origOrcasDsl.impl.ColumnImpl;
import de.opitzconsulting.origOrcasDsl.impl.ColumnRefImpl;
import de.opitzconsulting.origOrcasDsl.impl.ConstraintImpl;
import de.opitzconsulting.origOrcasDsl.impl.ForeignKeyImpl;
import de.opitzconsulting.origOrcasDsl.impl.HashPartitionImpl;
import de.opitzconsulting.origOrcasDsl.impl.HashPartitionsImpl;
import de.opitzconsulting.origOrcasDsl.impl.HashSubPartsImpl;
import de.opitzconsulting.origOrcasDsl.impl.HashSubSubPartImpl;
import de.opitzconsulting.origOrcasDsl.impl.IndexImpl;
import de.opitzconsulting.origOrcasDsl.impl.InlineCommentImpl;
import de.opitzconsulting.origOrcasDsl.impl.ListPartitionImpl;
import de.opitzconsulting.origOrcasDsl.impl.ListPartitionValueImpl;
import de.opitzconsulting.origOrcasDsl.impl.ListPartitionsImpl;
import de.opitzconsulting.origOrcasDsl.impl.ListSubPartImpl;
import de.opitzconsulting.origOrcasDsl.impl.ListSubPartsImpl;
import de.opitzconsulting.origOrcasDsl.impl.ListSubSubPartImpl;
import de.opitzconsulting.origOrcasDsl.impl.LobStorageImpl;
import de.opitzconsulting.origOrcasDsl.impl.ModelImpl;
import de.opitzconsulting.origOrcasDsl.impl.MviewImpl;
import de.opitzconsulting.origOrcasDsl.impl.MviewLogImpl;
import de.opitzconsulting.origOrcasDsl.impl.PrimaryKeyImpl;
import de.opitzconsulting.origOrcasDsl.impl.RangePartitionImpl;
import de.opitzconsulting.origOrcasDsl.impl.RangePartitionValueImpl;
import de.opitzconsulting.origOrcasDsl.impl.RangePartitionsImpl;
import de.opitzconsulting.origOrcasDsl.impl.RangeSubPartImpl;
import de.opitzconsulting.origOrcasDsl.impl.RangeSubPartsImpl;
import de.opitzconsulting.origOrcasDsl.impl.RangeSubSubPartImpl;
import de.opitzconsulting.origOrcasDsl.impl.RefPartitionImpl;
import de.opitzconsulting.origOrcasDsl.impl.RefPartitionsImpl;
import de.opitzconsulting.origOrcasDsl.impl.SequenceImpl;
import de.opitzconsulting.origOrcasDsl.impl.TableImpl;
import de.opitzconsulting.origOrcasDsl.impl.UniqueKeyImpl;

public class LoadIst
{
  private Log _log = LogFactory.getLog( OrcasMain.class );

  private Map<String,List<String>> excludeMap = new HashMap<String,List<String>>();

  private Map<String,Object> constraintMapForFK = new HashMap<String,Object>();
  private Map<String,String> constraintTableMapForFK = new HashMap<String,String>();

  private Parameters _parameters;

  private int _oracleMajorVersion;

  private CallableStatementProvider _callableStatementProvider;

  public LoadIst( CallableStatementProvider pCallableStatementProvider, Parameters pParameters )
  {
    _callableStatementProvider = pCallableStatementProvider;
    _parameters = pParameters;
  }

  private void registerConstarintForeFK( String pConstraintname, String pTablename, Object pConstarint )
  {
    constraintMapForFK.put( pConstraintname, pConstarint );
    constraintTableMapForFK.put( pConstraintname, pTablename );
  }

  public Model loadModel( boolean pWithSequeneceMayValueSelect )
  {
    isIgnoredSequence( "TEST" );
    isIgnoredMView( "TEST");
    isIgnoredTable( "TEST" );
    
    _oracleMajorVersion = loadOracleMajorVersion();

    Model pModel = new ModelImpl();

    loadSequencesIntoModel( pModel, pWithSequeneceMayValueSelect );

    loadMViewsIntoModel( pModel );

    loadTablesIntoModel( pModel );
    loadTableColumnsIntoModel( pModel );

    loadPartitioningIntoModel( pModel );

    loadMViewsLogsIntoModel( pModel );
    loadMViewsLogColumnsIntoModel( pModel );

    loadLobstorageIntoModel( pModel );

    loadIndexesIntoModel( pModel );
    loadIndexColumnsIntoModel( pModel );
    loadIndexExpressionsIntoModel( pModel );

    loadTableConstraintsIntoModel( pModel );
    loadTableConstraintColumnsIntoModel( pModel );

    loadTableCommentsIntoModel( pModel );
    loadTableColumnCommentsIntoModel( pModel );

    updateForeignkeyDestdata( pModel );

    removeNonePrebuildMviewTables( pModel );

    return pModel;
  }

  private int loadOracleMajorVersion()
  {
    try
    {
      final int[] lReturn = new int[1];
      String lCallExtensions = "" + //
                               " { " + //
                               "   ? = call " +
                               " DBMS_DB_VERSION.VERSION " + //
                               " } " + //
                               "";

      new WrapperCallableStatement( lCallExtensions, getCallableStatementProvider() )
      {
        @Override
        protected void useCallableStatement( CallableStatement pCallableStatement ) throws SQLException
        {
          pCallableStatement.registerOutParameter( 1, java.sql.Types.NUMERIC );

          pCallableStatement.execute();

          lReturn[0] = pCallableStatement.getInt( 1 );
        }
      }.execute();

      return lReturn[0];
    }
    catch( Exception e )
    {
      _log.debug( e, e );

      // should only happen if DBMS_DB_VERSION-Package does not exists
      return 9;
    }
  }

  private void removeNonePrebuildMviewTables( Model pModel )
  {
    List<String> lNonePrebuildMviewNames = new ArrayList<String>();

    for( ModelElement lModelElement : pModel.getModel_elements() )
    {
      if( lModelElement instanceof Mview )
      {
        Mview lMview = (Mview)lModelElement;

        if( lMview.getBuildMode() != BuildModeType.PREBUILT )
        {
          lNonePrebuildMviewNames.add( lMview.getMview_name() );
        }
      }
    }

    for( ModelElement lModelElement : new ArrayList<ModelElement>( pModel.getModel_elements() ) )
    {
      if( lModelElement instanceof Table )
      {
        Table lTable = (Table)lModelElement;

        if( lNonePrebuildMviewNames.contains( lTable.getName() ) )
        {
          pModel.getModel_elements().remove( lTable );
        }
      }
    }
  }

  private String getExcludeWhere( String pExcludeWhere )
  {
    if( pExcludeWhere.charAt( 0 ) == '@' )
    {
      return "object_name like '%$%'";
    }
    else
    {
      return pExcludeWhere;
    }
  }

  private void loadIgnoreCache( String pExcludeWhere, final String pType )
  {
    if( !excludeMap.containsKey( pType ) )
    {
      excludeMap.put( pType, new ArrayList<String>() );

      String lSql = "select object_name, case when ( " + getExcludeWhere( pExcludeWhere ) + " ) then 1 else 0 end is_exclude from user_objects where object_type=?";

      new WrapperIteratorResultSet( lSql, getCallableStatementProvider(), Collections.singletonList( pType ) )
      {
        @Override
        protected void useResultSetRow( ResultSet pResultSet ) throws SQLException
        {
          if( pResultSet.getInt( "is_exclude" ) == 1 )
          {
            excludeMap.get( pType ).add( pResultSet.getString( "object_name" ) );
          }
        }
      }.execute();
    }
  }

  private boolean isIgnored( String pName, String pExcludeWhere, String pType )
  {
    loadIgnoreCache( pExcludeWhere, pType );

    return excludeMap.get( pType ).contains( pName );
  }

  private boolean isIgnoredSequence( String pString )
  {
    return isIgnored( pString, _parameters.getExcludewheresequence(), "SEQUENCE" );
  }

  private boolean isIgnoredMView( String pString )
  { // TODO @
    return isIgnored( pString, "@", "MATERIALIZED VIEW" );
  }

  private boolean isIgnoredTable( String pString )
  {
    return isIgnored( pString, _parameters.getExcludewheretable(), "TABLE" ) || isIgnored( pString, "1=1", "VIEW" );
  }

  private int toInt( BigDecimal pBigDecimal )
  {
    if( pBigDecimal == null )
    {
      return DiffRepository.getNullIntValue();
    }
    return pBigDecimal.intValue();
  }

  private void loadSequencesIntoModel( final Model pModel, final boolean pWithSequeneceMayValueSelect )
  {
    String lSql = "" + //
                  " select sequence_name," + //
                  "        increment_by," + //
                  "        last_number," + //
                  "        cache_size," + //
                  "        min_value," + //
                  "        max_value," + //
                  "        cycle_flag," + //
                  "        order_flag" + //
                  "   from user_sequences" + //
                  "  order by sequence_name" + //
                  "";

    new WrapperIteratorResultSet( lSql, getCallableStatementProvider() )
    {
      @Override
      protected void useResultSetRow( ResultSet pResultSet ) throws SQLException
      {
        if( !isIgnoredSequence( pResultSet.getString( "sequence_name" ) ) )
        {
          Sequence lSequence = new SequenceImpl();

          lSequence.setSequence_name( pResultSet.getString( "sequence_name" ) );
          lSequence.setIncrement_by( toInt( pResultSet.getBigDecimal( "increment_by" ) ) );
          if( pWithSequeneceMayValueSelect )
          {
            lSequence.setMax_value_select( pResultSet.getString( "last_number" ) );
          }
          lSequence.setCache( toInt( pResultSet.getBigDecimal( "cache_size" ) ) );
          lSequence.setMinvalue( toInt( pResultSet.getBigDecimal( "min_value" ) ) );
          lSequence.setMaxvalue( toInt( pResultSet.getBigDecimal( "max_value" ) ) );

          if( "Y".equals( pResultSet.getString( "cycle_flag" ) ) )
          {
            lSequence.setCycle( CycleType.CYCLE );
          }
          else
          {
            lSequence.setCycle( CycleType.NOCYCLE );
          }

          if( "Y".equals( pResultSet.getString( "order_flag" ) ) )
          {
            lSequence.setOrder( OrderType.ORDER );
          }
          else
          {
            lSequence.setOrder( OrderType.NOORDER );
          }

          pModel.getModel_elements().add( lSequence );
        }
      }
    }.execute();
  }

  private void loadMViewsIntoModel( final Model pModel )
  {
    String lSql = "" + //
                  " select query," + //
                  "        mview_name," + //
                  "        updatable," + //
                  "        rewrite_enabled," + //
                  "        refresh_mode," + //
                  "        refresh_method," + //
                  "        build_mode," + //
                  "        staleness," + //
                  "        unknown_prebuilt," + //
                  "        compile_state," + //
                  "        trim(degree) degree," + //
                  "        trim(compression) compression," + //
                  "        trim(compress_for) compress_for," + //
                  "        tablespace_name" + //
                  "   from user_mviews mviews" + //
                  "   left outer join user_tables tables" + //
                  "        on mviews.mview_name = tables.table_name" + //
                  "  order by mview_name" + //
                  "";

    new WrapperIteratorResultSet( lSql, getCallableStatementProvider() )
    {
      @Override
      protected void useResultSetRow( ResultSet pResultSet ) throws SQLException
      {
        String lString = pResultSet.getString( "query" );

        if( !isIgnoredMView( pResultSet.getString( "mview_name" ) ) )
        {
          final Mview lMview = new MviewImpl();

          lMview.setViewSelectCLOB( lString );

          lMview.setMview_name( pResultSet.getString( "mview_name" ) );

          if( "IMMEDIATE".equals( pResultSet.getString( "build_mode" ) ) )
          {
            lMview.setBuildMode( BuildModeType.IMMEDIATE );
          }
          if( "DEFERRED".equals( pResultSet.getString( "build_mode" ) ) )
          {
            lMview.setBuildMode( BuildModeType.DEFERRED );
          }
          if( "PREBUILT".equals( pResultSet.getString( "build_mode" ) ) )
          {
            lMview.setBuildMode( BuildModeType.PREBUILT );
          }

          if( "COMMIT".equals( pResultSet.getString( "refresh_mode" ) ) )
          {
            lMview.setRefreshMode( RefreshModeType.COMMIT );
          }
          if( "DEMAND".equals( pResultSet.getString( "refresh_mode" ) ) )
          {
            lMview.setRefreshMode( RefreshModeType.DEMAND );
          }

          if( "COMPLETE".equals( pResultSet.getString( "refresh_method" ) ) )
          {
            lMview.setRefreshMethod( RefreshMethodType.COMPLETE );
          }
          if( "FORCE".equals( pResultSet.getString( "refresh_method" ) ) )
          {
            lMview.setRefreshMethod( RefreshMethodType.FORCE );
          }
          if( "FAST".equals( pResultSet.getString( "refresh_method" ) ) )
          {
            lMview.setRefreshMethod( RefreshMethodType.FAST );
          }
          if( "NEVER".equals( pResultSet.getString( "refresh_method" ) ) )
          {
            lMview.setRefreshMethod( RefreshMethodType.NEVER );
          }

          if( "Y".equals( pResultSet.getString( "rewrite_enabled" ) ) )
          {
            lMview.setQueryRewrite( EnableType.ENABLE );
          }
          else
          {
            lMview.setQueryRewrite( EnableType.DISABLE );
          }

          // Physical parameters nur, wenn nicht prebuilt
          if( !"PREBUILT".equals( pResultSet.getString( "build_mode" ) ) )
          {
            lMview.setTablespace( pResultSet.getString( "tablespace_name" ) );

            handleCompression( pResultSet.getString( "compression" ), pResultSet.getString( "compress_for" ), new CompressionHandler()
            {
              public void setCompression( CompressType pCompressType, CompressForType pCompressForType )
              {
                lMview.setCompression( pCompressType );
                lMview.setCompressionFor( pCompressForType );
              }
            } );

            handleDegree( pResultSet.getString( "degree" ), new DegreeHandler()
            {
              public void setDegree( ParallelType pParallelType, int ParallelDegree )
              {
                lMview.setParallel( pParallelType );
                lMview.setParallel_degree( ParallelDegree );
              }
            } );
          }

          pModel.getModel_elements().add( lMview );
        }
      }
    }.execute();
  }

  private interface DegreeHandler
  {
    void setDegree( ParallelType pParallelType, int ParallelDegree );
  }

  private void handleDegree( String pDegree, DegreeHandler pDegreeHandler )
  {
    if( pDegree != null )
    {
      ParallelType lParallelType;
      int lParallelDegree = DiffRepository.getNullIntValue();

      if( pDegree.equals( "1" ) )
      {
        lParallelType = ParallelType.NOPARALLEL;
      }
      else
      {
        lParallelType = ParallelType.PARALLEL;
        if( !pDegree.equals( "DEFAULT" ) )
        {
          lParallelDegree = toInt( new BigDecimal( pDegree ) );
        }
      }

      pDegreeHandler.setDegree( lParallelType, lParallelDegree );
    }
  }

  private interface CompressionHandler
  {
    void setCompression( CompressType pCompressType, CompressForType pCompressForType );
  }

  private void handleCompression( String pCompression, String pCompressFor, CompressionHandler pCompressionHandler )
  {
    if( pCompression != null )
    {
      CompressType lCompressType = null;
      CompressForType lCompressForType = null;

      if( "ENABLED".equalsIgnoreCase( pCompression ) )
      {
        lCompressType = CompressType.COMPRESS;

        if( pCompressFor != null )
        {
          if( pCompressFor.contains( "OLTP" ) || pCompressFor.equals( "ADVANCED" ) )
          {
            lCompressForType = CompressForType.ALL;
          }
          if( pCompressFor.equals( "BASIC" ) )
          {
            lCompressForType = CompressForType.DIRECT_LOAD;
          }
          if( pCompressFor.equals( "QUERY LOW" ) )
          {
            lCompressForType = CompressForType.QUERY_LOW;
          }
          if( pCompressFor.equals( "QUERY HIGH" ) )
          {
            lCompressForType = CompressForType.QUERY_HIGH;
          }
          if( pCompressFor.equals( "ARCHIVE LOW" ) )
          {
            lCompressForType = CompressForType.ARCHIVE_LOW;
          }
          if( pCompressFor.equals( "ARCHIVE HIGH" ) )
          {
            lCompressForType = CompressForType.ARCHIVE_HIGH;
          }
        }
      }
      if( "DISABLED".equalsIgnoreCase( pCompression ) )
      {
        lCompressType = CompressType.NOCOMPRESS;
      }

      pCompressionHandler.setCompression( lCompressType, lCompressForType );
    }
  }

  private Table findTable( Model pModel, String pTablename )
  {
    for( ModelElement lModelElement : pModel.getModel_elements() )
    {
      if( lModelElement instanceof Table )
      {
        if( ((Table)lModelElement).getName().equals( pTablename ) )
        {
          return (Table)lModelElement;
        }
      }
    }

    throw new IllegalStateException( "Table not found: " + pTablename );
  }

  private Index findIndex( Model pModel, String pTablename, String pIndexname )
  {
    for( IndexOrUniqueKey lIndexOrUniqueKey : findTable( pModel, pTablename ).getInd_uks() )
    {
      if( lIndexOrUniqueKey instanceof Index )
      {
        if( ((Index)lIndexOrUniqueKey).getConsName().equals( pIndexname ) )
        {
          return (Index)lIndexOrUniqueKey;
        }
      }
    }

    throw new IllegalStateException( "Index not found: " + pTablename + " " + pIndexname );
  }

  private UniqueKey findUniqueKey( Model pModel, String pTablename, String pUniquekeyname )
  {
    for( IndexOrUniqueKey lIndexOrUniqueKey : findTable( pModel, pTablename ).getInd_uks() )
    {
      if( lIndexOrUniqueKey instanceof UniqueKey )
      {
        if( ((UniqueKey)lIndexOrUniqueKey).getConsName().equals( pUniquekeyname ) )
        {
          return (UniqueKey)lIndexOrUniqueKey;
        }
      }
    }

    throw new IllegalStateException( "UK not found: " + pTablename + " " + pUniquekeyname );
  }

  private ForeignKey findForeignKey( Model pModel, String pTablename, String pForeignkeyname )
  {
    for( ForeignKey lForeignKey : findTable( pModel, pTablename ).getForeign_keys() )
    {
      if( lForeignKey.getConsName().equals( pForeignkeyname ) )
      {
        return lForeignKey;
      }
    }

    throw new IllegalStateException( "FK not found: " + pTablename + " " + pForeignkeyname );
  }

  private void loadTableColumnsIntoModel( final Model pModel )
  {
    String lSql;

    if( _oracleMajorVersion >= 12 )
    {
      lSql = "" + //
             " select user_tab_cols.table_name," + //
             "        user_tab_cols.column_name," + //
             "        data_type," + //
             "        data_type_owner," + //
             "        data_length," + //
             "        data_precision," + //
             "        data_scale," + //
             "        char_length," + //
             "        nullable," + //
             "        char_used," + //
             "        data_default," + //
             "        column_id," + //
             "        default_on_null, " + //
             "        generation_type " + //
             "   from user_tab_cols" + //
             "   left outer join user_tab_identity_cols" + //
             "       on (   user_tab_cols.column_name = user_tab_identity_cols.column_name" + //
             "          and user_tab_cols.table_name  = user_tab_identity_cols.table_name " + //
             "          )" + //
             "  where hidden_column = 'NO'" + //
             "";
    }
    else
    {
      lSql = "" + //
             " select user_tab_cols.table_name," + //
             "        user_tab_cols.column_name," + //
             "        data_type," + //
             "        data_type_owner," + //
             "        data_length," + //
             "        data_precision," + //
             "        data_scale," + //
             "        char_length," + //
             "        nullable," + //
             "        char_used," + //
             "        data_default," + //
             "        column_id," + //
             "        null default_on_null," + //
             "        null generation_type" + //
             "   from user_tab_cols" + //
             "  where hidden_column = 'NO'" + //
             "";
    }

    if( _parameters.isOrderColumnsByName() )
    {
      lSql += " order by table_name, column_name, column_id";
    }
    else
    {
      lSql += " order by table_name, column_id, column_name";
    }

    new WrapperIteratorResultSet( lSql, getCallableStatementProvider() )
    {
      @Override
      protected void useResultSetRow( ResultSet pResultSet ) throws SQLException
      {
        if( !isIgnoredTable( pResultSet.getString( "table_name" ) ) )
        {
          Column lColumn = new ColumnImpl();

          lColumn.setName( pResultSet.getString( "column_name" ) );

          lColumn.setDefault_value( pResultSet.getString( "data_default" ) );
          if( lColumn.getDefault_value() != null )
          {
            lColumn.setDefault_value( lColumn.getDefault_value().trim() );
            if( lColumn.getDefault_value().length() == 0 || lColumn.getDefault_value().equalsIgnoreCase( "NULL" ) )
            {
              lColumn.setDefault_value( null );
            }
          }

          lColumn.setNotnull( "N".equals( pResultSet.getString( "nullable" ) ) );

          if( "B".equals( pResultSet.getString( "char_used" ) ) )
          {
            lColumn.setByteorchar( CharType.BYTE );
          }
          if( "C".equals( pResultSet.getString( "char_used" ) ) )
          {
            lColumn.setByteorchar( CharType.CHAR );
          }

          if( "NUMBER".equals( pResultSet.getString( "data_type" ) ) )
          {
            lColumn.setData_type( DataType.NUMBER );
            lColumn.setPrecision( pResultSet.getInt( "data_precision" ) );
            lColumn.setScale( pResultSet.getInt( "data_scale" ) );
          }
          if( "BLOB".equals( pResultSet.getString( "data_type" ) ) )
          {
            lColumn.setData_type( DataType.BLOB );
          }
          if( "CLOB".equals( pResultSet.getString( "data_type" ) ) )
          {
            lColumn.setData_type( DataType.CLOB );
          }
          if( "NCLOB".equals( pResultSet.getString( "data_type" ) ) )
          {
            lColumn.setData_type( DataType.NCLOB );
          }
          if( "VARCHAR2".equals( pResultSet.getString( "data_type" ) ) )
          {
            lColumn.setData_type( DataType.VARCHAR2 );
            lColumn.setPrecision( pResultSet.getInt( "char_length" ) );
          }
          if( "NVARCHAR2".equals( pResultSet.getString( "data_type" ) ) )
          {
            lColumn.setData_type( DataType.NVARCHAR2 );
            lColumn.setPrecision( pResultSet.getInt( "char_length" ) );
            lColumn.setByteorchar( null );
          }
          if( "CHAR".equals( pResultSet.getString( "data_type" ) ) )
          {
            lColumn.setData_type( DataType.CHAR );
            lColumn.setPrecision( pResultSet.getInt( "char_length" ) );
          }
          if( "DATE".equals( pResultSet.getString( "data_type" ) ) )
          {
            lColumn.setData_type( DataType.DATE );
          }
          if( "XMLTYPE".equals( pResultSet.getString( "data_type" ) ) )
          {
            lColumn.setData_type( DataType.XMLTYPE );
          }
          if( pResultSet.getString( "data_type" ).contains( "TIMESTAMP" ) )
          {
            lColumn.setData_type( DataType.TIMESTAMP );
            lColumn.setPrecision( pResultSet.getInt( "data_scale" ) );
            if( pResultSet.getString( "data_type" ).contains( "WITH TIME ZONE" ) )
            {
              lColumn.setWith_time_zone( "with_time_zone" );
            }
          }
          if( "ROWID".equals( pResultSet.getString( "data_type" ) ) )
          {
            lColumn.setData_type( DataType.ROWID );
          }
          if( "LONG RAW".equals( pResultSet.getString( "data_type" ) ) )
          {
            lColumn.setData_type( DataType.LONG_RAW );
          }
          if( "LONG".equals( pResultSet.getString( "data_type" ) ) )
          {
            lColumn.setData_type( DataType.LONG );
          }
          if( "RAW".equals( pResultSet.getString( "data_type" ) ) )
          {
            lColumn.setData_type( DataType.RAW );
            lColumn.setPrecision( pResultSet.getInt( "data_length" ) );
          }
          if( "FLOAT".equals( pResultSet.getString( "data_type" ) ) )
          {
            lColumn.setData_type( DataType.FLOAT );
            lColumn.setPrecision( pResultSet.getInt( "data_precision" ) );
          }

          if( lColumn.getData_type() == null && pResultSet.getString( "data_type_owner" ) != null )
          {
            lColumn.setData_type( DataType.OBJECT );
            lColumn.setObject_type( pResultSet.getString( "data_type" ) );

            // TODO
            /*
             * if( cur_tab_cols.data_type_owner not in (user,'PUBLIC') ) { v_orig_column.i_object_type := cur_tab_cols.data_type_owner || '.' || v_orig_column.i_object_type; };
             */
          }

          if( lColumn.getDefault_value() != null && lColumn.getDefault_value().contains( "ISEQ$$" ) )
          {
            lColumn.setDefault_value( null );
          }

          String lGenerationType = pResultSet.getString( "generation_type" );
          if( lGenerationType != null )
          {
            lColumn.setDefault_value( null );

            ColumnIdentity lColumnIdentity = new ColumnIdentityImpl();
            lColumn.setIdentity( lColumnIdentity );

            if( lGenerationType.equals( "ALWAYS" ) )
            {
              lColumnIdentity.setAlways( "always" );
            }
            if( lGenerationType.equals( "BY DEFAULT" ) )
            {
              lColumnIdentity.setBy_default( "default" );
            }

            if( "YES".equals( pResultSet.getString( "default_on_null" ) ) )
            {
              lColumnIdentity.setOn_null( "null" );
            }
          }

          findTable( pModel, pResultSet.getString( "table_name" ) ).getColumns().add( lColumn );
        }
      }
    }.execute();
  }

  private void loadIndexesIntoModel( final Model pModel )
  {
    String lSql = "" + //
                  " select index_name," + //
                  "        table_name," + //
                  "        uniqueness," + //
                  "        tablespace_name," + //
                  "        logging," + //
                  "        degree," + //
                  "        partitioned," + //
                  "        index_type," + //
                  "        compression" + //
                  "   from user_indexes" + //
                  "  where generated = 'N'" + //
                  "    and (index_name,table_name) not in" + //
                  "        (" + //
                  "        select constraint_name," + //
                  "               table_name" + //
                  "          from user_constraints" + //
                  "         where constraint_type in ( 'U', 'P' )" + //
                  "           and constraint_name = user_constraints.index_name" + //
                  "        )" + //
                  "  order by table_name," + //
                  "           index_name" + //
                  "";

    new WrapperIteratorResultSet( lSql, getCallableStatementProvider() )
    {
      @Override
      protected void useResultSetRow( ResultSet pResultSet ) throws SQLException
      {
        if( !isIgnoredTable( pResultSet.getString( "table_name" ) ) )
        {
          final Index lIndex = new IndexImpl();

          lIndex.setConsName( pResultSet.getString( "index_name" ) );

          lIndex.setTablespace( pResultSet.getString( "tablespace_name" ) );

          if( "UNIQUE".equals( pResultSet.getString( "uniqueness" ) ) )
          {
            lIndex.setUnique( "unique" );
          }

          if( "BITMAP".equals( pResultSet.getString( "index_type" ) ) )
          {
            lIndex.setBitmap( "bitmap" );
          }

          handleDegree( pResultSet.getString( "degree" ), new DegreeHandler()
          {
            public void setDegree( ParallelType pParallelType, int ParallelDegree )
            {
              lIndex.setParallel( pParallelType );
              lIndex.setParallel_degree( ParallelDegree );
            }
          } );

          if( "YES".equals( pResultSet.getString( "logging" ) ) )
          {
            lIndex.setLogging( LoggingType.LOGGING );
          }
          else
          {
            lIndex.setLogging( LoggingType.NOLOGGING );
          }

          if( "NO".equals( pResultSet.getString( "partitioned" ) ) )
          {
            if( !"BITMAP".equals( pResultSet.getString( "index_type" ) ) )
            {
              lIndex.setGlobal( IndexGlobalType.GLOBAL );
            }
          }
          else
          {
            lIndex.setGlobal( IndexGlobalType.LOCAL );
            // set logging; logging is not set in Data-Dictionary since it is enabled at partition level
            lIndex.setLogging( LoggingType.LOGGING );
          }

          if( "ENABLED".equals( pResultSet.getString( "compression" ) ) )
          {
            lIndex.setCompression( CompressType.COMPRESS );
          }
          if( "DISABLED".equals( pResultSet.getString( "compression" ) ) )
          {
            lIndex.setCompression( CompressType.NOCOMPRESS );
          }

          findTable( pModel, pResultSet.getString( "table_name" ) ).getInd_uks().add( lIndex );
        }
      }
    }.execute();
  }

  private void loadIndexColumnsIntoModel( final Model pModel )
  {
    String lSql = "" + //
                  " select user_ind_columns.table_name," + //
                  "        user_ind_columns.index_name," + //
                  "        column_name" + //
                  "   from user_ind_columns," + //
                  "        user_indexes" + //
                  "  where generated = 'N'" + //
                  "     and user_ind_columns.index_name = user_indexes.index_name" + //
                  "     and (user_indexes.index_name,user_indexes.table_name) not in" + //
                  "         (" + //
                  "         select constraint_name," + //
                  "                table_name" + //
                  "           from user_constraints" + //
                  "          where constraint_type in ( 'U', 'P' )" + //
                  "            and constraint_name = user_constraints.index_name" + //
                  "          )" + //
                  "   order by table_name," + //
                  "            index_name," + //
                  "            column_position" + //
                  "";
    new WrapperIteratorResultSet( lSql, getCallableStatementProvider() )
    {
      @Override
      protected void useResultSetRow( ResultSet pResultSet ) throws SQLException
      {
        if( !isIgnoredTable( pResultSet.getString( "table_name" ) ) )
        {
          ColumnRef lColumnRef = new ColumnRefImpl();

          lColumnRef.setColumn_name( pResultSet.getString( "column_name" ) );

          findIndex( pModel, pResultSet.getString( "table_name" ), pResultSet.getString( "index_name" ) ).getIndex_columns().add( lColumnRef );
        }
      }
    }.execute();
  }

  private void setIndexColumnExpression( Model pModel, String pTablename, String pIndexName, int pColumnPosition, String pExpression, int pMaxColumnPositionForInd )
  {
    Index lIndex = findIndex( pModel, pTablename, pIndexName );

    // TODO ltrim(p_expression,',')
    lIndex.getIndex_columns().get( pColumnPosition - 1 ).setColumn_name( pExpression.replace( "\"", "" ).replace( " ", "" ) );

    if( pColumnPosition == pMaxColumnPositionForInd )
    {
      String lString = null;

      for( ColumnRef lColumnRef : lIndex.getIndex_columns() )
      {
        if( lString == null )
        {
          lString = "";
        }
        else
        {
          lString += ",";
        }

        lString += lColumnRef.getColumn_name();
      }

      lIndex.setFunction_based_expression( lString );
      lIndex.getIndex_columns().clear();
    }

  }

  private void loadIndexExpressionsIntoModel( final Model pModel )
  {
    String lSql = "" + //
                  " select user_ind_expressions.table_name," + //
                  "        user_ind_expressions.index_name," + //
                  "        column_position," + //
                  "        column_expression," + //
                  "        max (column_position)" + //
                  "        over" + //
                  "        (" + //
                  "          partition by" + //
                  "            user_ind_expressions.table_name," + //
                  "            user_ind_expressions.index_name" + //
                  "        ) as max_column_position_for_index" + //
                  "   from user_ind_expressions," + //
                  "        user_indexes" + //
                  "  where generated = 'N'" + //
                  "    and user_ind_expressions.index_name = user_indexes.index_name" + //
                  "    and (user_indexes.index_name,user_indexes.table_name) not in" + //
                  "        (" + //
                  "        select constraint_name," + //
                  "               table_name" + //
                  "          from user_constraints" + //
                  "         where constraint_type in ( 'U', 'P' )" + //
                  "           and constraint_name = user_constraints.index_name" + //
                  "        )" + //
                  "  order by table_name," + //
                  "           index_name," + //
                  "           column_position" + //
                  "";

    new WrapperIteratorResultSet( lSql, getCallableStatementProvider() )
    {
      @Override
      protected void useResultSetRow( ResultSet pResultSet ) throws SQLException
      {
        if( !isIgnoredTable( pResultSet.getString( "table_name" ) ) )
        {
          setIndexColumnExpression( pModel, pResultSet.getString( "table_name" ), pResultSet.getString( "index_name" ), pResultSet.getInt( "column_position" ), pResultSet.getString( "column_expression" ), pResultSet.getInt( "max_column_position_for_index" ) );
        }
      }
    }.execute();
  }

  private void loadTableConstraintsIntoModel( final Model pModel )
  {
    String lSql = "" + //
                  " select user_constraints.table_name," + //
                  "        constraint_name," + //
                  "        constraint_type," + //
                  "        r_constraint_name," + //
                  "        delete_rule," + //
                  "        deferrable," + //
                  "        deferred," + //
                  "        user_constraints.status," + //
                  "        user_constraints.generated," + //
                  "        user_constraints.index_name," + //
                  "        user_indexes.tablespace_name," + //
                  "        user_indexes.index_type," + //
                  "        search_condition" + //                  
                  "   from user_constraints" + //
                  "   left outer join user_indexes on (user_constraints.index_name = user_indexes.index_name)" + //
                  "  order by table_name," + //
                  "           constraint_name" + //
                  "";

    new WrapperIteratorResultSet( lSql, getCallableStatementProvider() )
    {
      @Override
      protected void useResultSetRow( ResultSet pResultSet ) throws SQLException
      {
        if( !isIgnoredTable( pResultSet.getString( "table_name" ) ) )
        {
          Table lTable = findTable( pModel, pResultSet.getString( "table_name" ) );

          EnableType lEnableType;
          if( "ENABLED".equals( pResultSet.getString( "status" ) ) )
          {
            lEnableType = EnableType.ENABLE;
          }
          else
          {
            lEnableType = EnableType.DISABLE;
          }

          DeferrType lDeferrType;
          if( "DEFERRABLE".equals( pResultSet.getString( "deferrable" ) ) )
          {
            if( "DEFERRED".equals( pResultSet.getString( "deferred" ) ) )
            {
              lDeferrType = DeferrType.DEFERRED;
            }
            else
            {
              lDeferrType = DeferrType.IMMEDIATE;
            }
          }
          else
          {
            lDeferrType = null;
          }

          boolean lGeneratedName = "GENERATED NAME".equals( pResultSet.getString( "generated" ) );

          if( "P".equals( pResultSet.getString( "constraint_type" ) ) )
          {
            PrimaryKey lPrimaryKey = new PrimaryKeyImpl();

            if( !lGeneratedName )
            {
              lPrimaryKey.setConsName( pResultSet.getString( "constraint_name" ) );
            }

            registerConstarintForeFK( pResultSet.getString( "constraint_name" ), pResultSet.getString( "table_name" ), lPrimaryKey );

            lPrimaryKey.setStatus( lEnableType );

            lPrimaryKey.setTablespace( pResultSet.getString( "tablespace_name" ) );

            if( "NORMAL/REV".equals( pResultSet.getString( "index_type" ) ) )
            {
              lPrimaryKey.setReverse( "reverse" );
            }

            lTable.setPrimary_key( lPrimaryKey );
          }

          if( "U".equals( pResultSet.getString( "constraint_type" ) ) )
          {
            UniqueKey lUniqueKey = new UniqueKeyImpl();

            lUniqueKey.setConsName( pResultSet.getString( "constraint_name" ) );

            registerConstarintForeFK( pResultSet.getString( "constraint_name" ), pResultSet.getString( "table_name" ), lUniqueKey );

            lUniqueKey.setStatus( lEnableType );

            lUniqueKey.setTablespace( pResultSet.getString( "tablespace_name" ) );

            if( !lUniqueKey.getConsName().equals( pResultSet.getString( "index_name" ) ) )
            {
              lUniqueKey.setIndexname( pResultSet.getString( "index_name" ) );
            }

            lTable.getInd_uks().add( lUniqueKey );
          }

          if( "R".equals( pResultSet.getString( "constraint_type" ) ) )
          {
            ForeignKey lForeignKey = new ForeignKeyImpl();

            lForeignKey.setConsName( pResultSet.getString( "constraint_name" ) );

            lForeignKey.setStatus( lEnableType );

            lForeignKey.setDeferrtype( lDeferrType );

            // in desttable wird der ref-constraintname zwischengespeichert, wird durch update_foreignkey_srcdata dann korrekt aktualisiert
            lForeignKey.setDestTable( pResultSet.getString( "r_constraint_name" ) );

            if( "NO ACTION".equals( pResultSet.getString( "delete_rule" ) ) )
            {
              lForeignKey.setDelete_rule( FkDeleteRuleType.NO_ACTION );
            }
            if( "SET NULL".equals( pResultSet.getString( "delete_rule" ) ) )
            {
              lForeignKey.setDelete_rule( FkDeleteRuleType.SET_NULL );
            }
            if( "CASCADE".equals( pResultSet.getString( "delete_rule" ) ) )
            {
              lForeignKey.setDelete_rule( FkDeleteRuleType.CASCADE );
            }

            lTable.getForeign_keys().add( lForeignKey );
          }

          if( "C".equals( pResultSet.getString( "constraint_type" ) ) )
          {
            if( !lGeneratedName )
            {
              Constraint lConstraint = new ConstraintImpl();

              lConstraint.setConsName( pResultSet.getString( "constraint_name" ) );

              lConstraint.setStatus( lEnableType );

              lConstraint.setDeferrtype( lDeferrType );

              lConstraint.setRule( pResultSet.getString( "search_condition" ) );

              lTable.getConstraints().add( lConstraint );
            }
          }
        }
      }
    }.execute();
  }

  private void loadTableConstraintColumnsIntoModel( final Model pModel )
  {
    String lSql = "" + //
                  " select user_cons_columns.table_name," + //
                  "       column_name," + //
                  "       position," + //
                  "       user_cons_columns.constraint_name," + //
                  "       constraint_type" + //
                  "  from user_cons_columns," + //
                  "       user_constraints" + //
                  " where user_cons_columns.constraint_name = user_constraints.constraint_name" + //
                  "   and user_cons_columns.table_name = user_constraints.table_name" + //
                  " order by table_name," + //
                  "          constraint_name," + //
                  "         position" + //
                  "";

    new WrapperIteratorResultSet( lSql, getCallableStatementProvider() )
    {
      @Override
      protected void useResultSetRow( ResultSet pResultSet ) throws SQLException
      {
        if( !isIgnoredTable( pResultSet.getString( "table_name" ) ) )
        {
          ColumnRef lColumnRef = new ColumnRefImpl();

          lColumnRef.setColumn_name( pResultSet.getString( "column_name" ) );

          if( "P".equals( pResultSet.getString( "constraint_type" ) ) )
          {
            findTable( pModel, pResultSet.getString( "table_name" ) ).getPrimary_key().getPk_columns().add( lColumnRef );
          }

          if( "U".equals( pResultSet.getString( "constraint_type" ) ) )
          {
            findUniqueKey( pModel, pResultSet.getString( "table_name" ), pResultSet.getString( "constraint_name" ) ).getUk_columns().add( lColumnRef );
          }

          if( "R".equals( pResultSet.getString( "constraint_type" ) ) )
          {
            findForeignKey( pModel, pResultSet.getString( "table_name" ), pResultSet.getString( "constraint_name" ) ).getSrcColumns().add( lColumnRef );
          }
        }
      }
    }.execute();
  }

  private void loadTableCommentsIntoModel( final Model pModel )
  {
    String lSql = "" + //
                  " select table_name," + //
                  "        comments" + //
                  "   from user_tab_comments" + //
                  "  where comments is not null" + //
                  "  order by table_name" + //
                  "";

    new WrapperIteratorResultSet( lSql, getCallableStatementProvider() )
    {
      @Override
      protected void useResultSetRow( ResultSet pResultSet ) throws SQLException
      {
        if( !isIgnoredTable( pResultSet.getString( "table_name" ) ) )
        {
          InlineComment lInlineComment = new InlineCommentImpl();

          lInlineComment.setComment( pResultSet.getString( "comments" ) );

          lInlineComment.setComment_object( CommentObjectType.TABLE );

          findTable( pModel, pResultSet.getString( "table_name" ) ).getComments().add( lInlineComment );
        }
      }
    }.execute();
  }

  private void loadTableColumnCommentsIntoModel( final Model pModel )
  {
    String lSql = "" + //
                  " select table_name," + //
                  "        column_name," + //
                  "        comments" + //
                  "   from user_col_comments" + //
                  "  where comments is not null" + //
                  "  order by table_name," + //
                  "           column_name" + //
                  "";

    new WrapperIteratorResultSet( lSql, getCallableStatementProvider() )
    {
      @Override
      protected void useResultSetRow( ResultSet pResultSet ) throws SQLException
      {
        if( !isIgnoredTable( pResultSet.getString( "table_name" ) ) )
        {
          InlineComment lInlineComment = new InlineCommentImpl();

          lInlineComment.setComment( pResultSet.getString( "comments" ) );

          lInlineComment.setColumn_name( pResultSet.getString( "column_name" ) );

          lInlineComment.setComment_object( CommentObjectType.COLUMN );

          findTable( pModel, pResultSet.getString( "table_name" ) ).getComments().add( lInlineComment );
        }
      }
    }.execute();
  }

  private void loadLobstorageIntoModel( final Model pModel )
  {
    String lSql = "" + //
                  " select table_name," + //
                  "        column_name," + //
                  "        tablespace_name" + //
                  "   from user_lobs" + //
                  "  order by table_name," + //
                  "           column_name" + //
                  "";

    new WrapperIteratorResultSet( lSql, getCallableStatementProvider() )
    {
      @Override
      protected void useResultSetRow( ResultSet pResultSet ) throws SQLException
      {
        if( !isIgnoredTable( pResultSet.getString( "table_name" ) ) )
        {
          LobStorage lLobStorage = new LobStorageImpl();

          lLobStorage.setColumn_name( pResultSet.getString( "column_name" ) );

          lLobStorage.setTablespace( pResultSet.getString( "tablespace_name" ) );

          Table lTable = findTable( pModel, pResultSet.getString( "table_name" ) );

          if( findColumn( lTable, lLobStorage.getColumn_name() ) != null )
          {
            lTable.getLobStorages().add( lLobStorage );
          }
        }
      }

      private Column findColumn( Table pTable, String pColumnName )
      {
        for( Column lColumn : pTable.getColumns() )
        {
          if( lColumn.getName().equals( pColumnName ) )
          {
            return lColumn;
          }
        }

        return null;
      }
    }.execute();
  }

  private void updateForeignkeyDestdata( Model pModel )
  {
    for( ModelElement lModelElement : pModel.getModel_elements() )
    {
      if( lModelElement instanceof Table )
      {
        for( ForeignKey lForeignKey : ((Table)lModelElement).getForeign_keys() )
        {
          String lRefConstraintName = lForeignKey.getDestTable();

          lForeignKey.setDestTable( constraintTableMapForFK.get( lRefConstraintName ) );

          Object lConstraint = constraintMapForFK.get( lRefConstraintName );

          EList<ColumnRef> lColumns = null;

          if( lConstraint instanceof PrimaryKey )
          {
            lColumns = ((PrimaryKey)lConstraint).getPk_columns();
          }

          if( lConstraint instanceof UniqueKey )
          {
            lColumns = ((UniqueKey)lConstraint).getUk_columns();
          }

          for( ColumnRef lColumnRef : lColumns )
          {
            ColumnRef lNewColumnRef = new ColumnRefImpl();

            lNewColumnRef.setColumn_name( lColumnRef.getColumn_name() );

            lForeignKey.getDestColumns().add( lNewColumnRef );
          }
        }
      }
    }
  }

  private void loadTablesIntoModel( final Model pModel )
  {
    String lSql = "" + //
                  " select tables.table_name," + //
                  "        tables.tablespace_name," + //
                  "        tables.temporary," + //
                  "        tables.duration," + //
                  "        tables.logging," + //
                  "        trim(degree) degree," + //
                  "        trim(compression) compression," + //
                  "        trim(compress_for) compress_for," + //
                  "        parts.def_tablespace_name as part_tabspace" + //
                  "   from user_tables tables" + //
                  "   left outer join user_part_tables parts" + //
                  "     on tables.table_name = parts.table_name " + //
                  "  order by table_name" + //
                  "";

    new WrapperIteratorResultSet( lSql, getCallableStatementProvider() )
    {
      @Override
      protected void useResultSetRow( ResultSet pResultSet ) throws SQLException
      {
        if( !isIgnoredTable( pResultSet.getString( "table_name" ) ) )
        {
          final Table lTable = new TableImpl();

          lTable.setName( pResultSet.getString( "table_name" ) );

          if( pResultSet.getString( "tablespace_name" ) != null )
          {
            lTable.setTablespace( pResultSet.getString( "tablespace_name" ) );
          }
          else
          {
            lTable.setTablespace( pResultSet.getString( "part_tabspace" ) );
          }

          if( "YES".equals( pResultSet.getString( "logging" ) ) )
          {
            lTable.setLogging( LoggingType.LOGGING );
          }
          else
          {
            lTable.setLogging( LoggingType.NOLOGGING );
          }

          pModel.getModel_elements().add( lTable );

          handleDegree( pResultSet.getString( "degree" ), new DegreeHandler()
          {
            public void setDegree( ParallelType pParallelType, int ParallelDegree )
            {
              lTable.setParallel( pParallelType );
              lTable.setParallel_degree( ParallelDegree );
            }
          } );

          if( "Y".equals( pResultSet.getString( "temporary" ) ) )
          {
            lTable.setPermanentness( PermanentnessType.GLOBAL_TEMPORARY );

            if( pResultSet.getString( "duration" ).contains( "SESSION" ) )
            {
              lTable.setTransactionControl( PermanentnessTransactionType.ON_COMMIT_PRESERVE );
            }
            else
            {
              lTable.setTransactionControl( PermanentnessTransactionType.ON_COMMIT_DELETE );
            }
          }
          else
          {
            lTable.setPermanentness( PermanentnessType.PERMANENT );
          }

          handleCompression( pResultSet.getString( "compression" ), pResultSet.getString( "compress_for" ), new CompressionHandler()
          {
            public void setCompression( CompressType pCompressType, CompressForType pCompressForType )
            {
              lTable.setCompression( pCompressType );
              lTable.setCompressionFor( pCompressForType );
            }
          } );
        }
      }
    }.execute();
  }

  private void loadMViewsLogColumnsIntoModel( final Model pModel )
  {
    String lSql = "" + //
                  " select master," + //
                  "        column_name, " + //
                  "        column_id " + //
                  "  from  (" + //  
                  "        select logs.master," + //
                  "               tab_columns.column_name," + //
                  "               tab_columns.column_id" + //
                  "          from user_mview_logs logs " +
                  "          join user_tab_columns tab_columns " + //         
                  "            on     logs.log_table = tab_columns.table_name" + // 
                  "               and tab_columns.column_name not like '%$$'" + //           
                  "        minus" + //            
                  "        select logs.master," + //
                  "               tab_columns.column_name," + //
                  "               tab_columns.column_id" + //
                  "          from user_mview_logs logs " + //
                  "          join user_tab_columns tab_columns" + // 
                  "            on logs.log_table = tab_columns.table_name" + // 
                  "           and tab_columns.column_name not like '%$$'" + // 
                  "          join user_constraints cons" + // 
                  "            on logs.master = cons.table_name" + //
                  "           and cons.constraint_type = 'P'" + //
                  "          join user_cons_columns cons_columns" + //
                  "            on cons.constraint_name = cons_columns.constraint_name" + //
                  "           and cons_columns.column_name = tab_columns.column_name" + //
                  "        )" + //           
                  "  order by master, column_id" + //            
                  "";

    new WrapperIteratorResultSet( lSql, getCallableStatementProvider() )
    {
      @Override
      protected void useResultSetRow( ResultSet pResultSet ) throws SQLException
      {
        String lTablename = pResultSet.getString( "master" );
        if( !isIgnoredTable( lTablename ) )
        {
          ColumnRef lColumnRef = new ColumnRefImpl();
          lColumnRef.setColumn_name( pResultSet.getString( "column_name" ) );

          findTable( pModel, lTablename ).getMviewLog().getColumns().add( lColumnRef );
        }
      }
    }.execute();
  }

  private void loadMViewsLogsIntoModel( final Model pModel )
  {
    String lSql = "" + //
                  " select master," + //
                  "        log_table," + //
                  "        rowids," + //
                  "        primary_key," + //
                  "        sequence," + //
                  "        include_new_values," + //
                  "        purge_asynchronous," + //
                  "        purge_deferred," + //
                  "        purge_start," + //
                  "        case when instr(purge_interval, 'sysdate') > 0 then substr(purge_interval, 11) end purge_interval," + //
                  "        case when purge_interval is not null and instr(purge_interval, 'to_date') > 0" + //
                  "          then to_date(substr(purge_interval, instr(purge_interval, '''',1,1)+1, instr(purge_interval, '''',1,2)-instr(purge_interval, '''',1,1)-1),substr(purge_interval, instr(purge_interval, '''',1,3)+1, instr(purge_interval, '''',1,4)-instr(purge_interval, '''',1,3)-1))" + //
                  "        end purge_next," + //
                  "        commit_scn_based," + //
                  "        tablespace_name, " + //
                  "        trim(degree) degree" + //
                  "   from user_mview_logs logs" + //
                  "   join user_tables tabs" + //
                  "     on logs.log_table = tabs.table_name" + //
                  "  order by master" + //
                  "";

    new WrapperIteratorResultSet( lSql, getCallableStatementProvider() )
    {
      @Override
      protected void useResultSetRow( ResultSet pResultSet ) throws SQLException
      {
        String lTablename = pResultSet.getString( "master" );

        if( !isIgnoredTable( lTablename ) )
        {
          final MviewLog lMviewLog = new MviewLogImpl();

          if( pResultSet.getString( "primary_key" ).equals( "YES" ) )
          {
            lMviewLog.setPrimaryKey( "primary" );
          }

          if( pResultSet.getString( "rowids" ).equals( "YES" ) )
          {
            lMviewLog.setRowid( "rowid" );
          }

          if( pResultSet.getString( "sequence" ).equals( "YES" ) )
          {
            lMviewLog.setWithSequence( "sequence" );
          }

          if( pResultSet.getString( "commit_scn_based" ).equals( "YES" ) )
          {
            lMviewLog.setCommitScn( "commit_scn" );
          }

          lMviewLog.setPurge( "purge" );
          if( pResultSet.getString( "purge_deferred" ).equals( "YES" ) )
          {
            lMviewLog.setStartWith( to_char( pResultSet.getDate( "purge_start" ) ) );
            lMviewLog.setRepeatInterval( pResultSet.getInt( "purge_interval" ) );
            lMviewLog.setNext( to_char( pResultSet.getDate( "purge_next" ) ) );
          }
          else
          {
            if( pResultSet.getString( "purge_asynchronous" ).equals( "YES" ) )
            {
              lMviewLog.setSynchronous( SynchronousType.ASYNCHRONOUS );
            }
            else
            {
              lMviewLog.setSynchronous( SynchronousType.SYNCHRONOUS );
            }
          }

          if( pResultSet.getString( "include_new_values" ).equals( "YES" ) )
          {
            lMviewLog.setNewValues( NewValuesType.INCLUDING );
          }
          else
          {
            lMviewLog.setNewValues( NewValuesType.EXCLUDING );
          }

          lMviewLog.setTablespace( pResultSet.getString( "tablespace_name" ) );

          handleDegree( pResultSet.getString( "degree" ), new DegreeHandler()
          {
            public void setDegree( ParallelType pParallelType, int pParallelDegree )
            {
              lMviewLog.setParallel( pParallelType );
              lMviewLog.setParallel_degree( pParallelDegree );
            }
          } );

          findTable( pModel, lTablename ).setMviewLog( lMviewLog );
        }
      }
    }.execute();
  }

  private String to_char( Date pDate )
  {
    if( pDate == null )
    {
      return null;
    }

    return (String)new WrapperReturnFirstValue( "select to_char( ?, '" + _parameters.getDateformat() + "') from dual", getCallableStatementProvider(), Collections.singletonList( pDate ) ).executeForValue();
  }

  private TableSubPart load_tablesubpart( String pTablename, String pSubpartitioningType )
  {
    if( pSubpartitioningType.equals( "NONE" ) )
    {
      return null;
    }

    String lSql = "select column_name from user_subpart_key_columns where name = ? and object_type = 'TABLE' order by column_position";

    if( pSubpartitioningType.equals( "HASH" ) )
    {
      HashSubParts lHashSubParts = new HashSubPartsImpl();

      ColumnRef lColumnRefImpl = new ColumnRefImpl();
      lHashSubParts.setColumn( lColumnRefImpl );

      lColumnRefImpl.setColumn_name( (String)new WrapperReturnFirstValue( lSql, getCallableStatementProvider(), Collections.singletonList( pTablename ) ).executeForValue() );

      return lHashSubParts;
    }
    if( pSubpartitioningType.equals( "LIST" ) )
    {
      ListSubParts lListSubParts = new ListSubPartsImpl();

      ColumnRefImpl lColumnRefImpl = new ColumnRefImpl();
      lListSubParts.setColumn( lColumnRefImpl );

      lColumnRefImpl.setColumn_name( (String)new WrapperReturnFirstValue( lSql, getCallableStatementProvider(), Collections.singletonList( pTablename ) ).executeForValue() );

      return lListSubParts;
    }
    if( pSubpartitioningType.equals( "RANGE" ) )
    {
      final RangeSubParts lRangeSubParts = new RangeSubPartsImpl();

      new WrapperIteratorResultSet( lSql, getCallableStatementProvider(), Collections.singletonList( pTablename ) )
      {
        @Override
        protected void useResultSetRow( ResultSet pResultSet ) throws SQLException
        {
          ColumnRefImpl lColumnRefImpl = new ColumnRefImpl();
          lColumnRefImpl.setColumn_name( pResultSet.getString( "column_name" ) );

          lRangeSubParts.getColumns().add( lColumnRefImpl );
        }
      }.execute();

      return lRangeSubParts;
    }

    throw new RuntimeException( "partitionstyp unbekannt: " + pSubpartitioningType + " " + pTablename );
  }

  private CallableStatementProvider getCallableStatementProvider()
  {
    return _callableStatementProvider;
  }

  private List<ListPartitionValue> getOrigListpartValuelist( String pHighValue )
  {
    List<ListPartitionValue> lListPartitionValueList = new ArrayList<ListPartitionValue>();

    if( "DEFAULT".equalsIgnoreCase( pHighValue ) )
    {
      ListPartitionValue lListPartitionValue = new ListPartitionValueImpl();
      lListPartitionValue.setValue( "default" );
      lListPartitionValueList.add( lListPartitionValue );
    }
    else
    {
      for( String lValue : separateStringList( pHighValue ) )
      {
        ListPartitionValue lListPartitionValue = new ListPartitionValueImpl();
        lListPartitionValue.setValue( lValue );
        lListPartitionValueList.add( lListPartitionValue );
      }
    }
    return lListPartitionValueList;
  }

  private List<RangePartitionValue> getOrigRangepartValuelist( String pHighValue )
  {
    List<RangePartitionValue> lRangePartitionValueList = new ArrayList<RangePartitionValue>();

    for( String lValue : separateStringList( pHighValue ) )
    {
      RangePartitionValue lRangePartitionValue = new RangePartitionValueImpl();

      if( "MAXVALUE".equalsIgnoreCase( lValue ) )
      {
        lRangePartitionValue.setMaxvalue( "maxvalue" );
      }
      else
      {
        lRangePartitionValue.setValue( lValue );
      }

      lRangePartitionValueList.add( lRangePartitionValue );
    }
    return lRangePartitionValueList;
  }

  private List<SubSubPart> loadSubpartlist( String pTablename, String pPartitionName, final String pSubpartitioningType )
  {
    final List<SubSubPart> lReturn = new ArrayList<SubSubPart>();

    String lSql = "" + //
                  " select subpartition_name," + //
                  "        tablespace_name," + //
                  "        high_value" + //                  
                  "   from user_tab_subpartitions" + //
                  "  where table_name = ?" + //
                  "    and partition_name = ?" + //
                  "  order by subpartition_position" + //
                  "";

    new WrapperIteratorResultSet( lSql, getCallableStatementProvider(), Arrays.asList( new Object[] { pTablename, pPartitionName } ) )
    {
      @Override
      protected void useResultSetRow( ResultSet pResultSet ) throws SQLException
      {
        if( pSubpartitioningType.equals( "HASH" ) )
        {
          HashSubSubPart lHashSubSubPart = new HashSubSubPartImpl();

          lHashSubSubPart.setName( pResultSet.getString( "subpartition_name" ) );
          lHashSubSubPart.setTablespace( pResultSet.getString( "tablespace_name" ) );

          lReturn.add( lHashSubSubPart );
        }
        if( pSubpartitioningType.equals( "LIST" ) )
        {
          ListSubSubPart lListSubSubPart = new ListSubSubPartImpl();

          lListSubSubPart.setName( pResultSet.getString( "subpartition_name" ) );
          lListSubSubPart.setTablespace( pResultSet.getString( "tablespace_name" ) );

          lListSubSubPart.getValue().addAll( getOrigListpartValuelist( pResultSet.getString( "high_value" ) ) );

          lReturn.add( lListSubSubPart );
        }
        if( pSubpartitioningType.equals( "RANGE" ) )
        {
          RangeSubSubPart lRangeSubSubPart = new RangeSubSubPartImpl();

          lRangeSubSubPart.setName( pResultSet.getString( "subpartition_name" ) );
          lRangeSubSubPart.setTablespace( pResultSet.getString( "tablespace_name" ) );

          lRangeSubSubPart.getValue().addAll( getOrigRangepartValuelist( pResultSet.getString( "high_value" ) ) );

          lReturn.add( lRangeSubSubPart );
        }
      }
    }.execute();

    return lReturn;
  }

  /**
   * note: partitioning details are loaded with single-selects.
   */
  private void loadPartitioningIntoModel( final Model pModel )
  {
    String lSql = "" + //
                  " select table_name, " + //
                  "        partitioning_type, " + //
                  "        subpartitioning_type, " + //
                  "        interval," + //
                  "        ref_ptn_constraint_name," + //
                  "        def_tablespace_name," + //                  
                  "        def_compression," + //
                  "        def_compress_for" + //
                  "   from user_part_tables" + //       
                  "";

    new WrapperIteratorResultSet( lSql, getCallableStatementProvider() )
    {
      @Override
      protected void useResultSetRow( ResultSet pResultSet ) throws SQLException
      {
        final String lTablename = pResultSet.getString( "table_name" );
        if( !isIgnoredTable( lTablename ) )
        {
          // Read compression type, works only for one compression type for all partitions
          handleCompression( pResultSet.getString( "def_compression" ), pResultSet.getString( "def_compress_for" ), new CompressionHandler()
          {
            public void setCompression( CompressType pCompressType, CompressForType pCompressForType )
            {
              Table lTable = findTable( pModel, lTablename );
              lTable.setCompression( pCompressType );
              lTable.setCompressionFor( pCompressForType );
            }
          } );

          if( pResultSet.getString( "partitioning_type" ).equals( "HASH" ) ) // and cur_part_tables.subpartitioning_type = 'NONE' 
          {
            final HashPartitions lHashPartitions = new HashPartitionsImpl();

            setPartitioningForTable( pModel, lTablename, lHashPartitions );

            lHashPartitions.setColumn( loadPartitionColumns( lTablename ).get( 0 ) );

            String lSql = "" + //
                          " select partition_name," + //
                          "        tablespace_name" + //
                          "   from user_tab_partitions" + //
                          "  where table_name = ?" + //
                          "  order by partition_position" + //
                          "";

            new WrapperIteratorResultSet( lSql, getCallableStatementProvider(), Collections.singletonList( lTablename ) )
            {
              @Override
              protected void useResultSetRow( ResultSet pResultSet ) throws SQLException
              {
                HashPartition lHashPartition = new HashPartitionImpl();

                lHashPartition.setName( pResultSet.getString( "partition_name" ) );
                lHashPartition.setTablespace( pResultSet.getString( "tablespace_name" ) );

                lHashPartitions.getPartitionList().add( lHashPartition );
              }
            }.execute();
          }

          if( pResultSet.getString( "partitioning_type" ).equals( "LIST" ) )
          {
            final ListPartitions lListPartitions = new ListPartitionsImpl();

            setPartitioningForTable( pModel, lTablename, lListPartitions );

            lListPartitions.setTableSubPart( load_tablesubpart( pResultSet.getString( "table_name" ), pResultSet.getString( "subpartitioning_type" ) ) );

            lListPartitions.setColumn( loadPartitionColumns( lTablename ).get( 0 ) );

            String lSql = "" + //
                          " select partition_name," + //
                          "        tablespace_name," + //
                          "        high_value" + //
                          "   from user_tab_partitions" + //
                          "  where table_name = ?" + //
                          "  order by partition_position" + //
                          "";

            new WrapperIteratorResultSet( lSql, getCallableStatementProvider(), Collections.singletonList( lTablename ) )
            {
              @Override
              protected void useResultSetRow( ResultSet pResultSet ) throws SQLException
              {
                String lHighValue = pResultSet.getString( "high_value" );
                ListPartition lListPartition = new ListPartitionImpl();

                lListPartition.setName( pResultSet.getString( "partition_name" ) );
                lListPartition.setTablespace( pResultSet.getString( "tablespace_name" ) );

                lListPartition.getValue().addAll( getOrigListpartValuelist( lHighValue ) );

                lListPartitions.getPartitionList().add( lListPartition );
              }
            }.execute();

            if( lListPartitions.getTableSubPart() != null )
            {
              for( ListPartition lListPartition : lListPartitions.getPartitionList() )
              {
                ListSubPart lListSubPart = new ListSubPartImpl();

                lListSubPart.setName( lListPartition.getName() );
                lListSubPart.getValue().addAll( lListPartition.getValue() );
                lListSubPart.getSubPartList().addAll( loadSubpartlist( lTablename, lListPartition.getName(), pResultSet.getString( "subpartitioning_type" ) ) );

                lListPartitions.getSubPartitionList().add( lListSubPart );
              }

              lListPartitions.getPartitionList().clear();
            }
          }

          if( pResultSet.getString( "partitioning_type" ).equals( "RANGE" ) )
          {
            final RangePartitions lRangePartitions = new RangePartitionsImpl();

            setPartitioningForTable( pModel, lTablename, lRangePartitions );

            lRangePartitions.setIntervalExpression( pResultSet.getString( "interval" ) );

            lRangePartitions.setTableSubPart( load_tablesubpart( pResultSet.getString( "table_name" ), pResultSet.getString( "subpartitioning_type" ) ) );

            lRangePartitions.getColumns().addAll( loadPartitionColumns( lTablename ) );

            String lSql = "" + //
                          " select partition_name," + //
                          "        tablespace_name," + //
                          "        high_value" + //
                          "   from user_tab_partitions" + //
                          "  where table_name = ?" + //
                          "  order by partition_position" + //
                          "";

            new WrapperIteratorResultSet( lSql, getCallableStatementProvider(), Collections.singletonList( lTablename ) )
            {
              @Override
              protected void useResultSetRow( ResultSet pResultSet ) throws SQLException
              {

                String lHighValue = pResultSet.getString( "high_value" );
                RangePartition lRangePartition = new RangePartitionImpl();

                lRangePartition.setName( pResultSet.getString( "partition_name" ) );
                lRangePartition.setTablespace( pResultSet.getString( "tablespace_name" ) );

                lRangePartition.getValue().addAll( getOrigRangepartValuelist( lHighValue ) );

                lRangePartitions.getPartitionList().add( lRangePartition );
              }
            }.execute();

            if( lRangePartitions.getTableSubPart() != null )
            {
              for( RangePartition lRangePartition : lRangePartitions.getPartitionList() )
              {
                RangeSubPart lRangeSubPart = new RangeSubPartImpl();

                lRangeSubPart.setName( lRangePartition.getName() );
                lRangeSubPart.getValue().addAll( lRangePartition.getValue() );
                lRangeSubPart.getSubPartList().addAll( loadSubpartlist( lTablename, lRangePartition.getName(), pResultSet.getString( "subpartitioning_type" ) ) );

                lRangePartitions.getSubPartitionList().add( lRangeSubPart );
              }

              lRangePartitions.getPartitionList().clear();
            }
          }

          if( pResultSet.getString( "partitioning_type" ).equals( "REFERENCE" ) )
          {
            final RefPartitions lRefPartitions = new RefPartitionsImpl();

            setPartitioningForTable( pModel, lTablename, lRefPartitions );

            lRefPartitions.setFkName( pResultSet.getString( "ref_ptn_constraint_name" ) );

            String lSql = "" + //
                          " select partition_name," + //
                          "        tablespace_name" + //
                          "   from user_tab_partitions" + //
                          "  where table_name = ?" + //
                          "  order by partition_position" + //
                          "";

            new WrapperIteratorResultSet( lSql, getCallableStatementProvider(), Collections.singletonList( lTablename ) )
            {
              @Override
              protected void useResultSetRow( ResultSet pResultSet ) throws SQLException
              {
                RefPartition lRefPartition = new RefPartitionImpl();

                lRefPartition.setName( pResultSet.getString( "partition_name" ) );
                lRefPartition.setTablespace( pResultSet.getString( "tablespace_name" ) );

                lRefPartitions.getPartitionList().add( lRefPartition );
              }
            }.execute();
          }
        }
      }
    }.execute();
  }

  private List<String> separateStringList( String pValues )
  {
    List<String> lReturn = new ArrayList<String>();

    boolean lExitLoop = false;

    while( !lExitLoop )
    {
      String lValue;

      int lIndexOf = pValues.indexOf( ',' );
      if( lIndexOf == -1 )
      {
        lValue = pValues.trim();
        lExitLoop = true;
      }
      else
      {
        lValue = pValues.substring( 0, lIndexOf ).trim();
        pValues = pValues.substring( lIndexOf + 1 );
      }

      lReturn.add( lValue );
    }

    return lReturn;
  }

  private List<ColumnRef> loadPartitionColumns( final String pTablename )
  {
    final List<ColumnRef> lColumnRefList = new ArrayList<ColumnRef>();

    String lSql = "" + //
                  "  select column_name" + //
                  "    from user_part_key_columns" + //
                  "   where name = ?" + //
                  "     and object_type = 'TABLE'" + //
                  "   order by column_position" + //
                  "";

    new WrapperIteratorResultSet( lSql, getCallableStatementProvider(), Collections.singletonList( pTablename ) )
    {
      @Override
      protected void useResultSetRow( ResultSet pResultSet ) throws SQLException
      {
        ColumnRef lColumnRef = new ColumnRefImpl();

        lColumnRef.setColumn_name( pResultSet.getString( "column_name" ) );
        lColumnRefList.add( lColumnRef );
      }
    }.execute();

    return lColumnRefList;
  }

  private void setPartitioningForTable( Model pModel, String pTablename, TablePartitioning pTablePartitioning )
  {
    Table lTable = findTable( pModel, pTablename );
    lTable.setTablePartitioning( pTablePartitioning );
    lTable.setLogging( LoggingType.LOGGING );
  }
}
