package de.opitzconsulting.orcas.diff;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;

import de.opitzconsulting.orcas.orig.diff.DiffRepository;
import de.opitzconsulting.orcas.orig.diff.ModelMerge;
import de.opitzconsulting.orcas.sql.WrapperIteratorResultSet;
import de.opitzconsulting.origOrcasDsl.BuildModeType;
import de.opitzconsulting.origOrcasDsl.CharType;
import de.opitzconsulting.origOrcasDsl.Column;
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
import de.opitzconsulting.origOrcasDsl.Index;
import de.opitzconsulting.origOrcasDsl.IndexGlobalType;
import de.opitzconsulting.origOrcasDsl.IndexOrUniqueKey;
import de.opitzconsulting.origOrcasDsl.InlineComment;
import de.opitzconsulting.origOrcasDsl.LobStorage;
import de.opitzconsulting.origOrcasDsl.LoggingType;
import de.opitzconsulting.origOrcasDsl.Model;
import de.opitzconsulting.origOrcasDsl.ModelElement;
import de.opitzconsulting.origOrcasDsl.Mview;
import de.opitzconsulting.origOrcasDsl.OrderType;
import de.opitzconsulting.origOrcasDsl.ParallelType;
import de.opitzconsulting.origOrcasDsl.PermanentnessTransactionType;
import de.opitzconsulting.origOrcasDsl.PermanentnessType;
import de.opitzconsulting.origOrcasDsl.PrimaryKey;
import de.opitzconsulting.origOrcasDsl.RefreshMethodType;
import de.opitzconsulting.origOrcasDsl.RefreshModeType;
import de.opitzconsulting.origOrcasDsl.Sequence;
import de.opitzconsulting.origOrcasDsl.Table;
import de.opitzconsulting.origOrcasDsl.UniqueKey;
import de.opitzconsulting.origOrcasDsl.impl.ColumnImpl;
import de.opitzconsulting.origOrcasDsl.impl.ColumnRefImpl;
import de.opitzconsulting.origOrcasDsl.impl.ConstraintImpl;
import de.opitzconsulting.origOrcasDsl.impl.ForeignKeyImpl;
import de.opitzconsulting.origOrcasDsl.impl.IndexImpl;
import de.opitzconsulting.origOrcasDsl.impl.InlineCommentImpl;
import de.opitzconsulting.origOrcasDsl.impl.LobStorageImpl;
import de.opitzconsulting.origOrcasDsl.impl.ModelImpl;
import de.opitzconsulting.origOrcasDsl.impl.MviewImpl;
import de.opitzconsulting.origOrcasDsl.impl.PrimaryKeyImpl;
import de.opitzconsulting.origOrcasDsl.impl.SequenceImpl;
import de.opitzconsulting.origOrcasDsl.impl.TableImpl;
import de.opitzconsulting.origOrcasDsl.impl.UniqueKeyImpl;
import oracle.jdbc.OracleDriver;

public class LoadIst
{
  public static void main( String[] pArgs )
  {
    pArgs = new String[] { OracleDriver.class.getName(), "jdbc:oracle:thin:@localhost:1522:XE", "orcas_orderentry", "orcas_orderentry" };

    JdbcConnectionHandler.initWithMainParameters( pArgs );

    LoadIst lLoadIst = new LoadIst();

    InitDiffRepository.init();

    Model lModel = lLoadIst.loadModel();

    DiffRepository.getModelMerge().cleanupValues( lModel );
  }

  private Map<String,List<String>> excludeMap = new HashMap<String,List<String>>();

  private Map<String,Object> constraintMapForFK = new HashMap<String,Object>();
  private Map<String,String> constraintTableMapForFK = new HashMap<String,String>();

  private void registerConstarintForeFK( String pConstraintname, String pTablename, Object pConstarint )
  {
    constraintMapForFK.put( pConstraintname, pConstarint );
    constraintTableMapForFK.put( pConstraintname, pTablename );
  }

