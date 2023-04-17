package de.opitzconsulting.orcas.diff;

import de.opitzconsulting.orcas.diff.OrcasDiff.DataHandler;
import de.opitzconsulting.orcas.orig.diff.*;
import de.opitzconsulting.origOrcasDsl.DataType;

import java.util.List;
import java.util.stream.Collectors;

import static de.opitzconsulting.origOrcasDsl.OrigOrcasDslPackage.Literals.*;

public class DdlBuilderAzureSql extends DdlBuilder {
    public DdlBuilderAzureSql(Parameters pParameters, DatabaseHandler pDatabaseHandler) {
        super(pParameters, pDatabaseHandler);
    }

    @Override
    protected String getDatatypeName(DataType pData_typeNew) {
        if (pData_typeNew == DataType.VARCHAR2 || pData_typeNew == DataType.NVARCHAR2) {
            return "VARCHAR";
        }
        if (pData_typeNew == DataType.NUMBER) {
            return "NUMERIC";
        }
        if (pData_typeNew == DataType.XMLTYPE) {
            return "XML";
        }
        if (pData_typeNew == DataType.RAW) {
            return "VARBINARY";
        }

        return super.getDatatypeName(pData_typeNew);
    }

    @Override
    public void alterSequenceIfNeeded(StatementBuilderAlter pP1, SequenceDiff pSequenceDiff, DataHandler pDataHandler) {
    }

    @Override
    public void setComment(StatementBuilder p, TableDiff pTableDiff, InlineCommentDiff pInlineCommentDiff) {
        if (pInlineCommentDiff.column_nameNew == null) {
            p.stmtStart("alter table");
            p.stmtAppend(pTableDiff.nameNew);

            p.stmtAppend("comment");
            p.stmtAppend("'" + pInlineCommentDiff.commentNew.replace("'", "''") + "'");
            p.stmtDone();
        }
    }

    @Override
    public void dropComment(StatementBuilder p, TableDiff pTableDiff, InlineCommentDiff pCommentDiff) {
        p.stmtStart("alter table");
        p.stmtAppend(pTableDiff.nameOld);

        p.stmtAppend("comment");
        p.stmtAppend("''");
        p.stmtDone();
    }

    @Override
    public void recreateColumn(StatementBuilder pP, TableDiff pTableDiff, ColumnDiff pColumnDiff) {
        pP.failIfAdditionsOnly("can't recreate columns");

        if ("virtual".equals(pColumnDiff.virtualNew) || "virtual".equals(pColumnDiff.virtualOld)) {
            pP.addStmt("alter table " + pTableDiff.nameNew + " drop column " + pColumnDiff.nameOld);
            pP.addStmt("alter table " + pTableDiff.nameNew + " add " + createColumnCreatePart(pColumnDiff, false));
        } else {
            pP.addStmt("alter table " + pTableDiff.nameNew + " alter column " + createColumnCreatePart(pColumnDiff, false));
        }
    }

    @Override
    protected String getColumnDatatype(ColumnDiff pColumnDiff) {
        if (pColumnDiff.data_typeNew == DataType.CLOB || pColumnDiff.data_typeNew == DataType.NCLOB) {
            return "varchar(max)";
        }
        if (pColumnDiff.data_typeNew == DataType.BLOB) {
            return "varbinary(max)";
        }
        if (pColumnDiff.data_typeNew == DataType.TIMESTAMP) {
            String type;
            if ("with_time_zone".equals(pColumnDiff.with_time_zoneNew)) {
                type = "datetimeoffset";
            } else {
                type = "datetime2";
            }

            if (pColumnDiff.precisionNew != null) {
                return type + "(" + pColumnDiff.precisionNew + ")";
            } else {
                return type;
            }
        }
        return super.getColumnDatatype(pColumnDiff);
    }

    @Override
    public void dropPrimaryKey(StatementBuilder p, TableDiff pTableDiff, PrimaryKeyDiff pPrimaryKeyDiff) {
        if (pPrimaryKeyDiff.consNameOld == null) {
            p.stmtStart("begin declare @CONS_NAME sysname = (select indexes.name from sys.indexes, sys.tables where tables.object_id = indexes.object_id and tables.name = '" + pTableDiff.nameOld + "' and OBJECT_SCHEMA_NAME(tables.object_id) = schema_name() and is_primary_key = 1 ) exec('alter table " + pTableDiff.nameOld + " drop constraint '+ @CONS_NAME) end");
            p.stmtDone(false);
        } else {
            dropTableConstraintByName(p, pTableDiff, pPrimaryKeyDiff.consNameOld, false);
        }
    }

