package de.opitzconsulting.orcas.diff;

import static de.opitzconsulting.origOrcasDsl.OrigOrcasDslPackage.Literals.*;

import java.util.List;
import java.util.stream.Collectors;

import de.opitzconsulting.orcas.orig.diff.ColumnDiff;
import de.opitzconsulting.orcas.orig.diff.IndexDiff;
import de.opitzconsulting.orcas.orig.diff.TableDiff;
import de.opitzconsulting.origOrcasDsl.CompressType;
import de.opitzconsulting.origOrcasDsl.DataType;
import de.opitzconsulting.origOrcasDsl.ParallelType;

public class DdlBuilderPostgres extends DdlBuilder {
    public DdlBuilderPostgres(Parameters pParameters, DatabaseHandler pDatabaseHandler) {
        super(pParameters, pDatabaseHandler);
    }

    @Override
    protected String getDatatypeName(DataType pData_typeNew) {
        if (pData_typeNew == DataType.VARCHAR2) {
            return "VARCHAR";
        }
        if (pData_typeNew == DataType.NUMBER) {
            return "NUMERIC";
        }

        return super.getDatatypeName(pData_typeNew);
    }

    @Override
    protected String getColumnDatatype(ColumnDiff pColumnDiff) {
        if (pColumnDiff.identityDiff.isNew) {
            if (pColumnDiff.data_typeNew == DataType.SMALLINT) {
                return "smallserial";
            }
            if (pColumnDiff.data_typeNew == DataType.INT) {
                return "serial";
            }
            if (pColumnDiff.data_typeNew == DataType.BIGINT) {
                return "bigserial";
            }

            throw new IllegalStateException("identity column cannot be used with datataype: " + pColumnDiff.data_typeNew);
        }
        return super.getColumnDatatype(pColumnDiff);
    }

    @Override
    protected String createColumnCreatePart(ColumnDiff pColumnDiff, boolean pWithoutNotNull) {
        String lReturn = pColumnDiff.nameNew + " " + getColumnDatatype(pColumnDiff);

        if (pColumnDiff.default_valueNew != null) {
            lReturn = lReturn + " default " + pColumnDiff.default_valueNew;
        }

        if (pColumnDiff.notnullNew) {
            if (!pWithoutNotNull) {
                lReturn = lReturn + " not null";
            }
        }

        return lReturn;
    }

    @Override
    public void recreateColumn(StatementBuilder p, TableDiff pTableDiff, ColumnDiff pColumnDiff) {
        p.failIfAdditionsOnly("can't recreate columns");

        String lTmpOldColumnameNew = "DTO_" + pColumnDiff.nameNew;
        String lTmpNewColumnameNew = "DTN_" + pColumnDiff.nameNew;

        p.stmtStartAlterTableNoCombine(pTableDiff);
        p.stmtAppend("add " + lTmpNewColumnameNew + " " + getColumnDatatype(pColumnDiff));
        if ("virtual".equals(pColumnDiff.virtualNew)) {
            p.stmtAppend("as (" + pColumnDiff.default_valueNew + ") virtual");
        }
        p.stmtDone();

        if (!"virtual".equals(pColumnDiff.virtualNew)) {
            p.addStmt("update " + pTableDiff.nameNew + " set " + lTmpNewColumnameNew + " = " + pColumnDiff.nameOld);
            p.addStmt("commit");
        }

        p.addStmt("alter table " + pTableDiff.nameNew + " rename column " + pColumnDiff.nameOld + " to " + lTmpOldColumnameNew);
        p.addStmt("alter table " + pTableDiff.nameNew + " rename column " + lTmpNewColumnameNew + " to " + pColumnDiff.nameNew);
        p.addStmt("alter table " + pTableDiff.nameNew + " drop column " + lTmpOldColumnameNew);

        if (pColumnDiff.default_valueNew != null && !"virtual".equals(pColumnDiff.virtualNew)) {
            p.stmtStart("alter table " + pTableDiff.nameNew + " modify ( " + pColumnDiff.nameNew + " default");
            p.stmtAppend(pColumnDiff.default_valueNew);
            p.stmtAppend(")");
            p.stmtDone();
        }

        if (pColumnDiff.notnullNew) {
            p.stmtStart("alter table " + pTableDiff.nameNew + " alter column " + pColumnDiff.nameNew);
            p.stmtAppend("set not null");
            p.stmtDone();
        }
    }