  public Model loadModel()
  {
    Model pModel = new ModelImpl();

    loadSequencesIntoModel( pModel );

    loadMViewsIntoModel( pModel );

    loadTablesIntoModel( pModel );
    loadTableColumnsIntoModel( pModel );

    loadLobstorageIntoModel( pModel );

    loadIndexesIntoModel( pModel );
    loadIndexColumnsIntoModel( pModel );
    loadIndexExpressionsIntoModel( pModel );

    loadTableConstraintsIntoModel( pModel );
    loadTableConstraintColumnsIntoModel( pModel );

    loadTableCommentsIntoModel( pModel );
    loadTableColumnCommentsIntoModel( pModel );

    updateForeignkeyDestdata( pModel );

    return pModel;
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

      String lSql = "select object_name, case when ( " +
                    getExcludeWhere( pExcludeWhere ) +
                    " ) then 1 else 0 end is_exclude from user_objects where object_type=?";

      new WrapperIteratorResultSet( lSql, JdbcConnectionHandler.getCallableStatementProvider(), Collections.singletonList( pType ) )
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
  { // TODO @
    return isIgnored( pString, "@", "SEQUENCE" );
  }

  private boolean isIgnoredMView( String pString )
  { // TODO @
    return isIgnored( pString, "@", "MATERIALIZED VIEW" );
  }

  private boolean isIgnoredTable( String pString )
  { // TODO @
    return isIgnored( pString, "@", "TABLE" );
  }

  private int toInt( BigDecimal pBigDecimal )
  {
    if( pBigDecimal == null )
    {
      return DiffRepository.getNullIntValue();
    }
    return pBigDecimal.intValue();
  }

  private void loadSequencesIntoModel( final Model pModel )
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

    new WrapperIteratorResultSet( lSql, JdbcConnectionHandler.getCallableStatementProvider() )
    {
      @Override
      protected void useResultSetRow( ResultSet pResultSet ) throws SQLException
      {
        if( !isIgnoredSequence( pResultSet.getString( 1 ) ) )
        {
          Sequence lSequence = new SequenceImpl();

          lSequence.setSequence_name( pResultSet.getString( "sequence_name" ) );
          lSequence.setIncrement_by( toInt( pResultSet.getBigDecimal( "increment_by" ) ) );
          lSequence.setMax_value_select( pResultSet.getString( "last_number" ) );
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
                  " select mview_name," + //
                  "        query," + //
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

    new WrapperIteratorResultSet( lSql, JdbcConnectionHandler.getCallableStatementProvider() )
    {
      @Override
      protected void useResultSetRow( ResultSet pResultSet ) throws SQLException
      {

        if( !isIgnoredMView( pResultSet.getString( "mview_name" ) ) )
        {
          final Mview lMview = new MviewImpl();

          lMview.setMview_name( pResultSet.getString( "mview_name" ) );
          lMview.setViewSelectCLOB( "\"" +
                                    pResultSet.getString( "query" ) +
                                    "\"" );

          // -- Zeilenumbrüche entfernen für Vergleichbarkeit
          // TODO v_orig_mview.i_viewselectclob := replace(replace(replace(v_orig_mview.i_viewselectclob, chr(13) || chr(10),' '), chr(10),' '), chr(13),' ');

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

            if( pResultSet.getString( "compression" ) != null )
            {
              if( "ENABLED".equals( pResultSet.getString( "compression" ) ) )
              {
                lMview.setCompression( CompressType.COMPRESS );
                if( pResultSet.getString( "compress_for" ).contains( "OLTP" ) )
                {
                  lMview.setCompressionFor( CompressForType.ALL );
                }
                if( "BASIC".equals( pResultSet.getString( "compress_for" ) ) )
                {
                  lMview.setCompressionFor( CompressForType.DIRECT_LOAD );
                }
                if( "QUERY LOW".equals( pResultSet.getString( "compress_for" ) ) )
                {
                  lMview.setCompressionFor( CompressForType.QUERY_LOW );
                }
                if( "QUERY HIGH".equals( pResultSet.getString( "compress_for" ) ) )
                {
                  lMview.setCompressionFor( CompressForType.QUERY_HIGH );
                }
                if( "ARCHIVE LOW".equals( pResultSet.getString( "compress_for" ) ) )
                {
                  lMview.setCompressionFor( CompressForType.ARCHIVE_LOW );
                }
                if( "ARCHIVE HIGH".equals( pResultSet.getString( "compress_for" ) ) )
                {
                  lMview.setCompressionFor( CompressForType.ARCHIVE_HIGH );
                }
              }
              if( "DISABLED".equals( pResultSet.getString( "compression" ) ) )
              {
                lMview.setCompression( CompressType.NOCOMPRESS );
              }
            }

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

    throw new IllegalStateException( "Table not found: " +
                                     pTablename );
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

    throw new IllegalStateException( "Index not found: " +
                                     pTablename +
                                     " " +
                                     pIndexname );
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

    throw new IllegalStateException( "UK not found: " +
                                     pTablename +
                                     " " +
                                     pUniquekeyname );
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

    throw new IllegalStateException( "FK not found: " +
                                     pTablename +
                                     " " +
                                     pForeignkeyname );
  }

  private void loadTableColumnsIntoModel( final Model pModel )
  {
    String lSql = "" + //
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
                  // TODO $IF DBMS_DB_VERSION.VERSION >= 12 $THEN
                  // default_on_null,
                  // generation_type
                  // $ELSE
                  "        null default_on_null," + //
                  "        null generation_type" + //
                  // $END
                  "   from user_tab_cols" + //
                  // $IF DBMS_DB_VERSION.VERSION >= 12 $THEN
                  // left outer join user_tab_identity_cols
                  // on ( user_tab_cols.column_name = user_tab_identity_cols.column_name
                  // and user_tab_cols.table_name = user_tab_identity_cols.table_name)
                  // $END
                  "  where hidden_column = 'NO'" + //
                  "  order by table_name, column_id, column_name" + //
                  "";

    // TODO: default_on_null, generation_type
    // TODO: left outer join user_tab_identity_cols on ( user_tab_cols.column_name = user_tab_identity_cols.column_name and user_tab_cols.table_name = user_tab_identity_cols.table_name)

    new WrapperIteratorResultSet( lSql, JdbcConnectionHandler.getCallableStatementProvider() )
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
            if( lColumn.getDefault_value().length() == 0 ||
                lColumn.getDefault_value().equals( "NULL" ) )
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
            if( pResultSet.getString( "data_type" ).contains( "TIMESTAMP" ) )
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
          if( pResultSet.getString( "data_type_owner" ) != null )
          {
            lColumn.setData_type( DataType.OBJECT );
            lColumn.setObject_type( pResultSet.getString( "data_type" ) );

            // TODO
            /*
             * if( cur_tab_cols.data_type_owner not in (user,'PUBLIC') ) then v_orig_column.i_object_type := cur_tab_cols.data_type_owner || '.' || v_orig_column.i_object_type; end if;
             */
          }

          // TODO

          // if( instr(v_orig_column.i_default_value,'ISEQ$$') > 0 )
          // then
          // v_orig_column.i_default_value := null;
          // end if;
          //
          // if( cur_tab_cols.generation_type is not null )
          // then
          // v_orig_column.i_default_value := null;
          //
          // v_orig_column.i_identity := new ot_orig_columnidentity();
          //
          // if( cur_tab_cols.generation_type = 'ALWAYS' )
          // then
          // v_orig_column.i_identity.i_always := 'always';
          // end if;
          // if( cur_tab_cols.generation_type = 'BY DEFAULT' )
          // then
          // v_orig_column.i_identity.i_by_default := 'default';
          // end if;
          //
          // if( cur_tab_cols.default_on_null = 'YES' )
          // then
          // v_orig_column.i_identity.i_on_null := 'null';
          // end if;
          // end if;*/

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

    new WrapperIteratorResultSet( lSql, JdbcConnectionHandler.getCallableStatementProvider() )
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
            if( !"BITMAP".equals( pResultSet.getString( "index_type" ) ) )
            {
              lIndex.setGlobal( IndexGlobalType.GLOBAL );
            }
          }
          else
          {
            lIndex.setGlobal( IndexGlobalType.LOCAL );
            // TODO
            lIndex.setLogging( LoggingType.LOGGING );
          }

          if( "NO".equals( pResultSet.getString( "partitioned" ) ) )
          {
            lIndex.setLogging( LoggingType.LOGGING );
          }
          else
          {
            lIndex.setLogging( LoggingType.NOLOGGING );
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
    new WrapperIteratorResultSet( lSql, JdbcConnectionHandler.getCallableStatementProvider() )
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

    // TODO replace(replace( ltrim(p_expression,',') ,'"',NULL),' ',NULL);
    lIndex.getIndex_columns().get( pColumnPosition ).setColumn_name( pExpression );

    if( pColumnPosition == pMaxColumnPositionForInd )
    {
      // TODO
      // lIndex.setFunction_based_expression( v_orig_index.i_index_columns );
      lIndex.getIndex_columns().clear();
    }

  }

  private void loadIndexExpressionsIntoModel( final Model pModel )
  {
    String lSql = "" + //
                  " select user_ind_expressions.table_name," + //
                  "        user_ind_expressions.index_name," + //
                  "        column_expression," + //
                  "        column_position," + //
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

    new WrapperIteratorResultSet( lSql, JdbcConnectionHandler.getCallableStatementProvider() )
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
                  "        search_condition," + //
                  "        r_constraint_name," + //
                  "        delete_rule," + //
                  "        deferrable," + //
                  "        deferred," + //
                  "        user_constraints.status," + //
                  "        user_constraints.generated," + //
                  "        user_constraints.index_name," + //
                  "        user_indexes.tablespace_name," + //
                  "        user_indexes.index_type" + //
                  "   from user_constraints" + //
                  "   left outer join user_indexes on (user_constraints.index_name = user_indexes.index_name)" + //
                  "  order by table_name," + //
                  "           constraint_name" + //
                  "";

    new WrapperIteratorResultSet( lSql, JdbcConnectionHandler.getCallableStatementProvider() )
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

            if( !lGeneratedName )
            {
              lForeignKey.setConsName( pResultSet.getString( "constraint_name" ) );
            }

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

    new WrapperIteratorResultSet( lSql, JdbcConnectionHandler.getCallableStatementProvider() )
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

    new WrapperIteratorResultSet( lSql, JdbcConnectionHandler.getCallableStatementProvider() )
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

    new WrapperIteratorResultSet( lSql, JdbcConnectionHandler.getCallableStatementProvider() )
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

    new WrapperIteratorResultSet( lSql, JdbcConnectionHandler.getCallableStatementProvider() )
    {
      @Override
      protected void useResultSetRow( ResultSet pResultSet ) throws SQLException
      {
        if( !isIgnoredTable( pResultSet.getString( "table_name" ) ) )
        {
          LobStorage lLobStorage = new LobStorageImpl();

          lLobStorage.setColumn_name( pResultSet.getString( "column_name" ) );

          lLobStorage.setTablespace( pResultSet.getString( "tablespace_name" ) );

          findTable( pModel, pResultSet.getString( "table_name" ) ).getLobStorages().add( lLobStorage );
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

    new WrapperIteratorResultSet( lSql, JdbcConnectionHandler.getCallableStatementProvider() )
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

          // TODO
          // if( cur_tables.compression is not null )
          // then
          // if ( upper(cur_tables.compression) = 'ENABLED' )
          // then
          // v_orig_table.i_compression := ot_orig_compresstype.c_compress;
          // if ( upper(NVL(cur_tables.compress_for,'NULL')) like '%OLTP%' )
          // then
          // v_orig_table.i_compressionfor := ot_orig_compressfortype.c_all();
          // elsif ( upper(NVL(cur_tables.compress_for,'NULL')) = 'BASIC' )
          // then
          // v_orig_table.i_compressionfor := ot_orig_compressfortype.c_direct_load();
          // elsif ( upper(NVL(cur_tables.compress_for,'NULL')) = 'QUERY LOW' )
          // then
          // v_orig_table.i_compressionfor := ot_orig_compressfortype.c_query_low();
          // elsif ( upper(NVL(cur_tables.compress_for,'NULL')) = 'QUERY HIGH' )
          // then
          // v_orig_table.i_compressionfor := ot_orig_compressfortype.c_query_high();
          // elsif ( upper(NVL(cur_tables.compress_for,'NULL')) = 'ARCHIVE LOW' )
          // then
          // v_orig_table.i_compressionfor := ot_orig_compressfortype.c_archive_low();
          // elsif ( upper(NVL(cur_tables.compress_for,'NULL')) = 'ARCHIVE HIGH' )
          // then
          // v_orig_table.i_compressionfor := ot_orig_compressfortype.c_archive_high();
          // end if;
          // elsif ( upper(cur_tables.compression) = 'DISABLED' )
          // then
          // v_orig_table.i_compression := ot_orig_compresstype.c_nocompress;
          // end if;
          // end if;
        }
      }
    }.execute();
  }
}

//
//
//
//procedure set_mviewlog( p_table_name in varchar2, p_orig_mviewlog in ot_orig_mviewlog )
//is
//  v_table_index number;
//begin
//  v_table_index := find_table_index( p_table_name );
//  
//  v_return( v_table_index ).i_mviewlog := p_orig_mviewlog;
//end;    
//
//procedure add_mviewlog_column( p_table_name in varchar2, p_orig_columnref in ot_orig_columnref )
//is
//  v_table_index number;     
//  v_orig_mviewlog ot_orig_mviewlog;
//begin
//  v_table_index := find_table_index( p_table_name );  
//
//  v_orig_mviewlog := v_return( v_table_index ).i_mviewlog;
//  
//  if ( v_orig_mviewlog.i_columns is null) 
//  then
//    v_orig_mviewlog.i_columns := new ct_orig_columnref_list();
//  end if;
//  
//  v_orig_mviewlog.i_columns.extend(1);
//  v_orig_mviewlog.i_columns( v_orig_mviewlog.i_columns.count ) := p_orig_columnref;
//  
//  v_return( v_table_index ).i_mviewlog := v_orig_mviewlog;
//end;        
//
//procedure load_mviewlog_columns
//is
//  v_orig_columnref ot_orig_columnref;
//begin            
//  for cur_mviewlog_columns in
//    (
//    select master, column_name, column_id from(
//
//      select logs.master,
//                     tab_columns.column_name,
//                     tab_columns.column_id
//                from user_mview_logs logs join
//                     user_tab_columns tab_columns            
//                  on logs.log_table = tab_columns.table_name 
//                 and tab_columns.column_name not like '%$$'           
//      minus            
//      select logs.master,
//                     tab_columns.column_name,
//                     tab_columns.column_id
//                from user_mview_logs logs 
//                join user_tab_columns tab_columns 
//                  on logs.log_table = tab_columns.table_name 
//                 and tab_columns.column_name not like '%$$' 
//                join user_constraints cons 
//                  on logs.master = cons.table_name
//                 and cons.constraint_type = 'P'
//                join user_cons_columns cons_columns
//                  on cons.constraint_name = cons_columns.constraint_name
//                 and cons_columns.column_name = tab_columns.column_name
//      )           
//    order by master, column_id            
//    )
//  loop
//    if( is_ignored_table(  cur_mviewlog_columns.master ) = 0 )
//    then      
//      v_orig_columnref := new ot_orig_columnref();         
//      v_orig_columnref.i_column_name := cur_mviewlog_columns.column_name;                
//      
//      add_mviewlog_column( cur_mviewlog_columns.master, v_orig_columnref );
//    end if;
//  end loop;    
//end;
//
//procedure load_mviewlogs
//is
//  c_date_format constant varchar2(30) := pa_orcas_run_parameter.get_dateformat();
//  v_orig_mviewlog ot_orig_mviewlog;
//begin    
//  for cur_mviewlogs in
//    (
//    select master,
//           log_table,
//           rowids,
//           primary_key,
//           sequence,
//           include_new_values,
//           purge_asynchronous,
//           purge_deferred,
//           purge_start,
//           case when instr(purge_interval, 'sysdate') > 0 then substr(purge_interval, 11) end purge_interval,
//           case when purge_interval is not null and instr(purge_interval, 'to_date') > 0 
//           then to_date(substr(purge_interval, instr(purge_interval, '''',1,1)+1, instr(purge_interval, '''',1,2)-instr(purge_interval, '''',1,1)-1),substr(purge_interval, instr(purge_interval, '''',1,3)+1, instr(purge_interval, '''',1,4)-instr(purge_interval, '''',1,3)-1))
//           end purge_next,
//           commit_scn_based,
//           tablespace_name, 
//           trim(degree) degree
//      from user_mview_logs logs
//      join user_tables tabs
//        on logs.log_table = tabs.table_name
//     order by master
//    )
//  loop
//    if( is_ignored_table( cur_mviewlogs.master ) = 0 )
//    then      
//      v_orig_mviewlog  := new ot_orig_mviewlog();
//      
//      if (cur_mviewlogs.primary_key = 'YES')
//      then
//        v_orig_mviewlog.i_primarykey := 'primary';
//      end if;  
//      
//      if (cur_mviewlogs.rowids = 'YES')
//      then
//        v_orig_mviewlog.i_rowid := 'rowid';
//      end if;  
//      
//      if (cur_mviewlogs.sequence = 'YES')
//      then
//        v_orig_mviewlog.i_withsequence := 'sequence';
//      end if;  
//      
//      if (cur_mviewlogs.commit_scn_based = 'YES')
//      then
//        v_orig_mviewlog.i_commitscn := 'commit_scn';
//      end if;
//      
//      v_orig_mviewlog.i_purge := 'purge';
//      if (cur_mviewlogs.purge_deferred = 'YES')
//      then
//        v_orig_mviewlog.i_startwith := to_char(cur_mviewlogs.purge_start, c_date_format);
//        v_orig_mviewlog.i_repeatInterval := cur_mviewlogs.purge_interval;
//        v_orig_mviewlog.i_next := to_char(cur_mviewlogs.purge_next, c_date_format);
//      else 
//        if ( cur_mviewlogs.purge_asynchronous = 'YES')
//          then
//            v_orig_mviewlog.i_synchronous := ot_orig_synchronoustype.c_asynchronous;
//          else 
//            v_orig_mviewlog.i_synchronous := ot_orig_synchronoustype.c_synchronous;
//        end if; 
//      end if; 
//      
//      if (cur_mviewlogs.include_new_values = 'YES')
//      then
//        v_orig_mviewlog.i_newvalues := ot_orig_newvaluestype.c_including;
//      else
//        v_orig_mviewlog.i_newvalues := ot_orig_newvaluestype.c_excluding;
//      end if;  
//      
//      v_orig_mviewlog.i_tablespace := cur_mviewlogs.tablespace_name;
//    
//      if( cur_mviewlogs.degree = '1' )        
//      then
//        v_orig_mviewlog.i_parallel := ot_orig_paralleltype.c_noparallel;        
//      else
//        v_orig_mviewlog.i_parallel := ot_orig_paralleltype.c_parallel();
//        if ( cur_mviewlogs.degree != 'DEFAULT' ) 
//        then
//          v_orig_mviewlog.i_parallel_degree := to_number(cur_mviewlogs.degree);
//        end if;
//      end if;   
//      
//      set_mviewlog( cur_mviewlogs.master, v_orig_mviewlog );
//    end if;
//  end loop;    
//end;        
//
//
//
//procedure set_compression( p_table_name in varchar2, p_orig_compression ot_orig_compresstype, p_orig_compressionfor ot_orig_compressfortype )
//is
//  v_table_index number;
//begin
//  v_table_index := find_table_index( p_table_name );
//  
//  v_return( v_table_index ).i_compression := p_orig_compression;
//  v_return( v_table_index ).i_compressionfor := p_orig_compressionfor;
//end;       
//
//procedure set_partitioning( p_table_name in varchar2, p_orig_tablepartitioning ot_orig_tablepartitioning )
//is
//  v_table_index number;
//begin
//  v_table_index := find_table_index( p_table_name );
//  
//  v_return( v_table_index ).i_tablepartitioning := p_orig_tablepartitioning;
//end;   
//
///**
// * Partitionierung wird mit einzelselects geladen, da die Struktur sehr uneinheitlich ist und es erwatungsgemaess nur wenige Daten zu lesen gibt.
// */
//procedure load_partitioning
//is
//  v_orig_hashpartitions ot_orig_hashpartitions;
//  v_orig_hashpartition ot_orig_hashpartition;
//  
//  v_orig_listpartitions ot_orig_listpartitions;
//  v_orig_listpartition ot_orig_listpartition;
//  v_orig_listpartitionvalu ot_orig_listpartitionvalu;      
//  v_orig_listsubpart ot_orig_listsubpart;
//  
//  v_orig_rangepartitions ot_orig_rangepartitions;
//  v_orig_rangepartition ot_orig_rangepartition;
//  v_orig_rangepartitionval ot_orig_rangepartitionval;
//  v_orig_rangesubpart ot_orig_rangesubpart;
//  
//  v_orig_compression ot_orig_compresstype;
//  v_orig_compressionfor ot_orig_compressfortype;
//  
//  v_high_value varchar2(32000);
//  v_exit_loop number;
//  
//  function load_tablesubpart( p_table_name in varchar2, p_subpartitioning_type in varchar2 ) return ot_orig_tablesubpart
//  is
//    v_orig_hashsubparts ot_orig_hashsubparts;
//    v_orig_listsubparts ot_orig_listsubparts;
//    v_orig_rangesubparts ot_orig_rangesubparts;
//    
//    v_orig_columnref ot_orig_columnref;
//  begin
//    if    ( p_subpartitioning_type = 'NONE' )
//    then
//      return null;
//    elsif ( p_subpartitioning_type = 'HASH' )
//    then
//      v_orig_hashsubparts := new ot_orig_hashsubparts();
//      
//      v_orig_hashsubparts.i_column := new ot_orig_columnref();
//      
//      for cur_part_col in
//        (
//        select column_name
//          from user_subpart_key_columns
//         where name = p_table_name
//           and object_type = 'TABLE'
//        )
//      loop
//        v_orig_hashsubparts.i_column.i_column_name := cur_part_col.column_name;
//      end loop;
//      
//      return v_orig_hashsubparts;
//    elsif ( p_subpartitioning_type = 'LIST' )
//    then
//      v_orig_listsubparts := new ot_orig_listsubparts();
//      
//      v_orig_listsubparts.i_column := new ot_orig_columnref();
//      
//      for cur_part_col in
//        (
//        select column_name
//          from user_subpart_key_columns
//         where name = p_table_name
//           and object_type = 'TABLE'
//        )
//      loop
//        v_orig_listsubparts.i_column.i_column_name := cur_part_col.column_name;
//      end loop;
//      
//      return v_orig_listsubparts;
//    elsif ( p_subpartitioning_type = 'RANGE' )
//    then
//      v_orig_rangesubparts := new ot_orig_rangesubparts();
//      
//      v_orig_rangesubparts.i_columns := new ct_orig_columnref_list();
//      
//      for cur_part_col in
//        (
//        select column_name
//          from user_subpart_key_columns
//         where name = p_table_name
//           and object_type = 'TABLE'
//           order by column_position
//        )
//      loop
//        v_orig_columnref := new ot_orig_columnref(cur_part_col.column_name);
//        v_orig_rangesubparts.i_columns.extend;
//        v_orig_rangesubparts.i_columns(v_orig_rangesubparts.i_columns.count) := v_orig_columnref;
//      end loop;
//      
//      return v_orig_rangesubparts;  
//    else
//      --            raise_application_error( -20000, 'partitionstyp unbekannt: ' || cur_part_tables.partitioning_type || ' ' || cur_part_tables.subpartitioning_type );            
//      return null;                
//    end if;        
//  end;
//  
//  function get_orig_listpart_valuelist(v_in_string varchar2)
//    return ct_orig_listpartitionvalu_list is
//    v_listpartitionvalu_list ct_orig_listpartitionvalu_list := new ct_orig_listpartitionvalu_list();
//    v_listpartitionvalu ot_orig_listpartitionvalu := new ot_orig_listpartitionvalu();
//    v_value_list varchar2(2000) := v_in_string;
//    v_comma number;
//    v_weiter boolean := true;
//
//  begin
//
//    while v_weiter loop
//
//      if instr(v_value_list, ',') > 0 then
//        v_comma := instr(v_value_list, ',');
//        v_listpartitionvalu.i_value := replace(substr(v_value_list, 1, v_comma - 1),'DEFAULT','default');
//        v_value_list := substr(v_value_list, v_comma + 1);
//      else
//        v_listpartitionvalu.i_value := v_value_list;
//        v_weiter := false;
//      end if;
//
//      v_listpartitionvalu_list.extend;
//      v_listpartitionvalu_list(v_listpartitionvalu_list.count) := v_listpartitionvalu;
//
//    end loop;
//
//    return v_listpartitionvalu_list;
//  end;  
//  
//  function get_orig_rangepart_valuelist(v_in_string varchar2)
//    return ct_orig_rangepartitionval_list is
//    v_rangepartitionval_list ct_orig_rangepartitionval_list := new ct_orig_rangepartitionval_list();
//    v_rangepartitionval ot_orig_rangepartitionval := new ot_orig_rangepartitionval();
//    v_value_list varchar2(2000) := v_in_string;
//    v_comma number;
//    v_weiter boolean := true;
//
//  begin
//
//    while v_weiter loop
//
//      if instr(v_value_list, ',') > 0 then
//        v_comma := instr(v_value_list, ',');
//        v_rangepartitionval.i_value := replace(substr(v_value_list, 1, v_comma - 1),'MAXVALUE','maxvalue');
//        v_value_list := substr(v_value_list, v_comma + 1);
//      else
//        v_rangepartitionval.i_value := v_value_list;
//        v_weiter := false;
//      end if;
//
//      v_rangepartitionval_list.extend;
//      v_rangepartitionval_list(v_rangepartitionval_list.count) := v_rangepartitionval;
//
//    end loop;
//
//    return v_rangepartitionval_list;
//  end;        
//  
//  function load_subpartlist( p_table_name in varchar2, p_partition_name in varchar2, p_subpartitioning_type in varchar2 ) return ct_orig_subsubpart_list
//  is
//    v_return ct_orig_subsubpart_list;
//    
//    v_orig_hashsubsubpart ot_orig_hashsubsubpart;
//    v_orig_listsubsubpart ot_orig_listsubsubpart;
//    v_orig_rangesubsubpart ot_orig_rangesubsubpart;
//    
//    v_orig_listpartitionvalu_list ct_orig_listpartitionvalu_list;
//    v_orig_listpartitionvalu ot_orig_listpartitionvalu;
//    v_orig_rangepartitionval_list ct_orig_rangepartitionval_list;
//    v_orig_rangepartitionval ot_orig_rangepartitionval;
//  begin
//    v_return := new ct_orig_subsubpart_list();
//    
//    for cur_tab_subpartitions in        
//      (
//      select subpartition_name, 
//             high_value, 
//             tablespace_name
//        from user_tab_subpartitions
//       where table_name = p_table_name
//         and partition_name = p_partition_name
//       order by subpartition_position
//      )
//    loop
//      v_return.extend;
//  
//      if    ( p_subpartitioning_type = 'HASH' )
//      then
//        v_orig_hashsubsubpart := new ot_orig_hashsubsubpart();
//        
//        v_orig_hashsubsubpart.i_name := cur_tab_subpartitions.subpartition_name;
//        v_orig_hashsubsubpart.i_tablespace := cur_tab_subpartitions.tablespace_name;
//        
//        v_return(v_return.count) := v_orig_hashsubsubpart;
//      elsif    ( p_subpartitioning_type = 'LIST' )
//      then
//        v_orig_listsubsubpart := new ot_orig_listsubsubpart();
//        v_orig_listpartitionvalu_list := get_orig_listpart_valuelist(cur_tab_subpartitions.high_value);
//          
//        v_orig_listsubsubpart.i_value := v_orig_listpartitionvalu_list;
//        v_orig_listsubsubpart.i_name := cur_tab_subpartitions.subpartition_name;
//        v_orig_listsubsubpart.i_tablespace := cur_tab_subpartitions.tablespace_name;
//          
//        v_return(v_return.count) := v_orig_listsubsubpart;  
//      elsif    ( p_subpartitioning_type = 'RANGE' )
//      then
//        v_orig_rangesubsubpart := new ot_orig_rangesubsubpart();
//        v_orig_rangepartitionval_list := get_orig_rangepart_valuelist(cur_tab_subpartitions.high_value);
//          
//        v_orig_rangesubsubpart.i_value := v_orig_rangepartitionval_list;
//        v_orig_rangesubsubpart.i_name := cur_tab_subpartitions.subpartition_name;
//        v_orig_rangesubsubpart.i_tablespace := cur_tab_subpartitions.tablespace_name;
//          
//        v_return(v_return.count) := v_orig_rangesubsubpart;    
//      else
//        --            raise_application_error( -20000, 'subpartitionstyp unbekannt: ' || cur_part_tables.subpartitioning_type );            
//        return null;                
//      end if;        
//    end loop;
//    
//    return v_return;
//  end;      
//begin            
//  for cur_part_tables in
//    (
//    select table_name, 
//           partitioning_type, 
//           subpartitioning_type, 
//           interval,
//           def_tablespace_name,
//           def_compression,
//           def_compress_for
//      from user_part_tables
//    )
//  loop
//    if( is_ignored_table( cur_part_tables.table_name ) = 0 )
//    then
//      -- Read compression type, works only for one compression type for all partitions
//      v_orig_compression := null;
//      v_orig_compressionfor := null;
//      if ( upper(NVL(cur_part_tables.def_compression,'NULL')) = 'ENABLED' )  
//      then
//        v_orig_compression := ot_orig_compresstype.c_compress;  
//        if ( upper(NVL(cur_part_tables.def_compress_for,'NULL')) like '%OLTP%' ) 
//        then
//          v_orig_compressionfor := ot_orig_compressfortype.c_all;
//        elsif ( upper(NVL(cur_part_tables.def_compress_for,'NULL')) = 'BASIC' ) 
//        then
//          v_orig_compressionfor := ot_orig_compressfortype.c_direct_load();  
//        elsif ( upper(NVL(cur_part_tables.def_compress_for,'NULL')) = 'QUERY LOW' ) 
//        then
//          v_orig_compressionfor := ot_orig_compressfortype.c_query_low();  
//        elsif ( upper(NVL(cur_part_tables.def_compress_for,'NULL')) = 'QUERY HIGH' ) 
//        then
//          v_orig_compressionfor := ot_orig_compressfortype.c_query_high();    
//        elsif ( upper(NVL(cur_part_tables.def_compress_for,'NULL')) = 'ARCHIVE LOW' ) 
//        then
//          v_orig_compressionfor := ot_orig_compressfortype.c_archive_low();  
//        elsif ( upper(NVL(cur_part_tables.def_compress_for,'NULL')) = 'ARCHIVE HIGH' ) 
//        then
//          v_orig_compressionfor := ot_orig_compressfortype.c_archive_high();      
//        end if;
//      end if;  
//      if (v_orig_compression is not null) 
//      then
//        set_compression( cur_part_tables.table_name, v_orig_compression, v_orig_compressionfor );
//      end if;  
//         
//      if    ( cur_part_tables.partitioning_type = 'HASH' and cur_part_tables.subpartitioning_type = 'NONE' )
//      then
//        v_orig_hashpartitions := ot_orig_hashpartitions();
//        
//        v_orig_hashpartitions.i_column := new ot_orig_columnref();
//        for cur_part_col in
//          (
//          select column_name
//            from user_part_key_columns
//           where name = cur_part_tables.table_name
//             and object_type = 'TABLE'
//          )
//        loop
//          v_orig_hashpartitions.i_column.i_column_name := cur_part_col.column_name;
//        end loop;
//        
//        v_orig_hashpartitions.i_partitionlist := new ct_orig_hashpartition_list();
//        for cur_tab_partitions in
//          (
//          select partition_name,
//                 tablespace_name
//            from user_tab_partitions
//           where table_name = cur_part_tables.table_name
//           order by partition_position
//          )
//        loop
//          v_orig_hashpartition := new ot_orig_hashpartition();
//          
//          v_orig_hashpartition.i_name := cur_tab_partitions.partition_name;
//          v_orig_hashpartition.i_tablespace := cur_tab_partitions.tablespace_name;
//          
//          v_orig_hashpartitions.i_partitionlist.extend;
//          v_orig_hashpartitions.i_partitionlist(v_orig_hashpartitions.i_partitionlist.count) := v_orig_hashpartition;
//        end loop;            
//      
//        set_partitioning( cur_part_tables.table_name, v_orig_hashpartitions );
//      elsif ( cur_part_tables.partitioning_type = 'LIST' )
//      then
//        v_orig_listpartitions := ot_orig_listpartitions();
//        
//        v_orig_listpartitions.i_tablesubpart := load_tablesubpart( cur_part_tables.table_name, cur_part_tables.subpartitioning_type );
//        v_orig_listpartitions.i_column := new ot_orig_columnref();
//        for cur_part_col in
//          (
//          select column_name
//            from user_part_key_columns
//           where name = cur_part_tables.table_name
//             and object_type = 'TABLE'
//          )
//        loop
//          v_orig_listpartitions.i_column.i_column_name := cur_part_col.column_name;
//        end loop;
//        
//        v_orig_listpartitions.i_partitionlist := new ct_orig_listpartition_list();
//        for cur_tab_partitions in
//          (
//          select partition_name,
//                 tablespace_name,
//                 high_value
//            from user_tab_partitions
//           where table_name = cur_part_tables.table_name
//           order by partition_position               
//          )
//        loop
//          v_high_value := cur_tab_partitions.high_value;
//          v_orig_listpartition := new ot_orig_listpartition();
//          
//          v_orig_listpartition.i_name := cur_tab_partitions.partition_name;
//          v_orig_listpartition.i_tablespace := cur_tab_partitions.tablespace_name;
//          
//          v_orig_listpartition.i_value := new ct_orig_listpartitionvalu_list();
//          
//          if( upper(v_high_value )= 'DEFAULT' )
//          then
//            v_orig_listpartitionvalu := new ot_orig_listpartitionvalu();
//            v_orig_listpartitionvalu.i_default := 'default';
//            
//            v_orig_listpartition.i_value.extend();
//            v_orig_listpartition.i_value( v_orig_listpartition.i_value.count ) := v_orig_listpartitionvalu;
//          else
//            v_exit_loop := 0;
//            loop
//              v_orig_listpartitionvalu := new ot_orig_listpartitionvalu();                
//            
//              if( instr( v_high_value, ',' ) = 0 )
//              then
//                v_orig_listpartitionvalu.i_value := trim( v_high_value );
//                v_exit_loop := 1;
//              else                    
//                v_orig_listpartitionvalu.i_value := trim( substr( v_high_value, 1, instr( v_high_value, ',' ) - 1 ) );                  
//                v_high_value := substr( v_high_value, instr( v_high_value, ',' ) + 1 );
//              end if;
//              
//              v_orig_listpartition.i_value.extend();
//              v_orig_listpartition.i_value( v_orig_listpartition.i_value.count ) := v_orig_listpartitionvalu;                                  
//              
//              if( v_exit_loop = 1 )
//              then
//                exit;
//              end if;
//            end loop;
//          end if;
//          
//          v_orig_listpartitions.i_partitionlist.extend;
//          v_orig_listpartitions.i_partitionlist(v_orig_listpartitions.i_partitionlist.count) := v_orig_listpartition;
//        end loop;            
//        
//        if( v_orig_listpartitions.i_tablesubpart is not null )
//        then
//          v_orig_listpartitions.i_subpartitionlist := new ct_orig_listsubpart_list();
//          
//          for i in 1..v_orig_listpartitions.i_partitionlist.count
//          loop
//            v_orig_listpartition := v_orig_listpartitions.i_partitionlist(i);
//            
//            v_orig_listsubpart := new ot_orig_listsubpart();
//            
//            v_orig_listsubpart.i_name := v_orig_listpartition.i_name;
//            v_orig_listsubpart.i_value := v_orig_listpartition.i_value;
//            v_orig_listsubpart.i_subpartlist := load_subpartlist( cur_part_tables.table_name, v_orig_listsubpart.i_name, cur_part_tables.subpartitioning_type );
//            
//            v_orig_listpartitions.i_subpartitionlist.extend;
//            v_orig_listpartitions.i_subpartitionlist(v_orig_listpartitions.i_subpartitionlist.count) := v_orig_listsubpart;
//          end loop;
//          
//          v_orig_listpartitions.i_partitionlist := null;
//        end if;
//      
//        set_partitioning( cur_part_tables.table_name, v_orig_listpartitions );
//      elsif ( cur_part_tables.partitioning_type = 'RANGE' )
//      then
//        v_orig_rangepartitions := ot_orig_rangepartitions();
//        
//        v_orig_rangepartitions.i_intervalexpression := cur_part_tables.interval;
//        
//        v_orig_rangepartitions.i_tablesubpart := load_tablesubpart( cur_part_tables.table_name, cur_part_tables.subpartitioning_type );
//        v_orig_rangepartitions.i_columns := new ct_orig_columnref_list();
//        for cur_part_col in
//          (
//          select column_name
//            from user_part_key_columns
//           where name = cur_part_tables.table_name
//             and object_type = 'TABLE'
//           order by column_position
//          )
//        loop
//          v_orig_rangepartitions.i_columns.extend;
//          v_orig_rangepartitions.i_columns( v_orig_rangepartitions.i_columns.count ) := new ot_orig_columnref();
//          v_orig_rangepartitions.i_columns( v_orig_rangepartitions.i_columns.count ).i_column_name := cur_part_col.column_name;              
//        end loop;
//        
//        v_orig_rangepartitions.i_partitionlist := new ct_orig_rangepartition_list();
//        for cur_tab_partitions in
//          (
//          select partition_name,
//                 tablespace_name,
//                 high_value
//            from user_tab_partitions
//           where table_name = cur_part_tables.table_name
//           order by partition_position               
//          )
//        loop
//          v_high_value := cur_tab_partitions.high_value;
//          v_orig_rangepartition := new ot_orig_rangepartition();
//          
//          v_orig_rangepartition.i_name := cur_tab_partitions.partition_name;
//          v_orig_rangepartition.i_tablespace := cur_tab_partitions.tablespace_name;
//          
//          v_orig_rangepartition.i_value := new ct_orig_rangepartitionval_list();
//          
//          v_exit_loop := 0;
//          loop
//            v_orig_rangepartitionval := new ot_orig_rangepartitionval();                
//          
//            if( instr( v_high_value, ',' ) = 0 )
//            then
//              v_orig_rangepartitionval.i_value := trim( v_high_value );
//              v_exit_loop := 1;
//            else                    
//              v_orig_rangepartitionval.i_value := trim( substr( v_high_value, 1, instr( v_high_value, ',' ) - 1 ) );                 
//              v_high_value := substr( v_high_value, instr( v_high_value, ',' ) + 1 );
//            end if;
//            
//            if( upper(v_orig_rangepartitionval.i_value) = 'MAXVALUE' )
//            then
//              v_orig_rangepartitionval.i_maxvalue := 'maxvalue';
//              v_orig_rangepartitionval.i_value := null;
//            end if;                
//            
//            v_orig_rangepartition.i_value.extend();
//            v_orig_rangepartition.i_value( v_orig_rangepartition.i_value.count ) := v_orig_rangepartitionval;                                  
//            
//            if( v_exit_loop = 1 )
//            then
//              exit;
//            end if;
//          end loop;
//          
//          v_orig_rangepartitions.i_partitionlist.extend;
//          v_orig_rangepartitions.i_partitionlist(v_orig_rangepartitions.i_partitionlist.count) := v_orig_rangepartition;
//        end loop;      
//        
//        -- SUBPARTIIONING
//        if( v_orig_rangepartitions.i_tablesubpart is not null )
//        then
//          v_orig_rangepartitions.i_subpartitionlist := new ct_orig_rangesubpart_list();
//          
//          for i in 1..v_orig_rangepartitions.i_partitionlist.count
//          loop
//            v_orig_rangepartition := v_orig_rangepartitions.i_partitionlist(i);
//            
//            v_orig_rangesubpart := new ot_orig_rangesubpart();
//            
//            v_orig_rangesubpart.i_name := v_orig_rangepartition.i_name;
//            v_orig_rangesubpart.i_value := v_orig_rangepartition.i_value;
//            v_orig_rangesubpart.i_subpartlist := load_subpartlist( cur_part_tables.table_name, v_orig_rangesubpart.i_name, cur_part_tables.subpartitioning_type );
//            
//            v_orig_rangepartitions.i_subpartitionlist.extend;
//            v_orig_rangepartitions.i_subpartitionlist(v_orig_rangepartitions.i_subpartitionlist.count) := v_orig_rangesubpart;
//          end loop;
//          
//          v_orig_rangepartitions.i_partitionlist := null;
//        end if;
//      
//        set_partitioning( cur_part_tables.table_name, v_orig_rangepartitions );            
//      else
//--            raise_application_error( -20000, 'partitionstyp unbekannt: ' || cur_part_tables.partitioning_type || ' ' || cur_part_tables.subpartitioning_type );            
//        null;
//      end if;
//    end if;
//  end loop;    
//end;    
//
//begin
