package de.opitzconsulting.orcas.diff;

import static de.opitzconsulting.origOrcasDsl.OrigOrcasDslPackage.Literals.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import de.opitzconsulting.orcas.diff.OrcasDiff.DataHandler;
import de.opitzconsulting.orcas.orig.diff.ColumnDiff;
import de.opitzconsulting.orcas.orig.diff.ColumnIdentityDiff;
import de.opitzconsulting.orcas.orig.diff.ColumnRefDiff;
import de.opitzconsulting.orcas.orig.diff.ConstraintDiff;
import de.opitzconsulting.orcas.orig.diff.ForeignKeyDiff;
import de.opitzconsulting.orcas.orig.diff.HashPartitionDiff;
import de.opitzconsulting.orcas.orig.diff.HashPartitionsDiff;
import de.opitzconsulting.orcas.orig.diff.HashSubPartsDiff;
import de.opitzconsulting.orcas.orig.diff.HashSubSubPartDiff;
import de.opitzconsulting.orcas.orig.diff.IndexDiff;
import de.opitzconsulting.orcas.orig.diff.InlineCommentDiff;
import de.opitzconsulting.orcas.orig.diff.ListPartitionDiff;
import de.opitzconsulting.orcas.orig.diff.ListPartitionValueDiff;
import de.opitzconsulting.orcas.orig.diff.ListPartitionsDiff;
import de.opitzconsulting.orcas.orig.diff.ListSubPartDiff;
import de.opitzconsulting.orcas.orig.diff.ListSubPartsDiff;
import de.opitzconsulting.orcas.orig.diff.ListSubSubPartDiff;
import de.opitzconsulting.orcas.orig.diff.LobStorageDiff;
import de.opitzconsulting.orcas.orig.diff.LobStorageParametersDiff;
import de.opitzconsulting.orcas.orig.diff.MviewDiff;
import de.opitzconsulting.orcas.orig.diff.PrimaryKeyDiff;
import de.opitzconsulting.orcas.orig.diff.RangePartitionDiff;
import de.opitzconsulting.orcas.orig.diff.RangePartitionValueDiff;
import de.opitzconsulting.orcas.orig.diff.RangePartitionsDiff;
import de.opitzconsulting.orcas.orig.diff.RangeSubPartDiff;
import de.opitzconsulting.orcas.orig.diff.RangeSubPartsDiff;
import de.opitzconsulting.orcas.orig.diff.RangeSubSubPartDiff;
import de.opitzconsulting.orcas.orig.diff.RefPartitionDiff;
import de.opitzconsulting.orcas.orig.diff.RefPartitionsDiff;
import de.opitzconsulting.orcas.orig.diff.SequenceDiff;
import de.opitzconsulting.orcas.orig.diff.TableDiff;
import de.opitzconsulting.orcas.orig.diff.UniqueKeyDiff;
import de.opitzconsulting.orcas.orig.diff.VarrayStorageDiff;
import de.opitzconsulting.origOrcasDsl.BuildModeType;
import de.opitzconsulting.origOrcasDsl.CompressForType;
import de.opitzconsulting.origOrcasDsl.CompressType;
import de.opitzconsulting.origOrcasDsl.DataType;
import de.opitzconsulting.origOrcasDsl.EnableType;
import de.opitzconsulting.origOrcasDsl.FkDeleteRuleType;
import de.opitzconsulting.origOrcasDsl.LoggingType;
import de.opitzconsulting.origOrcasDsl.NewValuesType;
import de.opitzconsulting.origOrcasDsl.ParallelType;
import de.opitzconsulting.origOrcasDsl.PermanentnessType;
import de.opitzconsulting.origOrcasDsl.RefreshModeType;
import de.opitzconsulting.origOrcasDsl.SynchronousType;

public abstract class DdlBuilder
{
  private Parameters parameters;

  public DdlBuilder( Parameters pParameters )
  {
    parameters = pParameters;
  }

  public boolean isAllColumnsNew( List<ColumnRefDiff> pColumns, TableDiff pTableDiff )
  {
    return !pColumns.stream()//
    .filter( p -> !p.isNew )//
    .filter( p -> !isColumnNew( pTableDiff, p.column_nameOld ) )//
    .findAny()//
    .isPresent();
  }

  private String createRangeValuelist( List<RangePartitionValueDiff> pRangePartitionValueDiffList )
  {
    String lReturn = "";
    for( int i = 0; i < pRangePartitionValueDiffList.size(); i++ )
    {
      RangePartitionValueDiff lRangePartitionValueDiff = pRangePartitionValueDiffList.get( i );
      if( i != 0 )
      {
        lReturn = lReturn + ",";
      }

      if( lRangePartitionValueDiff.valueNew != null )
      {
        lReturn = lReturn + lRangePartitionValueDiff.valueNew;
      }
      else
      {
        lReturn = lReturn + "maxvalue";
      }
    }

    return lReturn;
  }

  private String createListValuelist( List<ListPartitionValueDiff> pListPartitionValueDiffList )
  {
    String lReturn = "";

    for( int i = 0; i < pListPartitionValueDiffList.size(); i++ )
    {
      ListPartitionValueDiff lListPartitionValueDiff = pListPartitionValueDiffList.get( i );
      if( i != 0 )
      {
        lReturn = lReturn + ",";
      }

      if( lListPartitionValueDiff.valueNew != null )
      {
        lReturn = lReturn + lListPartitionValueDiff.valueNew;
      }
      else
      {
        lReturn = lReturn + "default";
      }
    }

    return lReturn;
  }

  private String createSubRangeClause( RangeSubSubPartDiff pRangeSubSubPartDiff )
  {
    String lReturn = "";
    lReturn = lReturn + "subpartition " + pRangeSubSubPartDiff.nameNew + " values less than (";

    lReturn = lReturn + createRangeValuelist( pRangeSubSubPartDiff.valueDiff );

    lReturn = lReturn + ")";

    if( pRangeSubSubPartDiff.tablespaceNew != null )
    {
      lReturn = lReturn + " tablespace " + pRangeSubSubPartDiff.tablespaceNew;
    }

    return lReturn;
  }

  protected void dropTableConstraintByName( StatementBuilder p, TableDiff pTableDiff, String pCconstraintName, boolean pIsgnoreIfAdditionsOnly )
  {
    p.stmtStartAlterTable( pTableDiff );
    p.stmtAppend( "drop constraint " + pCconstraintName );
    p.stmtDone( pIsgnoreIfAdditionsOnly );
  }

  public void dropTable( StatementBuilder p, TableDiff pTableDiff, DataHandler pDataHandler )
  {
    p.failIfAdditionsOnly( pTableDiff.isNew, "cant't recreate table" );

    pDataHandler.dropWithDropmodeCheck( p, "select 1 from " + pTableDiff.nameOld, () -> p.addStmt( "drop table " + pTableDiff.nameOld, true ) );
  }

  public void dropColumn( StatementBuilder p, TableDiff pTableDiff, ColumnDiff pColumnDiff, DataHandler pDataHandler )
  {
    Runnable lDropHandler = getColumnDropHandler( p, pTableDiff, Collections.singletonList( pColumnDiff ) );

    pDataHandler.dropWithDropmodeCheck( p, getColumnDropCheckSelect( pTableDiff, pColumnDiff ), lDropHandler );
  }

  private String getColumnDropCheckSelect( TableDiff pTableDiff, ColumnDiff pColumnDiff )
  {
    return "select 1 from " + pTableDiff.nameOld + " where " + pColumnDiff.nameOld + " is not null";
  }

  public Runnable getColumnDropHandler( StatementBuilder p, TableDiff pTableDiff, List<ColumnDiff> pColumnDiffList )
  {
    Runnable lAdditionsOnlyAlternativeHandler = () ->
    {
      pColumnDiffList.stream()//
      .filter( pColumnDiff -> pColumnDiff.notnullOld && pColumnDiff.default_valueOld == null )//
      .forEach( pColumnDiff ->
      {
        p.stmtStartAlterTable( pTableDiff );
        p.stmtAppend( "modify ( " + pColumnDiff.nameOld );
        p.stmtAppend( "null" );
        p.stmtAppend( ")" );
        p.stmtDone( StatementBuilder.ADDITIONSONLY_ALTERNATIVE_COMMENT );
      } );
    };

    return () ->
    {
      p.stmtStartAlterTableNoCombine( pTableDiff );

      if( parameters.isSetUnusedInsteadOfDropColumn() )
      {
        p.stmtAppend( "set unused" );
      }
      else
      {
        p.stmtAppend( "drop" );
      }

      p.stmtAppend( "(" + pColumnDiffList.stream().map( pColumnDiff -> pColumnDiff.nameOld ).collect( Collectors.joining( "," ) ) + ")" );
      p.stmtDone( lAdditionsOnlyAlternativeHandler );
    };
  }

  public void dropUniqueKey( StatementBuilder p, TableDiff pTableDiff, UniqueKeyDiff pUniqueKeyDiff )
  {
    dropTableConstraintByName( p, pTableDiff, pUniqueKeyDiff.consNameOld, !pTableDiff.isNew || isAllColumnsOnlyOld( pTableDiff, pUniqueKeyDiff.uk_columnsDiff ) );
  }

  public void dropIndex( StatementBuilder p, TableDiff pTableDiff, IndexDiff pIndexDiff )
  {
    p.addStmt( "drop index " + pIndexDiff.consNameOld, !pTableDiff.isNew || pIndexDiff.uniqueOld == null || isAllColumnsOnlyOld( pTableDiff, pIndexDiff.index_columnsDiff ) );
  }

  public void dropForeignKey( StatementBuilder p, TableDiff pTableDiff, ForeignKeyDiff pForeignKeyDiff )
  {
    dropTableConstraintByName( p, pTableDiff, pForeignKeyDiff.consNameOld, !pTableDiff.isNew || isAllColumnsOnlyOld( pTableDiff, pForeignKeyDiff.srcColumnsDiff ) );
  }

  public void dropPrimaryKey( StatementBuilder p, TableDiff pTableDiff, PrimaryKeyDiff pPrimaryKeyDiff )
  {
    dropTableConstraintByName( p, pTableDiff, pPrimaryKeyDiff.consNameOld, false );
  }

  public void dropConstraint( StatementBuilder p, TableDiff pTableDiff, ConstraintDiff pConstraintDiff )
  {
    dropTableConstraintByName( p, pTableDiff, pConstraintDiff.consNameOld, false );
  }

