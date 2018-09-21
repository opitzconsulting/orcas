package de.opitzconsulting.orcas.diff;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;

import de.opitzconsulting.orcas.orig.diff.DiffRepository;
import de.opitzconsulting.orcas.sql.CallableStatementProvider;
import de.opitzconsulting.orcas.sql.WrapperIteratorResultSet;
import de.opitzconsulting.orcas.sql.WrapperReturnFirstValue;
import de.opitzconsulting.origOrcasDsl.Column;
import de.opitzconsulting.origOrcasDsl.ColumnRef;
import de.opitzconsulting.origOrcasDsl.CommentObjectType;
import de.opitzconsulting.origOrcasDsl.Constraint;
import de.opitzconsulting.origOrcasDsl.DataType;
import de.opitzconsulting.origOrcasDsl.FkDeleteRuleType;
import de.opitzconsulting.origOrcasDsl.ForeignKey;
import de.opitzconsulting.origOrcasDsl.Index;
import de.opitzconsulting.origOrcasDsl.IndexOrUniqueKey;
import de.opitzconsulting.origOrcasDsl.InlineComment;
import de.opitzconsulting.origOrcasDsl.LobStorage;
import de.opitzconsulting.origOrcasDsl.Model;
import de.opitzconsulting.origOrcasDsl.ModelElement;
import de.opitzconsulting.origOrcasDsl.ParallelType;
import de.opitzconsulting.origOrcasDsl.PrimaryKey;
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
import de.opitzconsulting.origOrcasDsl.impl.PrimaryKeyImpl;
import de.opitzconsulting.origOrcasDsl.impl.TableImpl;
import de.opitzconsulting.origOrcasDsl.impl.UniqueKeyImpl;

public class LoadIstMySql extends LoadIst
{
  private Map<String, List<String>> includeMap = new HashMap<String, List<String>>();

  private Map<String, Object> constraintMapForFK = new HashMap<String, Object>();
  private Map<String, Table> constraintTableMapForFK = new HashMap<String, Table>();

  private Parameters _parameters;

  private CallableStatementProvider _callableStatementProvider;

  public LoadIstMySql( CallableStatementProvider pCallableStatementProvider, Parameters pParameters )
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
    isIgnoredTable( "TEST", "TEST" );

    Model pModel = new ModelImpl();

    loadTablesIntoModel( pModel );
    loadTableColumnsIntoModel( pModel );

    /*
     * loadLobstorageIntoModel( pModel );
     */
    loadIndexesIntoModel( pModel );
    loadIndexColumnsIntoModel( pModel );
    /*
     * loadIndexExpressionsIntoModel( pModel );
     */
    loadTableConstraintsIntoModel( pModel );
    loadTableConstraintColumnsIntoModel( pModel );
    /*
     * loadTableCommentsIntoModel( pModel ); loadTableColumnCommentsIntoModel(
     * pModel );
     * 
     * updateForeignkeyDestdata( pModel );
     */

