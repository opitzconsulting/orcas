package de.opitzconsulting.orcas.diff;

import static de.opitzconsulting.origOrcasDsl.OrigOrcasDslPackage.Literals.*;

import java.util.List;
import java.util.stream.Collectors;

import de.opitzconsulting.orcas.orig.diff.ColumnDiff;
import de.opitzconsulting.orcas.orig.diff.ForeignKeyDiff;
import de.opitzconsulting.orcas.orig.diff.IndexDiff;
import de.opitzconsulting.orcas.orig.diff.InlineCommentDiff;
import de.opitzconsulting.orcas.orig.diff.PrimaryKeyDiff;
import de.opitzconsulting.orcas.orig.diff.TableDiff;
import de.opitzconsulting.orcas.orig.diff.UniqueKeyDiff;
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
  public void setComment( StatementBuilder p, TableDiff pTableDiff, InlineCommentDiff pInlineCommentDiff )
  {
    if( pInlineCommentDiff.column_nameNew == null )
    {
      p.stmtStart( "alter table" );
      p.stmtAppend( pTableDiff.nameNew );

      p.stmtAppend( "comment" );
      p.stmtAppend( "'" + pInlineCommentDiff.commentNew.replace( "'", "''" ) + "'" );
      p.stmtDone();
    }
  }

  @Override
  public void dropComment( StatementBuilder p, TableDiff pTableDiff, InlineCommentDiff pCommentDiff )
  {
    p.stmtStart( "alter table" );
    p.stmtAppend( pTableDiff.nameOld );

    p.stmtAppend( "comment" );
    p.stmtAppend( "''" );
    p.stmtDone();
  }

  @Override
  public void recreateColumn( StatementBuilder pP, TableDiff pTableDiff, ColumnDiff pColumnDiff )
  {
    pP.failIfAdditionsOnly( "can't recreate columns" );

    pP.addStmt( "alter table " + pTableDiff.nameNew + " modify column " + createColumnCreatePart( pColumnDiff, false ) );
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

  @Override
  public void dropUniqueKey( StatementBuilder pP, TableDiff pTableDiff, UniqueKeyDiff pUniqueKeyDiff )
  {
    pP.stmtStartAlterTable( pTableDiff );
    pP.stmtAppend( "drop index " + pUniqueKeyDiff.consNameOld );
    pP.stmtDone( !pTableDiff.isNew || isAllColumnsOnlyOld( pTableDiff, pUniqueKeyDiff.uk_columnsDiff ) );
  }

  @Override
  public Runnable getColumnDropHandler( StatementBuilder pP, TableDiff pTableDiff, List<ColumnDiff> pColumnDiffList )
  {
    Runnable lAdditionsOnlyAlternativeHandler = () ->
    {
      pColumnDiffList.stream()//
      .filter( pColumnDiff -> pColumnDiff.notnullOld && pColumnDiff.default_valueOld == null )//
      .forEach( pColumnDiff ->
      {
        pP.stmtStartAlterTable( pTableDiff );
        pP.stmtAppend( "modify ( " + pColumnDiff.nameOld );
        pP.stmtAppend( "null" );
        pP.stmtAppend( ")" );
        pP.stmtDone( StatementBuilder.ADDITIONSONLY_ALTERNATIVE_COMMENT );
      } );
    };

    return () ->
    {
      pP.stmtStartAlterTableNoCombine( pTableDiff );

      pP.stmtAppend( pColumnDiffList.stream().map( pColumnDiff -> " drop " + pColumnDiff.nameOld ).collect( Collectors.joining( "," ) ) );
      pP.stmtDone( lAdditionsOnlyAlternativeHandler );
    };
  }
}