  public void dropMaterializedViewLog( StatementBuilder p, TableDiff pTableDiff )
  {
    p.addStmt( "drop materialized view log on " + pTableDiff.nameOld );
  }

  public void dropSequence( StatementBuilder p, SequenceDiff lSequenceDiff )
  {
    p.addStmt( "drop sequence " + lSequenceDiff.sequence_nameOld, true );
  }

  public void dropMview( StatementBuilder p, MviewDiff pMviewDiff )
  {
    p.addStmt( "drop materialized view " + pMviewDiff.mview_nameOld );
  }

  public void dropComment( StatementBuilder p, TableDiff pTableDiff, InlineCommentDiff pCommentDiff )
  {
    boolean lIsColumnComment = pCommentDiff.column_nameOld != null;

    p.stmtStart( "comment on" );
    p.stmtAppend( pCommentDiff.comment_objectOld.getName() );
    p.stmtAppend( " " );
    p.stmtAppend( pTableDiff.nameOld );
    if( lIsColumnComment )
    {
      p.stmtAppend( "." );
      p.stmtAppend( pCommentDiff.column_nameOld );
    }
    p.stmtAppend( "is" );
    p.stmtAppend( "''" );
    p.stmtDone( true );
  }

  public void alterSequenceIfNeeded( StatementBuilderAlter p1, SequenceDiff pSequenceDiff, DataHandler pDataHandler )
  {
    BigDecimal lMaxValueSelectValue = pDataHandler.getSequenceMaxValueSelectValue( pSequenceDiff );

    BigDecimal lIstValue = BigDecimal.valueOf( Long.valueOf( pSequenceDiff.max_value_selectOld ) );
    if( lMaxValueSelectValue != null && lIstValue != null && lMaxValueSelectValue.compareTo( lIstValue ) > 0 )
    {
      p1.failIfAdditionsOnly( !pSequenceDiff.increment_byIsEqual, "cant't change increment by" );

      p1.handleAlterBuilder()//
      .forceDifferent( SEQUENCE__MAX_VALUE_SELECT )//
      .handle( p ->
      {
        p.addStmt( "alter sequence " + pSequenceDiff.sequence_nameNew + " increment by " + (lMaxValueSelectValue.longValue() - lIstValue.longValue()) );
        p.addStmt( "declare\n v_dummy number;\n begin\n select " + pSequenceDiff.sequence_nameNew + ".nextval into v_dummy from dual;\n end;" );
        p.addStmt( "alter sequence " + pSequenceDiff.sequence_nameNew + " increment by " + nvl( pSequenceDiff.increment_byNew, 1 ) );
      } );
    }
    else
    {
      p1.handleAlterBuilder()//
      .ifDifferent( SEQUENCE__INCREMENT_BY )//
      .failIfAdditionsOnly()//
      .handle( p -> p.addStmt( "alter sequence " + pSequenceDiff.sequence_nameNew + " increment by " + nvl( pSequenceDiff.increment_byNew, 1 ) ) );
    }

    p1.handleAlterBuilder()//
    .ifDifferent( SEQUENCE__MAXVALUE )//
    .failIfAdditionsOnly()//
    .handle( p -> p.addStmt( "alter sequence " + pSequenceDiff.sequence_nameNew + " maxvalue " + pSequenceDiff.maxvalueNew ) );

    p1.handleAlterBuilder()//
    .ifDifferent( SEQUENCE__MINVALUE )//
    .failIfAdditionsOnly()//
    .handle( p -> p.addStmt( "alter sequence " + pSequenceDiff.sequence_nameNew + " minvalue " + nvl( pSequenceDiff.minvalueNew, 1 ) ) );

    p1.handleAlterBuilder()//
    .ifDifferent( SEQUENCE__CYCLE )//
    .failIfAdditionsOnly()//
    .handle( p -> p.addStmt( "alter sequence " + pSequenceDiff.sequence_nameNew + " " + pSequenceDiff.cycleNew.getLiteral() ) );

    p1.handleAlterBuilder()//
    .ifDifferent( SEQUENCE__CACHE )//
    .ignoreIfAdditionsOnly()//
    .handle( p -> p.addStmt( "alter sequence " + pSequenceDiff.sequence_nameNew + " cache " + nvl( pSequenceDiff.cacheNew, 20 ) ) );

    p1.handleAlterBuilder()//
    .ifDifferent( SEQUENCE__ORDER )//
    .failIfAdditionsOnly()//
    .handle( p -> p.addStmt( "alter sequence " + pSequenceDiff.sequence_nameNew + " " + pSequenceDiff.orderNew.getLiteral() ) );
  }

  public void createSequnece( StatementBuilder p, SequenceDiff pSequenceDiff, DataHandler pDataHandler )
  {
    BigDecimal lMaxValueSelectValue = pDataHandler.getSequenceMaxValueSelectValue( pSequenceDiff );

    p.stmtStart( "create sequence " + pSequenceDiff.sequence_nameNew );

    if( pSequenceDiff.increment_byNew != null )
    {
      p.stmtAppend( "increment by " + pSequenceDiff.increment_byNew );
    }

    if( lMaxValueSelectValue != null )
    {
      p.stmtAppend( "start with " + lMaxValueSelectValue );
    }
    else
    {
      if( pSequenceDiff.startwithNew != null )
      {
        p.stmtAppend( "start with " + pSequenceDiff.startwithNew );
      }
    }

    if( pSequenceDiff.maxvalueNew != null )
    {
      p.stmtAppend( "maxvalue " + pSequenceDiff.maxvalueNew );
    }

    if( pSequenceDiff.minvalueNew != null )
    {
      p.stmtAppend( "minvalue " + pSequenceDiff.minvalueNew );
    }

    if( pSequenceDiff.cycleNew != null )
    {
      p.stmtAppend( pSequenceDiff.cycleNew.getLiteral() );
    }

    if( pSequenceDiff.cacheNew != null )
    {
      p.stmtAppend( "cache " + pSequenceDiff.cacheNew );
    }

    if( pSequenceDiff.orderNew != null )
    {
      p.stmtAppend( pSequenceDiff.orderNew.getLiteral() );
    }

    p.stmtDone();
  }

  public void recreateColumn( StatementBuilder p, TableDiff pTableDiff, ColumnDiff pColumnDiff )
  {
    p.failIfAdditionsOnly( "can't recreate columns" );

    String lTmpOldColumnameNew = "DTO_" + pColumnDiff.nameNew;
    String lTmpNewColumnameNew = "DTN_" + pColumnDiff.nameNew;

    p.stmtStartAlterTableNoCombine( pTableDiff );
    p.stmtAppend( "add " + lTmpNewColumnameNew + " " + getColumnDatatype( pColumnDiff ) );
    p.stmtDone();

    p.addStmt( "update " + pTableDiff.nameNew + " set " + lTmpNewColumnameNew + " = " + pColumnDiff.nameOld );
    p.addStmt( "commit" );

    p.addStmt( "alter table " + pTableDiff.nameNew + " rename column " + pColumnDiff.nameOld + " to " + lTmpOldColumnameNew );
    p.addStmt( "alter table " + pTableDiff.nameNew + " rename column " + lTmpNewColumnameNew + " to " + pColumnDiff.nameNew );
    p.addStmt( "alter table " + pTableDiff.nameNew + " drop column " + lTmpOldColumnameNew );

    if( pColumnDiff.default_valueNew != null )
    {
      p.stmtStart( "alter table " + pTableDiff.nameNew + " modify ( " + pColumnDiff.nameNew + " default" );
      p.stmtAppend( pColumnDiff.default_valueNew );
      p.stmtAppend( ")" );
      p.stmtDone();
    }

    if( pColumnDiff.notnullNew )
    {
      p.stmtStart( "alter table " + pTableDiff.nameNew + " modify ( " + pColumnDiff.nameNew );
      p.stmtAppend( "not null" );
      p.stmtAppend( ")" );
      p.stmtDone();
    }
  }

  public void alterColumnIfNeeded( StatementBuilderAlter p1, TableDiff pTableDiff, ColumnDiff pColumnDiff )
  {
    p1.handleAlterBuilder()//
    .ifDifferent( COLUMN__BYTEORCHAR )//
    .ifDifferent( COLUMN__PRECISION )//
    .ifDifferent( COLUMN__SCALE )//
    .handle( p ->
    {
      p.stmtStartAlterTable( pTableDiff.nameNew );
      p.stmtAppend( "modify ( " + pColumnDiff.nameNew + " " + getColumnDatatype( pColumnDiff ) + ")" );
      p.stmtDone();
    } );

    p1.handleAlterBuilder()//
    .ifDifferent( COLUMN__DEFAULT_VALUE )//
    .ignoreIfAdditionsOnly( pColumnDiff.default_valueNew == null )//
    .failIfAdditionsOnly( pColumnDiff.default_valueOld != null, "can't change default" )//
    .handle( p ->
    {
      p.stmtStartAlterTable( pTableDiff );
      p.stmtAppend( "modify ( " + pColumnDiff.nameNew + " default" );

      if( pColumnDiff.default_valueNew == null )
      {
        p.stmtAppend( "null" );
      }
      else
      {
        p.stmtAppend( pColumnDiff.default_valueNew );
      }
      p.stmtAppend( ")" );
      p.stmtDone();
    } );

    p1.handleAlterBuilder()//
    .ifDifferent( COLUMN__NOTNULL )//
    .ignoreIfAdditionsOnly( pColumnDiff.notnullNew )//
    .handle( p ->
    {
      p.stmtStartAlterTable( pTableDiff );
      p.stmtAppend( "modify ( " + pColumnDiff.nameNew );
      if( pColumnDiff.notnullNew == false )
      {
        p.stmtAppend( "null" );
      }
      else
      {
        p.stmtAppend( "not null" );
      }
      p.stmtAppend( ")" );
      p.stmtDone();
    } );
  }

  public void createPrimarykey( StatementBuilder p, TableDiff pTableDiff )
  {
    boolean lHasIndexParameters = pTableDiff.primary_keyDiff.tablespaceNew != null || pTableDiff.primary_keyDiff.reverseNew != null;

    if( lHasIndexParameters )
    {
      p.stmtStartAlterTableNoCombine( pTableDiff );
    }
    else
    {
      p.stmtStartAlterTable( pTableDiff );
    }
    p.stmtAppend( "add" );
    if( pTableDiff.primary_keyDiff.consNameNew != null )
    {
      p.stmtAppend( "constraint " + pTableDiff.primary_keyDiff.consNameNew );
    }
    p.stmtAppend( "primary key (" + getColumnList( pTableDiff.primary_keyDiff.pk_columnsDiff ) + ")" );

    if( lHasIndexParameters )
    {
      p.stmtAppend( "using index" );

      if( pTableDiff.primary_keyDiff.reverseNew != null )
      {
        p.stmtAppend( "reverse" );
      }

      if( pTableDiff.primary_keyDiff.tablespaceNew != null )
      {
        p.stmtAppend( "tablespace " + pTableDiff.primary_keyDiff.tablespaceNew );
      }
    }

    p.stmtDone( pTableDiff.isOld );
  }