    return pModel;
  }

  // private String getExcludeWhere( String pExcludeWhere )
  // {
  // if( pExcludeWhere.charAt( 0 ) == '@' )
  // {
  // return "object_name like '%$%'";
  // }
  // else
  // {
  // return pExcludeWhere;
  // }
  // }

  private void loadIgnoreCache( String pExcludeWhere, final String pType )
  {
    if( !includeMap.containsKey( pType ) )
    {
      includeMap.put( pType, new ArrayList<String>() );

      String lSql = "select table_name, owner, 0 is_exclude from " + getDataDictionaryView( "tables" );

      new WrapperIteratorResultSet( lSql, getCallableStatementProvider() )
      {
        @Override
        protected void useResultSetRow( ResultSet pResultSet ) throws SQLException
        {
          if( pResultSet.getInt( "is_exclude" ) == 0 )
          {
            includeMap.get( pType ).add( getNameWithOwner( pResultSet.getString( "table_name" ), pResultSet.getString( "owner" ) ) );
          }
        }
      }.execute();

      // String lSql = "select object_name, owner, case when ( " +
      // getExcludeWhere( pExcludeWhere ) + " ) then 1 else 0 end is_exclude
      // from " + getDataDictionaryView( "objects" ) + " where object_type=?";
      //
      // new WrapperIteratorResultSet( lSql, getCallableStatementProvider(),
      // Collections.singletonList( pType ) )
      // {
      // @Override
      // protected void useResultSetRow( ResultSet pResultSet ) throws
      // SQLException
      // {
      // if( pResultSet.getInt( "is_exclude" ) == 0 )
      // {
      // includeMap.get( pType ).add( getNameWithOwner( pResultSet.getString(
      // "object_name" ), pResultSet.getString( "owner" ) ) );
      // }
      // }
      // }.execute();
    }
  }

  private boolean isIgnored( String pName, String pOwner, String pExcludeWhere, String pType )
  {
    loadIgnoreCache( pExcludeWhere, pType );

    return !includeMap.get( pType ).contains( getNameWithOwner( pName, pOwner ) );
  }

  private boolean isIgnoredTable( String pString, String pOwner )
  {
    if( pString.equalsIgnoreCase( OrcasScriptRunner.ORCAS_UPDATES_TABLE ) )
    {
      return true;
    }

    return isIgnored( pString, pOwner, _parameters.getExcludewheretable(), "TABLE" );
  }

  private int toInt( BigDecimal pBigDecimal )
  {
    if( pBigDecimal == null )
    {
      return DiffRepository.getNullIntValue();
    }
    return pBigDecimal.intValue();
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

  private String getNameWithOwner( String pObjectName, String pOwner )
  {
    if( _parameters.getMultiSchema() )
    {
      return pOwner + "." + pObjectName;
    }
    else
    {
      return pObjectName;
    }
  }

  private Table findTable( Model pModel, String pTablename, String pOwner )
  {
    for( ModelElement lModelElement : pModel.getModel_elements() )
    {
      if( lModelElement instanceof Table )
      {
        if( ((Table) lModelElement).getName().equals( getNameWithOwner( pTablename, pOwner ) ) )
        {
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

  private void loadTableColumnsIntoModel( final Model pModel )
  {
    String lSql;

    lSql = "" + //
           " select table_name," + //
           "        owner," + //
           "        column_name," + //
           "        column_type," + //
           "        character_maximum_length," + //
           "        numeric_precision," + //
           "        numeric_scale," + //
           "        is_nullable," + //
           "        column_default," + //
           "        ordinal_position" + //
           "   from " + getDataDictionaryView( "columns" ) + //
           "";

    if( _parameters.isOrderColumnsByName() )
    {
      lSql += " order by table_name, column_name, ordinal_position";
    }
    else
    {
      lSql += " order by table_name, ordinal_position, column_name";
    }

    new WrapperIteratorResultSet( lSql, getCallableStatementProvider() )
    {
      @Override
      protected void useResultSetRow( ResultSet pResultSet ) throws SQLException
      {
        if( !isIgnoredTable( pResultSet.getString( "table_name" ), pResultSet.getString( "owner" ) ) )
        {
          Column lColumn = new ColumnImpl();

          lColumn.setName( pResultSet.getString( "column_name" ) );

          if( !"NULL".equals( pResultSet.getString( "column_default" ) ) )
          {
            lColumn.setDefault_value( pResultSet.getString( "column_default" ) );
          }

          lColumn.setNotnull( "NO".equals( pResultSet.getString( "is_nullable" ) ) );

          if( pResultSet.getString( "column_type" ).startsWith( "numeric" ) || pResultSet.getString( "column_type" ).startsWith( "decimal" ) )
          {
            lColumn.setData_type( DataType.NUMBER );
            lColumn.setPrecision( pResultSet.getInt( "numeric_precision" ) );
            lColumn.setScale( pResultSet.getInt( "numeric_scale" ) );
          }
          if( pResultSet.getString( "column_type" ).startsWith( "tinyint" ) )
          {
            lColumn.setData_type( DataType.TINYINT );
          }
          if( pResultSet.getString( "column_type" ).startsWith( "smallint" ) )
          {
            lColumn.setData_type( DataType.SMALLINT );
          }
          if( pResultSet.getString( "column_type" ).startsWith( "int" ) )
          {
            lColumn.setData_type( DataType.INT );
          }
          if( pResultSet.getString( "column_type" ).startsWith( "bigint" ) )
          {
            lColumn.setData_type( DataType.BIGINT );
          }
          if( pResultSet.getString( "column_type" ).startsWith( "bit" ) )
          {
            lColumn.setData_type( DataType.BIT );
            lColumn.setPrecision( pResultSet.getInt( "numeric_precision" ) );
          }
          if( pResultSet.getString( "column_type" ).startsWith( "blob" ) )
          {
            lColumn.setData_type( DataType.BLOB );
          }
          if( pResultSet.getString( "column_type" ).startsWith( "clob" ) )
          {
            lColumn.setData_type( DataType.CLOB );
          }
          if( pResultSet.getString( "column_type" ).startsWith( "varchar" ) )
          {
            lColumn.setData_type( DataType.VARCHAR2 );
            lColumn.setPrecision( pResultSet.getInt( "character_maximum_length" ) );
          }
          if( pResultSet.getString( "column_type" ).startsWith( "char" ) )
          {
            lColumn.setData_type( DataType.CHAR );
            lColumn.setPrecision( pResultSet.getInt( "character_maximum_length" ) );
          }
          if( pResultSet.getString( "column_type" ).startsWith( "datetime" ) )
          {
            lColumn.setData_type( DataType.DATE );
          }

          if( pResultSet.getString( "column_type" ).endsWith( "unsigned" ) )
          {
            lColumn.setUnsigned( true );
          }

          findTable( pModel, pResultSet.getString( "table_name" ), pResultSet.getString( "owner" ) ).getColumns().add( lColumn );
        }
      }
    }.execute();

  }

  private String getDataDictionaryView( String pName )
  {
    String lViewName = "information_schema." + pName;

    return "(select " + pName + ".*, database() owner from " + lViewName + " where table_schema = database()) " + pName;
  }

  private void loadIndexesIntoModel( final Model pModel )
  {
    String lSql = "" + //
                  " select distinct" + //
                  "        index_name," + //
                  "        owner," + //
                  "        table_name," + //
                  "        database() as table_owner," + //
                  "        non_unique" + //
                  "   from " + getDataDictionaryView( "statistics" ) + //
                  "  where (index_name,table_name,owner) not in" + //
                  "        (" + //
                  "        select constraint_name," + //
                  "               table_name," + //
                  "               owner" + //
                  "          from " + getDataDictionaryView( "table_constraints" ) + //
                  "         where constraint_type in ( 'PRIMARY KEY', 'UNIQUE' )" + //
                  "        )" + //
                  "  order by table_name," + //
                  "           index_name" + //
                  "";

    new WrapperIteratorResultSet( lSql, getCallableStatementProvider() )
    {
      @Override
      protected void useResultSetRow( ResultSet pResultSet ) throws SQLException
      {
        if( !isIgnoredTable( pResultSet.getString( "table_name" ), pResultSet.getString( "table_owner" ) ) )
        {
          final Index lIndex = new IndexImpl();

          lIndex.setConsName( getNameWithOwner( pResultSet.getString( "index_name" ), pResultSet.getString( "owner" ) ) );

          if( "0".equals( pResultSet.getString( "non_unique" ) ) )
          {
            lIndex.setUnique( "unique" );
          }

          findTable( pModel, pResultSet.getString( "table_name" ), pResultSet.getString( "table_owner" ) ).getInd_uks().add( lIndex );
        }
      }
    }.execute();
  }

  private void loadIndexColumnsIntoModel( final Model pModel )
  {
    String lSql = "" + //
                  " select index_name," + //
                  "        owner," + //
                  "        table_name," + //
                  "        database() as table_owner," + //
                  "        column_name" + //
                  "   from " + getDataDictionaryView( "statistics" ) + //
                  "  where (index_name,table_name,owner) not in" + //
                  "        (" + //
                  "        select constraint_name," + //
                  "               table_name," + //
                  "               owner" + //
                  "          from " + getDataDictionaryView( "table_constraints" ) + //
                  "         where constraint_type in ( 'PRIMARY KEY', 'UNIQUE' )" + //
                  "        )" + //
                  "   order by table_name," + //
                  "            index_name," + //
                  "            seq_in_index" + //
                  "";

    new WrapperIteratorResultSet( lSql, getCallableStatementProvider() )
    {
      @Override
      protected void useResultSetRow( ResultSet pResultSet ) throws SQLException
      {
        if( !isIgnoredTable( pResultSet.getString( "table_name" ), pResultSet.getString( "owner" ) ) )
        {
          ColumnRef lColumnRef = new ColumnRefImpl();

          lColumnRef.setColumn_name( pResultSet.getString( "column_name" ) );

          findIndex( pModel, pResultSet.getString( "table_name" ), pResultSet.getString( "table_owner" ), pResultSet.getString( "index_name" ), pResultSet.getString( "owner" ) ).getIndex_columns().add( lColumnRef );
        }
      }
    }.execute();
  }

  private void setIndexColumnExpression( Model pModel, String pTablename, String pTableOwner, String pIndexName, String pIndexOwner, int pColumnPosition, String pExpression, int pMaxColumnPositionForInd )
  {
    Index lIndex = findIndex( pModel, pTablename, pTableOwner, pIndexName, pIndexOwner );

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
                  "    and (indexes.index_name,indexes.table_name,indexes.owner) not in" + //
                  "        (" + //
                  "        select constraint_name," + //
                  "               table_name," + //
                  "               owner" + //
                  "          from " + getDataDictionaryView( "constraints" ) + //
                  "         where constraint_type in ( 'U', 'P' )" + //
                  "           and constraint_name = constraints.index_name" + //
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
        if( !isIgnoredTable( pResultSet.getString( "table_name" ), pResultSet.getString( "owner" ) ) )
        {
          setIndexColumnExpression( pModel, pResultSet.getString( "table_name" ), pResultSet.getString( "table_owner" ), pResultSet.getString( "index_name" ), pResultSet.getString( "owner" ), pResultSet.getInt( "column_position" ), pResultSet.getString( "column_expression" ), pResultSet.getInt( "max_column_position_for_index" ) );
        }
      }
    }.execute();
  }

  private void loadTableConstraintsIntoModel( final Model pModel )
  {
    String lSql = "" + //
                  " select table_name," + //
                  "        owner," + //
                  "        constraint_name," + //
                  "        constraint_type," + //
                  "        (" + //
                  "        select delete_rule" + //
                  "          from information_schema.referential_constraints" + //
                  "         where referential_constraints.table_name = table_constraints.table_name" + //
                  "           and referential_constraints.constraint_schema = table_constraints.owner" + //
                  "           and referential_constraints.constraint_name = table_constraints.constraint_name" + //
                  "        ) delete_rule" + //
                  "   from " + getDataDictionaryView( "table_constraints" ) + //
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

          if( "PRIMARY KEY".equals( pResultSet.getString( "constraint_type" ) ) )
          {
            PrimaryKey lPrimaryKey = new PrimaryKeyImpl();

            if( !pResultSet.getString( "constraint_name" ).equals( "PRIMARY" ) )
            {
              lPrimaryKey.setConsName( pResultSet.getString( "constraint_name" ) );
            }

            registerConstarintForFK( pResultSet.getString( "constraint_name" ), lTable, pResultSet.getString( "owner" ), lPrimaryKey );

            lTable.setPrimary_key( lPrimaryKey );
          }

          if( "UNIQUE".equals( pResultSet.getString( "constraint_type" ) ) )
          {
            UniqueKey lUniqueKey = new UniqueKeyImpl();

            lUniqueKey.setConsName( pResultSet.getString( "constraint_name" ) );

            registerConstarintForFK( pResultSet.getString( "constraint_name" ), lTable, pResultSet.getString( "owner" ), lUniqueKey );

            lTable.getInd_uks().add( lUniqueKey );
          }

          if( "FOREIGN KEY".equals( pResultSet.getString( "constraint_type" ) ) )
          {
            ForeignKey lForeignKey = new ForeignKeyImpl();

            lForeignKey.setConsName( pResultSet.getString( "constraint_name" ) );

            if( "RESTRICTED".equals( pResultSet.getString( "delete_rule" ) ) )
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

          if( "CHECK".equals( pResultSet.getString( "constraint_type" ) ) )
          {
            Constraint lConstraint = new ConstraintImpl();

            lConstraint.setConsName( pResultSet.getString( "constraint_name" ) );

            lConstraint.setRule( parseCheckConstraintValue( lTable, lConstraint.getConsName() ) );

            lTable.getConstraints().add( lConstraint );
          }
        }
      }

      private String parseCheckConstraintValue( Table pTable, String pCheckConstraintName )
      {
        String lShowCreateResult = getShowCreateResult( pTable );

        String lConstraintStart = "CONSTRAINT `" + pCheckConstraintName + "` CHECK ";

        lShowCreateResult = lShowCreateResult.substring( lShowCreateResult.indexOf( lConstraintStart ) + lConstraintStart.length() + 1 );

        StringBuilder lReturn = new StringBuilder( "" );

        int lNestLevel = 1;
        for( int i = 0;; i++ )
        {
          if( lShowCreateResult.charAt( i ) == '(' )
          {
            lNestLevel++;
          }
          if( lShowCreateResult.charAt( i ) == ')' )
          {
            lNestLevel--;
            if( lNestLevel == 0 )
            {
              return lReturn.toString();
            }
          }

          if( lShowCreateResult.charAt( i ) != '`' )
          {
            lReturn.append( lShowCreateResult.charAt( i ) );
          }
        }
      }

      private String getShowCreateResult( Table lTable )
      {
        return (String) new WrapperReturnFirstValue( "SHOW CREATE TABLE " + lTable.getName(), getCallableStatementProvider() )
        {
          @Override
          protected int getObjectIndex()
          {
            return 2;
          };
        }.executeForValue();
      }
    }.execute();
  }

  private void loadTableConstraintColumnsIntoModel( final Model pModel )
  {
    String lSql = "" + //
                  " select key_column_usage.table_name," + //
                  "        key_column_usage.owner," + //
                  "        column_name," + //
                  "        ordinal_position," + //
                  "        referenced_table_name," + //
                  "        referenced_column_name," + //
                  "        key_column_usage.constraint_name," + //
                  "        table_constraints.constraint_type" + //
                  "   from " + getDataDictionaryView( "key_column_usage" ) + "," + //
                  "       " + getDataDictionaryView( "table_constraints" ) + //
                  " where key_column_usage.constraint_name = table_constraints.constraint_name" + //
                  "   and key_column_usage.table_name = table_constraints.table_name" + //
                  "   and key_column_usage.owner = table_constraints.owner" + //
                  " order by key_column_usage.table_name," + //
                  "          constraint_name," + //
                  "          ordinal_position" + //
                  "";

    new WrapperIteratorResultSet( lSql, getCallableStatementProvider() )
    {
      @Override
      protected void useResultSetRow( ResultSet pResultSet ) throws SQLException
      {
        if( !isIgnoredTable( pResultSet.getString( "table_name" ), pResultSet.getString( "owner" ) ) )
        {
          ColumnRef lColumnRef = new ColumnRefImpl();

          lColumnRef.setColumn_name( pResultSet.getString( "column_name" ) );

          if( "PRIMARY KEY".equals( pResultSet.getString( "constraint_type" ) ) )
          {
            findTable( pModel, pResultSet.getString( "table_name" ), pResultSet.getString( "owner" ) ).getPrimary_key().getPk_columns().add( lColumnRef );
          }

          if( "UNIQUE".equals( pResultSet.getString( "constraint_type" ) ) )
          {
            findUniqueKey( pModel, pResultSet.getString( "table_name" ), pResultSet.getString( "owner" ), pResultSet.getString( "constraint_name" ) ).getUk_columns().add( lColumnRef );
          }

          if( "FOREIGN KEY".equals( pResultSet.getString( "constraint_type" ) ) )
          {
            ForeignKey lForeignKey = findForeignKey( pModel, pResultSet.getString( "table_name" ), pResultSet.getString( "owner" ), pResultSet.getString( "constraint_name" ) );
            lForeignKey.getSrcColumns().add( lColumnRef );
            lForeignKey.setDestTable( pResultSet.getString( "referenced_table_name" ) );

            ColumnRef lDestColumnRef = new ColumnRefImpl();
            lDestColumnRef.setColumn_name( pResultSet.getString( "referenced_column_name" ) );
            lForeignKey.getDestColumns().add( lDestColumnRef );
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

          lInlineComment.setColumn_name( pResultSet.getString( "column_name" ) );

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
                  "        tablespace_name" + //
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
          LobStorage lLobStorage = new LobStorageImpl();

          lLobStorage.setColumn_name( pResultSet.getString( "column_name" ) );

          Table lTable = findTable( pModel, pResultSet.getString( "table_name" ), pResultSet.getString( "owner" ) );

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
                  "        tables.owner," + //
                  "        tables.table_comment" + //
                  "   from " + getDataDictionaryView( "tables" ) + //
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

          if( pResultSet.getString( "table_comment" ) != null && pResultSet.getString( "table_comment" ).trim().length() != 0 )
          {
            InlineComment lInlineComment = new InlineCommentImpl();

            lInlineComment.setComment( pResultSet.getString( "table_comment" ) );
            lInlineComment.setComment_object( CommentObjectType.TABLE );

            lTable.getComments().add( lInlineComment );
          }

          pModel.getModel_elements().add( lTable );
        }
      }
    }.execute();
  }

  private CallableStatementProvider getCallableStatementProvider()
  {
    return _callableStatementProvider;
  }
}