    @Override
    protected String createColumnCreatePart(ColumnDiff pColumnDiff, boolean pWithoutNotNull) {
        boolean isVirtual = "virtual".equals(pColumnDiff.virtualNew);

        String lReturn = pColumnDiff.nameNew + (isVirtual ? "" : (" " + getColumnDatatype(pColumnDiff)));

        if (pColumnDiff.default_valueNew != null) {
            if (isVirtual) {
                lReturn = lReturn + " as (" + pColumnDiff.default_valueNew + ")";
            } else {
                if (pColumnDiff.default_nameNew != null) {
                    lReturn = lReturn + " constraint " + pColumnDiff.default_nameNew;
                }
                lReturn = lReturn + " default " + pColumnDiff.default_valueNew;
            }
        }

        if (pColumnDiff.notnullNew) {
            if (!pWithoutNotNull) {
                lReturn = lReturn + " not null";
            }
        }

        if (pColumnDiff.identityDiff.isNew) {
            lReturn = lReturn + " AUTO_INCREMENT";
        }

        return lReturn;
    }

    @Override
    protected String createPkCreateWithTableCreate(PrimaryKeyDiff pPrimary_keyDiff) {
        if (pPrimary_keyDiff.isNew) {
            if (pPrimary_keyDiff.consNameNew == null) {
                return ", primary key (" + getColumnList(pPrimary_keyDiff.pk_columnsDiff) + ")";
            } else {
                return ", constraint " + pPrimary_keyDiff.consNameNew + " primary key (" + getColumnList(pPrimary_keyDiff.pk_columnsDiff) + ")";
            }
        } else {
            return "";
        }
    }

    @Override
    public void createPrimarykey(StatementBuilder pP, TableDiff pTableDiff) {
        if (pTableDiff.isOld) {
            super.createPrimarykey(pP, pTableDiff);
        }
    }

    @Override
    public void createUniqueKey(StatementBuilder p, TableDiff pTableDiff, UniqueKeyDiff pUniqueKeyDiff) {
        boolean lHasTablespace = pUniqueKeyDiff.tablespaceNew != null;
        boolean lHasIndex = pUniqueKeyDiff.indexnameNew != null && !pUniqueKeyDiff.indexnameNew.equals(pUniqueKeyDiff.consNameNew);

        if (lHasTablespace || lHasIndex) {
            p.stmtStartAlterTableNoCombine(pTableDiff);
        } else {
            p.stmtStartAlterTable(pTableDiff);
        }

        p.stmtAppend("add constraint " + pUniqueKeyDiff.consNameNew + " unique (" + getColumnList(pUniqueKeyDiff.uk_columnsDiff) + ")");
        if (lHasTablespace) {
            p.stmtAppend("using index tablespace " + pUniqueKeyDiff.tablespaceNew);
        }
        if (pUniqueKeyDiff.statusNew != null) {
            p.stmtAppend(pUniqueKeyDiff.statusNew.getName());
        }

        p.stmtDone(pTableDiff.isOld && !isAllColumnsOnlyNew(pTableDiff, pUniqueKeyDiff.uk_columnsDiff));
    }

    @Override
    public void createIndex(StatementBuilder pP, TableDiff pTableDiff, IndexDiff pIndexDiff, boolean pIsIndexParallelCreate) {
        super.createIndex(pP, pTableDiff, pIndexDiff, false);
    }

    @Override
    public void dropIndex(StatementBuilder pP, TableDiff pTableDiff, IndexDiff pIndexDiff) {
        pP.addStmt("drop index " + pIndexDiff.consNameOld + " on " + pTableDiff.nameOld, !pTableDiff.isNew || pIndexDiff.uniqueOld == null || isAllColumnsOnlyOld(pTableDiff, pIndexDiff.index_columnsDiff));
    }