  public void createConstraint( StatementBuilder p, TableDiff pTableDiff, ConstraintDiff pConstraintDiff )
  {
    p.stmtStartAlterTable( pTableDiff );
    p.stmtAppend( "add constraint " + pConstraintDiff.consNameNew + " check (" + pConstraintDiff.ruleNew + ")" );
    if( pConstraintDiff.deferrtypeNew != null )
    {
      p.stmtAppend( "deferrable initially " + pConstraintDiff.deferrtypeNew.getName() );
    }
    if( pConstraintDiff.statusNew != null )
    {
      p.stmtAppend( pConstraintDiff.statusNew.getName() );
    }

    p.stmtDone( getConstraintIgnoreIfAdditionsOnly( pTableDiff, pConstraintDiff.ruleNew ) );
  }

  public boolean isContainsOnlyDropColumns( TableDiff pTableDiff, String pRule )
  {
    List<String> lRuleParts = splitRuleParts( pRule );

    return !pTableDiff.columnsDiff.stream()//
    .filter( p -> p.isNew )//
    .filter( p -> lRuleParts.contains( p.nameNew ) )//
    .findAny()//
    .isPresent();
  }

  private List<String> splitRuleParts( String pRule )
  {
    return Arrays.asList( pRule.toUpperCase().split( "[^\\w]+" ) );
  }

  private boolean getConstraintIgnoreIfAdditionsOnly( TableDiff pTableDiff, String pRuleNew )
  {
    if( !pTableDiff.isOld )
    {
      return false;
    }

    List<String> lRuleParts = splitRuleParts( pRuleNew );

    return containsNotNull( lRuleParts ) || constraintContainsOldColumns( lRuleParts, pTableDiff.columnsDiff );
  }

  private boolean constraintContainsOldColumns( List<String> pRuleParts, List<ColumnDiff> pColumnsDiff )
  {
    return pColumnsDiff//
    .stream()//
    .filter( p -> p.isOld )//
    .filter( p -> pRuleParts.contains( p.nameOld ) )//
    .findAny()//
    .isPresent();
  }

  private boolean containsNotNull( List<String> pRuleParts )
  {
    for( int i = 0; i < pRuleParts.size(); i++ )
    {
      String lRulePart = pRuleParts.get( i );

      if( lRulePart.equals( "NOT" ) )
      {
        if( i != pRuleParts.size() - 1 )
        {
          if( pRuleParts.get( i + 1 ).equals( "NULL" ) )
          {
            return true;
          }
        }
      }
    }

    return false;
  }

  private String createSubListClause( ListSubSubPartDiff pListSubSubPartDiff )
  {
    String lReturn = "";
    lReturn = lReturn + "subpartition " + pListSubSubPartDiff.nameNew + " values (";

    lReturn = lReturn + createListValuelist( pListSubSubPartDiff.valueDiff );

    lReturn = lReturn + ")";

    if( pListSubSubPartDiff.tablespaceNew != null )
    {
      lReturn = lReturn + " tablespace " + pListSubSubPartDiff.tablespaceNew;
    }

    return lReturn;
  }

  private String createSubHashClause( HashSubSubPartDiff pHashSubSubPartDiff )
  {
    String lReturn = "";
    lReturn = lReturn + "subpartition " + pHashSubSubPartDiff.nameNew;

    if( pHashSubSubPartDiff.tablespaceNew != null )
    {
      lReturn = lReturn + " tablespace " + pHashSubSubPartDiff.tablespaceNew;
    }

    return lReturn;
  }

  private String createSubpartitions( List<HashSubSubPartDiff> pHashSubSubPartDiffList, List<ListSubSubPartDiff> pListSubSubPartDiffList, List<RangeSubSubPartDiff> pRangeSubSubPartDiffList )
  {
    String lReturn = "";

    if( !pHashSubSubPartDiffList.isEmpty() )
    {
      lReturn = lReturn + "(";
      for( int i = 0; i < pHashSubSubPartDiffList.size(); i++ )
      {
        HashSubSubPartDiff lHashSubSubPartDiff = pHashSubSubPartDiffList.get( i );

        if( i != 0 )
        {
          lReturn = lReturn + ",";
        }

        lReturn = lReturn + createSubHashClause( lHashSubSubPartDiff );
      }

      lReturn = lReturn + ")";
    }

    if( !pListSubSubPartDiffList.isEmpty() )
    {
      lReturn = lReturn + "(";
      for( int i = 0; i < pListSubSubPartDiffList.size(); i++ )
      {
        ListSubSubPartDiff lListSubSubPartDiff = pListSubSubPartDiffList.get( i );

        if( i != 0 )
        {
          lReturn = lReturn + ",";
        }

        lReturn = lReturn + createSubListClause( lListSubSubPartDiff );
      }

      lReturn = lReturn + ")";
    }

    if( !pRangeSubSubPartDiffList.isEmpty() )
    {
      lReturn = lReturn + "(";
      for( int i = 0; i < pRangeSubSubPartDiffList.size(); i++ )
      {
        RangeSubSubPartDiff lRangeSubSubPartDiff = pRangeSubSubPartDiffList.get( i );

        if( i != 0 )
        {
          lReturn = lReturn + ",";
        }

        lReturn = lReturn + createSubRangeClause( lRangeSubSubPartDiff );
      }

      lReturn = lReturn + ")";
    }

    return lReturn;
  }

  private String createRangeSubParts( RangeSubPartsDiff pRangeSubPartsDiff )
  {
    String lReturn = "";
    lReturn = lReturn + " subpartition by range (";
    for( int i = 0; i < pRangeSubPartsDiff.columnsDiff.size(); i++ )
    {
      if( i != 0 )
      {
        lReturn = lReturn + ",";
      }
      lReturn = lReturn + pRangeSubPartsDiff.columnsDiff.get( i ).column_nameNew;
    }
    lReturn = lReturn + ")";

    return lReturn;
  }

  private String createListSubParts( ListSubPartsDiff pListSubPartsDiff )
  {
    return " subpartition by list (" + pListSubPartsDiff.columnDiff.column_nameNew + ")";
  }

  private String createHashSubParts( HashSubPartsDiff pHashSubPartsDiff )
  {
    return " subpartition by hash (" + pHashSubPartsDiff.columnDiff.column_nameNew + ")";
  }

  private String createTableSubParts( HashSubPartsDiff pHashSubPartsDiff, ListSubPartsDiff pListSubPartsDiff, RangeSubPartsDiff pRangeSubPartsDiff )
  {
    if( pHashSubPartsDiff.isNew )
    {
      return createHashSubParts( pHashSubPartsDiff );
    }
    if( pListSubPartsDiff.isNew )
    {
      return createListSubParts( pListSubPartsDiff );
    }
    if( pRangeSubPartsDiff.isNew )
    {
      return createRangeSubParts( pRangeSubPartsDiff );
    }

    throw new RuntimeException( "subpartitionstyp unbekannt" );
  }

  private boolean hasSubpartitioning( HashSubPartsDiff pHashSubPartsDiff, ListSubPartsDiff pListSubPartsDiff, RangeSubPartsDiff pRangeSubPartsDiff )
  {
    return pHashSubPartsDiff.isNew || pListSubPartsDiff.isNew || pRangeSubPartsDiff.isNew;
  }

  private String createRangeClause( RangePartitionsDiff pRangePartitionsDiff )
  {
    String lReturn = "";
    lReturn = lReturn + "partition by range (" + getColumnList( pRangePartitionsDiff.columnsDiff ) + ")";

    if( pRangePartitionsDiff.intervalExpressionNew != null )
    {
      lReturn = lReturn + "interval (" + pRangePartitionsDiff.intervalExpressionNew + ")";
    }

    if( hasSubpartitioning( pRangePartitionsDiff.tableSubPartHashSubPartsDiff, pRangePartitionsDiff.tableSubPartListSubPartsDiff, pRangePartitionsDiff.tableSubPartRangeSubPartsDiff ) )
    {
      lReturn = lReturn + createTableSubParts( pRangePartitionsDiff.tableSubPartHashSubPartsDiff, pRangePartitionsDiff.tableSubPartListSubPartsDiff, pRangePartitionsDiff.tableSubPartRangeSubPartsDiff );

      lReturn = lReturn + "(";

      for( int i = 0; i < pRangePartitionsDiff.subPartitionListDiff.size(); i++ )
      {
        RangeSubPartDiff lRangeSubPartDiff = pRangePartitionsDiff.subPartitionListDiff.get( i );
        if( i != 0 )
        {
          lReturn = lReturn + ",";
        }
        lReturn = lReturn + "partition " + lRangeSubPartDiff.nameNew + " values less than (";

        lReturn = lReturn + createRangeValuelist( lRangeSubPartDiff.valueDiff );

        lReturn = lReturn + ")";

        lReturn = lReturn + createSubpartitions( lRangeSubPartDiff.subPartListHashSubSubPartDiff, lRangeSubPartDiff.subPartListListSubSubPartDiff, lRangeSubPartDiff.subPartListRangeSubSubPartDiff );
      }
      lReturn = lReturn + ")";
    }
    else
    {
      lReturn = lReturn + "(";
      for( int i = 0; i < pRangePartitionsDiff.partitionListDiff.size(); i++ )
      {
        RangePartitionDiff lRangePartitionDiff = pRangePartitionsDiff.partitionListDiff.get( i );
        if( i != 0 )
        {
          lReturn = lReturn + ",";
        }
        lReturn = lReturn + "partition " + lRangePartitionDiff.nameNew + " values less than (";

        lReturn = lReturn + createRangeValuelist( lRangePartitionDiff.valueDiff );

        lReturn = lReturn + ")";

        if( lRangePartitionDiff.tablespaceNew != null )
        {
          lReturn = lReturn + " tablespace " + lRangePartitionDiff.tablespaceNew;
        }
      }
      lReturn = lReturn + ")";
    }

    return lReturn;
  }

