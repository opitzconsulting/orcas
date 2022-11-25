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
import de.opitzconsulting.origOrcasDsl.Column;
import de.opitzconsulting.origOrcasDsl.ColumnIdentity;
import de.opitzconsulting.origOrcasDsl.ColumnRef;
import de.opitzconsulting.origOrcasDsl.CommentObjectType;
import de.opitzconsulting.origOrcasDsl.Constraint;
import de.opitzconsulting.origOrcasDsl.DataType;
import de.opitzconsulting.origOrcasDsl.DeferrType;
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
import de.opitzconsulting.origOrcasDsl.Sequence;
import de.opitzconsulting.origOrcasDsl.Table;
import de.opitzconsulting.origOrcasDsl.UniqueKey;
import de.opitzconsulting.origOrcasDsl.impl.ColumnIdentityImpl;
import de.opitzconsulting.origOrcasDsl.impl.ColumnImpl;
import de.opitzconsulting.origOrcasDsl.impl.ColumnRefImpl;
import de.opitzconsulting.origOrcasDsl.impl.ConstraintImpl;
import de.opitzconsulting.origOrcasDsl.impl.ForeignKeyImpl;
import de.opitzconsulting.origOrcasDsl.impl.IndexImpl;
import de.opitzconsulting.origOrcasDsl.impl.InlineCommentImpl;
import de.opitzconsulting.origOrcasDsl.impl.LobStorageImpl;
import de.opitzconsulting.origOrcasDsl.impl.ModelImpl;
import de.opitzconsulting.origOrcasDsl.impl.PrimaryKeyImpl;
import de.opitzconsulting.origOrcasDsl.impl.SequenceImpl;
import de.opitzconsulting.origOrcasDsl.impl.TableImpl;
import de.opitzconsulting.origOrcasDsl.impl.UniqueKeyImpl;

public class LoadIstPostgres extends LoadIst {
    private Map<String, List<String>> includeMap = new HashMap<String, List<String>>();

    private Map<String, Object> constraintMapForFK = new HashMap<String, Object>();
    private Map<String, Table> constraintTableMapForFK = new HashMap<String, Table>();

    private Parameters _parameters;

    private CallableStatementProvider _callableStatementProvider;

