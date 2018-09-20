package de.opitzconsulting.orcas.diff;

import static de.opitzconsulting.origOrcasDsl.OrigOrcasDslPackage.Literals.*;

import de.opitzconsulting.orcas.orig.diff.ColumnDiff;
import de.opitzconsulting.orcas.orig.diff.ForeignKeyDiff;
import de.opitzconsulting.orcas.orig.diff.IndexDiff;
import de.opitzconsulting.orcas.orig.diff.InlineCommentDiff;
import de.opitzconsulting.orcas.orig.diff.PrimaryKeyDiff;
import de.opitzconsulting.orcas.orig.diff.TableDiff;
import de.opitzconsulting.origOrcasDsl.DataType;

public class DdlBuilderMySql extends DdlBuilder
{
  public DdlBuilderMySql( Parameters pParameters )
  {
    super( pParameters );
  }

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
  protected String getColumnDatatype( ColumnDiff pColumnDiff )
  {
    if( pColumnDiff.identityDiff.isNew )
    {
      if( pColumnDiff.precisionNew == null || (pColumnDiff.precisionNew.intValue() != 10 && pColumnDiff.precisionNew.intValue() != 19) )
      {
        throw new RuntimeException( "precision must be 10 (INT) or 19 (BIGINT) for autoincrement int-determination: " + pColumnDiff.nameNew + " " + pColumnDiff.precisionNew );
      }

      if( pColumnDiff.precisionNew.intValue() != 10 )
      {
        return "BIGINT(" + pColumnDiff.precisionNew + ")";
      }
      else
      {
        return "INT(" + pColumnDiff.precisionNew + ")";
      }
    }

    return super.getColumnDatatype( pColumnDiff );
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
    .handle( p -> p.addStmt( "alter table " + pTableDiff.nameNew + " modify column " + createColumnCreatePart( pColumnDiff, false ) ) );
  }

  @Override
  protected String createColumnCreatePart( ColumnDiff pColumnDiff, boolean pWithoutNotNull )
  {
    String lReturn = pColumnDiff.nameNew + " " + getColumnDatatype( pColumnDiff );

    if( pColumnDiff.default_valueNew != null )
    {
      lReturn = lReturn + " default " + pColumnDiff.default_valueNew;
    }

    if( pColumnDiff.notnullNew )
    {
      if( !pWithoutNotNull )
      {
        lReturn = lReturn + " not null";
      }
    }

    if( pColumnDiff.identityDiff.isNew )
    {
      lReturn = lReturn + " AUTO_INCREMENT";
    }

    return lReturn;
  }

  @Override
  protected String createPkCreateWithTableCreate( PrimaryKeyDiff pPrimary_keyDiff )
  {
    if( pPrimary_keyDiff.isNew )
    {
      return ", primary key (" + getColumnList( pPrimary_keyDiff.pk_columnsDiff ) + ")";
    }
    else
    {
      return "";
    }
  }

  @Override
  public void createPrimarykey( StatementBuilder pP, TableDiff pTableDiff )
  {
    if( pTableDiff.isOld )
    {
      super.createPrimarykey( pP, pTableDiff );
    }
  }

  public void dropPrimaryKey( StatementBuilder p, TableDiff pTableDiff, PrimaryKeyDiff pPrimaryKeyDiff )
  {
    p.stmtStartAlterTable( pTableDiff );
    p.stmtAppend( "drop primary key" );
    p.stmtDone( false );
  }

  @Override
  public void createIndex( StatementBuilder pP, TableDiff pTableDiff, IndexDiff pIndexDiff, boolean pIsIndexParallelCreate )
  {
    super.createIndex( pP, pTableDiff, pIndexDiff, false );
  }

  @Override
  public void dropForeignKey( StatementBuilder pP, TableDiff pTableDiff, ForeignKeyDiff pForeignKeyDiff )
  {
    pP.stmtStartAlterTable( pTableDiff );
    pP.stmtAppend( "drop foreign key " + pForeignKeyDiff.consNameOld );
    pP.stmtDone( !pTableDiff.isNew || isAllColumnsOnlyOld( pTableDiff, pForeignKeyDiff.srcColumnsDiff ) );
  }

  @Override
  public void dropIndex( StatementBuilder pP, TableDiff pTableDiff, IndexDiff pIndexDiff )
  {
    pP.addStmt( "drop index " + pIndexDiff.consNameOld + " on " + pTableDiff.nameOld, !pTableDiff.isNew || pIndexDiff.uniqueOld == null || isAllColumnsOnlyOld( pTableDiff, pIndexDiff.index_columnsDiff ) );
  }
}