  private String createListClause( ListPartitionsDiff pListPartitionsDiff )
  {
    String lReturn = "";

    lReturn = lReturn + "partition by list (" + pListPartitionsDiff.columnDiff.column_nameNew;
    lReturn = lReturn + ")";

    if( hasSubpartitioning( pListPartitionsDiff.tableSubPartHashSubPartsDiff, pListPartitionsDiff.tableSubPartListSubPartsDiff, pListPartitionsDiff.tableSubPartRangeSubPartsDiff ) )
    {
      lReturn = lReturn + createTableSubParts( pListPartitionsDiff.tableSubPartHashSubPartsDiff, pListPartitionsDiff.tableSubPartListSubPartsDiff, pListPartitionsDiff.tableSubPartRangeSubPartsDiff );

      lReturn = lReturn + "(";

      for( int i = 0; i < pListPartitionsDiff.subPartitionListDiff.size(); i++ )
      {
        ListSubPartDiff lListSubPartDiff = pListPartitionsDiff.subPartitionListDiff.get( i );
        if( i != 0 )
        {
          lReturn = lReturn + ",";
        }
        lReturn = lReturn + "partition " + lListSubPartDiff.nameNew + " values (";

        lReturn = lReturn + createListValuelist( lListSubPartDiff.valueDiff );

        lReturn = lReturn + ")";

        lReturn = lReturn + createSubpartitions( lListSubPartDiff.subPartListHashSubSubPartDiff, lListSubPartDiff.subPartListListSubSubPartDiff, lListSubPartDiff.subPartListRangeSubSubPartDiff );
      }
      lReturn = lReturn + ")";
    }
    else
    {
      lReturn = lReturn + "(";

      for( int i = 0; i < pListPartitionsDiff.partitionListDiff.size(); i++ )
      {
        ListPartitionDiff lListPartitionDiff = pListPartitionsDiff.partitionListDiff.get( i );
        if( i != 0 )
        {
          lReturn = lReturn + ",";
        }
        lReturn = lReturn + "partition " + lListPartitionDiff.nameNew + " values (";

        lReturn = lReturn + createListValuelist( lListPartitionDiff.valueDiff );

        lReturn = lReturn + ")";

        if( lListPartitionDiff.tablespaceNew != null )
        {
          lReturn = lReturn + " tablespace " + lListPartitionDiff.tablespaceNew;
        }
      }
      lReturn = lReturn + ")";
    }

    return lReturn;
  }

  private String createHashClause( HashPartitionsDiff pHashPartitionsDiff )
  {
    String lReturn = "";
    lReturn = lReturn + "partition by hash (" + pHashPartitionsDiff.columnDiff.column_nameNew;
    lReturn = lReturn + ")(";
    for( int i = 0; i < pHashPartitionsDiff.partitionListDiff.size(); i++ )
    {
      HashPartitionDiff lHashPartitionDiff = pHashPartitionsDiff.partitionListDiff.get( i );
      if( i != 0 )
      {
        lReturn = lReturn + ",";
      }
      lReturn = lReturn + "partition " + lHashPartitionDiff.nameNew;

      if( lHashPartitionDiff.tablespaceNew != null )
      {
        lReturn = lReturn + " tablespace " + lHashPartitionDiff.tablespaceNew;
      }
    }
    lReturn = lReturn + ")";

    return lReturn;
  }

  private String createRefClause( RefPartitionsDiff pRefPartitionsDiff )
  {
    String lReturn = "";

    lReturn = lReturn + "partition by reference (" + pRefPartitionsDiff.fkNameNew;
    lReturn = lReturn + ")(";
    for( int i = 0; i < pRefPartitionsDiff.partitionListDiff.size(); i++ )
    {
      RefPartitionDiff lRefPartitionDiff = pRefPartitionsDiff.partitionListDiff.get( i );
      if( i != 0 )
      {
        lReturn = lReturn + ",";
      }
      lReturn = lReturn + "partition " + lRefPartitionDiff.nameNew;

      if( lRefPartitionDiff.tablespaceNew != null )
      {
        lReturn = lReturn + " tablespace " + lRefPartitionDiff.tablespaceNew;
      }
    }
    lReturn = lReturn + ")";

    return lReturn;
  }

  private LobStorageDiff findLobstorage( TableDiff pTableDiff, String pColumnName )
  {
    for( LobStorageDiff lLobStorageDiff : pTableDiff.lobStoragesDiff )
    {
      if( lLobStorageDiff.isNew && lLobStorageDiff.column_nameNew.equals( pColumnName ) )
      {
        return lLobStorageDiff;
      }
    }

    return null;
  }

  private VarrayStorageDiff findVarraystorage( TableDiff pTableDiff, String pColumnName )
  {
    for( VarrayStorageDiff lLobStorageDiff : pTableDiff.varrayStoragesDiff )
    {
      if( lLobStorageDiff.column_nameNew.equals( pColumnName ) )
      {
        return lLobStorageDiff;
      }
    }

    return null;
  }

  public void createIndex( StatementBuilder p, TableDiff pTableDiff, IndexDiff pIndexDiff, boolean pIsIndexParallelCreate )
  {
    p.stmtStart( "create" );
    if( pIndexDiff.uniqueNew != null )
    {
      p.stmtAppend( pIndexDiff.uniqueNew );
    }
    if( pIndexDiff.bitmapNew != null )
    {
      p.stmtAppend( "bitmap" );
    }
    p.stmtAppend( "index" );
    p.stmtAppend( pIndexDiff.consNameNew );
    p.stmtAppend( "on" );
    p.stmtAppend( pTableDiff.nameNew );
    p.stmtAppend( "(" );
    if( pIndexDiff.function_based_expressionNew != null )
    {
      p.stmtAppend( pIndexDiff.function_based_expressionNew );
    }
    else
    {
      p.stmtAppend( getColumnList( pIndexDiff.index_columnsDiff ) );
    }
    p.stmtAppend( ")" );
    if( pIndexDiff.domain_index_expressionNew != null )
    {
      p.stmtAppend( pIndexDiff.domain_index_expressionNew );
    }
    else
    {
      if( pIndexDiff.loggingNew != null )
      {
        p.stmtAppend( pIndexDiff.loggingNew.getLiteral() );
      }
    }
    if( pIndexDiff.tablespaceNew != null )
    {
      p.stmtAppend( "tablespace" );
      p.stmtAppend( pIndexDiff.tablespaceNew );
    }
    if( pIndexDiff.globalNew != null )
    {
      p.stmtAppend( pIndexDiff.globalNew.getLiteral() );
    }
    if( pIndexDiff.bitmapNew == null && pIndexDiff.compressionNew == CompressType.COMPRESS )
    {
      p.stmtAppend( "compress" );
    }
    if( pIndexDiff.compressionNew == CompressType.NOCOMPRESS )
    {
      p.stmtAppend( "nocompress" );
    }

    if( pIndexDiff.parallelNew == ParallelType.PARALLEL || pIsIndexParallelCreate )
    {
      p.stmtAppend( "parallel" );
      if( pIndexDiff.parallel_degreeNew != null && pIndexDiff.parallel_degreeNew > 1 )
      {
        p.stmtAppend( " " + pIndexDiff.parallel_degreeNew );
      }
    }

    if( parameters.isCreateIndexOnline() )
    {
      p.stmtAppend( "online" );
    }

    boolean lIgnoreIfAdditionsOnly = pTableDiff.isOld && pIndexDiff.uniqueNew != null && !isAllColumnsOnlyNew( pTableDiff, pIndexDiff.index_columnsDiff );
    p.stmtDone( lIgnoreIfAdditionsOnly );

    if( pIndexDiff.parallelNew != ParallelType.PARALLEL && pIsIndexParallelCreate )
    {
      p.addStmt( "alter index " + pIndexDiff.consNameNew + " noparallel", lIgnoreIfAdditionsOnly );
    }
  }

  private String createPartitioningClause( TableDiff pTableDiff )
  {
    String lReturn = "";

    lReturn = lReturn + " ";

    if( pTableDiff.tablePartitioningRangePartitionsDiff.isNew )
    {
      lReturn = lReturn + createRangeClause( pTableDiff.tablePartitioningRangePartitionsDiff );
    }
    if( pTableDiff.tablePartitioningListPartitionsDiff.isNew )
    {
      lReturn = lReturn + createListClause( pTableDiff.tablePartitioningListPartitionsDiff );
    }
    if( pTableDiff.tablePartitioningHashPartitionsDiff.isNew )
    {
      lReturn = lReturn + createHashClause( pTableDiff.tablePartitioningHashPartitionsDiff );
    }
    if( pTableDiff.tablePartitioningRefPartitionsDiff.isNew )
    {
      lReturn = lReturn + createRefClause( pTableDiff.tablePartitioningRefPartitionsDiff );
    }

    return lReturn;
  }

  private String createRefFkClause( TableDiff pTableDiff )
  {
    String lReturn = "";

    if( pTableDiff.tablePartitioningRefPartitionsDiff.isNew )
    {
      ForeignKeyDiff lForeignKeyDiff = OrcasDiff.getFkForRefPartitioning( pTableDiff );

      lReturn = lReturn + ", " + createForeignKeyClause( lForeignKeyDiff );
    }
    return lReturn;
  }

  private String createColumnClause( List<ColumnDiff> pColumnListDiffList )
  {
    String lReturn = null;

    for( ColumnDiff lColumnDiff : pColumnListDiffList )
    {
      if( lReturn != null )
      {
        lReturn = lReturn + ",";
      }
      else
      {
        lReturn = "";
      }

      if( lColumnDiff.isNew )
      {
        lReturn = lReturn + " " + createColumnCreatePart( lColumnDiff, false );
      }
    }

    return lReturn;
  }

