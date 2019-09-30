package de.opitzconsulting.orcas.diff;

import java.math.BigDecimal;
import java.math.BigInteger;
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
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
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
import de.opitzconsulting.origOrcasDsl.LobCompressForType;
import de.opitzconsulting.origOrcasDsl.LobDeduplicateType;
import de.opitzconsulting.origOrcasDsl.LobStorage;
import de.opitzconsulting.origOrcasDsl.LobStorageParameters;
import de.opitzconsulting.origOrcasDsl.LobStorageType;
import de.opitzconsulting.origOrcasDsl.LoggingType;
import de.opitzconsulting.origOrcasDsl.Model;
import de.opitzconsulting.origOrcasDsl.ModelElement;
import de.opitzconsulting.origOrcasDsl.Mview;
import de.opitzconsulting.origOrcasDsl.MviewLog;
import de.opitzconsulting.origOrcasDsl.NestedTableStorage;
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
import de.opitzconsulting.origOrcasDsl.VarrayStorage;
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
import de.opitzconsulting.origOrcasDsl.impl.LobStorageParametersImpl;
import de.opitzconsulting.origOrcasDsl.impl.ModelImpl;
import de.opitzconsulting.origOrcasDsl.impl.MviewImpl;
import de.opitzconsulting.origOrcasDsl.impl.MviewLogImpl;
import de.opitzconsulting.origOrcasDsl.impl.NestedTableStorageImpl;
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
import de.opitzconsulting.origOrcasDsl.impl.VarrayStorageImpl;

public class LoadIstOracle extends LoadIst
{
  private Log _log = LogFactory.getLog( OrcasMain.class );

  private Map<String, List<String>> includeMap = new HashMap<String, List<String>>();
  private List<String> excludeIndexList;

  private Map<String, Object> constraintMapForFK = new HashMap<String, Object>();
  private Map<String, Table> constraintTableMapForFK = new HashMap<String, Table>();

  private Map<String, String> constraintTypes = new HashMap<String, String>();

  private Map<String, Table> tableCache = new HashMap<String, Table>();

  private Parameters _parameters;

  private int _oracleMajorVersion;

  private CallableStatementProvider _callableStatementProvider;

  public LoadIstOracle( CallableStatementProvider pCallableStatementProvider, Parameters pParameters )
  {
    _callableStatementProvider = pCallableStatementProvider;
    _parameters = pParameters;
  }

  private void registerConstarintForFK( String pUkConstraintname, Table pTable, String pTableOwner, Object pConstarint )
  {
    constraintMapForFK.put( getNameWithOwner( pUkConstraintname, pTableOwner ), pConstarint );
    constraintTableMapForFK.put( getNameWithOwner( pUkConstraintname, pTableOwner ), pTable );
  }

  @Override
  public Model loadModel( boolean pWithSequeneceMayValueSelect )
  {
    isIgnoredSequence( "TEST", "TEST" );
    isIgnoredMView( "TEST", "TEST" );
    isIgnoredTable( "TEST", "TEST" );
    isIgnoredIndex( "TEST", "TEST", "TEST" );

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
    loadNestedTableStorageIntoModel( pModel );

    loadIndexesIntoModel( pModel );
    loadIndexColumnsIntoModel( pModel );
    loadIndexExpressionsIntoModel( pModel );

    loadTableConstraintsIntoModel( pModel );
    loadTableConstraintColumnsIntoModel( pModel );
    removeNestedTableConstraintsFromModel( pModel );

    loadTableCommentsIntoModel( pModel );
    loadTableColumnCommentsIntoModel( pModel );

    updateForeignkeyDestdata( pModel );

    removeNonePrebuildMviewTables( pModel );

    return pModel;
  }

  private void removeNestedTableConstraintsFromModel(Model pModel) {
    pModel.getModel_elements()
          .stream()
          .filter(p->p instanceof Table)
          .map(p->(Table)p)
          .forEach(pTable->{
            List<String> lObjectTypecolumns = pTable.getColumns()
                                          .stream()
                                          .filter(p -> p.getObject_type() != null)
                                          .map(p -> p.getName_string())
                                          .collect(Collectors.toList());

            if(!lObjectTypecolumns.isEmpty()) {
              pTable.getInd_uks().removeAll(
                  pTable.getInd_uks()
                        .stream()
                        .filter(p -> p instanceof UniqueKey)
                        .map(p -> (UniqueKey) p)
                        .filter(p -> p.getConsName().startsWith("SYS_"))
                        .filter(p -> p.getUk_columns()
                                      .stream()
                                      .map(ColumnRef::getColumn_name_string)
                                      .allMatch(o -> o.startsWith("SYS_"))
                        )
                        .collect(Collectors.toList())
              );
            }
          });
  }