    @Override
    protected boolean isNumericDatatypeUnsignedSupported() {
        return true;
    }

    @Override
    public Runnable getColumnDropHandler(StatementBuilder pP, TableDiff pTableDiff, List<ColumnDiff> pColumnDiffList) {
        Runnable lAdditionsOnlyAlternativeHandler = () ->
        {
            pColumnDiffList.stream()//
                    .filter(pColumnDiff -> pColumnDiff.notnullOld && pColumnDiff.default_valueOld == null)//
                    .forEach(pColumnDiff ->
                    {
                        pP.stmtStartAlterTable(pTableDiff);
                        pP.stmtAppend("modify ( " + pColumnDiff.nameOld);
                        pP.stmtAppend("null");
                        pP.stmtAppend(")");
                        pP.stmtDone(StatementBuilder.ADDITIONSONLY_ALTERNATIVE_COMMENT);
                    });
        };

        return () ->
        {
            pP.stmtStartAlterTableNoCombine(pTableDiff);

            pP.stmtAppend(pColumnDiffList.stream().map(pColumnDiff -> " drop column " + pColumnDiff.nameOld).collect(Collectors.joining(",")));
            pP.stmtDone(lAdditionsOnlyAlternativeHandler);
        };
    }

    @Override
    public void alterColumnIfNeeded(StatementBuilderAlter p1, TableDiff pTableDiff, ColumnDiff pColumnDiff) {
        p1.handleAlterBuilder()//
                .ifDifferent(COLUMN__BYTEORCHAR)//
                .ifDifferent(COLUMN__PRECISION)//
                .ifDifferent(COLUMN__SCALE)//
                .handle(p ->
                {
                    p.stmtStartAlterTable(pTableDiff.nameNew);
                    p.stmtAppend("alter column");
                    p.stmtAppend(pColumnDiff.nameNew);
                    p.stmtAppend(getColumnDatatype(pColumnDiff));
                    p.stmtDone();
                });

        p1.handleAlterBuilder()//
                .ifDifferent(COLUMN__VIRTUAL)
                .failIfAdditionsOnly("virtual".equals(pColumnDiff.virtualNew), "can't make existing column virtual")
                .failIfAdditionsOnly(!"virtual".equals(pColumnDiff.virtualNew), "can't materialize virtual column")
                .handle(p ->
                {

                });

        p1.handleAlterBuilder()//
                .ifDifferent(
                        COLUMN__DEFAULT_VALUE,
                        getDatabaseHandler().isExpressionDifferent(pColumnDiff.default_valueNew, pColumnDiff.default_valueOld))//
                .ignoreIfAdditionsOnly(pColumnDiff.default_valueNew == null)//
                .failIfAdditionsOnly(pColumnDiff.default_valueOld != null, "can't change default")//
                .handle(p ->
                {
                    if (pColumnDiff.default_nameOld != null) {
                        p.stmtStartAlterTable(pTableDiff);
                        p.stmtAppend("drop constraint");
                        p.stmtAppend(pColumnDiff.default_nameOld);

                        p.stmtDone();
                    }

                    p.stmtStartAlterTable(pTableDiff);
                    p.stmtAppend("add constraint");
                    if (pColumnDiff.default_nameNew != null) {
                        p.stmtAppend(pColumnDiff.default_nameNew);
                    } else {
                        p.stmtAppend(pColumnDiff.nameNew);
                    }
                    p.stmtAppend("default");
                    p.stmtAppend(pColumnDiff.default_valueNew);
                    p.stmtAppend("for");
                    p.stmtAppend(pColumnDiff.nameNew);

                    p.stmtDone();
                });

        p1.handleAlterBuilder()//
                .ifDifferent(COLUMN__NOTNULL)//
                .ignoreIfAdditionsOnly(pColumnDiff.notnullNew)//
                .handle(p ->
                {
                    p.stmtStartAlterTable(pTableDiff);
                    p.stmtAppend("alter column");
                    p.stmtAppend(pColumnDiff.nameNew);
                    p.stmtAppend(getColumnDatatype(pColumnDiff));
                    if (pColumnDiff.notnullNew == false) {
                        p.stmtAppend("null");
                    } else {
                        p.stmtAppend("not null");
                    }
                    p.stmtDone();
                });
    }
}