  public void alterIndexIfNeeded( StatementBuilderAlter p1, IndexDiff pIndexDiff, boolean pIsIndexmovetablespace, String pDefaultTablespace )
  {
    p1.handleAlterBuilder()//
    .ifDifferent( INDEX_OR_UNIQUE_KEY__CONS_NAME )//
    .handle( p ->
    {
      p.stmtStart( "alter index" );
      p.stmtAppend( pIndexDiff.consNameOld );
      p.stmtAppend( "rename to" );
      p.stmtAppend( pIndexDiff.consNameNew );
      p.stmtDone();
    } );

    p1.handleAlterBuilder()//
    .ifDifferent( INDEX__PARALLEL )//
    .ifDifferent( INDEX__PARALLEL_DEGREE )//
    .ignoreIfAdditionsOnly()//
    .handle( p ->
    {
      p.stmtStart( "alter index" );
      p.stmtAppend( pIndexDiff.consNameNew );
      if( pIndexDiff.parallelNew == ParallelType.PARALLEL )
      {
        p.stmtAppend( "parallel" );
        if( pIndexDiff.parallel_degreeNew != null && pIndexDiff.parallel_degreeNew > 1 )
        {
          p.stmtAppend( " " + pIndexDiff.parallel_degreeNew );
        }
      }
      else
      {
        p.stmtAppend( "noparallel" );
      }

      p.stmtDone();
    } );

    p1.handleAlterBuilder()//
    .ifDifferent( INDEX__LOGGING )//
    .ignoreIfAdditionsOnly()//
    .handle( p ->
    {
      p.stmtStart( "alter index" );
      p.stmtAppend( pIndexDiff.consNameNew );
      if( pIndexDiff.loggingNew == LoggingType.NOLOGGING )
      {
        p.stmtAppend( "nologging" );
      }
      else
      {
        p.stmtAppend( "logging" );
      }

      p.stmtDone();
    } );

    p1.handleAlterBuilder()//
    .ifDifferent( INDEX_OR_UNIQUE_KEY__TABLESPACE, pIsIndexmovetablespace )//
    .ignoreIfAdditionsOnly()//
    .handle( p ->
    {
      p.stmtStart( "alter index" );
      p.stmtAppend( pIndexDiff.consNameNew );
      p.stmtAppend( "rebuild tablespace" );
      p.stmtAppend( nvl( pIndexDiff.tablespaceNew, pDefaultTablespace ) );
      p.stmtDone();
    } );
  }

  public boolean isColumnNew( TableDiff pTableDiff, String pColumnName )
  {
    return pTableDiff.columnsDiff//
    .stream()//
    .filter( p -> p.isNew )//
    .filter( p -> p.nameNew.equals( pColumnName ) )//
    .findAny()//
    .isPresent();
  }

  private boolean isColumnOnlyNew( TableDiff pTableDiff, String pColumnName )
  {
    return pTableDiff.columnsDiff//
    .stream()//
    .filter( p -> !p.isOld && p.isNew )//
    .filter( p -> p.nameNew.equals( pColumnName ) )//
    .findAny()//
    .isPresent();
  }

  private boolean isAllColumnsOnlyNew( TableDiff pTableDiff, List<ColumnRefDiff> pColumnsDiff )
  {
    return !pColumnsDiff//
    .stream()//
    .filter( p -> p.isNew )//
    .filter( p -> !isColumnOnlyNew( pTableDiff, p.column_nameNew ) )//
    .findAny()//
    .isPresent();
  }

  public boolean isColumnOnlyOld( TableDiff pTableDiff, String pColumnName )
  {
    return pTableDiff.columnsDiff//
    .stream()//
    .filter( p -> p.isOld && !p.isNew )//
    .filter( p -> p.nameOld.equals( pColumnName ) )//
    .findAny()//
    .isPresent();
  }

  public boolean isAllColumnsOnlyOld( TableDiff pTableDiff, List<ColumnRefDiff> pColumnsDiff )
  {
    return !pColumnsDiff//
    .stream()//
    .filter( p -> p.isOld )//
    .filter( p -> !isColumnOnlyOld( pTableDiff, p.column_nameOld ) )//
    .findAny()//
    .isPresent();
  }

  public void createUniqueKey( StatementBuilder p, TableDiff pTableDiff, UniqueKeyDiff pUniqueKeyDiff )
  {
    boolean lHasTablespace = pUniqueKeyDiff.tablespaceNew != null;
    boolean lHasIndex = pUniqueKeyDiff.indexnameNew != null && !pUniqueKeyDiff.indexnameNew.equals( pUniqueKeyDiff.consNameNew );

    if( lHasTablespace || lHasIndex )
    {
      p.stmtStartAlterTableNoCombine( pTableDiff );
    }
    else
    {
      p.stmtStartAlterTable( pTableDiff );
    }

    p.stmtAppend( "add constraint " + pUniqueKeyDiff.consNameNew + " unique (" + getColumnList( pUniqueKeyDiff.uk_columnsDiff ) + ")" );
    if( lHasTablespace )
    {
      p.stmtAppend( "using index tablespace " + pUniqueKeyDiff.tablespaceNew );
    }
    else
    {
      if( lHasIndex )
      {
        p.stmtAppend( "using index " + pUniqueKeyDiff.indexnameNew );
      }
    }
    if( pUniqueKeyDiff.statusNew != null )
    {
      p.stmtAppend( pUniqueKeyDiff.statusNew.getName() );
    }

    p.stmtDone( pTableDiff.isOld && !isAllColumnsOnlyNew( pTableDiff, pUniqueKeyDiff.uk_columnsDiff ) );
  }

  public void setComment( StatementBuilder p, TableDiff pTableDiff, InlineCommentDiff pInlineCommentDiff )
  {
    p.stmtStart( "comment on" );
    p.stmtAppend( pInlineCommentDiff.comment_objectNew.getName() );
    p.stmtAppend( pTableDiff.nameNew );
    if( pInlineCommentDiff.column_nameNew != null )
    {
      p.stmtAppend( "." );
      p.stmtAppend( pInlineCommentDiff.column_nameNew );
    }
    p.stmtAppend( "is" );
    p.stmtAppend( "'" + pInlineCommentDiff.commentNew.replace( "'", "''" ) + "'" );
    p.stmtDone();
  }

  public void alterMviewIfNeeded( StatementBuilderAlter p1, MviewDiff pMviewDiff )
  {
    p1.handleAlterBuilder()//
    .ifDifferent( MVIEW__QUERY_REWRITE )//
    .handle( p ->
    {
      EnableType lQueryRewriteNew = pMviewDiff.queryRewriteNew;

      if( lQueryRewriteNew == null )
      {
        lQueryRewriteNew = EnableType.DISABLE;
      }

      p.addStmt( "alter materialized view " + pMviewDiff.mview_nameNew + " " + lQueryRewriteNew.getLiteral() + " query rewrite" );
    } );

    p1.handleAlterBuilder()//
    .ifDifferent( MVIEW__REFRESH_MODE )//
    .ifDifferent( MVIEW__REFRESH_METHOD )//
    .handle( p ->
    {
      RefreshModeType lRefreshModeType = pMviewDiff.refreshModeNew;
      String lRefreshmode;
      if( lRefreshModeType == null )
      {
        lRefreshmode = "";
      }
      else
      {
        lRefreshmode = " on " + lRefreshModeType.getLiteral();
      }
      p.addStmt( "alter materialized view " + pMviewDiff.mview_nameNew + " " + adjustRefreshmethodLiteral( pMviewDiff.refreshMethodNew.getLiteral() ) + lRefreshmode );
    } );

    // Physical parameters nur, wenn nicht prebuilt
    boolean lNotPrebuild = pMviewDiff.buildModeNew != BuildModeType.PREBUILT;

    p1.handleAlterBuilder()//
    .ifDifferent( MVIEW__PARALLEL, lNotPrebuild )//
    .ifDifferent( MVIEW__PARALLEL_DEGREE, lNotPrebuild )//
    .handle( p ->
    {
      p.stmtStart( "alter materialized view" );
      p.stmtAppend( pMviewDiff.mview_nameNew );

      handleParallel( p, pMviewDiff.parallelNew, pMviewDiff.parallel_degreeNew, true );

      p.stmtDone();
    } );

    p1.handleAlterBuilder()//
    .ifDifferent( MVIEW__COMPRESSION, lNotPrebuild )//
    .handle( p ->
    {
      p.stmtStart( "alter materialized view" );
      p.stmtAppend( pMviewDiff.mview_nameNew );

      handleCompression( p, pMviewDiff.compressionNew, pMviewDiff.compressionForNew, true );

      p.stmtDone();
    } );
  }

  public void alterMviewlogIfNeeded( StatementBuilderAlter p1, TableDiff pTableDiff, String pDateformat )
  {
    p1.handleAlterBuilder()//
    .ifDifferent( MVIEW_LOG__PARALLEL )//
    .ifDifferent( MVIEW_LOG__PARALLEL_DEGREE )//
    .handle( p ->
    {
      p.stmtStart( "alter materialized view log on" );
      p.stmtAppend( pTableDiff.nameNew );
      handleParallel( p, pTableDiff.mviewLogDiff.parallelNew, pTableDiff.mviewLogDiff.parallel_degreeNew, true );

      p.stmtDone();
    } );

    p1.handleAlterBuilder()//
    .ifDifferent( MVIEW_LOG__NEW_VALUES )//
    .handle( p ->
    {
      p.stmtStart( "alter materialized view log on" );
      p.stmtAppend( pTableDiff.nameNew );

      if( pTableDiff.mviewLogDiff.newValuesNew == NewValuesType.INCLUDING )
      {
        p.stmtAppend( "including" );
      }
      else
      {
        p.stmtAppend( "excluding" );
      }
      p.stmtAppend( "new values" );

      p.stmtDone();
    } );

    p1.handleAlterBuilder()//
    .ifDifferent( MVIEW_LOG__START_WITH )//
    .ifDifferent( MVIEW_LOG__NEXT )//
    .ifDifferent( MVIEW_LOG__REPEAT_INTERVAL )//
    .handle( p ->
    {
      p.stmtStart( "alter materialized view log on" );
      p.stmtAppend( pTableDiff.nameNew );
      p.stmtAppend( "purge" );
      if( pTableDiff.mviewLogDiff.startWithIsEqual == false )
      {
        p.stmtAppend( "start with" );
        p.stmtAppend( "to_date('" + pTableDiff.mviewLogDiff.startWithNew + "','" + pDateformat + "')" );
      }
      if( pTableDiff.mviewLogDiff.nextIsEqual == false )
      {
        p.stmtAppend( "next" );
        p.stmtAppend( "to_date('" + pTableDiff.mviewLogDiff.nextNew + "','" + pDateformat + "')" );
      }
      else
      {
        if( pTableDiff.mviewLogDiff.repeatIntervalIsEqual == false )
        {
          p.stmtAppend( "repeat interval" );
          p.stmtAppend( pTableDiff.mviewLogDiff.repeatIntervalNew + "" );
        }
      }

      p.stmtDone();
    } );

    p1.handleAlterBuilder()//
    .ifDifferent( MVIEW_LOG__SYNCHRONOUS )//
    .handle( p ->
    {
      p.stmtAppend( "alter materialized view log on" );
      p.stmtAppend( pTableDiff.nameNew );
      if( pTableDiff.mviewLogDiff.synchronousNew == SynchronousType.ASYNCHRONOUS )
      {
        p.stmtAppend( "purge immediate asynchronous" );
      }
      else
      {
        p.stmtAppend( "purge immediate synchronous" );
      }

      p.stmtDone();
    } );
  }