  private int loadOracleMajorVersion()
  {
    try
    {
      final int[] lReturn = new int[1];
      String lCallExtensions = "" + //
                               " { " + //
                               "   ? = call " + " DBMS_DB_VERSION.VERSION " + //
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
        Mview lMview = (Mview) lModelElement;

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
        Table lTable = (Table) lModelElement;

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
    if( !includeMap.containsKey( pType ) )
    {
      includeMap.put( pType, new ArrayList<String>() );

      String lSql = "select object_name, owner, case when ( " + getExcludeWhere( pExcludeWhere ) + " ) then 1 else 0 end is_exclude from " + getDataDictionaryView( "objects" ) + " where object_type=?";

      new WrapperIteratorResultSet( lSql, getCallableStatementProvider(), Collections.singletonList( pType ) )
      {
        @Override
        protected void useResultSetRow( ResultSet pResultSet ) throws SQLException
        {
          if( pResultSet.getInt( "is_exclude" ) == 0 )
          {
            includeMap.get( pType ).add( getNameWithOwner( pResultSet.getString( "object_name" ), pResultSet.getString( "owner" ) ) );
          }
        }
      }.execute();

      if( pType.equals("TABLE") ){
        String lSqlNestedTable = "select table_name, owner from " + getDataDictionaryView( "nested_tables" );

        new WrapperIteratorResultSet( lSqlNestedTable, getCallableStatementProvider() )
        {
          @Override
          protected void useResultSetRow( ResultSet pResultSet ) throws SQLException
          {
            includeMap.get( pType ).remove( getNameWithOwner( pResultSet.getString( "table_name" ), pResultSet.getString( "owner" ) ) );
          }
        }.execute();
      }
    }
  }

  private boolean isIgnored( String pName, String pOwner, String pExcludeWhere, String pType )
  {
    loadIgnoreCache( pExcludeWhere, pType );

    return !includeMap.get( pType ).contains( getNameWithOwner( pName, pOwner ) );
  }

  private boolean isIgnoredSequence( String pString, String pOwner )
  {
    return isIgnored( pString, pOwner, _parameters.getExcludewheresequence(), "SEQUENCE" );
  }

  private boolean isIgnoredMView( String pString, String pOwner )
  { // TODO @
    return isIgnored( pString, pOwner, "@", "MATERIALIZED VIEW" );
  }

  private boolean isIgnoredTable( String pTableName, String pOwner )
  {
    if( pTableName.equalsIgnoreCase( OrcasScriptRunner.ORCAS_UPDATES_TABLE ) )
    {
      return true;
    }

    return isIgnored( pTableName, pOwner, _parameters.getExcludewheretable(), "TABLE" );
  }

  private String getIndexNameWithOwner( String pTableName, String pIndexName, String pOwner )
  {
    return getNameWithOwner( pTableName + "." + pIndexName, pOwner );
  }

  private boolean isIgnoredIndex( String pTableName, String pIndexName, String pOwner )
  {
    if( excludeIndexList == null )
    {
      excludeIndexList = new ArrayList<String>();

      String lSql = "select constraint_name," + //
                    "       table_name," + //
                    "       owner" + //
                    "  from " + getDataDictionaryView( "constraints" ) + //
                    " where constraint_type in ( 'U', 'P' )";

      new WrapperIteratorResultSet( lSql, getCallableStatementProvider() )
      {
        @Override
        protected void useResultSetRow( ResultSet pResultSet ) throws SQLException
        {
          excludeIndexList.add( getIndexNameWithOwner( pResultSet.getString( "table_name" ), pResultSet.getString( "constraint_name" ), pResultSet.getString( "owner" ) ) );
        }
      }.execute();
    }

    if( excludeIndexList.contains( getIndexNameWithOwner( pTableName, pIndexName, pOwner ) ) )
    {
      return true;
    }

    return isIgnoredTable( pTableName, pOwner );
  }

  private BigInteger toBigInt( BigDecimal pBigDecimal )
  {
    if( pBigDecimal == null )
    {
      return null;
    }
    return pBigDecimal.toBigInteger();
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
                  "        owner," + //
                  "        increment_by," + //
                  "        last_number," + //
                  "        cache_size," + //
                  "        min_value," + //
                  "        max_value," + //
                  "        cycle_flag," + //
                  "        order_flag" + //
                  "   from " + getDataDictionaryView( "sequences" ) + //
                  "  order by sequence_name" + //
                  "";

    new WrapperIteratorResultSet( lSql, getCallableStatementProvider() )
    {
      @Override
      protected void useResultSetRow( ResultSet pResultSet ) throws SQLException
      {
        if( !isIgnoredSequence( pResultSet.getString( "sequence_name" ), pResultSet.getString( "owner" ) ) )
        {
          Sequence lSequence = new SequenceImpl();

          lSequence.setSequence_name( getNameWithOwner( pResultSet.getString( "sequence_name" ), pResultSet.getString( "owner" ) ) );

          logLoading( "sequence", lSequence.getSequence_name() );

          lSequence.setIncrement_by( toBigInt( pResultSet.getBigDecimal( "increment_by" ) ) );
          if( pWithSequeneceMayValueSelect )
          {
            lSequence.setMax_value_select( pResultSet.getString( "last_number" ) );
          }
          lSequence.setCache( toBigInt( pResultSet.getBigDecimal( "cache_size" ) ) );
          lSequence.setMinvalue( toBigInt( pResultSet.getBigDecimal( "min_value" ) ) );
          lSequence.setMaxvalue( toBigInt( pResultSet.getBigDecimal( "max_value" ) ) );

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

  private void logLoading( String pType, String pName )
  {
    _log.debug( "loading: " + pType + " " + pName );
  }

  private void logLoading( String pType, String pName, String pDetailName )
  {
    logLoading( pType, pName + "." + pDetailName );
  }

  private void loadMViewsIntoModel( final Model pModel )
  {
    String lSql = "" + //
                  " select query," + //
                  "        mview_name," + //
                  "        mviews.owner," + //
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
                  "   from " + getDataDictionaryView( "mviews" ) + //
                  "   left outer join " + getDataDictionaryView( "tables" ) + //
                  "        on mviews.mview_name = tables.table_name" + //
                  "        and mviews.owner = tables.owner" + //
                  "  order by mview_name" + //
                  "";

    new WrapperIteratorResultSet( lSql, getCallableStatementProvider() )
    {
      @Override
      protected void useResultSetRow( ResultSet pResultSet ) throws SQLException
      {
        String lString = pResultSet.getString( "query" );

        if( !isIgnoredMView( pResultSet.getString( "mview_name" ), pResultSet.getString( "owner" ) ) )
        {
          final Mview lMview = new MviewImpl();

          lMview.setViewSelectCLOB( lString );

          lMview.setMview_name( getNameWithOwner( pResultSet.getString( "mview_name" ), pResultSet.getString( "owner" ) ) );

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

  private String getNameWithOwner( String pObjectName, String pOwner )
  {
    if( _parameters.getMultiSchema() )
    {
      return pOwner.toString() + "." + pObjectName;
    }
    else
    {
      return pObjectName;
    }
  }

  private Table findTable( Model pModel, String pTablename, String pOwner )
  {
    String lTableNameWithOwner = getNameWithOwner( pTablename, pOwner );

    if( tableCache.containsKey( lTableNameWithOwner ) )
    {
      return tableCache.get( lTableNameWithOwner );
    }

    for( ModelElement lModelElement : pModel.getModel_elements() )
    {
      if( lModelElement instanceof Table )
      {
        if( ((Table) lModelElement).getName().equals( lTableNameWithOwner ) )
        {
          tableCache.put( lTableNameWithOwner, (Table) lModelElement );

          return (Table) lModelElement;
        }
      }
    }

    throw new IllegalStateException( "Table not found: " + pTablename );
  }

  private Index findIndex( Model pModel, String pTablename, String pTableOwner, String pIndexname, String pIndexOwner )
  {
    for( IndexOrUniqueKey lIndexOrUniqueKey : findTable( pModel, pTablename, pTableOwner ).getInd_uks() )
    {
      if( lIndexOrUniqueKey instanceof Index )
      {
        if( ((Index) lIndexOrUniqueKey).getConsName().equals( getNameWithOwner( pIndexname, pIndexOwner ) ) )
        {
          return (Index) lIndexOrUniqueKey;
        }
      }
    }

    throw new IllegalStateException( "Index not found: " + pTablename + " " + pIndexname );
  }

  private UniqueKey findUniqueKey( Model pModel, String pTablename, String pOwner, String pUniquekeyname )
  {
    for( IndexOrUniqueKey lIndexOrUniqueKey : findTable( pModel, pTablename, pOwner ).getInd_uks() )
    {
      if( lIndexOrUniqueKey instanceof UniqueKey )
      {
        if( ((UniqueKey) lIndexOrUniqueKey).getConsName().equals( pUniquekeyname ) )
        {
          return (UniqueKey) lIndexOrUniqueKey;
        }
      }
    }

    throw new IllegalStateException( "UK not found: " + pTablename + " " + pUniquekeyname );
  }

  private ForeignKey findForeignKey( Model pModel, String pTablename, String pOwner, String pForeignkeyname )
  {
    for( ForeignKey lForeignKey : findTable( pModel, pTablename, pOwner ).getForeign_keys() )
    {
      if( lForeignKey.getConsName().equals( pForeignkeyname ) )
      {
        return lForeignKey;
      }
    }

    throw new IllegalStateException( "FK not found: " + pTablename + " " + pForeignkeyname );
  }

  private Column findColumn( Model pModel, String pTablename, String pOwner, String pColumnName )
  {
    for( Column lColumn : findTable( pModel, pTablename, pOwner ).getColumns() )
    {
      if( lColumn.getName_string().equals( pColumnName ) )
      {
        return lColumn;
      }
    }

    throw new IllegalStateException( "column not found: " + pTablename + " " + pColumnName );
  }

  private void loadTableColumnsIntoModel( final Model pModel )
  {
    String lSql;

    if( _oracleMajorVersion >= 12 )
    {
      lSql = "" + //
             " select tab_cols.table_name," + //
             "        tab_cols.owner," + //
             "        tab_cols.column_name," + //
             "        data_type," + //
             "        data_type_owner," + //
             "        data_length," + //
             "        data_precision," + //
             "        data_scale," + //
             "        char_length," + //
             "        nullable," + //
             "        char_used," + //
             "        column_id," + //
             "        default_on_null, " + //
             "        generation_type " + //
             "   from " + getDataDictionaryView( "tab_cols" ) + //
             "   left outer join " + getDataDictionaryView( "tab_identity_cols" ) + //
             "       on (   tab_cols.column_name = tab_identity_cols.column_name" + //
             "          and tab_cols.table_name  = tab_identity_cols.table_name " + //
             "          and tab_cols.owner       = tab_identity_cols.owner " + //
             "          )" + //
             "  where hidden_column = 'NO'" + //
             "";
    }
    else
    {
      lSql = "" + //
             " select tab_cols.table_name," + //
             "        tab_cols.owner," + //
             "        tab_cols.column_name," + //
             "        data_type," + //
             "        data_type_owner," + //
             "        data_length," + //
             "        data_precision," + //
             "        data_scale," + //
             "        char_length," + //
             "        nullable," + //
             "        char_used," + //
             "        column_id," + //
             "        null default_on_null," + //
             "        null generation_type" + //
             "   from " + getDataDictionaryView( "tab_cols" ) + //
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
        if( !isIgnoredTable( pResultSet.getString( "table_name" ), pResultSet.getString( "owner" ) ) )
        {
          Column lColumn = new ColumnImpl();

          lColumn.setName_string( pResultSet.getString( "column_name" ) );

          logLoading( "column", pResultSet.getString( "table_name" ), lColumn.getName_string() );

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
          if( "UROWID".equals( pResultSet.getString( "data_type" ) ) )
          {
            lColumn.setData_type( DataType.UROWID );
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
            lColumn.setObject_type( getNameWithOwner(pResultSet.getString( "data_type" ), pResultSet.getString( "data_type_owner" )) );

            // TODO
            /*
             * if( cur_tab_cols.data_type_owner not in (user,'PUBLIC') ) {
             * v_orig_column.i_object_type := cur_tab_cols.data_type_owner ||
             * '.' || v_orig_column.i_object_type; };
             */
          }

          String lGenerationType = pResultSet.getString( "generation_type" );
          if( lGenerationType != null )
          {
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

          findTable( pModel, pResultSet.getString( "table_name" ), pResultSet.getString( "owner" ) ).getColumns().add( lColumn );
        }
      }
    }.execute();

    lSql = "" + //
           " select tab_cols.table_name," + //
           "        tab_cols.owner," + //
           "        tab_cols.column_name," + //
           "        data_default," + //
           "        virtual_column" + //
           "   from " + getDataDictionaryView( "tab_cols" ) + //
           "  where hidden_column = 'NO'" + //
           "    and data_default is not null" + //
           "";

    new WrapperIteratorResultSet( lSql, getCallableStatementProvider() )
    {
      @Override
      protected void useResultSetRow( ResultSet pResultSet ) throws SQLException
      {
        if( !isIgnoredTable( pResultSet.getString( "table_name" ), pResultSet.getString( "owner" ) ) )
        {
          Column lColumn = findColumn( pModel, pResultSet.getString( "table_name" ), pResultSet.getString( "owner" ), pResultSet.getString( "column_name" ) );

          logLoading( "column-default", pResultSet.getString( "table_name" ), lColumn.getName_string() );

          lColumn.setDefault_value( pResultSet.getString( "data_default" ) );
          if( lColumn.getDefault_value() != null )
          {
            lColumn.setDefault_value( lColumn.getDefault_value().trim() );
            if( lColumn.getDefault_value().length() == 0 || lColumn.getDefault_value().equalsIgnoreCase( "NULL" ) )
            {
              lColumn.setDefault_value( null );
            }
          }

          if( lColumn.getDefault_value() != null && lColumn.getDefault_value().contains( "ISEQ$$" ) )
          {
            lColumn.setDefault_value( null );
          }

          if( lColumn.getIdentity() != null )
          {
            lColumn.setDefault_value( null );
          }

          if (lColumn.getDefault_value() != null)
          {
            if ( pResultSet.getString( "virtual_column" ).equals( "YES" ) )
            {
              lColumn.setVirtual( "virtual" );
              lColumn.setDefault_value(lColumn.getDefault_value().replace("\"",""));
            }
          }
        }
      }
    }.execute();

  }

  private String getDataDictionaryView( String pName )
  {
    if( !_parameters.getMultiSchema() )
    {
      String lViewName = "user_" + pName;

      boolean lHasOwnerColumn = false;

      if( pName.equalsIgnoreCase( "mviews" ) )
      {
        lHasOwnerColumn = true;
      }
      if( pName.equalsIgnoreCase( "constraints" ) )
      {
        lHasOwnerColumn = true;
      }
      if( pName.equalsIgnoreCase( "cons_columns" ) )
      {
        lHasOwnerColumn = true;
      }

      if( lHasOwnerColumn )
      {
        return "(select * from " + lViewName + ") " + pName;
      }
      else
      {
        return "(select " + lViewName + ".*, USER owner from " + lViewName + ") " + pName;
      }
    }
    else
    {
      String lOwnerColumnName = null;

      if( pName.equalsIgnoreCase( "sequences" ) )
      {
        lOwnerColumnName = "sequence_owner";
      }
      if( pName.equalsIgnoreCase( "ind_columns" ) )
      {
        lOwnerColumnName = "index_owner";
      }
      if( pName.equalsIgnoreCase( "mview_logs" ) )
      {
        lOwnerColumnName = "log_owner";
      }
      if( pName.equalsIgnoreCase( "ind_expressions" ) )
      {
        lOwnerColumnName = "index_owner";
      }
      if( pName.equalsIgnoreCase( "tab_partitions" ) )
      {
        lOwnerColumnName = "table_owner";
      }
      if( pName.equalsIgnoreCase( "tab_subpartitions" ) )
      {
        lOwnerColumnName = "table_owner";
      }

      String lViewName;

      if( _parameters.getMultiSchemaDbaViews() )
      {
        lViewName = "dba_" + pName;
      }
      else
      {
        lViewName = "all_" + pName;
      }

      String lSelectList = "*";

      if( lOwnerColumnName == null )
      {
        lOwnerColumnName = "owner";
      }
      else
      {
        lSelectList = lViewName + ".*," + lViewName + "." + lOwnerColumnName + " owner";
      }

      String lWhereClause;
      if( _parameters.getMultiSchemaExcludewhereowner() == null )
      {
        throw new IllegalArgumentException( "cant use dba_views or all_views without exclude-owner" );
      }
      else
      {
        lWhereClause = " where not(" + _parameters.getMultiSchemaExcludewhereowner().replace( "owner", lOwnerColumnName ) + ")";
      }

      return "(select " + lSelectList + " from " + lViewName + lWhereClause + ") " + pName;
    }
  }

  private void loadIndexesIntoModel( final Model pModel )
  {
    String lSql = "" + //
                  " select index_name," + //
                  "        owner," + //
                  "        table_name," + //
                  "        table_owner," + //
                  "        uniqueness," + //
                  "        tablespace_name," + //
                  "        logging," + //
                  "        degree," + //
                  "        partitioned," + //
                  "        index_type," + //
                  "        compression" + //
                  "   from " + getDataDictionaryView( "indexes" ) + //
                  "  where generated = 'N'" + //
                  "  order by table_name," + //
                  "           index_name" + //
                  "";

    new WrapperIteratorResultSet( lSql, getCallableStatementProvider() )
    {
      @Override
      protected void useResultSetRow( ResultSet pResultSet ) throws SQLException
      {
        if( !isIgnoredIndex( pResultSet.getString( "table_name" ), pResultSet.getString( "index_name" ), pResultSet.getString( "table_owner" ) ) )
        {
          final Index lIndex = new IndexImpl();

          lIndex.setConsName( getNameWithOwner( pResultSet.getString( "index_name" ), pResultSet.getString( "owner" ) ) );

          logLoading( "index", pResultSet.getString( "table_name" ), lIndex.getConsName() );

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
            // set logging; logging is not set in Data-Dictionary since it is
            // enabled at partition level
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

          findTable( pModel, pResultSet.getString( "table_name" ), pResultSet.getString( "table_owner" ) ).getInd_uks().add( lIndex );
        }
      }
    }.execute();
  }

  private void loadIndexColumnsIntoModel( final Model pModel )
  {
    String lSql = "" + //
                  " select ind_columns.table_name," + //
                  "        ind_columns.index_name," + //
                  "        column_name," + //
                  "        ind_columns.owner," + //
                  "        indexes.table_owner" + //
                  "   from " + getDataDictionaryView( "ind_columns" ) + "," + //
                  "        " + getDataDictionaryView( "indexes" ) + //
                  "  where generated = 'N'" + //
                  "     and ind_columns.index_name = indexes.index_name" + //
                  "     and ind_columns.owner = indexes.owner" + //
                  "   order by table_name," + //
                  "            index_name," + //
                  "            column_position" + //
                  "";
    new WrapperIteratorResultSet( lSql, getCallableStatementProvider() )
    {
      @Override
      protected void useResultSetRow( ResultSet pResultSet ) throws SQLException
      {
        if( !isIgnoredIndex( pResultSet.getString( "table_name" ), pResultSet.getString( "index_name" ), pResultSet.getString( "owner" ) ) )
        {
          ColumnRef lColumnRef = new ColumnRefImpl();

          lColumnRef.setColumn_name_string( pResultSet.getString( "column_name" ) );

          logLoading( "index-column", pResultSet.getString( "table_name" ), lColumnRef.getColumn_name_string() );

          findIndex( pModel, pResultSet.getString( "table_name" ), pResultSet.getString( "table_owner" ), pResultSet.getString( "index_name" ), pResultSet.getString( "owner" ) ).getIndex_columns().add( lColumnRef );
        }
      }
    }.execute();
  }

  private void setIndexColumnExpression( Model pModel, String pTablename, String pTableOwner, String pIndexName, String pIndexOwner, int pColumnPosition, String pExpression, int pMaxColumnPositionForInd )
  {
    Index lIndex = findIndex( pModel, pTablename, pTableOwner, pIndexName, pIndexOwner );

    // TODO ltrim(p_expression,',')
    lIndex.getIndex_columns().get( pColumnPosition - 1 ).setColumn_name_string( pExpression.replace( "\"", "" ).replace( " ", "" ) );

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

        lString += lColumnRef.getColumn_name_string();
      }

      lIndex.setFunction_based_expression( lString );
      lIndex.getIndex_columns().clear();
    }

  }

  private void loadIndexExpressionsIntoModel( final Model pModel )
  {
    String lSql = "" + //
                  " select ind_expressions.table_name," + //
                  "        indexes.table_owner," + //
                  "        ind_expressions.index_name," + //
                  "        ind_expressions.owner," + //
                  "        column_position," + //
                  "        column_expression," + //
                  "        max (column_position)" + //
                  "        over" + //
                  "        (" + //
                  "          partition by" + //
                  "            ind_expressions.table_name," + //
                  "            ind_expressions.index_name" + //
                  "        ) as max_column_position_for_index" + //
                  "   from " + getDataDictionaryView( "ind_expressions" ) + "," + //
                  "        " + getDataDictionaryView( "indexes" ) + //
                  "  where generated = 'N'" + //
                  "    and ind_expressions.index_name = indexes.index_name" + //
                  "    and ind_expressions.owner = indexes.owner" + //
                  "  order by table_name," + //
                  "           index_name," + //
                  "           column_position" + //
                  "";

    new WrapperIteratorResultSet( lSql, getCallableStatementProvider() )
    {
      @Override
      protected void useResultSetRow( ResultSet pResultSet ) throws SQLException
      {
        if( !isIgnoredIndex( pResultSet.getString( "table_name" ), pResultSet.getString( "index_name" ), pResultSet.getString( "owner" ) ) )
        {
          setIndexColumnExpression( pModel, pResultSet.getString( "table_name" ), pResultSet.getString( "table_owner" ), pResultSet.getString( "index_name" ), pResultSet.getString( "owner" ), pResultSet.getInt( "column_position" ), pResultSet.getString( "column_expression" ), pResultSet.getInt( "max_column_position_for_index" ) );
        }
      }
    }.execute();
  }

  private void loadTableConstraintsIntoModel( final Model pModel )
  {
    String lSql = "" + //
                  " select constraints.table_name," + //
                  "        constraints.owner," + //
                  "        constraint_name," + //
                  "        constraint_type," + //
                  "        r_constraint_name," + //
                  "        r_owner," + //
                  "        delete_rule," + //
                  "        deferrable," + //
                  "        deferred," + //
                  "        constraints.status," + //
                  "        constraints.generated constraint_generated," + //
                  "        constraints.index_name," + //
                  "        indexes.tablespace_name," + //
                  "        indexes.index_type," + //
                  "        indexes.generated index_generated," + //
                  "        indexes.owner index_owner" + //
                  "   from " + getDataDictionaryView( "constraints" ) + //
                  "   left outer join " + getDataDictionaryView( "indexes" ) + " on (constraints.index_name = indexes.index_name " + //
                  " and constraints.owner = indexes.owner)" + //
                  " where constraint_type != 'C'" + //
                  "  order by table_name," + //
                  "           constraint_name" + //
                  "";

    new WrapperIteratorResultSet( lSql, getCallableStatementProvider() )
    {
      @Override
      protected void useResultSetRow( ResultSet pResultSet ) throws SQLException
      {
        if( !isIgnoredTable( pResultSet.getString( "table_name" ), pResultSet.getString( "owner" ) ) )
        {
          Table lTable = findTable( pModel, pResultSet.getString( "table_name" ), pResultSet.getString( "owner" ) );

          EnableType lEnableType = getEnableType( pResultSet );

          DeferrType lDeferrType = getDeferrType( pResultSet );

          logLoading( "constraint-" + pResultSet.getString( "constraint_type" ), pResultSet.getString( "table_name" ), pResultSet.getString( "constraint_name" ) );

          constraintTypes.put( getIndexNameWithOwner( pResultSet.getString( "table_name" ), pResultSet.getString( "constraint_name" ), pResultSet.getString( "owner" ) ), pResultSet.getString( "constraint_type" ) );

          if( "P".equals( pResultSet.getString( "constraint_type" ) ) )
          {
            PrimaryKey lPrimaryKey = new PrimaryKeyImpl();

            if( !isGeneratedName( pResultSet.getString( "constraint_generated" ) ) )
            {
              lPrimaryKey.setConsName( pResultSet.getString( "constraint_name" ) );
            }

            registerConstarintForFK( pResultSet.getString( "constraint_name" ), lTable, pResultSet.getString( "owner" ), lPrimaryKey );

            lPrimaryKey.setStatus( lEnableType );

            lPrimaryKey.setTablespace( pResultSet.getString( "tablespace_name" ) );

            if( "N".equals(pResultSet.getString("index_generated")) && !Objects.equals(lPrimaryKey.getConsName(), pResultSet.getString("index_name")))
            {
              lPrimaryKey.setIndexname( getNameWithOwner( pResultSet.getString( "index_name" ), pResultSet.getString( "index_owner" ) ) );
              lPrimaryKey.setTablespace( null );
            }

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

            registerConstarintForFK( pResultSet.getString( "constraint_name" ), lTable, pResultSet.getString( "owner" ), lUniqueKey );

            lUniqueKey.setStatus( lEnableType );

            lUniqueKey.setTablespace( pResultSet.getString( "tablespace_name" ) );

            if( !lUniqueKey.getConsName().equals( pResultSet.getString( "index_name" ) ) )
            {
            	if (pResultSet.getString( "index_name" ) != null) {
            		lUniqueKey.setIndexname( getNameWithOwner( pResultSet.getString( "index_name" ), pResultSet.getString( "index_owner" ) ) );
            	}
            }

            lTable.getInd_uks().add( lUniqueKey );
          }

          if( "R".equals( pResultSet.getString( "constraint_type" ) ) )
          {
            ForeignKey lForeignKey = new ForeignKeyImpl();

            lForeignKey.setConsName( pResultSet.getString( "constraint_name" ) );

            lForeignKey.setStatus( lEnableType );

            lForeignKey.setDeferrtype( lDeferrType );

            // in desttable wird der ref-constraintname zwischengespeichert,
            // wird durch update_foreignkey_srcdata dann korrekt aktualisiert
            lForeignKey.setDestTable( getNameWithOwner( pResultSet.getString( "r_constraint_name" ), pResultSet.getString( "r_owner" ) ) );

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
        }
      }
    }.execute();

    lSql = "" + //
           " select constraints.table_name," + //
           "        constraints.owner," + //
           "        constraint_name," + //
           "        delete_rule," + //
           "        deferrable," + //
           "        deferred," + //
           "        constraints.status," + //
           "        constraints.generated," + //
           "        search_condition" + //
           "   from " + getDataDictionaryView( "constraints" ) + //
           " where constraint_type = 'C'" + //
           "  order by table_name," + //
           "           constraint_name" + //
           "";

    new WrapperIteratorResultSet( lSql, getCallableStatementProvider() )
    {
      @Override
      protected void useResultSetRow( ResultSet pResultSet ) throws SQLException
      {
        if( !isIgnoredTable( pResultSet.getString( "table_name" ), pResultSet.getString( "owner" ) ) )
        {
          Table lTable = findTable( pModel, pResultSet.getString( "table_name" ), pResultSet.getString( "owner" ) );

          EnableType lEnableType = getEnableType( pResultSet );

          DeferrType lDeferrType = getDeferrType( pResultSet );

          boolean lGeneratedName = isGeneratedName( pResultSet.getString( "generated" ) );

          logLoading( "constraint-C", pResultSet.getString( "table_name" ), pResultSet.getString( "constraint_name" ) );

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

    }.execute();
  }

  private boolean isGeneratedName( String pName ) throws SQLException
  {
    return "GENERATED NAME".equals( pName );
  }

  private DeferrType getDeferrType( ResultSet pResultSet ) throws SQLException
  {
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
    return lDeferrType;
  }

  private EnableType getEnableType( ResultSet pResultSet ) throws SQLException
  {
    EnableType lEnableType;
    if( "ENABLED".equals( pResultSet.getString( "status" ) ) )
    {
      lEnableType = EnableType.ENABLE;
    }
    else
    {
      lEnableType = EnableType.DISABLE;
    }
    return lEnableType;
  }

  private void loadTableConstraintColumnsIntoModel( final Model pModel )
  {
    String lSql = "" + //
                  " select table_name," + //
                  "        owner," + //
                  "        column_name," + //
                  "        position," + //
                  "        constraint_name" + //
                  "   from " + getDataDictionaryView( "cons_columns" ) + //
                  " order by table_name," + //
                  "          constraint_name," + //
                  "          position" + //
                  "";

    new WrapperIteratorResultSet( lSql, getCallableStatementProvider() )
    {
      @Override
      protected void useResultSetRow( ResultSet pResultSet ) throws SQLException
      {
        if( !isIgnoredTable( pResultSet.getString( "table_name" ), pResultSet.getString( "owner" ) ) )
        {
          String lString = constraintTypes.get( getIndexNameWithOwner( pResultSet.getString( "table_name" ), pResultSet.getString( "constraint_name" ), pResultSet.getString( "owner" ) ) );

          ColumnRef lColumnRef = new ColumnRefImpl();

          lColumnRef.setColumn_name_string( pResultSet.getString( "column_name" ) );

          logLoading( "constraint-column-" + lString, pResultSet.getString( "table_name" ), lColumnRef.getColumn_name_string() );

          if( "P".equals( lString ) )
          {
            findTable( pModel, pResultSet.getString( "table_name" ), pResultSet.getString( "owner" ) ).getPrimary_key().getPk_columns().add( lColumnRef );
          }

          if( "U".equals( lString ) )
          {
            findUniqueKey( pModel, pResultSet.getString( "table_name" ), pResultSet.getString( "owner" ), pResultSet.getString( "constraint_name" ) ).getUk_columns().add( lColumnRef );
          }

          if( "R".equals( lString ) )
          {
            findForeignKey( pModel, pResultSet.getString( "table_name" ), pResultSet.getString( "owner" ), pResultSet.getString( "constraint_name" ) ).getSrcColumns().add( lColumnRef );
          }
        }
      }
    }.execute();
  }

  private void loadTableCommentsIntoModel( final Model pModel )
  {
    String lSql = "" + //
                  " select table_name," + //
                  "        owner," + //
                  "        comments" + //
                  "   from " + getDataDictionaryView( "tab_comments" ) + //
                  "  where comments is not null" + //
                  "  order by table_name" + //
                  "";

    new WrapperIteratorResultSet( lSql, getCallableStatementProvider() )
    {
      @Override
      protected void useResultSetRow( ResultSet pResultSet ) throws SQLException
      {
        if( !isIgnoredTable( pResultSet.getString( "table_name" ), pResultSet.getString( "owner" ) ) )
        {
          InlineComment lInlineComment = new InlineCommentImpl();

          lInlineComment.setComment( pResultSet.getString( "comments" ) );

          lInlineComment.setComment_object( CommentObjectType.TABLE );

          findTable( pModel, pResultSet.getString( "table_name" ), pResultSet.getString( "owner" ) ).getComments().add( lInlineComment );
        }
      }
    }.execute();
  }

  private void loadTableColumnCommentsIntoModel( final Model pModel )
  {
    String lSql = "" + //
                  " select table_name," + //
                  "        column_name," + //
                  "        owner," + //
                  "        comments" + //
                  "   from " + getDataDictionaryView( "col_comments" ) + //
                  "  where comments is not null" + //
                  "  order by table_name," + //
                  "           column_name" + //
                  "";

    new WrapperIteratorResultSet( lSql, getCallableStatementProvider() )
    {
      @Override
      protected void useResultSetRow( ResultSet pResultSet ) throws SQLException
      {
        if( !isIgnoredTable( pResultSet.getString( "table_name" ), pResultSet.getString( "owner" ) ) )
        {
          InlineComment lInlineComment = new InlineCommentImpl();

          lInlineComment.setComment( pResultSet.getString( "comments" ) );

          lInlineComment.setColumn_name_string( pResultSet.getString( "column_name" ) );

          lInlineComment.setComment_object( CommentObjectType.COLUMN );

          findTable( pModel, pResultSet.getString( "table_name" ), pResultSet.getString( "owner" ) ).getComments().add( lInlineComment );
        }
      }
    }.execute();
  }

  private void loadLobstorageIntoModel( final Model pModel )
  {
    String lSql = "" + //
                  " select table_name," + //
                  "        owner," + //
                  "        column_name," + //
                  "        tablespace_name," + //
                  "        securefile," + //
                  "        deduplication," + //
                  "        compression," + //
                  "        (select count(1) from " + getDataDictionaryView( "varrays" ) + " where varrays.owner = lobs.owner and parent_table_name = table_name and parent_table_column = column_name ) as is_varray" + //
                  "   from " + getDataDictionaryView( "lobs" ) + //
                  "  order by table_name," + //
                  "           column_name" + //
                  "";

    new WrapperIteratorResultSet( lSql, getCallableStatementProvider() )
    {
      @Override
      protected void useResultSetRow( ResultSet pResultSet ) throws SQLException
      {
        if( !isIgnoredTable( pResultSet.getString( "table_name" ), pResultSet.getString( "owner" ) ) )
        {
          LobStorageType lLobStorageTypeValue = "NO".equals( pResultSet.getString( "securefile" ) ) ? LobStorageType.BASICFILE : LobStorageType.SECUREFILE;
          LobStorageParameters lLobStorageParameters = new LobStorageParametersImpl();
          String lDeduplicationString = pResultSet.getString( "deduplication" );
          lLobStorageParameters.setLobDeduplicateType( "NONE".equals( lDeduplicationString ) || "NO".equals( lDeduplicationString ) ? LobDeduplicateType.KEEP_DUPLICATES : LobDeduplicateType.DEDUPLICATE );
          String lCompressionString = pResultSet.getString( "compression" );
          lLobStorageParameters.setCompressType( "NONE".equals( lCompressionString ) || "NO".equals( lCompressionString ) ? CompressType.NOCOMPRESS : CompressType.COMPRESS );

          if( lLobStorageParameters.getCompressType() != CompressType.NOCOMPRESS )
          {
            lLobStorageParameters.setLobCompressForType( LobCompressForType.MEDIUM );

            if( "HIGH".equals( lCompressionString ) )
            {
              lLobStorageParameters.setLobCompressForType( LobCompressForType.HIGH );
            }
            if( "LOW".equals( lCompressionString ) )
            {
              lLobStorageParameters.setLobCompressForType( LobCompressForType.LOW );
            }
          }

          lLobStorageParameters.setTablespace( pResultSet.getString( "tablespace_name" ) );

          if( pResultSet.getInt( "is_varray" ) == 0 )
          {
            LobStorage lLobStorage = new LobStorageImpl();

            lLobStorage.setColumn_name( pResultSet.getString( "column_name" ) );
            lLobStorage.setLobStorageType( lLobStorageTypeValue );
            lLobStorage.setLobStorageParameters( lLobStorageParameters );

            Table lTable = findTable( pModel, pResultSet.getString( "table_name" ), pResultSet.getString( "owner" ) );

            if( findColumn( lTable, lLobStorage.getColumn_name() ) != null )
            {
              lTable.getLobStorages().add( lLobStorage );
            }
          }
          else
          {
            VarrayStorage lVarrayStorage = new VarrayStorageImpl();

            lVarrayStorage.setColumn_name( pResultSet.getString( "column_name" ) );
            lVarrayStorage.setLobStorageType( lLobStorageTypeValue );
            lVarrayStorage.setLobStorageParameters( lLobStorageParameters );

            Table lTable = findTable( pModel, pResultSet.getString( "table_name" ), pResultSet.getString( "owner" ) );

            if( findColumn( lTable, lVarrayStorage.getColumn_name() ) != null )
            {
              lTable.getVarrayStorages().add( lVarrayStorage );
            }
          }
        }
      }

      private Column findColumn( Table pTable, String pColumnName )
      {
        for( Column lColumn : pTable.getColumns() )
        {
          if( lColumn.getName_string().equals( pColumnName ) )
          {
            return lColumn;
          }
        }

        return null;
      }
    }.execute();
  }

  private void loadNestedTableStorageIntoModel( final Model pModel )
  {
    String lSql = "" + //
        " select table_name," + //
        "        owner," + //
        "        parent_table_name," + //
        "        parent_table_column" + //
        "   from " + getDataDictionaryView( "nested_tables" ) + //
        "  order by table_name," + //
        "           parent_table_column" + //
        "";

    new WrapperIteratorResultSet( lSql, getCallableStatementProvider() )
    {
      @Override
      protected void useResultSetRow( ResultSet pResultSet ) throws SQLException
      {
        if( !isIgnoredTable( pResultSet.getString( "parent_table_name" ), pResultSet.getString( "owner" ) ) )
        {
          NestedTableStorage lNestedTableStorage = new NestedTableStorageImpl();

          lNestedTableStorage.setColumn_name(pResultSet.getString( "parent_table_column" ));
          lNestedTableStorage.setStorage_clause_string(pResultSet.getString( "table_name" ));

          Table lTable = findTable( pModel, pResultSet.getString( "parent_table_name" ), pResultSet.getString( "owner" ) );
          lTable.getNestedTableStorages().add( lNestedTableStorage );
        }
      }
    }.execute();
  }

  private void updateForeignkeyDestdata( Model pModel )
  {
    for( ModelElement lModelElement : pModel.getModel_elements() )
    {
      if( lModelElement instanceof Table )
      {
        for( ForeignKey lForeignKey : ((Table) lModelElement).getForeign_keys() )
        {
          String lRefConstraintName = lForeignKey.getDestTable();

          lForeignKey.setDestTable( constraintTableMapForFK.get( lRefConstraintName ).getName() );

          Object lConstraint = constraintMapForFK.get( lRefConstraintName );

          EList<ColumnRef> lColumns = null;

          if( lConstraint instanceof PrimaryKey )
          {
            lColumns = ((PrimaryKey) lConstraint).getPk_columns();
          }

          if( lConstraint instanceof UniqueKey )
          {
            lColumns = ((UniqueKey) lConstraint).getUk_columns();
          }

          for( ColumnRef lColumnRef : lColumns )
          {
            ColumnRef lNewColumnRef = new ColumnRefImpl();

            lNewColumnRef.setColumn_name_string( lColumnRef.getColumn_name_string() );

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
                  "        tables.owner," + //
                  "        tables.tablespace_name," + //
                  "		   tables.pct_free," + //
                  "        tables.temporary," + //
                  "        tables.duration," + //
                  "        tables.logging," + //
                  "        trim(degree) degree," + //
                  "        trim(compression) compression," + //
                  "        trim(compress_for) compress_for," + //
                  "        part_tables.def_tablespace_name as part_tabspace" + //
                  "   from " + getDataDictionaryView( "tables" ) + //
                  "   left outer join " + getDataDictionaryView( "part_tables" ) + //
                  "     on tables.table_name = part_tables.table_name " + //
                  "     and tables.owner = part_tables.owner " + //
                  "  order by table_name" + //
                  "";

    new WrapperIteratorResultSet( lSql, getCallableStatementProvider() )
    {
      @Override
      protected void useResultSetRow( ResultSet pResultSet ) throws SQLException
      {
        if( !isIgnoredTable( pResultSet.getString( "table_name" ), pResultSet.getString( "owner" ) ) )
        {
          final Table lTable = new TableImpl();

          lTable.setName( getNameWithOwner( pResultSet.getString( "table_name" ), pResultSet.getString( "owner" ) ) );

          logLoading( "table", lTable.getName() );

          if( pResultSet.getString( "tablespace_name" ) != null )
          {
            lTable.setTablespace( pResultSet.getString( "tablespace_name" ) );
          }
          else
          {
            lTable.setTablespace( pResultSet.getString( "part_tabspace" ) );
          }

          // set pctfree
          lTable.setPctfree( pResultSet.getInt( "pct_free" ) );

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

          tableCache.put( lTable.getName(), lTable );
        }
      }
    }.execute();
  }

  private void loadMViewsLogColumnsIntoModel( final Model pModel )
  {
    String lSql = "" + //
                  " select master," + //
                  "        column_name, " + //
                  "        owner, " + //
                  "        column_id " + //
                  "  from  (" + //
                  "        select mview_logs.master," + //
                  "               tab_columns.column_name," + //
                  "               tab_columns.owner, " + //
                  "               tab_columns.column_id" + //
                  "          from " + getDataDictionaryView( "mview_logs" ) + "          join " + getDataDictionaryView( "tab_columns" ) + //
                  "            on     mview_logs.log_table = tab_columns.table_name" + //
                  "            and     mview_logs.owner = tab_columns.owner" + //
                  "               and tab_columns.column_name not like '%$$'" + //
                  "        minus" + //
                  "        select mview_logs.master," + //
                  "               tab_columns.column_name," + //
                  "               tab_columns.owner, " + //
                  "               tab_columns.column_id" + //
                  "          from " + getDataDictionaryView( "mview_logs" ) + //
                  "          join " + getDataDictionaryView( "tab_columns" ) + //
                  "            on mview_logs.log_table = tab_columns.table_name" + //
                  "           and mview_logs.owner = tab_columns.owner" + //
                  "           and tab_columns.column_name not like '%$$'" + //
                  "          join " + getDataDictionaryView( "constraints" ) + //
                  "            on mview_logs.master = constraints.table_name" + //
                  "           and mview_logs.owner = constraints.owner" + //
                  "           and constraints.constraint_type = 'P'" + //
                  "          join " + getDataDictionaryView( "cons_columns" ) + //
                  "            on constraints.constraint_name = cons_columns.constraint_name" + //
                  "           and constraints.owner = cons_columns.owner" + //
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
        String lOwner = pResultSet.getString( "owner" );
        if( !isIgnoredTable( lTablename, lOwner ) )
        {
          ColumnRef lColumnRef = new ColumnRefImpl();
          lColumnRef.setColumn_name_string( pResultSet.getString( "column_name" ) );

          findTable( pModel, lTablename, lOwner ).getMviewLog().getColumns().add( lColumnRef );
        }
      }
    }.execute();
  }

  private void loadMViewsLogsIntoModel( final Model pModel )
  {
    String lSql = "" + //
                  " select master," + //
                  "        mview_logs.owner," + //
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
                  "   from " + getDataDictionaryView( "mview_logs" ) + //
                  "   join " + getDataDictionaryView( "tables" ) + //
                  "     on mview_logs.log_table = tables.table_name" + //
                  "    and mview_logs.owner = tables.owner" + //
                  "  order by master" + //
                  "";

    new WrapperIteratorResultSet( lSql, getCallableStatementProvider() )
    {
      @Override
      protected void useResultSetRow( ResultSet pResultSet ) throws SQLException
      {
        String lTablename = pResultSet.getString( "master" );
        String lOwner = pResultSet.getString( "owner" );

        if( !isIgnoredTable( lTablename, lOwner ) )
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

          findTable( pModel, lTablename, lOwner ).setMviewLog( lMviewLog );
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

    return (String) new WrapperReturnFirstValue( "select to_char( ?, '" + _parameters.getDateformat() + "') from dual", getCallableStatementProvider(), Collections.singletonList( pDate ) ).executeForValue();
  }

  private TableSubPart load_tablesubpart( String pTablename, String pSubpartitioningType )
  {
    if( pSubpartitioningType.equals( "NONE" ) )
    {
      return null;
    }

    String lSql = "select column_name from " + getDataDictionaryView( "subpart_key_columns" ) + " where name = ? and object_type = 'TABLE' order by column_position";

    if( pSubpartitioningType.equals( "HASH" ) )
    {
      HashSubParts lHashSubParts = new HashSubPartsImpl();

      ColumnRef lColumnRefImpl = new ColumnRefImpl();
      lHashSubParts.setColumn( lColumnRefImpl );

      lColumnRefImpl.setColumn_name_string( (String) new WrapperReturnFirstValue( lSql, getCallableStatementProvider(), Collections.singletonList( pTablename ) ).executeForValue() );

      return lHashSubParts;
    }
    if( pSubpartitioningType.equals( "LIST" ) )
    {
      ListSubParts lListSubParts = new ListSubPartsImpl();

      ColumnRefImpl lColumnRefImpl = new ColumnRefImpl();
      lListSubParts.setColumn( lColumnRefImpl );

      lColumnRefImpl.setColumn_name_string( (String) new WrapperReturnFirstValue( lSql, getCallableStatementProvider(), Collections.singletonList( pTablename ) ).executeForValue() );

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
          lColumnRefImpl.setColumn_name_string( pResultSet.getString( "column_name" ) );

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
                  "   from " + getDataDictionaryView( "tab_subpartitions" ) + //
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
                  "        owner, " + //
                  "        partitioning_type, " + //
                  "        subpartitioning_type, " + //
                  "        interval," + //
                  "        ref_ptn_constraint_name," + //
                  "        def_tablespace_name," + //
                  "        def_compression," + //
                  "        def_compress_for" + //
                  "   from " + getDataDictionaryView( "part_tables" ) + //
                  "";

    new WrapperIteratorResultSet( lSql, getCallableStatementProvider() )
    {
      @Override
      protected void useResultSetRow( ResultSet pResultSet ) throws SQLException
      {
        final String lTablename = pResultSet.getString( "table_name" );
        final String lOwner = pResultSet.getString( "owner" );
        if( !isIgnoredTable( lTablename, lOwner ) )
        {
          // Read compression type, works only for one compression type for all
          // partitions
          handleCompression( pResultSet.getString( "def_compression" ), pResultSet.getString( "def_compress_for" ), new CompressionHandler()
          {
            public void setCompression( CompressType pCompressType, CompressForType pCompressForType )
            {
              Table lTable = findTable( pModel, lTablename, lOwner );
              lTable.setCompression( pCompressType );
              lTable.setCompressionFor( pCompressForType );
            }
          } );

          if( pResultSet.getString( "partitioning_type" ).equals( "HASH" ) ) // and
                                                                             // cur_part_tables.subpartitioning_type
                                                                             // =
                                                                             // 'NONE'
          {
            final HashPartitions lHashPartitions = new HashPartitionsImpl();

            setPartitioningForTable( pModel, lTablename, lOwner, lHashPartitions );

            lHashPartitions.setColumn( loadPartitionColumns( lTablename, lOwner ).get( 0 ) );

            String lSql = "" + //
            " select partition_name," + //
            "        tablespace_name" + //
            "   from " + getDataDictionaryView( "tab_partitions" ) + //
            "  where table_name = ?" + //
            "    and owner = ?" + //
            "  order by partition_position" + //
            "";

            new WrapperIteratorResultSet( lSql, getCallableStatementProvider(), ImmutableList.of( lTablename, lOwner ) )
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

            setPartitioningForTable( pModel, lTablename, lOwner, lListPartitions );

            lListPartitions.setTableSubPart( load_tablesubpart( pResultSet.getString( "table_name" ), pResultSet.getString( "subpartitioning_type" ) ) );

            lListPartitions.setColumn( loadPartitionColumns( lTablename, lOwner ).get( 0 ) );

            String lSql = "" + //
            " select partition_name," + //
            "        tablespace_name," + //
            "        high_value" + //
            "   from " + getDataDictionaryView( "tab_partitions" ) + //
            "  where table_name = ?" + //
            "    and owner = ?" + //
            "  order by partition_position" + //
            "";

            new WrapperIteratorResultSet( lSql, getCallableStatementProvider(), ImmutableList.of( lTablename, lOwner ) )
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

            setPartitioningForTable( pModel, lTablename, lOwner, lRangePartitions );

            lRangePartitions.setIntervalExpression( pResultSet.getString( "interval" ) );

            lRangePartitions.setTableSubPart( load_tablesubpart( pResultSet.getString( "table_name" ), pResultSet.getString( "subpartitioning_type" ) ) );

            lRangePartitions.getColumns().addAll( loadPartitionColumns( lTablename, lOwner ) );

            String lSql = "" + //
            " select partition_name," + //
            "        tablespace_name," + //
            "        high_value" + //
            "   from " + getDataDictionaryView( "tab_partitions" ) + //
            "  where table_name = ?" + //
            "    and owner = ?" + //
            "  order by partition_position" + //
            "";

            new WrapperIteratorResultSet( lSql, getCallableStatementProvider(), ImmutableList.of( lTablename, lOwner ) )
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

            setPartitioningForTable( pModel, lTablename, lOwner, lRefPartitions );

            lRefPartitions.setFkName( pResultSet.getString( "ref_ptn_constraint_name" ) );

            String lSql = "" + //
            " select partition_name," + //
            "        tablespace_name" + //
            "   from " + getDataDictionaryView( "tab_partitions" ) + //
            "  where table_name = ?" + //
            "    and owner = ?" + //
            "  order by partition_position" + //
            "";

            new WrapperIteratorResultSet( lSql, getCallableStatementProvider(), ImmutableList.of( lTablename, lOwner ) )
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

  private List<ColumnRef> loadPartitionColumns( final String pTablename, final String pOwner )
  {
    final List<ColumnRef> lColumnRefList = new ArrayList<ColumnRef>();

    String lSql = "" + //
                  "  select column_name" + //
                  "    from " + getDataDictionaryView( "part_key_columns" ) + //
                  "   where name = ?" + //
                  "     and owner = ?" + //
                  "     and object_type = 'TABLE'" + //
                  "   order by column_position" + //
                  "";

    new WrapperIteratorResultSet( lSql, getCallableStatementProvider(), ImmutableList.of( pTablename, pOwner ) )
    {
      @Override
      protected void useResultSetRow( ResultSet pResultSet ) throws SQLException
      {
        ColumnRef lColumnRef = new ColumnRefImpl();

        lColumnRef.setColumn_name_string( pResultSet.getString( "column_name" ) );
        lColumnRefList.add( lColumnRef );
      }
    }.execute();

    return lColumnRefList;
  }

  private void setPartitioningForTable( Model pModel, String pTablename, String pOwner, TablePartitioning pTablePartitioning )
  {
    Table lTable = findTable( pModel, pTablename, pOwner );
    lTable.setTablePartitioning( pTablePartitioning );
    lTable.setLogging( LoggingType.LOGGING );
  }
}