    @Override
    public void createIndex(
        StatementBuilder p, TableDiff pTableDiff, IndexDiff pIndexDiff, boolean pIsIndexParallelCreate) {
        p.stmtStart("create");
        if (pIndexDiff.uniqueNew != null) {
            p.stmtAppend(pIndexDiff.uniqueNew);
        }
        if (pIndexDiff.bitmapNew != null) {
            p.stmtAppend("bitmap");
        }
        p.stmtAppend("index");
        p.stmtAppend(pIndexDiff.consNameNew);
        p.stmtAppend("on");
        p.stmtAppend(pTableDiff.nameNew);
        p.stmtAppend("(");
        if (pIndexDiff.function_based_expressionNew != null) {
            p.stmtAppend(pIndexDiff.function_based_expressionNew);
        } else {
            p.stmtAppend(getColumnList(pIndexDiff.index_columnsDiff));
        }
        p.stmtAppend(")");
        if (pIndexDiff.domain_index_expressionNew != null) {
            p.stmtAppend(pIndexDiff.domain_index_expressionNew);
        } else {
            if (pIndexDiff.loggingNew != null) {
                p.stmtAppend(pIndexDiff.loggingNew.getLiteral());
            }
        }
        if (pIndexDiff.tablespaceNew != null) {
            p.stmtAppend("tablespace");
            p.stmtAppend(pIndexDiff.tablespaceNew);
        }
        if (pIndexDiff.globalNew != null) {
            p.stmtAppend(pIndexDiff.globalNew.getLiteral());
        }
        if (pIndexDiff.bitmapNew == null && pIndexDiff.compressionNew == CompressType.COMPRESS) {
            p.stmtAppend("compress");
        }
        if (pIndexDiff.compressionNew == CompressType.NOCOMPRESS) {
            p.stmtAppend("nocompress");
        }

        if (pIndexDiff.parallelNew == ParallelType.PARALLEL) {
            p.stmtAppend("parallel");
            if (pIndexDiff.parallel_degreeNew != null && pIndexDiff.parallel_degreeNew > 1) {
                p.stmtAppend(" " + pIndexDiff.parallel_degreeNew);
            }
        }
    }

    @Override
    public Runnable getColumnDropHandler(
        StatementBuilder p, TableDiff pTableDiff, List<ColumnDiff> pColumnDiffList) {
        Runnable lAdditionsOnlyAlternativeHandler = () ->
        {
            pColumnDiffList.stream()//
                           .filter(pColumnDiff -> pColumnDiff.notnullOld && pColumnDiff.default_valueOld == null)//
                           .forEach(pColumnDiff ->
                           {
                               p.stmtStartAlterTable(pTableDiff);
                               p.stmtAppend("alter column");
                               p.stmtAppend(pColumnDiff.nameOld);
                               p.stmtAppend("drop not null");
                               p.stmtDone(StatementBuilder.ADDITIONSONLY_ALTERNATIVE_COMMENT);
                           });
        };

        return () ->
        {
            p.stmtStartAlterTableNoCombine(pTableDiff);

            p.stmtAppend("drop column");

            p.stmtAppend(pColumnDiffList.stream().map(pColumnDiff -> pColumnDiff.nameOld).collect(Collectors.joining(",")));
            p.stmtDone(lAdditionsOnlyAlternativeHandler);
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
              p.stmtAppend(pColumnDiff.nameNew + " type " + getColumnDatatype(pColumnDiff));
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
              p.stmtStartAlterTable(pTableDiff);
              p.stmtAppend(" alter " + pColumnDiff.nameNew);

              if (pColumnDiff.default_valueNew == null) {
                  p.stmtAppend("drop default");
              } else {
                  p.stmtAppend("set default");
                  p.stmtAppend(pColumnDiff.default_valueNew);
              }

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
              if (pColumnDiff.notnullNew == false) {
                  p.stmtAppend("drop not null");
              } else {
                  p.stmtAppend("set not null");
              }
              p.stmtDone();
          });
    }
}