  public void createMviewlog( StatementBuilder p, TableDiff pTableDiff, String pDateformat )
  {
    String c_date_format = pDateformat;
    p.stmtStart( "create materialized view log on" );
    p.stmtAppend( pTableDiff.nameNew );

    if( pTableDiff.mviewLogDiff.tablespaceNew != null )
    {
      p.stmtAppend( "tablespace" );
      p.stmtAppend( pTableDiff.mviewLogDiff.tablespaceNew );
    }

    handleParallel( p, pTableDiff.mviewLogDiff.parallelNew, pTableDiff.mviewLogDiff.parallel_degreeNew, false );

    p.stmtAppend( "with" );

    if( nvl( pTableDiff.mviewLogDiff.primaryKeyNew, "null" ).equals( "primary" ) || !nvl( pTableDiff.mviewLogDiff.rowidNew, "null" ).equals( "rowid" ) )
    {
      p.stmtAppend( "primary key" );
      if( nvl( pTableDiff.mviewLogDiff.rowidNew, "null" ).equals( "rowid" ) )
      {
        p.stmtAppend( "," );
      }
    }

    if( nvl( pTableDiff.mviewLogDiff.rowidNew, "null" ).equals( "rowid" ) )
    {
      p.stmtAppend( "rowid" );
    }

    if( nvl( pTableDiff.mviewLogDiff.withSequenceNew, "null" ).equals( "sequence" ) )
    {
      p.stmtAppend( "," );
      p.stmtAppend( "sequence" );
    }

    if( pTableDiff.mviewLogDiff.columnsDiff.size() > 0 )
    {
      p.stmtAppend( "(" );
      p.stmtAppend( getColumnList( pTableDiff.mviewLogDiff.columnsDiff ) );
      p.stmtAppend( ")" );
    }

    if( nvl( pTableDiff.mviewLogDiff.commitScnNew, "null" ).equals( "commit_scn" ) )
    {
      p.stmtAppend( "," );
      p.stmtAppend( "commit scn" );
    }

    if( pTableDiff.mviewLogDiff.newValuesNew != null )
    {
      p.stmtAppend( pTableDiff.mviewLogDiff.newValuesNew.getLiteral() );
      p.stmtAppend( "new values" );
    }

    if( pTableDiff.mviewLogDiff.startWithNew != null || pTableDiff.mviewLogDiff.nextNew != null || (pTableDiff.mviewLogDiff.repeatIntervalNew != null && pTableDiff.mviewLogDiff.repeatIntervalNew != 0) )
    {
      p.stmtAppend( "purge" );
      if( pTableDiff.mviewLogDiff.startWithNew != null )
      {
        p.stmtAppend( "start with" );
        p.stmtAppend( "to_date('" + pTableDiff.mviewLogDiff.startWithNew + "','" + c_date_format + "')" );
      }
      if( pTableDiff.mviewLogDiff.nextNew != null )
      {
        p.stmtAppend( "next" );
        p.stmtAppend( "to_date('" + pTableDiff.mviewLogDiff.nextNew + "','" + c_date_format + "')" );
      }
      else
      {
        if( pTableDiff.mviewLogDiff.repeatIntervalNew != null && pTableDiff.mviewLogDiff.repeatIntervalNew != 0 )
        {
          p.stmtAppend( "repeat interval" );
          p.stmtAppend( "" + pTableDiff.mviewLogDiff.repeatIntervalNew );
        }
      }
    }
    else
    {
      if( pTableDiff.mviewLogDiff.synchronousNew == SynchronousType.ASYNCHRONOUS )
      {
        p.stmtAppend( "purge immediate asynchronous" );
      }
    }

    p.stmtDone();
  }

  public void createTable( StatementBuilder p, TableDiff pTableDiff )
  {
    p.stmtStart( "create" );
    if( pTableDiff.permanentnessNew == PermanentnessType.GLOBAL_TEMPORARY )
    {
      p.stmtAppend( "global " + pTableDiff.permanentnessNew.getLiteral() );
    }
    p.stmtAppend( "table" );
    p.stmtAppend( pTableDiff.nameNew );
    p.stmtAppend( "(" );
    p.stmtAppend( createColumnClause( pTableDiff.columnsDiff ) );
    p.stmtAppend( createPkCreateWithTableCreate( pTableDiff.primary_keyDiff ) );
    p.stmtAppend( createRefFkClause( pTableDiff ) );
    p.stmtAppend( ")" );
    if( pTableDiff.transactionControlNew != null )
    {
      p.stmtAppend( "on commit " );
      p.stmtAppend( pTableDiff.transactionControlNew.getLiteral() );
      p.stmtAppend( "rows nocache" );
    }
    else
    {
      pTableDiff.lobStoragesDiff.stream().filter( lLobstorage -> lLobstorage.isNew ).forEach( lLobstorage -> addLobStorage( p, lLobstorage ) );
      pTableDiff.varrayStoragesDiff.stream().filter( lVarraystorage -> lVarraystorage.isNew ).forEach( lVarraystorage -> addVarrayStorage( p, lVarraystorage ) );

      if( pTableDiff.tablespaceNew != null )
      {
        p.stmtAppend( "tablespace" );
        p.stmtAppend( pTableDiff.tablespaceNew );
      }

      if( pTableDiff.permanentnessNew != PermanentnessType.GLOBAL_TEMPORARY )
      {
        // add pctfree
        if( pTableDiff.pctfreeNew != null )
        {
          p.stmtAppend( "pctfree" );
          p.stmtAppend( "" + pTableDiff.pctfreeNew );
        }
      }

      if( pTableDiff.permanentnessNew != PermanentnessType.GLOBAL_TEMPORARY )
      {
        if( pTableDiff.loggingNew == LoggingType.NOLOGGING )
        {
          p.stmtAppend( "nologging" );
        }
      }
      handleCompression( p, pTableDiff.compressionNew, pTableDiff.compressionForNew, false );
    }

    p.stmtAppend( createPartitioningClause( pTableDiff ) );

    handleParallel( p, pTableDiff.parallelNew, pTableDiff.parallel_degreeNew, false );

    p.stmtDone();
  }

  protected String createPkCreateWithTableCreate( PrimaryKeyDiff pPrimary_keyDiff )
  {
    return "";
  }

  public void createMview( StatementBuilder p, MviewDiff pMviewDiff )
  {
    p.stmtStart( "create materialized view" );
    p.stmtAppend( pMviewDiff.mview_nameNew );

    if( pMviewDiff.buildModeNew == BuildModeType.PREBUILT )
    {
      p.stmtAppend( "on prebuilt table" );
    }
    else
    {
      // Physical properties nur, wenn nicht prebuilt
      if( pMviewDiff.tablespaceNew != null )
      {
        p.stmtAppend( "tablespace" );
        p.stmtAppend( pMviewDiff.tablespaceNew );
      }

      handleCompression( p, pMviewDiff.compressionNew, pMviewDiff.compressionForNew, false );

      handleParallel( p, pMviewDiff.parallelNew, pMviewDiff.parallel_degreeNew, false );

      if( pMviewDiff.buildModeNew != null )
      {
        p.stmtAppend( "build" );
        p.stmtAppend( pMviewDiff.buildModeNew.getLiteral() );
      }
    }

    if( pMviewDiff.refreshMethodNew != null )
    {
      p.stmtAppend( adjustRefreshmethodLiteral( pMviewDiff.refreshMethodNew.getLiteral() ) );

      if( pMviewDiff.refreshModeNew != null )
      {
        p.stmtAppend( "on" );
        p.stmtAppend( pMviewDiff.refreshModeNew.getLiteral() );
      }
    }

    if( pMviewDiff.queryRewriteNew == EnableType.ENABLE )
    {
      p.stmtAppend( "enable query rewrite" );
    }

    p.stmtAppend( "as" );
    p.stmtAppend( pMviewDiff.viewSelectCLOBNew );
    p.stmtDone();
  }

  private <T> T nvl( T pObject, T pDefault )
  {
    return pObject == null ? pDefault : pObject;
  }

  public void alterTableIfNeeded( StatementBuilderAlter p1, TableDiff pTableDiff, boolean pTablemovetablespace, String pDefaultTablespace )
  {
    p1.handleAlterBuilder()//
    .ifDifferent( TABLE__TABLESPACE, pTablemovetablespace )//
    .ignoreIfAdditionsOnly()//
    .handle( p ->
    {
      p.stmtStartAlterTable( pTableDiff );
      p.stmtAppend( "move tablespace" );
      p.stmtAppend( nvl( pTableDiff.tablespaceNew, pDefaultTablespace ) );
      p.stmtDone();
    } );

    p1.handleAlterBuilder()//
    .ifDifferent( TABLE__LOGGING, pTableDiff.transactionControlNew == null )//
    .ignoreIfAdditionsOnly()//
    .handle( p ->
    {
      p.stmtStartAlterTable( pTableDiff );
      if( pTableDiff.loggingNew == LoggingType.NOLOGGING )
      {
        p.stmtAppend( "nologging" );
      }
      else
      {
        p.stmtAppend( "logging" );
      }
      p.stmtDone();
    } );

    p1.handleAlterBuilder()//
    .ifDifferent( TABLE__PARALLEL )//
    .ifDifferent( TABLE__PARALLEL_DEGREE )//
    .ignoreIfAdditionsOnly()//
    .handle( p ->
    {
      p.stmtStartAlterTable( pTableDiff );

      handleParallel( p, pTableDiff.parallelNew, pTableDiff.parallel_degreeNew, true );

      p.stmtDone();
    } );

    p1.handleAlterBuilder()//
    .ifDifferent( TABLE__COMPRESSION, pTableDiff.permanentnessNew != PermanentnessType.GLOBAL_TEMPORARY )//
    .ifDifferent( TABLE__COMPRESSION_FOR, pTableDiff.permanentnessNew != PermanentnessType.GLOBAL_TEMPORARY )//
    .ignoreIfAdditionsOnly()//
    .handle( p ->
    {
      p.stmtStartAlterTable( pTableDiff );

      handleCompression( p, pTableDiff.compressionNew, pTableDiff.compressionForNew, true );

      p.stmtDone();
    } );
  }

