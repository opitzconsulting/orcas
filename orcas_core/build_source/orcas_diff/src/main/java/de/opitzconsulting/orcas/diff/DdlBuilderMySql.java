package de.opitzconsulting.orcas.diff;

import static de.opitzconsulting.origOrcasDsl.OrigOrcasDslPackage.Literals.*;

import de.opitzconsulting.orcas.diff.OrcasDiff.StatementBuilderAlter;
import de.opitzconsulting.orcas.orig.diff.ColumnDiff;
import de.opitzconsulting.orcas.orig.diff.InlineCommentDiff;
import de.opitzconsulting.orcas.orig.diff.TableDiff;
import de.opitzconsulting.origOrcasDsl.DataType;

public class DdlBuilderMySql extends DdlBuilder
{
  @Override
  protected String getDatatypeName( DataType pData_typeNew )
  {
    if( pData_typeNew == DataType.VARCHAR2 )
    {
      return "VARCHAR";
    }
    if( pData_typeNew == DataType.NUMBER )
    {
      return "NUMERIC";
    }

    return super.getDatatypeName( pData_typeNew );
  }

  @Override
  public void setComment( StatementBuilder pP, TableDiff pTableDiff, InlineCommentDiff pInlineCommentDiff )
  {
  }

  @Override
  public void alterColumnIfNeeded( StatementBuilderAlter p1, TableDiff pTableDiff, ColumnDiff pColumnDiff )
  {
    p1.handleAlterBuilder()//
    .ifDifferent( COLUMN__BYTEORCHAR )//
    .ifDifferent( COLUMN__PRECISION )//
    .ifDifferent( COLUMN__SCALE )//
    .ifDifferent( COLUMN__DEFAULT_VALUE )//
    .ifDifferent( COLUMN__NOTNULL )//
    .handle( p -> p.addStmt( "alter table " + pTableDiff.nameNew + " modify column " + createColumnCreatePart( pColumnDiff ) ) );
  }
}