    public LoadIstPostgres(CallableStatementProvider pCallableStatementProvider, Parameters pParameters) {
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

        loadTablesIntoModel(pModel);
        loadTableColumnsIntoModel(pModel);

        loadSequencesIntoModel(pModel, true);

        /*
         * loadLobstorageIntoModel( pModel );
         */
        loadIndexesIntoModel(pModel);
        loadIndexColumnsIntoModel(pModel);
        /*
         * loadIndexExpressionsIntoModel( pModel );
         */
        loadTableConstraintsIntoModel(pModel);
        loadTableConstraintColumnsIntoModel(pModel);
        loadTableForeignKeyConstraintColumnsIntoModel(pModel);
        loadTableCommentsIntoModel(pModel);
        loadTableColumnCommentsIntoModel(pModel);
        /*
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

            String lSql = "select tablename, tableowner, 0 is_exclude from " + getDataDictionaryView("tables");

            new WrapperIteratorResultSet(lSql, getCallableStatementProvider()) {
                @Override
                protected void useResultSetRow(ResultSet pResultSet) throws SQLException {
                    if (pResultSet.getInt("is_exclude") == 0) {
                        includeMap.get(pType).add(getNameWithOwner(pResultSet.getString("tablename"), pResultSet.getString("tableowner")));
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

        return isIgnored(pString, pOwner, _parameters.getExcludewheretable(), "TABLE");
    }

    private boolean isIgnoredSequence(String pString, String pOwner, Model pModel) {
        if (pString.toLowerCase().startsWith(OrcasScriptRunner.ORCAS_UPDATES_TABLE.toLowerCase())) {
            return true;
        }

        return pModel.getModel_elements()
                .stream()
                .filter(it->it instanceof Table)
                .anyMatch(it->((Table) it).getColumns()
                        .stream()
                        .filter(col-> pString.equalsIgnoreCase(((Table) it).getName() + "_" + col.getName() + "_seq"))
                        .anyMatch(col->col.getIdentity()!=null));
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

        lSql = "select a.attnum             as ordinal_position,\n"
            + "       a.attname            as column_name,\n"
            + "       t.typname            as data_type,\n"
            + "       a.attlen             as attlen,\n"
            + "       a.attnotnull = false as is_nullable,\n"
            + "       c.relname            as table_name,\n"
            + "       case\n"
            + "           when atttypmod = -1\n"
            + "               then null\n"
            + "           else ((atttypmod - 4) >> 16) & 65535\n"
            + "           end              as precision,\n"
            + "       case\n"
            + "           when atttypmod = -1 then null\n"
            + "           else (atttypmod - 4) & 65535\n"
            + "           end              as scale,\n"
            + "       pg_get_serial_sequence(a.attrelid::regclass::text, a.attname) is not null as is_generated,\n"
            + "       (select adsrc\n"
            + "          from pg_attrdef ad\n"
            + "         where ad.adrelid = c.oid\n"
            + "           and ad.adnum = a.attnum\n"
            + "       ) as column_default"
            + "  from " + getDataDictionaryView("pg_class", "c") + ",\n"
            + "       pg_attribute a,\n"
            + "       pg_type t\n"
            + " where c.relkind = 'r'\n"
            + "   and a.attnum > 0\n"
            + "   and a.attrelid = c.oid\n"
            + "   and a.atttypid = t.oid\n";

        if (_parameters.isOrderColumnsByName()) {
            lSql += " order by c.relname, a.attname, a.attnum";
        } else {
            lSql += " order by c.relname, a.attnum, a.attname";
        }

        new WrapperIteratorResultSet(lSql, getCallableStatementProvider()) {
            @Override
            protected void useResultSetRow(ResultSet pResultSet) throws SQLException {
                if (!isIgnoredTable(pResultSet.getString("table_name"), null)) {
                    Column lColumn = new ColumnImpl();

                    lColumn.setName(pResultSet.getString("column_name"));

                    lColumn.setNotnull(!pResultSet.getBoolean("is_nullable"));

                    if (pResultSet.getString("data_type").startsWith("numeric")) {
                        lColumn.setData_type(DataType.NUMBER);
                        lColumn.setPrecision(pResultSet.getInt("precision"));
                        lColumn.setScale(pResultSet.getInt("scale"));
                    }
                    if (pResultSet.getString("data_type").startsWith("int2")) {
                        lColumn.setData_type(DataType.SMALLINT);
                    }
                    if (pResultSet.getString("data_type").startsWith("int4")) {
                        lColumn.setData_type(DataType.INT);
                    }
                    if (pResultSet.getString("data_type").startsWith("int8")) {
                        lColumn.setData_type(DataType.BIGINT);
                    }
                    if (pResultSet.getString("data_type").startsWith("bit")) {
                        lColumn.setData_type(DataType.BIT);
                    }
                    if (pResultSet.getString("data_type").startsWith("blob")) {
                        lColumn.setData_type(DataType.BLOB);
                    }
                    if (pResultSet.getString("data_type").equals("bytea")) {
                        lColumn.setData_type(DataType.BLOB);
                    }
                    if (pResultSet.getString("data_type").startsWith("clob")) {
                        lColumn.setData_type(DataType.CLOB);
                    }
                    if (pResultSet.getString("data_type").equals("text")) {
                        lColumn.setData_type(DataType.CLOB);
                    }
                    if (pResultSet.getString("data_type").startsWith("varchar")) {
                        lColumn.setData_type(DataType.VARCHAR2);
                        lColumn.setPrecision(pResultSet.getInt("scale"));
                    }
                    if (pResultSet.getString("data_type").startsWith("char")) {
                        lColumn.setData_type(DataType.CHAR);
                        lColumn.setPrecision(pResultSet.getInt("scale"));
                    }
                    if (pResultSet.getString("data_type").startsWith("datetime")) {
                        lColumn.setData_type(DataType.DATE);
                    }
                    if (pResultSet.getString("data_type").equals("date")) {
                        lColumn.setData_type(DataType.DATE);
                    }
                    if (pResultSet.getString("data_type").startsWith("timestamp")) {
                        lColumn.setData_type(DataType.TIMESTAMP);
                    }
                    if (pResultSet.getString("data_type").startsWith("bool")) {
                        lColumn.setData_type(DataType.BOOLEAN);
                    }

                    if (pResultSet.getBoolean("is_generated")) {
                        ColumnIdentity lColumnIdentity = new ColumnIdentityImpl();

                        lColumnIdentity.setBy_default("default");
                        lColumnIdentity.setOn_null("null");

                        lColumn.setIdentity(lColumnIdentity);
                    } else {
                        if (null != pResultSet.getString("column_default")) {
                            lColumn.setDefault_value(pResultSet.getString("column_default"));
                        }
                    }

                    if (lColumn.getData_type() == null) {
                        throw new IllegalStateException("datatype unknown: " + pResultSet.getString("data_type") + " of " + pResultSet.getString(
                            "table_name") + "." + lColumn.getName());
                    }

                    findTable(pModel, pResultSet.getString("table_name"), null).getColumns().add(lColumn);
                }
            }
        }.execute();

    }

    private String getDataDictionaryView(String pName) {
        return getDataDictionaryView(pName, pName);
    }

    private String getDataDictionaryView(String pName, String pAliasName) {
        String lViewName = pName.startsWith("pg_") ? pName : "pg_" + pName;

        if (lViewName.equals("pg_class")) {
            return "(select "
                + lViewName
                + ".oid, "
                + lViewName
                + ".*, user as owner from "
                + lViewName
                + " where relnamespace in (select oid from pg_namespace where nspname = 'public' or nspname = user) and relowner = (select usesysid from pg_user where usename = user) ) "
                + pAliasName;
        }

        return "(select "
            + lViewName
            + ".*, user as owner from "
            + lViewName
            + " where (schemaname = 'public' or schemaname = user) and tableowner = user ) "
            + pName;
    }

    private void loadIndexesIntoModel(final Model pModel) {
        String lSql = "select ct.relname            as table_name,\n"
            + "       ci.relname            as index_name,\n"
            + "       indisunique\n"
            + "  from " + getDataDictionaryView("pg_class", "ct") + ",\n"
            + "       pg_class ci,\n"
            + "       pg_index i\n"
            + " where i.indrelid = ct.oid\n"
            + "   and i.indexrelid = ci.oid\n"
            + "   and (indisunique = false or ci.relname not in (select conname from pg_constraint co where co.conrelid = ct.oid))\n"
            + " order by ci.relname";

        new WrapperIteratorResultSet(lSql, getCallableStatementProvider()) {
            @Override
            protected void useResultSetRow(ResultSet pResultSet) throws SQLException {
                if (!isIgnoredTable(pResultSet.getString("table_name"), null)) {
                    final Index lIndex = new IndexImpl();

                    lIndex.setConsName(getNameWithOwner(pResultSet.getString("index_name"), null));
                    if (pResultSet.getBoolean("indisunique")) {
                        lIndex.setUnique("unique");
                    }

                    findTable(pModel, pResultSet.getString("table_name"), null).getInd_uks().add(lIndex);
                }
            }
        }.execute();
    }

    private void loadIndexColumnsIntoModel(final Model pModel) {
        String lSql = "select a.attname            as column_name,\n"
            + "       ct.relname            as table_name,\n"
            + "       ci.relname            as index_name\n"
            + "  from " + getDataDictionaryView("pg_class", "ct") + ",\n"
            + "       pg_class ci,\n"
            + "       pg_attribute a,\n"
            + "       pg_type t,\n"
            + "       pg_index i\n"
            + " where i.indrelid = ct.oid\n"
            + "   and i.indexrelid = ci.oid\n"
            + "   and a.attnum > 0\n"
            + "   and a.attrelid = ci.oid\n"
            + "   and a.atttypid = t.oid\n"
            + "   and (indisunique = false or ci.relname not in (select conname from pg_constraint co where co.conrelid = ct.oid))\n"
            + " order by ci.relname, a.attnum";

        new WrapperIteratorResultSet(lSql, getCallableStatementProvider()) {
            @Override
            protected void useResultSetRow(ResultSet pResultSet) throws SQLException {
                if (!isIgnoredTable(pResultSet.getString("table_name"), null)) {
                    ColumnRef lColumnRef = new ColumnRefImpl();

                    lColumnRef.setColumn_name(pResultSet.getString("column_name"));

                    findIndex(
                        pModel,
                        pResultSet.getString("table_name"),
                        null,
                        pResultSet.getString("index_name"),
                        null).getIndex_columns().add(lColumnRef);
                }
            }
        }.execute();
    }

    private void setIndexColumnExpression(
        Model pModel,
        String pTablename,
        String pTableOwner,
        String pIndexName,
        String pIndexOwner,
        int pColumnPosition,
        String pExpression,
        int pMaxColumnPositionForInd) {
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
                    setIndexColumnExpression(
                        pModel,
                        pResultSet.getString("table_name"),
                        pResultSet.getString("table_owner"),
                        pResultSet.getString("index_name"),
                        pResultSet.getString("owner"),
                        pResultSet.getInt("column_position"),
                        pResultSet.getString("column_expression"),
                        pResultSet.getInt("max_column_position_for_index"));
                }
            }
        }.execute();
    }

    private void loadSequencesIntoModel(final Model pModel, final boolean pWithSequeneceMayValueSelect) {
        String lSql = "" + //
            " select relname" + //
            "   from " + getDataDictionaryView("pg_class") +
            "  where relkind = 'S'" + //
            "  order by relname " + //
            "";

        new WrapperIteratorResultSet(lSql, getCallableStatementProvider()) {
            @Override
            protected void useResultSetRow(ResultSet pResultSet) throws SQLException {
                if (!isIgnoredSequence(pResultSet.getString("relname"), null, pModel)) {
                    Sequence lSequence = new SequenceImpl();

                    lSequence.setSequence_name(getNameWithOwner(pResultSet.getString("relname"), null));

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

    private void loadTableConstraintsIntoModel(final Model pModel) {
        String lSql = "select conname as constraint_name,\n"
            + "       c.relname as table_name,\n"
            + "       contype as constraint_type,\n"
            + "       confdeltype as delete_rule,\n"
            + "       condeferred,\n"
            + "       consrc\n"
            + "  from pg_constraint co,\n"
            + "       " + getDataDictionaryView("pg_class", "c") + "\n"
            + " where co.conrelid = c.oid\n"
            + "   and c.relkind = 'r'\n"
            + " order by c.relname, conname";

        new WrapperIteratorResultSet(lSql, getCallableStatementProvider()) {
            @Override
            protected void useResultSetRow(ResultSet pResultSet) throws SQLException {
                if (!isIgnoredTable(pResultSet.getString("table_name"), null)) {
                    Table lTable = findTable(pModel, pResultSet.getString("table_name"), null);

                    if ("p".equals(pResultSet.getString("constraint_type"))) {
                        PrimaryKey lPrimaryKey = new PrimaryKeyImpl();

                        lPrimaryKey.setConsName(pResultSet.getString("constraint_name"));

                        registerConstarintForFK(pResultSet.getString("constraint_name"), lTable, null, lPrimaryKey);

                        lTable.setPrimary_key(lPrimaryKey);
                    }

                    if ("u".equals(pResultSet.getString("constraint_type"))) {
                        UniqueKey lUniqueKey = new UniqueKeyImpl();

                        lUniqueKey.setConsName(pResultSet.getString("constraint_name"));

                        registerConstarintForFK(pResultSet.getString("constraint_name"), lTable, null, lUniqueKey);

                        lTable.getInd_uks().add(lUniqueKey);
                    }

                    if ("f".equals(pResultSet.getString("constraint_type"))) {
                        ForeignKey lForeignKey = new ForeignKeyImpl();

                        lForeignKey.setConsName(pResultSet.getString("constraint_name"));

                        if ("r".equals(pResultSet.getString("delete_rule"))) {
                            lForeignKey.setDelete_rule(FkDeleteRuleType.NO_ACTION);
                        }
                        if ("a".equals(pResultSet.getString("delete_rule"))) {
                            lForeignKey.setDelete_rule(FkDeleteRuleType.NO_ACTION);
                        }
                        if ("n".equals(pResultSet.getString("delete_rule"))) {
                            lForeignKey.setDelete_rule(FkDeleteRuleType.SET_NULL);
                        }
                        if ("c".equals(pResultSet.getString("delete_rule"))) {
                            lForeignKey.setDelete_rule(FkDeleteRuleType.CASCADE);
                        }

                        if (pResultSet.getBoolean("condeferred")) {
                            lForeignKey.setDeferrtype(DeferrType.DEFERRED);
                        }

                        lTable.getForeign_keys().add(lForeignKey);
                    }

                    if ("c".equals(pResultSet.getString("constraint_type"))) {
                        Constraint lConstraint = new ConstraintImpl();

                        lConstraint.setConsName(pResultSet.getString("constraint_name"));

                        String lConsrc = pResultSet.getString("consrc");
                        lConstraint.setRule(lConsrc.substring(1, lConsrc.length() - 1));

                        if (pResultSet.getBoolean("condeferred")) {
                            lConstraint.setDeferrtype(DeferrType.DEFERRED);
                        }

                        lTable.getConstraints().add(lConstraint);
                    }
                }
            }
        }.execute();
    }

    private void loadTableConstraintColumnsIntoModel(final Model pModel) {
        String lSql = "select a.attname  as column_name,\n"
            + "       ct.relname as table_name,\n"
            + "       c.conname as constraint_name,\n"
            + "       contype as constraint_type\n"
            + "  from " + getDataDictionaryView("pg_class", "ct") + ",\n"
            + "       pg_attribute a,\n"
            + "       pg_constraint c\n"
            + " where c.conrelid = ct.oid\n"
            + "   and a.attnum > 0\n"
            + "   and a.attnum=ANY(c.conkey)\n"
            + "   and a.attrelid = ct.oid\n"
            + "   and contype != 'c'\n"
            + "   and contype != 'f'\n"
            + " order by ct.relname, a.attnum";

        new WrapperIteratorResultSet(lSql, getCallableStatementProvider()) {
            @Override
            protected void useResultSetRow(ResultSet pResultSet) throws SQLException {
                if (!isIgnoredTable(pResultSet.getString("table_name"), null)) {
                    ColumnRef lColumnRef = new ColumnRefImpl();

                    lColumnRef.setColumn_name(pResultSet.getString("column_name"));

                    if ("p".equals(pResultSet.getString("constraint_type"))) {
                        findTable(pModel, pResultSet.getString("table_name"), null)
                            .getPrimary_key()
                            .getPk_columns()
                            .add(lColumnRef);
                    }

                    if ("u".equals(pResultSet.getString("constraint_type"))) {
                        findUniqueKey(
                            pModel,
                            pResultSet.getString("table_name"),
                            null,
                            pResultSet.getString("constraint_name")).getUk_columns().add(lColumnRef);
                    }
                }
            }
        }.execute();
    }

    private void loadTableForeignKeyConstraintColumnsIntoModel(final Model pModel) {
        String lSql = "select a.attname  as column_name,\n"
            + "       ar.attname  as referenced_column_name,\n"
            + "       ct.relname as table_name,\n"
            + "       ctr.relname as referenced_table_name,\n"
            + "       c.conname as constraint_name\n"
            + "  from " + getDataDictionaryView("pg_class", "ct") + ",\n"
            + "       pg_class ctr,\n"
            + "       pg_attribute a,\n"
            + "       pg_attribute ar,\n"
            + "       pg_constraint c\n"
            + " where c.conrelid = ct.oid\n"
            + "   and c.confrelid = ctr.oid\n"
            + "   and a.attnum > 0\n"
            + "   and a.attrelid = ct.oid\n"
            + "   and a.attnum=ANY(c.conkey)\n"
            + "   and ar.attnum > 0\n"
            + "   and ar.attrelid = ctr.oid\n"
            + "   and ar.attnum=ANY(c.confkey)\n"
            + "   and contype = 'f'\n"
            + "   and array_position(c.conkey,a.attnum) = array_position(c.confkey,ar.attnum)\n"
            + " order by ct.relname, array_position(c.conkey,a.attnum)";

        new WrapperIteratorResultSet(lSql, getCallableStatementProvider()) {
            @Override
            protected void useResultSetRow(ResultSet pResultSet) throws SQLException {
                if (!isIgnoredTable(pResultSet.getString("table_name"), null)) {
                    ColumnRef lColumnRef = new ColumnRefImpl();

                    lColumnRef.setColumn_name(pResultSet.getString("column_name"));

                    ForeignKey
                        lForeignKey =
                        findForeignKey(
                            pModel,
                            pResultSet.getString("table_name"),
                            null,
                            pResultSet.getString("constraint_name"));
                    lForeignKey.getSrcColumns().add(lColumnRef);
                    lForeignKey.setDestTable(pResultSet.getString("referenced_table_name"));

                    ColumnRef lDestColumnRef = new ColumnRefImpl();
                    lDestColumnRef.setColumn_name(pResultSet.getString("referenced_column_name"));
                    lForeignKey.getDestColumns().add(lDestColumnRef);
                }
            }
        }.execute();
    }

    private void loadTableCommentsIntoModel(final Model pModel) {
        String lSql = "" + //
            " select c.relname table_name," + //
            "        c.owner," + //
            "        pg_catalog.obj_description(c.oid) as comments" + //
            "   from " + getDataDictionaryView("pg_class", "c") + //
            "  where c.relkind = 'r'" +//
            "";

        new WrapperIteratorResultSet(lSql, getCallableStatementProvider()) {
            @Override
            protected void useResultSetRow(ResultSet pResultSet) throws SQLException {
                if (!isIgnoredTable(pResultSet.getString("table_name"), pResultSet.getString("owner"))) {
                    InlineComment lInlineComment = new InlineCommentImpl();

                    String lComments = pResultSet.getString("comments");

                    if (lComments != null && lComments.trim().length() > 0) {
                        lInlineComment.setComment(lComments);

                        lInlineComment.setComment_object(CommentObjectType.TABLE);

                        findTable(pModel, pResultSet.getString("table_name"), pResultSet.getString("owner")).getComments().add(lInlineComment);
                    }
                }
            }
        }.execute();
    }

    private void loadTableColumnCommentsIntoModel(final Model pModel) {
        String lSql = "" + //
            " select c.relname as table_name," + //
            "        a.attname  as column_name," + //
            "        c.owner," + //
            "        pg_catalog.col_description(c.oid,a.attnum) as comments" + //
            "   from " + getDataDictionaryView("pg_class", "c") + "," + //
            "        pg_attribute a" + //
            "  where c.relkind = 'r'" + //
            "    and a.attnum > 0" + //
            "    and a.attrelid = c.oid";

        new WrapperIteratorResultSet(lSql, getCallableStatementProvider()) {
            @Override
            protected void useResultSetRow(ResultSet pResultSet) throws SQLException {
                if (!isIgnoredTable(pResultSet.getString("table_name"), pResultSet.getString("owner"))) {
                    InlineComment lInlineComment = new InlineCommentImpl();

                    String lComments = pResultSet.getString("comments");

                    if (lComments != null && lComments.trim().length() > 0) {
                        lInlineComment.setComment(lComments);

                        lInlineComment.setColumn_name(pResultSet.getString("column_name"));

                        lInlineComment.setComment_object(CommentObjectType.COLUMN);

                        findTable(pModel, pResultSet.getString("table_name"), pResultSet.getString("owner")).getComments().add(lInlineComment);
                    }
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
            " select tables.tablename," + //
            "        tables.tableowner" + //
            "   from " + getDataDictionaryView("tables") + //
            "  order by tablename" + //
            "";

        new WrapperIteratorResultSet(lSql, getCallableStatementProvider()) {
            @Override
            protected void useResultSetRow(ResultSet pResultSet) throws SQLException {
                if (!isIgnoredTable(pResultSet.getString("tablename"), pResultSet.getString("tableowner"))) {
                    final Table lTable = new TableImpl();

                    lTable.setName(getNameWithOwner(pResultSet.getString("tablename"), pResultSet.getString("tableowner")));

                    pModel.getModel_elements().add(lTable);
                }
            }
        }.execute();
    }

    private CallableStatementProvider getCallableStatementProvider() {
        return _callableStatementProvider;
    }
}