  private void handleParallel( AbstractStatementBuilder p, ParallelType pParallelType, Integer pParallelDegree, boolean pWithDefault )
  {
    if( pParallelType == ParallelType.PARALLEL )
    {
      p.stmtAppend( "parallel" );
      if( pParallelDegree != null && pParallelDegree > 1 )
      {
        p.stmtAppend( "" + pParallelDegree );
      }
    }
    else
    {
      if( pWithDefault || pParallelType == ParallelType.NOPARALLEL )
      {
        p.stmtAppend( "noparallel" );
      }
    }
  }

  private void handleCompression( AbstractStatementBuilder p, CompressType pCompressionType, CompressForType pCompressForType, boolean pWithDefault )
  {
    if( pCompressionType == CompressType.COMPRESS )
    {
      p.stmtAppend( "compress" );

      if( pCompressForType == CompressForType.ALL )
      {
        p.stmtAppend( "for all operations" );
      }
      if( pCompressForType == CompressForType.DIRECT_LOAD )
      {
        p.stmtAppend( "for direct_load operations" );
      }
      if( pCompressForType == CompressForType.QUERY_LOW )
      {
        p.stmtAppend( "for query low" );
      }
      if( pCompressForType == CompressForType.QUERY_HIGH )
      {
        p.stmtAppend( "for query high" );
      }
      if( pCompressForType == CompressForType.ARCHIVE_LOW )
      {
        p.stmtAppend( "for archive low" );
      }
      if( pCompressForType == CompressForType.ARCHIVE_HIGH )
      {
        p.stmtAppend( "for archive high" );
      }
    }
    else
    {
      if( pWithDefault || pCompressionType == CompressType.NOCOMPRESS )
      {
        p.stmtAppend( "nocompress" );
      }
    }
  }

  private String adjustRefreshmethodLiteral( String pLiteral )
  {
    return pLiteral.replace( "refresh_", " refresh " ).replace( "never_", "never " );
  }

  private String createForeignKeyClause( ForeignKeyDiff pForeignKeyDiff )
  {
    String lReturn;

    lReturn = "constraint " + pForeignKeyDiff.consNameNew + " foreign key (" + getColumnList( pForeignKeyDiff.srcColumnsDiff ) + ") references " + pForeignKeyDiff.destTableNew + "(" + getColumnList( pForeignKeyDiff.destColumnsDiff ) + ")";

    if( pForeignKeyDiff.delete_ruleNew != null )
    {
      if( pForeignKeyDiff.delete_ruleNew == FkDeleteRuleType.CASCADE )
      {
        lReturn = lReturn + " on delete cascade";
      }
      if( pForeignKeyDiff.delete_ruleNew == FkDeleteRuleType.SET_NULL )
      {
        lReturn = lReturn + " on delete set null";
      }
    }

    if( pForeignKeyDiff.deferrtypeNew != null )
    {
      lReturn = lReturn + " deferrable initially  " + pForeignKeyDiff.deferrtypeNew.getName();
    }

    return lReturn;
  }

  public void createForeignKey( StatementBuilder p, TableDiff pTableDiff, ForeignKeyDiff pForeignKeyDiff, boolean pIsMultiSchema, DataHandler pDataHandler )
  {
    String lFkFalseDataSelect;
    String lFkFalseDataWherePart;

    if( parameters.isCleanupFkValuesOnDropmode() && pTableDiff.isOld )
    {
      lFkFalseDataWherePart = null;
      for( ColumnRefDiff lColumnRefDiffSrc : pForeignKeyDiff.srcColumnsDiff )
      {
        if( lFkFalseDataWherePart != null )
        {
          lFkFalseDataWherePart = lFkFalseDataWherePart + " or ";
        }
        else
        {
          lFkFalseDataWherePart = "";
        }
        lFkFalseDataWherePart = lFkFalseDataWherePart + lColumnRefDiffSrc.column_nameNew + " is not null ";
      }

      lFkFalseDataWherePart = "where (" + lFkFalseDataWherePart + ") and (" + getColumnList( pForeignKeyDiff.srcColumnsDiff ) + ") not in (select " + getColumnList( pForeignKeyDiff.destColumnsDiff ) + "  from " + pForeignKeyDiff.destTableNew + ")";
      lFkFalseDataSelect = "select 1 from " + pTableDiff.nameNew + " " + lFkFalseDataWherePart;

      if( pDataHandler.hasRowsIgnoreExceptions( lFkFalseDataSelect ) )
      {
        if( pForeignKeyDiff.delete_ruleNew == FkDeleteRuleType.CASCADE )
        {
          p.addStmt( "delete " + pTableDiff.nameNew + " " + lFkFalseDataWherePart );
          p.addStmt( "commit" );
        }
        else
        {
          if( pForeignKeyDiff.delete_ruleNew == FkDeleteRuleType.SET_NULL )
          {
            p.addStmt( "update " + pTableDiff.nameNew + " set " + getColumnList( pForeignKeyDiff.srcColumnsDiff ) + " = null " + lFkFalseDataWherePart );
            p.addStmt( "commit" );
          }
          else
          {
            throw new RuntimeException( "error FK rebuild " + pForeignKeyDiff.consNameNew + " on table " + pTableDiff.nameNew + " data-cleaning not possible , missing delete rule. " + lFkFalseDataSelect );
          }
        }
      }
    }

    if( pIsMultiSchema )
    {
      if( !getSchemaName( pTableDiff.nameNew ).equals( getSchemaName( pForeignKeyDiff.destTableNew ) ) )
      {
        p.addStmt( "grant references on " + pForeignKeyDiff.destTableNew + " to " + getSchemaName( pTableDiff.nameNew ) );
      }
    }

    p.stmtStartAlterTable( pTableDiff );
    p.stmtAppend( "add" );
    p.stmtAppend( createForeignKeyClause( pForeignKeyDiff ) );
    p.stmtDone( pTableDiff.isOld && !isAllColumnsOnlyNew( pTableDiff, pForeignKeyDiff.srcColumnsDiff ) );
  }

  private String getSchemaName( String pName )
  {
    if( pName.indexOf( '.' ) < 0 )
    {
      return "";
    }

    return pName.substring( 0, pName.indexOf( '.' ) );
  }

  protected String getColumnList( List<ColumnRefDiff> pColumnRefDiffList )
  {
    String lReturn = null;
    for( ColumnRefDiff lColumnRefDiff : pColumnRefDiffList )
    {
      if( lColumnRefDiff.isNew )
      {
        if( lReturn != null )
        {
          lReturn = lReturn + ",";
        }
        else
        {
          lReturn = "";
        }

        lReturn = lReturn + lColumnRefDiff.column_nameNew;
      }
    }

    return lReturn;
  }

  protected String getColumnDatatype( ColumnDiff pColumnDiff )
  {
    String lDatatype = "";
    if( pColumnDiff.data_typeNew == DataType.OBJECT )
    {
      lDatatype = pColumnDiff.object_typeNew;
    }
    else
    {
      if( pColumnDiff.data_typeNew == DataType.LONG_RAW )
      {
        lDatatype = "long raw";
      }
      else
      {
        lDatatype = getDatatypeName( pColumnDiff.data_typeNew );
      }

      if( pColumnDiff.precisionNew != null )
      {
        lDatatype = lDatatype + "(" + pColumnDiff.precisionNew;

        if( pColumnDiff.scaleNew != null )
        {
          lDatatype = lDatatype + "," + pColumnDiff.scaleNew;
        }

        if( pColumnDiff.byteorcharNew != null )
        {
          lDatatype = lDatatype + " " + pColumnDiff.byteorcharNew.getName().toUpperCase();
        }

        lDatatype = lDatatype + ")";
      }

      if( isNumericDatatypeUnsignedSupported() ) {
        if (pColumnDiff.unsignedNew) {
          lDatatype = lDatatype + " unsigned";
        }
      }

      if( "with_time_zone".equals( pColumnDiff.with_time_zoneNew ) )
      {
        lDatatype = lDatatype + " with time zone";
      }
    }

    return lDatatype;
  }

  protected boolean isNumericDatatypeUnsignedSupported()
  {
    return false;
  }

  protected String getDatatypeName( DataType pData_typeNew )
  {
    return pData_typeNew.name().toUpperCase();
  }

  public void createColumn( StatementBuilder p, TableDiff pTableDiff, ColumnDiff pColumnDiff )
  {
    createColumns( p, pTableDiff, Collections.singletonList( pColumnDiff ) );
  }

  private boolean isOnOldTableWothNotNullAndNoDefault( TableDiff pTableDiff, ColumnDiff pColumnDiff )
  {
    return pColumnDiff.notnullNew && pTableDiff.isOld && pColumnDiff.default_valueNew == null;
  }

  public void createColumns( StatementBuilder p, TableDiff pTableDiff, List<ColumnDiff> pColumnDiffList )
  {
    prepareCreateColumn( p, pTableDiff, pColumnDiffList, false );

    boolean lIsAnyOnOldTableWothNotNullAndNoDefault = pColumnDiffList.stream()//
    .filter( pColumnDiff -> isOnOldTableWothNotNullAndNoDefault( pTableDiff, pColumnDiff ) )//
    .findAny()//
    .isPresent();

    if( lIsAnyOnOldTableWothNotNullAndNoDefault )
    {
      p.stmtDone( () ->
      {
        prepareCreateColumn( p, pTableDiff, pColumnDiffList, true );
        p.stmtDone( StatementBuilder.ADDITIONSONLY_ALTERNATIVE_COMMENT );
      } );
    }
    else
    {
      p.stmtDone();
    }
  }

  private void prepareCreateColumn( StatementBuilder p, TableDiff pTableDiff, List<ColumnDiff> pColumnDiffList, boolean pForAdditionsOnlyMode )
  {
    p.stmtStartAlterTable( pTableDiff );
    p.stmtAppend( "add" );
    if( pColumnDiffList.size() > 1 )
    {
      p.stmtAppend( "(" );
    }

    boolean[] lIsFirst = new boolean[] { true };

    pColumnDiffList.forEach( pColumnDiff ->
    {
      if( lIsFirst[0] )
      {
        lIsFirst[0] = false;
      }
      else
      {
        p.stmtAppend( "," );
      }

      p.stmtAppend( createColumnCreatePart( pColumnDiff, pForAdditionsOnlyMode ? isOnOldTableWothNotNullAndNoDefault( pTableDiff, pColumnDiff ) : false ) );

      LobStorageDiff lLobstorage = findLobstorage( pTableDiff, pColumnDiff.nameNew );
      if( lLobstorage != null )
      {
        addLobStorage( p, lLobstorage );
      }

      VarrayStorageDiff lVarraystorage = findVarraystorage( pTableDiff, pColumnDiff.nameNew );
      if( lVarraystorage != null )
      {
        addVarrayStorage( p, lVarraystorage );
      }
    } );

    if( pColumnDiffList.size() > 1 )
    {
      p.stmtAppend( ")" );
    }
  }

