package de.opitzconsulting.orcas.diff;

import de.opitzconsulting.orcas.orig.diff.DiffRepository;
import de.opitzconsulting.orcas.sql.CallableStatementProvider;
import de.opitzconsulting.orcas.sql.WrapperIteratorResultSet;
import de.opitzconsulting.origOrcasDsl.*;
import de.opitzconsulting.origOrcasDsl.impl.*;
import org.eclipse.emf.common.util.EList;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoadIstAzureSql extends LoadIst {
    private Map<String, List<String>> includeMap = new HashMap<String, List<String>>();

    private Map<String, Object> constraintMapForFK = new HashMap<String, Object>();
    private Map<String, Table> constraintTableMapForFK = new HashMap<String, Table>();

    private Parameters _parameters;

    private CallableStatementProvider _callableStatementProvider;

    public LoadIstAzureSql(CallableStatementProvider pCallableStatementProvider, Parameters pParameters) {
        _callableStatementProvider = pCallableStatementProvider;
        _parameters = pParameters;
    }

    private void registerConstarintForFK(String pUkConstraintname, Table pTable, String pTableOwner, Object pConstarint) {
        constraintMapForFK.put(getNameWithOwner(pUkConstraintname, pTableOwner), pConstarint);
        constraintTableMapForFK.put(getNameWithOwner(pUkConstraintname, pTableOwner), pTable);
    }

    @Override
    public Model loadModel(boolean pWithSequeneceMayValueSelect) {
        isIgnoredTable("TEST", "TEST");

        Model pModel = new ModelImpl();

        loadSequencesIntoModel(pModel, true);

        loadTablesIntoModel(pModel);
        loadTableColumnsIntoModel(pModel);

        /*
         * loadLobstorageIntoModel( pModel );
         */
        loadIndexesIntoModel(pModel);
        loadIndexColumnsIntoModel(pModel);
        /*
         * loadIndexExpressionsIntoModel( pModel );
         */
        loadTableCheckConstraintsIntoModel(pModel);
        loadTableForeignKeysIntoModel(pModel);
        loadTableForeignKeyColumnsIntoModel(pModel);
        //loadTableConstraintColumnsIntoModel(pModel);
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

    private void loadIgnoreCache(String pExcludeWhere, final String pType) {
        if (!includeMap.containsKey(pType)) {
            includeMap.put(pType, new ArrayList<String>());

            String lSql = "";

            if (pType.equals("TABLE")) {
                lSql = "select name, owner, 0 is_exclude from " + getDataDictionaryView("tables");

                if (!ParameterDefaults.excludewheretable.equals(_parameters.getExcludewheretable())) {
                    lSql += " where not (" + _parameters.getExcludewheretable() + ")";
                }
            }

            if (pType.equals("SEQUENCE")) {
                lSql = "select name, owner, 0 is_exclude from " + getDataDictionaryView("sequences");

                if (!ParameterDefaults.excludewheresequence.equals(_parameters.getExcludewheresequence())) {
                    lSql += " where not (" + _parameters.getExcludewheresequence() + ")";
                }
            }

            new WrapperIteratorResultSet(lSql, getCallableStatementProvider()) {
                @Override
                protected void useResultSetRow(ResultSet pResultSet) throws SQLException {
                    if (pResultSet.getInt("is_exclude") == 0) {
                        includeMap.get(pType).add(getNameWithOwner(pResultSet.getString("name"), pResultSet.getString("owner")));
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

    private boolean isIgnored(String pName, String pOwner, String pExcludeWhere, String pType) {
        loadIgnoreCache(pExcludeWhere, pType);

        return !includeMap.get(pType).contains(getNameWithOwner(pName, pOwner));
    }

    private boolean isIgnoredTable(String pString, String pOwner) {
        if (pString.equalsIgnoreCase(OrcasScriptRunner.ORCAS_UPDATES_TABLE)) {
            return true;
        }

        if (pString.equalsIgnoreCase("MSREPLICATION_OPTIONS")) {
            return true;
        }

        if (pString.equalsIgnoreCase("SPT_MONITOR")) {
            return true;
        }
        if (pString.equalsIgnoreCase("SPT_FALLBACK_DB")) {
            return true;
        }
        if (pString.equalsIgnoreCase("SPT_FALLBACK_DEV")) {
            return true;
        }
        if (pString.equalsIgnoreCase("SPT_FALLBACK_USG")) {
            return true;
        }
        if (pString.equalsIgnoreCase("SYSDIAGRAMS")) {
            return true;
        }

        return isIgnored(pString, pOwner, _parameters.getExcludewheretable(), "TABLE");
    }

    private boolean isIgnoredSequence(String pString, String pOwner) {
        return isIgnored(pString, pOwner, _parameters.getExcludewheretable(), "SEQUENCE");
    }

    private int toInt(BigDecimal pBigDecimal) {
        if (pBigDecimal == null) {
            return DiffRepository.getNullIntValue();
        }
        return pBigDecimal.intValue();
    }

    private interface DegreeHandler {
        void setDegree(ParallelType pParallelType, int ParallelDegree);
    }

    private void handleDegree(String pDegree, DegreeHandler pDegreeHandler) {
        if (pDegree != null) {
            ParallelType lParallelType;
            int lParallelDegree = DiffRepository.getNullIntValue();

            if (pDegree.equals("1")) {
                lParallelType = ParallelType.NOPARALLEL;
            } else {
                lParallelType = ParallelType.PARALLEL;
                if (!pDegree.equals("DEFAULT")) {
                    lParallelDegree = toInt(new BigDecimal(pDegree));
                }
            }

            pDegreeHandler.setDegree(lParallelType, lParallelDegree);
        }
    }

    private String getNameWithOwner(String pObjectName, String pOwner) {
        if (_parameters.getMultiSchema()) {
            return pOwner + "." + pObjectName;
        } else {
            return pObjectName;
        }
    }

    private Table findTable(Model pModel, String pTablename, String pOwner) {
        for (ModelElement lModelElement : pModel.getModel_elements()) {
            if (lModelElement instanceof Table) {
                if (((Table) lModelElement).getName().equals(getNameWithOwner(pTablename, pOwner))) {
                    return (Table) lModelElement;
                }
            }
        }

        throw new IllegalStateException("Table not found: " + pTablename);
    }

    private Index findIndex(Model pModel, String pTablename, String pTableOwner, String pIndexname, String pIndexOwner) {
        for (IndexOrUniqueKey lIndexOrUniqueKey : findTable(pModel, pTablename, pTableOwner).getInd_uks()) {
            if (lIndexOrUniqueKey instanceof Index) {
                if (((Index) lIndexOrUniqueKey).getConsName().equals(getNameWithOwner(pIndexname, pIndexOwner))) {
                    return (Index) lIndexOrUniqueKey;
                }
            }
        }

        throw new IllegalStateException("Index not found: " + pTablename + " " + pIndexname);
    }

    private UniqueKey findUniqueKey(Model pModel, String pTablename, String pOwner, String pUniquekeyname) {
        for (IndexOrUniqueKey lIndexOrUniqueKey : findTable(pModel, pTablename, pOwner).getInd_uks()) {
            if (lIndexOrUniqueKey instanceof UniqueKey) {
                if (((UniqueKey) lIndexOrUniqueKey).getConsName().equals(pUniquekeyname)) {
                    return (UniqueKey) lIndexOrUniqueKey;
                }
            }
        }

        throw new IllegalStateException("UK not found: " + pTablename + " " + pUniquekeyname);
    }

    private ForeignKey findForeignKey(Model pModel, String pTablename, String pOwner, String pForeignkeyname) {
        for (ForeignKey lForeignKey : findTable(pModel, pTablename, pOwner).getForeign_keys()) {
            if (lForeignKey.getConsName().equals(pForeignkeyname)) {
                return lForeignKey;
            }
        }

        throw new IllegalStateException("FK not found: " + pTablename + " " + pForeignkeyname);
    }

    private void loadTableColumnsIntoModel(final Model pModel) {
        String lSql;

        lSql = "" + //
                " select tables.name as table_name," + //
                "        columns.owner," + //
                "        columns.name as column_name," + //
                "        sys.types.name as data_type," + //
                "        columns.max_length as max_length," + //
                "        columns.precision as precision," + //
                "        columns.scale as scale," + //
                "        columns.is_nullable as is_nullable," + //
                "        columns.is_computed," + //
                "        (select definition from " + getDataDictionaryView("computed_columns") + " where computed_columns.object_id = columns.object_id and computed_columns.column_id = columns.column_id) as column_virtual_definition," + //
                "        (select definition from " + getDataDictionaryView("default_constraints") + " where default_constraints.object_id = columns.default_object_id) as column_default," + //
                "        (select default_constraints.name from " + getDataDictionaryView("default_constraints") + " where default_constraints.object_id = columns.default_object_id) as column_default_name" + //
                "   from " + getDataDictionaryView("columns") + //
                "       ," + getDataDictionaryView("tables") + //
                "       ,sys.types" + //
                "  where tables.object_id = columns.object_id and sys.types.user_type_id = columns.user_type_id" +//
                "";

        if (_parameters.isOrderColumnsByName()) {
            lSql += " order by tables.name, columns.name, column_id";
        } else {
            lSql += " order by tables.name, column_id, columns.name";
        }

        new WrapperIteratorResultSet(lSql, getCallableStatementProvider()) {
            @Override
            protected void useResultSetRow(ResultSet pResultSet) throws SQLException {
                if (!isIgnoredTable(pResultSet.getString("table_name"), pResultSet.getString("owner"))) {
                    Column lColumn = new ColumnImpl();

                    String column_name = pResultSet.getString("column_name");
                    if (isStringName(column_name)) {
                        lColumn.setName_string(column_name);
                    } else {
                        lColumn.setName(column_name);
                    }

                    if (!"(NULL)".equals(pResultSet.getString("column_default"))) {
                        lColumn.setDefault_value(pResultSet.getString("column_default"));
                        lColumn.setDefault_name(pResultSet.getString("column_default_name"));
                    }

                    if (pResultSet.getBoolean("is_computed")) {
                        lColumn.setVirtual("virtual");
                        lColumn.setDefault_value(pResultSet.getString("column_virtual_definition"));
                    }

                    lColumn.setNotnull(!pResultSet.getBoolean("is_nullable"));

                    if (pResultSet.getString("data_type").startsWith("numeric") || pResultSet.getString("data_type").startsWith("decimal")) {
                        lColumn.setData_type(DataType.NUMBER);
                        lColumn.setPrecision(pResultSet.getInt("precision"));
                        lColumn.setScale(pResultSet.getInt("scale"));
                    }
                    if (pResultSet.getString("data_type").startsWith("tinyint")) {
                        lColumn.setData_type(DataType.TINYINT);
                    }
                    if (pResultSet.getString("data_type").startsWith("smallint")) {
                        lColumn.setData_type(DataType.SMALLINT);
                    }
                    if (pResultSet.getString("data_type").startsWith("int")) {
                        lColumn.setData_type(DataType.INT);
                    }
                    if (pResultSet.getString("data_type").startsWith("bigint")) {
                        lColumn.setData_type(DataType.BIGINT);
                    }
                    if (pResultSet.getString("data_type").startsWith("bit")) {
                        lColumn.setData_type(DataType.BIT);
                    }
                    if (pResultSet.getString("data_type").startsWith("blob")) {
                        lColumn.setData_type(DataType.BLOB);
                    }
                    if (pResultSet.getString("data_type").startsWith("clob")) {
                        lColumn.setData_type(DataType.CLOB);
                    }
                    if (pResultSet.getString("data_type").startsWith("varchar")) {
                        int max_length = pResultSet.getInt("max_length");
                        if (max_length != -1) {
                            lColumn.setData_type(DataType.VARCHAR2);
                            lColumn.setPrecision(max_length);
                        } else {
                            lColumn.setData_type(DataType.CLOB);
                        }
                    }
                    if (pResultSet.getString("data_type").startsWith("char")) {
                        lColumn.setData_type(DataType.CHAR);
                        lColumn.setPrecision(pResultSet.getInt("max_length"));
                    }
                    if (pResultSet.getString("data_type").equals("varbinary")) {
                        int max_length = pResultSet.getInt("max_length");
                        if (max_length != -1) {
                            lColumn.setData_type(DataType.RAW);
                            lColumn.setPrecision(max_length);
                        } else {
                            lColumn.setData_type(DataType.BLOB);
                        }
                    }
                    if (pResultSet.getString("data_type").startsWith("float")) {
                        lColumn.setData_type(DataType.FLOAT);
                    }
                    if (pResultSet.getString("data_type").startsWith("real")) {
                        lColumn.setData_type(DataType.FLOAT);
                    }
                    if (pResultSet.getString("data_type").startsWith("date")) {
                        lColumn.setData_type(DataType.DATE);
                    }
                    if (pResultSet.getString("data_type").startsWith("datetime")) {
                        lColumn.setData_type(DataType.TIMESTAMP);
                    }
                    if (pResultSet.getString("data_type").startsWith("timestamp")) {
                        lColumn.setData_type(DataType.TIMESTAMP);
                        lColumn.setPrecision(pResultSet.getInt("precision"));
                    }
                    if (pResultSet.getString("data_type").startsWith("xml")) {
                        lColumn.setData_type(DataType.XMLTYPE);
                    }

                    if (pResultSet.getString("data_type").endsWith("unsigned")) {
                        lColumn.setUnsigned(true);
                    }

                    if (lColumn.getData_type() == null) {
                        throw new RuntimeException("datatype unknown: " + pResultSet.getString("table_name") + "." + column_name + " " + pResultSet.getString("data_type"));
                    }

          /*if( pResultSet.getString( "extra" ) != null && pResultSet.getString( "extra" ).contains( "auto_increment" ) )
          {
            ColumnIdentity lColumnIdentity = new ColumnIdentityImpl();

            lColumnIdentity.setBy_default( "default" );
            lColumnIdentity.setOn_null( "null" );

            lColumn.setIdentity( lColumnIdentity );
          }*/

                    findTable(pModel, pResultSet.getString("table_name"), pResultSet.getString("owner")).getColumns().add(lColumn);
                }
            }
        }.execute();

    }

    private String getDataDictionaryView(String pName) {
        String lViewName = "sys." + pName;

        return "(select sys." + pName + ".*, schema_name() owner from " + lViewName + ", sys.objects where sys.objects.object_id = sys." + pName + ".object_id and OBJECT_SCHEMA_NAME(sys.objects.object_id) = schema_name()) " + pName;
    }

    private void loadIndexesIntoModel(final Model pModel) {
        String lSql = "" + //
                " select distinct" + //
                "        indexes.name as index_name," + //
                "        indexes.owner as owner," + //
                "        tables.name as table_name," + //
                "        schema_name() as table_owner," + //
                "        indexes.is_primary_key as is_primary_key," + //
                "        indexes.is_unique_constraint as is_unique_constraint," + //
                "        is_unique" + //
                "   from " + getDataDictionaryView("indexes") + //
                "      , " + getDataDictionaryView("tables") + //
                "  where tables.object_id = indexes.object_id" +//
                "  order by tables.name," + //
                "           indexes.name" + //
                "";

        new WrapperIteratorResultSet(lSql, getCallableStatementProvider()) {
            @Override
            protected void useResultSetRow(ResultSet pResultSet) throws SQLException {
                if (!isIgnoredTable(pResultSet.getString("table_name"), pResultSet.getString("table_owner"))) {
                    Table table = findTable(pModel, pResultSet.getString("table_name"), pResultSet.getString("table_owner"));

                    if (pResultSet.getBoolean("is_primary_key")) {
                        PrimaryKey primaryKey = new PrimaryKeyImpl();
                        table.setPrimary_key(primaryKey);
                        primaryKey.setConsName(returnNullIfGenerated(pResultSet.getString("index_name")));
                    } else {
                        if (pResultSet.getString("index_name") != null) {
                            if (pResultSet.getBoolean("is_unique_constraint")) {
                                final UniqueKey lIndex = new UniqueKeyImpl();

                                lIndex.setConsName(getNameWithOwner(pResultSet.getString("index_name"), pResultSet.getString("owner")));

                                table.getInd_uks().add(lIndex);
                            } else {
                                final Index lIndex = new IndexImpl();

                                lIndex.setConsName(getNameWithOwner(pResultSet.getString("index_name"), pResultSet.getString("owner")));

                                if (pResultSet.getBoolean("is_unique")) {
                                    lIndex.setUnique("unique");
                                }

                                table.getInd_uks().add(lIndex);
                            }
                        }
                    }
                }
            }
        }.execute();
    }

    private void loadIndexColumnsIntoModel(final Model pModel) {
        String lSql = "" + //
                " select indexes.name as index_name," + //
                "        indexes.owner as owner," + //
                "        tables.name as table_name," + //
                "        tables.owner as table_owner," + //
                "        columns.name as column_name," + //
                "        indexes.is_primary_key as is_primary_key," + //
                "        indexes.is_unique_constraint as is_unique_constraint" + //
                "   from " + getDataDictionaryView("index_columns") + //
                "      , " + getDataDictionaryView("indexes") + //
                "      , " + getDataDictionaryView("tables") + //
                "      , " + getDataDictionaryView("columns") + //
                "  where tables.object_id = indexes.object_id and index_columns.object_id = indexes.object_id and index_columns.index_id = indexes.index_id and columns.object_id = tables.object_id and columns.column_id = index_columns.column_id" +//
                "   order by tables.name," + //
                "            indexes.name," + //
                "            key_ordinal" + //
                "";

        new WrapperIteratorResultSet(lSql, getCallableStatementProvider()) {
            @Override
            protected void useResultSetRow(ResultSet pResultSet) throws SQLException {
                if (!isIgnoredTable(pResultSet.getString("table_name"), pResultSet.getString("owner"))) {
                    ColumnRef lColumnRef = new ColumnRefImpl();

                    String column_name = pResultSet.getString("column_name");
                    if (isStringName(column_name)) {
                        lColumnRef.setColumn_name_string(column_name);
                    } else {
                        lColumnRef.setColumn_name(column_name);
                    }

                    if (pResultSet.getBoolean("is_primary_key")) {
                        findTable(pModel, pResultSet.getString("table_name"), pResultSet.getString("table_owner")).getPrimary_key().getPk_columns().add(lColumnRef);
                    } else {
                        if (pResultSet.getString("index_name") != null) {
                            if (pResultSet.getBoolean("is_unique_constraint")) {
                                findUniqueKey(pModel, pResultSet.getString("table_name"), pResultSet.getString("table_owner"), pResultSet.getString("index_name")).getUk_columns().add(lColumnRef);
                            } else {
                                findIndex(pModel, pResultSet.getString("table_name"), pResultSet.getString("table_owner"), pResultSet.getString("index_name"), pResultSet.getString("owner")).getIndex_columns().add(lColumnRef);
                            }
                        }
                    }
                }
            }
        }.execute();
    }

    private void setIndexColumnExpression(Model pModel, String pTablename, String pTableOwner, String pIndexName, String pIndexOwner, int pColumnPosition, String pExpression, int pMaxColumnPositionForInd) {
        Index lIndex = findIndex(pModel, pTablename, pTableOwner, pIndexName, pIndexOwner);

        // TODO ltrim(p_expression,',')
        lIndex.getIndex_columns().get(pColumnPosition - 1).setColumn_name(pExpression.replace("\"", "").replace(" ", ""));

        if (pColumnPosition == pMaxColumnPositionForInd) {
            String lString = null;

            for (ColumnRef lColumnRef : lIndex.getIndex_columns()) {
                if (lString == null) {
                    lString = "";
                } else {
                    lString += ",";
                }

                lString += lColumnRef.getColumn_name();
            }

            lIndex.setFunction_based_expression(lString);
            lIndex.getIndex_columns().clear();
        }

    }

    private void loadIndexExpressionsIntoModel(final Model pModel) {
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
                "   from " + getDataDictionaryView("ind_expressions") + "," + //
                "        " + getDataDictionaryView("indexes") + //
                "  where generated = 'N'" + //
                "    and ind_expressions.index_name = indexes.index_name" + //
                "    and ind_expressions.owner = indexes.owner" + //
                "    and (indexes.index_name,indexes.table_name,indexes.owner) not in" + //
                "        (" + //
                "        select constraint_name," + //
                "               table_name," + //
                "               owner" + //
                "          from " + getDataDictionaryView("constraints") + //
                "         where constraint_type in ( 'U', 'P' )" + //
                "           and constraint_name = constraints.index_name" + //
                "        )" + //
                "  order by table_name," + //
                "           index_name," + //
                "           column_position" + //
                "";

        new WrapperIteratorResultSet(lSql, getCallableStatementProvider()) {
            @Override
            protected void useResultSetRow(ResultSet pResultSet) throws SQLException {
                if (!isIgnoredTable(pResultSet.getString("table_name"), pResultSet.getString("owner"))) {
                    setIndexColumnExpression(pModel, pResultSet.getString("table_name"), pResultSet.getString("table_owner"), pResultSet.getString("index_name"), pResultSet.getString("owner"), pResultSet.getInt("column_position"), pResultSet.getString("column_expression"), pResultSet.getInt("max_column_position_for_index"));
                }
            }
        }.execute();
    }

    private void loadSequencesIntoModel(final Model pModel, final boolean pWithSequeneceMayValueSelect) {
        String lSql = "" + //
                " select name," + //
                "        owner" + //
                "   from " + getDataDictionaryView("sequences") + //
                "  order by name" + //
                "";

        new WrapperIteratorResultSet(lSql, getCallableStatementProvider()) {
            @Override
            protected void useResultSetRow(ResultSet pResultSet) throws SQLException {
                if (!isIgnoredSequence(pResultSet.getString("name"), pResultSet.getString("owner"))) {
                    Sequence lSequence = new SequenceImpl();

                    lSequence.setSequence_name(getNameWithOwner(pResultSet.getString("name"), pResultSet.getString("owner")));

                    /*
                     * lSequence.setIncrement_by( toInt( pResultSet.getBigDecimal(
                     * "increment_by" ) ) ); if( pWithSequeneceMayValueSelect ) {
                     * lSequence.setMax_value_select( pResultSet.getString( "last_number"
                     * ) ); } lSequence.setCache( toInt( pResultSet.getBigDecimal(
                     * "cache_size" ) ) ); lSequence.setMinvalue( toInt(
                     * pResultSet.getBigDecimal( "min_value" ) ) ); lSequence.setMaxvalue(
                     * toInt( pResultSet.getBigDecimal( "max_value" ) ) );
                     *
                     * if( "Y".equals( pResultSet.getString( "cycle_flag" ) ) ) {
                     * lSequence.setCycle( CycleType.CYCLE ); } else { lSequence.setCycle(
                     * CycleType.NOCYCLE ); }
                     *
                     * if( "Y".equals( pResultSet.getString( "order_flag" ) ) ) {
                     * lSequence.setOrder( OrderType.ORDER ); } else { lSequence.setOrder(
                     * OrderType.NOORDER ); }
                     */

                    pModel.getModel_elements().add(lSequence);
                }
            }
        }.execute();
    }

    private void loadTableCheckConstraintsIntoModel(final Model pModel) {
        String lSql = "" + //
                " select tables.name as table_name," + //
                "        tables.owner as owner," + //
                "        check_constraints.name as constraint_name," + //
                "        definition" + //
                "   from " + getDataDictionaryView("check_constraints") + //
                "      , " + getDataDictionaryView("tables") + //
                "  where tables.object_id = check_constraints.parent_object_id" + //
                "  order by table_name," + //
                "           constraint_name" + //
                "";

        new WrapperIteratorResultSet(lSql, getCallableStatementProvider()) {
            @Override
            protected void useResultSetRow(ResultSet pResultSet) throws SQLException {
                if (!isIgnoredTable(pResultSet.getString("table_name"), pResultSet.getString("owner"))) {
                    Table lTable = findTable(pModel, pResultSet.getString("table_name"), pResultSet.getString("owner"));

                    Constraint lConstraint = new ConstraintImpl();

                    lConstraint.setConsName(pResultSet.getString("constraint_name"));

                    lConstraint.setRule(pResultSet.getString("definition"));

                    lTable.getConstraints().add(lConstraint);
                }
            }
        }.execute();
    }

    private String returnNullIfGenerated(String pName) {
        if (pName.matches(".*__.*__.*")) {
            return null;
        }
        return pName;
    }

    private boolean isStringName(String pName) {
        if (pName.contains(" ")) {
            return true;
        }
        return false;
    }


    private void loadTableForeignKeysIntoModel(final Model pModel) {
        String lSql = "" + //
                " select tables.name as table_name," + //
                "        tables.owner as owner," + //
                "        ref_table.name as ref_table_name," + //
                "        foreign_keys.name as constraint_name," + //
                "        delete_referential_action_desc" + //
                "   from " + getDataDictionaryView("foreign_keys") + //
                "      , " + getDataDictionaryView("tables") + //
                "      , sys.tables ref_table" + //
                "  where tables.object_id = foreign_keys.parent_object_id and ref_table.object_id = foreign_keys.referenced_object_id" + //
                "  order by tables.name," + //
                "           foreign_keys.name" + //
                "";

        new WrapperIteratorResultSet(lSql, getCallableStatementProvider()) {
            @Override
            protected void useResultSetRow(ResultSet pResultSet) throws SQLException {
                if (!isIgnoredTable(pResultSet.getString("table_name"), pResultSet.getString("owner"))) {
                    Table lTable = findTable(pModel, pResultSet.getString("table_name"), pResultSet.getString("owner"));

                    ForeignKey lForeignKey = new ForeignKeyImpl();

                    lForeignKey.setConsName(pResultSet.getString("constraint_name"));
                    lForeignKey.setDestTable(pResultSet.getString("ref_table_name"));

                    if (pResultSet.getString("delete_referential_action_desc") != null) {
                        if (pResultSet.getString("delete_referential_action_desc").equals("NO_ACTION")) {
                            lForeignKey.setDelete_rule(FkDeleteRuleType.NO_ACTION);
                        }
                        if (pResultSet.getString("delete_referential_action_desc").equals("CASCADE")) {
                            lForeignKey.setDelete_rule(FkDeleteRuleType.CASCADE);
                        }
                        if (pResultSet.getString("delete_referential_action_desc").equals("SET_NULL")) {
                            lForeignKey.setDelete_rule(FkDeleteRuleType.SET_NULL);
                        }
                    }

                    lTable.getForeign_keys().add(lForeignKey);
                }
            }
        }.execute();
    }

    private void loadTableForeignKeyColumnsIntoModel(final Model pModel) {
        String lSql = "" + //
                " select tables.name as table_name," + //
                "        tables.owner as owner," + //
                "        foreign_keys.name as constraint_name," + //
                "        columns.name as column_name," + //
                "        ref_columns.name as ref_column_name" + //
                "   from " + getDataDictionaryView("foreign_keys") + //
                "      , " + getDataDictionaryView("tables") + //
                "      , sys.tables ref_table" + //
                "      , sys.foreign_key_columns foreign_key_columns" + //
                "      , " + getDataDictionaryView("columns") + //
                "      , sys.columns ref_columns" + //
                "  where tables.object_id = foreign_keys.parent_object_id " +//
                "   and ref_table.object_id = foreign_keys.referenced_object_id" + //
                "   and foreign_key_columns.constraint_object_id = foreign_keys.object_id" + //
                "   and columns.column_id = foreign_key_columns.parent_column_id" + //
                "   and columns.object_id = tables.object_id" + //
                "   and ref_columns.column_id = foreign_key_columns.referenced_column_id" + //
                "   and ref_columns.object_id = ref_table.object_id" + //
                "  order by tables.name," + //
                "           foreign_keys.name," + //
                "           foreign_key_columns.constraint_column_id" + //
                "";

        new WrapperIteratorResultSet(lSql, getCallableStatementProvider()) {
            @Override
            protected void useResultSetRow(ResultSet pResultSet) throws SQLException {
                if (!isIgnoredTable(pResultSet.getString("table_name"), pResultSet.getString("owner"))) {
                    ForeignKey foreignKey = findForeignKey(pModel, pResultSet.getString("table_name"), pResultSet.getString("owner"), pResultSet.getString("constraint_name"));

                    ColumnRef lColumnRef = new ColumnRefImpl();
                    String column_name = pResultSet.getString("column_name");
                    if (isStringName(column_name)) {
                        lColumnRef.setColumn_name_string(column_name);
                    } else {
                        lColumnRef.setColumn_name(column_name);
                    }
                    foreignKey.getSrcColumns().add(lColumnRef);

                    ColumnRef lRefColumnRef = new ColumnRefImpl();
                    String ref_column_name = pResultSet.getString("ref_column_name");
                    if (isStringName(ref_column_name)) {
                        lRefColumnRef.setColumn_name_string(ref_column_name);
                    } else {
                        lRefColumnRef.setColumn_name(ref_column_name);
                    }
                    foreignKey.getDestColumns().add(lRefColumnRef);
                }
            }
        }.execute();
    }

    private void loadTableCommentsIntoModel(final Model pModel) {
        String lSql = "" + //
                " select table_name," + //
                "        owner," + //
                "        comments" + //
                "   from " + getDataDictionaryView("tab_comments") + //
                "  where comments is not null" + //
                "  order by table_name" + //
                "";

        new WrapperIteratorResultSet(lSql, getCallableStatementProvider()) {
            @Override
            protected void useResultSetRow(ResultSet pResultSet) throws SQLException {
                if (!isIgnoredTable(pResultSet.getString("table_name"), pResultSet.getString("owner"))) {
                    InlineComment lInlineComment = new InlineCommentImpl();

                    lInlineComment.setComment(pResultSet.getString("comments"));

                    lInlineComment.setComment_object(CommentObjectType.TABLE);

                    findTable(pModel, pResultSet.getString("table_name"), pResultSet.getString("owner")).getComments().add(lInlineComment);
                }
            }
        }.execute();
    }

    private void loadTableColumnCommentsIntoModel(final Model pModel) {
        String lSql = "" + //
                " select table_name," + //
                "        column_name," + //
                "        owner," + //
                "        comments" + //
                "   from " + getDataDictionaryView("col_comments") + //
                "  where comments is not null" + //
                "  order by table_name," + //
                "           column_name" + //
                "";

        new WrapperIteratorResultSet(lSql, getCallableStatementProvider()) {
            @Override
            protected void useResultSetRow(ResultSet pResultSet) throws SQLException {
                if (!isIgnoredTable(pResultSet.getString("table_name"), pResultSet.getString("owner"))) {
                    InlineComment lInlineComment = new InlineCommentImpl();

                    lInlineComment.setComment(pResultSet.getString("comments"));

                    lInlineComment.setColumn_name(pResultSet.getString("column_name"));

                    lInlineComment.setComment_object(CommentObjectType.COLUMN);

                    findTable(pModel, pResultSet.getString("table_name"), pResultSet.getString("owner")).getComments().add(lInlineComment);
                }
            }
        }.execute();
    }

    private void loadLobstorageIntoModel(final Model pModel) {
        String lSql = "" + //
                " select table_name," + //
                "        owner," + //
                "        column_name," + //
                "        tablespace_name" + //
                "   from " + getDataDictionaryView("lobs") + //
                "  order by table_name," + //
                "           column_name" + //
                "";

        new WrapperIteratorResultSet(lSql, getCallableStatementProvider()) {
            @Override
            protected void useResultSetRow(ResultSet pResultSet) throws SQLException {
                if (!isIgnoredTable(pResultSet.getString("table_name"), pResultSet.getString("owner"))) {
                    LobStorage lLobStorage = new LobStorageImpl();

                    lLobStorage.setColumn_name(pResultSet.getString("column_name"));

                    Table lTable = findTable(pModel, pResultSet.getString("table_name"), pResultSet.getString("owner"));

                    if (findColumn(lTable, lLobStorage.getColumn_name()) != null) {
                        lTable.getLobStorages().add(lLobStorage);
                    }
                }
            }

            private Column findColumn(Table pTable, String pColumnName) {
                for (Column lColumn : pTable.getColumns()) {
                    if (lColumn.getName().equals(pColumnName)) {
                        return lColumn;
                    }
                }

                return null;
            }
        }.execute();
    }

    private void updateForeignkeyDestdata(Model pModel) {
        for (ModelElement lModelElement : pModel.getModel_elements()) {
            if (lModelElement instanceof Table) {
                for (ForeignKey lForeignKey : ((Table) lModelElement).getForeign_keys()) {
                    String lRefConstraintName = lForeignKey.getDestTable();

                    lForeignKey.setDestTable(constraintTableMapForFK.get(lRefConstraintName).getName());

                    Object lConstraint = constraintMapForFK.get(lRefConstraintName);

                    EList<ColumnRef> lColumns = null;

                    if (lConstraint instanceof PrimaryKey) {
                        lColumns = ((PrimaryKey) lConstraint).getPk_columns();
                    }

                    if (lConstraint instanceof UniqueKey) {
                        lColumns = ((UniqueKey) lConstraint).getUk_columns();
                    }

                    for (ColumnRef lColumnRef : lColumns) {
                        ColumnRef lNewColumnRef = new ColumnRefImpl();

                        lNewColumnRef.setColumn_name(lColumnRef.getColumn_name());

                        lForeignKey.getDestColumns().add(lNewColumnRef);
                    }
                }
            }
        }
    }

    private void loadTablesIntoModel(final Model pModel) {
        String lSql = "" + //
                " select name," + //
                "        owner" + //
                "   from " + getDataDictionaryView("tables") + //
                "  order by name" + //
                "";

        new WrapperIteratorResultSet(lSql, getCallableStatementProvider()) {
            @Override
            protected void useResultSetRow(ResultSet pResultSet) throws SQLException {
                if (!isIgnoredTable(pResultSet.getString("name"), pResultSet.getString("owner"))) {
                    final Table lTable = new TableImpl();

                    lTable.setName(getNameWithOwner(pResultSet.getString("name"), pResultSet.getString("owner")));

          /*if( pResultSet.getString( "table_comment" ) != null && pResultSet.getString( "table_comment" ).trim().length() != 0 )
          {
            InlineComment lInlineComment = new InlineCommentImpl();

            lInlineComment.setComment( pResultSet.getString( "table_comment" ) );
            lInlineComment.setComment_object( CommentObjectType.TABLE );

            lTable.getComments().add( lInlineComment );
          }*/

                    pModel.getModel_elements().add(lTable);
                }
            }
        }.execute();
    }

    private CallableStatementProvider getCallableStatementProvider() {
        return _callableStatementProvider;
    }
}