  private void addVarrayStorage( StatementBuilder p, VarrayStorageDiff pVarraystorage )
  {
    if( pVarraystorage.lobStorageTypeNew != null || isLobStorageParametersDiffNotEmpty( pVarraystorage.lobStorageParametersDiff ) )
    {
      p.stmtAppend( "varray " + pVarraystorage.column_nameNew + " store as" );
      if( pVarraystorage.lobStorageTypeNew != null )
      {
        p.stmtAppend( pVarraystorage.lobStorageTypeNew.getLiteral() );
      }
      p.stmtAppend( "lob" );
      addLobStorageParameters( p, pVarraystorage.lobStorageParametersDiff );
    }
  }

  private void addLobStorageParameters( StatementBuilder p, LobStorageParametersDiff pLobStorageParametersDiff )
  {
    if( isLobStorageParametersDiffNotEmpty( pLobStorageParametersDiff ) )
    {
      p.stmtAppend( "(" );
      if( pLobStorageParametersDiff.tablespaceNew != null )
      {
        p.stmtAppend( "tablespace " + pLobStorageParametersDiff.tablespaceNew );
      }
      if( pLobStorageParametersDiff.lobDeduplicateTypeNew != null )
      {
        p.stmtAppend( pLobStorageParametersDiff.lobDeduplicateTypeNew.getLiteral() );
      }
      if( pLobStorageParametersDiff.compressTypeNew != null )
      {
        p.stmtAppend( pLobStorageParametersDiff.compressTypeNew.getLiteral() );

        if( pLobStorageParametersDiff.lobCompressForTypeNew != null )
        {
          p.stmtAppend( pLobStorageParametersDiff.lobCompressForTypeNew.getLiteral() );
        }
      }
      p.stmtAppend( ")" );
    }
  }

  private boolean isLobStorageParametersDiffNotEmpty( LobStorageParametersDiff pLobStorageParametersDiff )
  {
    return pLobStorageParametersDiff.tablespaceNew != null || pLobStorageParametersDiff.lobDeduplicateTypeNew != null || pLobStorageParametersDiff.compressTypeNew != null;
  }

  private void addLobStorage( StatementBuilder p, LobStorageDiff pLobstorage )
  {
    if( pLobstorage.lobStorageTypeNew != null || isLobStorageParametersDiffNotEmpty( pLobstorage.lobStorageParametersDiff ) )
    {
      p.stmtAppend( "lob (" + pLobstorage.column_nameNew + ") store as" );
      if( pLobstorage.lobStorageTypeNew != null )
      {
        p.stmtAppend( pLobstorage.lobStorageTypeNew.getLiteral() );
      }
      addLobStorageParameters( p, pLobstorage.lobStorageParametersDiff );
    }
  }

  protected String createColumnCreatePart( ColumnDiff pColumnDiff, boolean pWithoutNotNull )
  {
    String lReturn = pColumnDiff.nameNew + " " + getColumnDatatype( pColumnDiff );

    if( pColumnDiff.default_valueNew != null )
    {
      lReturn = lReturn + " default " + pColumnDiff.default_valueNew;
    }

    if( pColumnDiff.identityDiff.isNew )
    {
      lReturn = lReturn + " generated";
      if( pColumnDiff.identityDiff.alwaysNew != null )
      {
        lReturn = lReturn + " always";
      }
      if( pColumnDiff.identityDiff.by_defaultNew != null )
      {
        lReturn = lReturn + " by default";
      }
      if( pColumnDiff.identityDiff.on_nullNew != null )
      {
        lReturn = lReturn + " on null";
      }
      lReturn = lReturn + " as identity";

      lReturn = lReturn + " (";

      if( pColumnDiff.identityDiff.increment_byNew != null && pColumnDiff.identityDiff.increment_byNew > 0 )
      {
        lReturn = lReturn + " increment by " + pColumnDiff.identityDiff.increment_byNew;
      }
      else
      {
        lReturn = lReturn + " increment by 1";
      }

      if( pColumnDiff.identityDiff.maxvalueNew != null && pColumnDiff.identityDiff.maxvalueNew > 0 )
      {
        lReturn = lReturn + " maxvalue " + pColumnDiff.identityDiff.maxvalueNew;
      }

      if( pColumnDiff.identityDiff.minvalueNew != null && pColumnDiff.identityDiff.minvalueNew > 0 )
      {
        lReturn = lReturn + " minvalue " + pColumnDiff.identityDiff.minvalueNew;
      }

      if( pColumnDiff.identityDiff.cycleNew != null )
      {
        lReturn = lReturn + " " + pColumnDiff.identityDiff.cycleNew.getLiteral();
      }

      if( pColumnDiff.identityDiff.cacheNew != null && pColumnDiff.identityDiff.cacheNew > 0 )
      {
        lReturn = lReturn + " cache " + pColumnDiff.identityDiff.cacheNew;
      }

      if( pColumnDiff.identityDiff.orderNew != null )
      {
        lReturn = lReturn + " " + pColumnDiff.identityDiff.orderNew.getLiteral();
      }

      lReturn = lReturn + " )";
    }

    if( pColumnDiff.notnullNew )
    {
      if( !pWithoutNotNull )
      {
        lReturn = lReturn + " not null";
      }
    }

    return lReturn;
  }

  public boolean isMultiCreatePossible( TableDiff pTableDiff, List<ColumnDiff> pCreateColumnDiffList )
  {
    return !pCreateColumnDiffList.stream().filter( p -> findVarraystorage( pTableDiff, p.nameNew ) != null ).findAny().isPresent();
  }

  public boolean isMultiDropPossible( TableDiff pTableDiff, List<ColumnDiff> pDropColumnDiffList, DataHandler pDataHandler )
  {
    return !pDropColumnDiffList.stream().filter( p -> !pDataHandler.isDropOk( getColumnDropCheckSelect( pTableDiff, p ) ) ).findAny().isPresent();
  }

  public boolean isContainsOnlyNewColumns( TableDiff pTableDiff, String pRuleOld )
  {
    List<String> lRuleParts = splitRuleParts( pRuleOld );

    return !pTableDiff.columnsDiff.stream()//
    .filter( p -> !p.isNew )//
    .filter( p -> lRuleParts.contains( p.nameOld ) )//
    .findAny()//
    .isPresent();
  }

  public void alterUniqueKeyIfNeeded( StatementBuilderAlter p1, TableDiff pTableDiff, UniqueKeyDiff pUniqueKeyDiff )
  {
    p1.handleAlterBuilder()//
    .ifDifferent( INDEX_OR_UNIQUE_KEY__CONS_NAME )//
    .handle( p ->
    {
      p.stmtStartAlterTableNoCombine( pTableDiff );
      p.stmtAppend( "rename constraint" );
      p.stmtAppend( pUniqueKeyDiff.consNameOld );
      p.stmtAppend( "to" );
      p.stmtAppend( pUniqueKeyDiff.consNameNew );
      p.stmtDone();

      // different name matching only allowed with implicit index names, the
      // index needs to be renamed as well
      renameUnderlyingIndex( p, pTableDiff, pUniqueKeyDiff.consNameOld, pUniqueKeyDiff.consNameNew );
    } );
  }

  public void alterForeignKeyIfNeeded( StatementBuilderAlter p1, TableDiff pTableDiff, ForeignKeyDiff pForeignKeyDiff, DataHandler pDataHandler )
  {
    p1.handleAlterBuilder()//
    .ifDifferent( FOREIGN_KEY__CONS_NAME )//
    .handle( p ->
    {
      p.stmtStartAlterTableNoCombine( pTableDiff );
      p.stmtAppend( "rename constraint" );
      p.stmtAppend( pForeignKeyDiff.consNameOld );
      p.stmtAppend( "to" );
      p.stmtAppend( pForeignKeyDiff.consNameNew );
      p.stmtDone();
    } );
  }

  public void alterPrimarykeyIfNeeded( StatementBuilderAlter p1, TableDiff pTableDiff )
  {
    p1.handleAlterBuilder()//
    .ifDifferent( PRIMARY_KEY__CONS_NAME )//
    .handle( p ->
    {
      p.stmtStartAlterTableNoCombine( pTableDiff );
      p.stmtAppend( "rename constraint" );
      p.stmtAppend( pTableDiff.primary_keyDiff.consNameOld );
      p.stmtAppend( "to" );
      p.stmtAppend( pTableDiff.primary_keyDiff.consNameNew );
      p.stmtDone();

      renameUnderlyingIndex( p, pTableDiff, pTableDiff.primary_keyDiff.consNameOld, pTableDiff.primary_keyDiff.consNameNew );
    } );
  }

  private void renameUnderlyingIndex( StatementBuilder p, TableDiff pTableDiff, String pIndexNameOld, String pIndexNameNew )
  {
    String lSchemaPrefix = "";

    int lIndexOf = pTableDiff.nameNew.indexOf( '.' );
    if( lIndexOf != -1 )
    {
      lSchemaPrefix = pTableDiff.nameNew.substring( 0, lIndexOf + 1 );
    }

    p.stmtStart( "alter index" );
    p.stmtAppend( lSchemaPrefix + pIndexNameOld );
    p.stmtAppend( "rename to" );
    p.stmtAppend( lSchemaPrefix + pIndexNameNew );
    p.stmtDone();
  }

  public void updateConstraintIfNeeded( StatementBuilderAlter p1, TableDiff pTableDiff, ConstraintDiff pConstraintDiff )
  {
    p1.handleAlterBuilder()//
    .ifDifferent( CONSTRAINT__CONS_NAME )//
    .handle( p ->
    {
      p.stmtStartAlterTableNoCombine( pTableDiff );
      p.stmtAppend( "rename constraint" );
      p.stmtAppend( pConstraintDiff.consNameOld );
      p.stmtAppend( "to" );
      p.stmtAppend( pConstraintDiff.consNameNew );
      p.stmtDone();
    } );
  }

  public void dropColumnIdentity( StatementBuilder pP, TableDiff pTableDiff, ColumnDiff pColumnDiff, ColumnIdentityDiff pIdentityDiff )
  {
  }
}
