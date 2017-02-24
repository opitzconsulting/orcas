package de.opitzconsulting.orcas.diff;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import de.opitzconsulting.orcas.orig.diff.ColumnDiff;
import de.opitzconsulting.orcas.orig.diff.ColumnRefDiff;
import de.opitzconsulting.orcas.orig.diff.ConstraintDiff;
import de.opitzconsulting.orcas.orig.diff.ForeignKeyDiff;
import de.opitzconsulting.orcas.orig.diff.HashPartitionDiff;
import de.opitzconsulting.orcas.orig.diff.HashPartitionsDiff;
import de.opitzconsulting.orcas.orig.diff.HashSubPartsDiff;
import de.opitzconsulting.orcas.orig.diff.HashSubSubPartDiff;
import de.opitzconsulting.orcas.orig.diff.IndexDiff;
import de.opitzconsulting.orcas.orig.diff.IndexOrUniqueKeyDiff;
import de.opitzconsulting.orcas.orig.diff.InlineCommentDiff;
import de.opitzconsulting.orcas.orig.diff.ListPartitionDiff;
import de.opitzconsulting.orcas.orig.diff.ListPartitionValueDiff;
import de.opitzconsulting.orcas.orig.diff.ListPartitionsDiff;
import de.opitzconsulting.orcas.orig.diff.ListSubPartDiff;
import de.opitzconsulting.orcas.orig.diff.ListSubPartsDiff;
import de.opitzconsulting.orcas.orig.diff.ListSubSubPartDiff;
import de.opitzconsulting.orcas.orig.diff.LobStorageDiff;
import de.opitzconsulting.orcas.orig.diff.ModelDiff;
import de.opitzconsulting.orcas.orig.diff.MviewDiff;
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
import de.opitzconsulting.orcas.sql.CallableStatementProvider;
import de.opitzconsulting.orcas.sql.WrapperReturnFirstValue;
import de.opitzconsulting.orcas.sql.WrapperReturnValueFromResultSet;
import de.opitzconsulting.origOrcasDsl.BuildModeType;
import de.opitzconsulting.origOrcasDsl.CompressForType;
import de.opitzconsulting.origOrcasDsl.CompressType;
import de.opitzconsulting.origOrcasDsl.DataType;
import de.opitzconsulting.origOrcasDsl.EnableType;
import de.opitzconsulting.origOrcasDsl.FkDeleteRuleType;
import de.opitzconsulting.origOrcasDsl.LoggingType;
import de.opitzconsulting.origOrcasDsl.Model;
import de.opitzconsulting.origOrcasDsl.NewValuesType;
import de.opitzconsulting.origOrcasDsl.ParallelType;
import de.opitzconsulting.origOrcasDsl.PermanentnessType;
import de.opitzconsulting.origOrcasDsl.RefreshModeType;
import de.opitzconsulting.origOrcasDsl.SynchronousType;

public class OrcasDiff extends AbstractStatementBuilder
{
  private CallableStatementProvider _callableStatementProvider;

  public OrcasDiff( CallableStatementProvider pCallableStatementProvider, Parameters pParameters )
  {
    _callableStatementProvider = pCallableStatementProvider;
    _parameters = pParameters;
  }

  private Parameters _parameters;

  private boolean isIndexRecreate( TableDiff pTableDiff, String pIndexname )
  {
    for( IndexOrUniqueKeyDiff lIndexOrUniqueKeyDiff : pTableDiff.ind_uksIndexDiff )
    {
      if( lIndexOrUniqueKeyDiff.consNameNew.equals( pIndexname ) )
      {
        return lIndexOrUniqueKeyDiff.isRecreateNeeded;
      }
    }

    throw new RuntimeException( "index not found: " + pIndexname + " " + pTableDiff.nameNew );
  }

  private boolean isRecreateColumn( ColumnDiff pColumnDiff )
  {
    if( pColumnDiff.data_typeNew != null && pColumnDiff.data_typeOld != null )
    {
      if( !pColumnDiff.data_typeIsEqual )
      {
        return true;
      }

      if( isLessTahnOrNull( pColumnDiff.precisionNew, pColumnDiff.precisionOld ) )
      {
        return true;
      }

      if( isLessTahnOrNull( pColumnDiff.scaleNew, pColumnDiff.scaleOld ) )
      {
        return true;
      }
    }

    if( !pColumnDiff.object_typeIsEqual )
    {
      return true;
    }

    return false;
  }

  private boolean isLessTahnOrNull( Integer pValue1, Integer pValue2 )
  {
    if( pValue1 == null && pValue2 == null )
    {
      return false;
    }

    if( pValue1 == null || pValue2 == null )
    {
      return true;
    }

    return pValue1 < pValue2;
  }

  private void updateIsRecreateNeeded( ModelDiff pModelDiff )
  {
    for( TableDiff lTableDiff : pModelDiff.model_elementsTableDiff )
    {
      if( lTableDiff.isMatched )
      {
        if( !lTableDiff.permanentnessIsEqual || !lTableDiff.transactionControlIsEqual )
        {
          lTableDiff.isRecreateNeeded = true;
        }

        for( ColumnDiff lColumnDiff : lTableDiff.columnsDiff )
        {
          if( lColumnDiff.isMatched )
          {
            if( isRecreateColumn( lColumnDiff ) )
            {
              lColumnDiff.isRecreateNeeded = true;
            }
          }
        }

        if( lTableDiff.primary_keyDiff.isMatched )
        {
          if( !lTableDiff.primary_keyDiff.consNameIsEqual //
              || !lTableDiff.primary_keyDiff.pk_columnsIsEqual || !lTableDiff.primary_keyDiff.reverseIsEqual || (!lTableDiff.primary_keyDiff.tablespaceIsEqual && isIndexmovetablespace()) )
          {
            lTableDiff.primary_keyDiff.isRecreateNeeded = true;
          }
        }

        for( IndexDiff lIndexDiff : lTableDiff.ind_uksIndexDiff )
        {
          if( lIndexDiff.isMatched )
          {
            if( ((!lIndexDiff.index_columnsIsEqual //
                  || !lIndexDiff.function_based_expressionIsEqual || !lIndexDiff.domain_index_expressionIsEqual)
            // domain index kann nicht abgeglichen werden                           
                 && lIndexDiff.domain_index_expressionNew == null) ||
                !lIndexDiff.uniqueIsEqual ||
                !lIndexDiff.bitmapIsEqual ||
                !lIndexDiff.globalIsEqual ||
                !lIndexDiff.compressionIsEqual )
            {
              lIndexDiff.isRecreateNeeded = true;
            }
          }
        }

        for( UniqueKeyDiff lUniqueKeyDiff : lTableDiff.ind_uksUniqueKeyDiff )
        {
          if( lUniqueKeyDiff.isMatched )
          {
            if( !lUniqueKeyDiff.uk_columnsIsEqual || //
                !lUniqueKeyDiff.indexnameIsEqual ||
                (!lUniqueKeyDiff.tablespaceIsEqual && isIndexmovetablespace()) )
            {
              lUniqueKeyDiff.isRecreateNeeded = true;
            }
            else
            {
              if( lUniqueKeyDiff.indexnameNew != null )
              {
                if( isIndexRecreate( lTableDiff, lUniqueKeyDiff.indexnameNew ) )
                {
                  lUniqueKeyDiff.isRecreateNeeded = true;
                }
              }
            }
          }
        }

        for( ConstraintDiff lConstraintDiff : lTableDiff.constraintsDiff )
        {
          if( lConstraintDiff.isMatched )
          {
            if( !lConstraintDiff.ruleIsEqual //
                || !lConstraintDiff.deferrtypeIsEqual )
            {
              lConstraintDiff.isRecreateNeeded = true;
            }
          }
        }

        for( ForeignKeyDiff lForeignKeyDiff : lTableDiff.foreign_keysDiff )
        {
          if( lForeignKeyDiff.isMatched )
          {
            if( !lForeignKeyDiff.isEqual )
            {
              lForeignKeyDiff.isRecreateNeeded = true;
            }
          }
        }

        if( !lTableDiff.mviewLogDiff.columnsIsEqual //
            || //
            ("rowid".equalsIgnoreCase( lTableDiff.mviewLogDiff.rowidNew ) //                               
             && //
             !lTableDiff.mviewLogDiff.primaryKeyIsEqual //
            ) //
            || //
            (lTableDiff.mviewLogDiff.rowidNew == null && //
             !"primary".equalsIgnoreCase( lTableDiff.mviewLogDiff.primaryKeyOld ) // 
            ) //
            || !lTableDiff.mviewLogDiff.rowidIsEqual //
            || !lTableDiff.mviewLogDiff.withSequenceIsEqual //
            || !lTableDiff.mviewLogDiff.commitScnIsEqual //
            || !lTableDiff.mviewLogDiff.tablespaceIsEqual )
        {
          lTableDiff.mviewLogDiff.isRecreateNeeded = true;
        }

        // these changes should by applied with alter statements, but it results in ORA-27476
        if( lTableDiff.mviewLogDiff.startWithIsEqual == false || lTableDiff.mviewLogDiff.nextIsEqual == false || lTableDiff.mviewLogDiff.repeatIntervalIsEqual == false )
        {
          lTableDiff.mviewLogDiff.isRecreateNeeded = true;
        }
      }
    }

    for( MviewDiff lMviewDiff : pModelDiff.model_elementsMviewDiff )
    {
      if( lMviewDiff.isMatched )
      {
        if( !lMviewDiff.tablespaceIsEqual //
            || !replaceLinefeedBySpace( lMviewDiff.viewSelectCLOBNew ).equals( replaceLinefeedBySpace( lMviewDiff.viewSelectCLOBOld ) ) //
            || !lMviewDiff.buildModeIsEqual )
        {
          lMviewDiff.isRecreateNeeded = true;
        }
      }
    }
  }

  private String replaceLinefeedBySpace( String pValue )
  {
    return pValue.replace( Character.toString( (char)13 ) + Character.toString( (char)10 ), " " ).replace( Character.toString( (char)10 ), " " ).replace( Character.toString( (char)13 ), " " );
  }

  private boolean isIndexmovetablespace()
  {
    return _parameters.isIndexmovetablespace();
  }

  public DiffResult compare( Model pModelSoll, Model pModelIst )
  {
    ModelDiff lModelDiff = new ModelDiff( pModelSoll );
    lModelDiff.mergeWithOldValue( pModelIst );

    sortTablesForRefPart( lModelDiff );

    updateIsRecreateNeeded( lModelDiff );

    handleAllTables( lModelDiff );

    handleAllSequences( lModelDiff );

    handleAllMviews( lModelDiff );

    return new DiffResult( getStmtList() );
  }

  public class DiffResult
  {
    private List<String> sqlStatements = new ArrayList<String>();

    public List<String> getSqlStatements()
    {
      return sqlStatements;
    }

    private DiffResult( List<String> pSqlStatements )
    {
      sqlStatements.addAll( pSqlStatements );
    }
  }

  private void handleSequence( SequenceDiff pSequenceDiff )
  {
    BigDecimal lSollStartValue = null;

    try
    {
      if( pSequenceDiff.max_value_selectNew != null )
      {
        lSollStartValue = (BigDecimal)new WrapperReturnFirstValue( pSequenceDiff.max_value_selectNew, getCallableStatementProvider() ).executeForValue();

        lSollStartValue = lSollStartValue.add( BigDecimal.valueOf( 1 ) );
      }
    }
    catch( Exception e )
    {
      //    kann vorkommen, wenn fuer das select benoetigte Tabellen nicht exisitieren. kann erst richtig korrigiert werden, wenn auch der Tabellenabgleich auf dieses Package umgestellt wurde      
    }

    if( pSequenceDiff.isMatched == false )
    {
      stmtStart( "create sequence " + pSequenceDiff.sequence_nameNew );
      if( pSequenceDiff.increment_byNew != null )
      {
        stmtAppend( "increment by " + pSequenceDiff.increment_byNew );
      }

      if( lSollStartValue != null )
      {
        stmtAppend( "start with " + lSollStartValue );
      }

      if( pSequenceDiff.maxvalueNew != null )
      {
        stmtAppend( "maxvalue " + pSequenceDiff.maxvalueNew );
      }

      if( pSequenceDiff.minvalueNew != null )
      {
        stmtAppend( "minvalue " + pSequenceDiff.minvalueNew );
      }

      if( pSequenceDiff.cycleNew != null )
      {
        stmtAppend( pSequenceDiff.cycleNew.getLiteral() );
      }

      if( pSequenceDiff.cacheNew != null )
      {
        stmtAppend( "cache " + pSequenceDiff.cacheNew );
      }

      if( pSequenceDiff.orderNew != null )
      {
        stmtAppend( pSequenceDiff.orderNew.getLiteral() );
      }

      stmtDone();
    }
    else
    {
      BigDecimal lIstValue = BigDecimal.valueOf( Long.valueOf( pSequenceDiff.max_value_selectOld ) );
      if( lSollStartValue != null && lIstValue != null && lSollStartValue.compareTo( lIstValue ) > 0 )
      {
        addStmt( "alter sequence " + pSequenceDiff.sequence_nameNew + " increment by " + (lSollStartValue.longValue() - lIstValue.longValue()) );
        addStmt( "declare\n v_dummy number;\n begin\n select " + pSequenceDiff.sequence_nameNew + ".nextval into v_dummy from dual;\n end;" );
        addStmt( "alter sequence " + pSequenceDiff.sequence_nameNew + " increment by " + nvl( pSequenceDiff.increment_byNew, 1 ) );
      }
      else
      {
        if( pSequenceDiff.increment_byIsEqual == false )
        {
          addStmt( "alter sequence " + pSequenceDiff.sequence_nameNew + " increment by " + nvl( pSequenceDiff.increment_byNew, 1 ) );
        }
      }

      if( pSequenceDiff.maxvalueIsEqual == false )
      {
        addStmt( "alter sequence " + pSequenceDiff.sequence_nameNew + " maxvalue " + pSequenceDiff.maxvalueNew );
      }

      if( pSequenceDiff.minvalueIsEqual == false )
      {
        addStmt( "alter sequence " + pSequenceDiff.sequence_nameNew + " minvalue " + nvl( pSequenceDiff.minvalueNew, 1 ) );
      }

      if( pSequenceDiff.cycleIsEqual == false )
      {
        addStmt( "alter sequence " + pSequenceDiff.sequence_nameNew + " " + pSequenceDiff.cycleNew.getLiteral() );
      }

      if( pSequenceDiff.cacheIsEqual == false )
      {
        addStmt( "alter sequence " + pSequenceDiff.sequence_nameNew + " cache " + nvl( pSequenceDiff.cacheNew, 20 ) );
      }

      if( pSequenceDiff.orderIsEqual == false )
      {
        addStmt( "alter sequence " + pSequenceDiff.sequence_nameNew + " " + pSequenceDiff.orderNew.getLiteral() );
      }
    }
  }

  private void handleAllSequences( ModelDiff pModelDiff )
  {
    for( SequenceDiff lSequenceDiff : pModelDiff.model_elementsSequenceDiff )
    {
      if( lSequenceDiff.isEqual == false )
      {
        if( lSequenceDiff.isNew == true )
        {
          handleSequence( lSequenceDiff );
        }
        else
        {
          addStmt( "drop sequence " + lSequenceDiff.sequence_nameOld );
        }
      }
    }
  }

  private void dropTableConstraintByName( String pTablename, String pCconstraintName )
  {
    addStmt( "alter table " + pTablename + " drop constraint " + pCconstraintName );
  }

  private void handleAllTables( ModelDiff pModelDiff )
  {
    for( TableDiff lTableDiff : pModelDiff.model_elementsTableDiff )
    {
      for( ForeignKeyDiff lForeignKeyDiff : lTableDiff.foreign_keysDiff )
      {
        if( lForeignKeyDiff.isOld == true && (lForeignKeyDiff.isMatched == false || lForeignKeyDiff.isRecreateNeeded == true) )
        {
          dropTableConstraintByName( lTableDiff.nameOld, lForeignKeyDiff.consNameOld );
        }
      }
    }

    for( TableDiff lTableDiff : pModelDiff.model_elementsTableDiff )
    {
      if( lTableDiff.isOld == true && (lTableDiff.isMatched == false || lTableDiff.isRecreateNeeded == true) )
      {
        dropWithDropmodeCheck( "select 1 from " + lTableDiff.nameOld, "drop table " + lTableDiff.nameOld );
      }
      else
      {
        for( ConstraintDiff lConstraintDiff : lTableDiff.constraintsDiff )
        {
          if( lConstraintDiff.isOld == true && (lConstraintDiff.isMatched == false || lConstraintDiff.isRecreateNeeded == true) )
          {
            dropTableConstraintByName( lTableDiff.nameOld, lConstraintDiff.consNameOld );
          }
        }

        if( lTableDiff.mviewLogDiff.isOld == true && (lTableDiff.mviewLogDiff.isMatched == false || lTableDiff.mviewLogDiff.isRecreateNeeded == true) )
        {
          addStmt( "drop materialized view log on " + lTableDiff.nameOld );
        }

        for( UniqueKeyDiff lUniqueKeyDiff : lTableDiff.ind_uksUniqueKeyDiff )
        {
          if( lUniqueKeyDiff.isOld == true && (lUniqueKeyDiff.isMatched == false || lUniqueKeyDiff.isRecreateNeeded == true) )
          {
            dropTableConstraintByName( lTableDiff.nameOld, lUniqueKeyDiff.consNameOld );
          }
        }

        for( IndexDiff lIndexDiff : lTableDiff.ind_uksIndexDiff )
        {
          if( lIndexDiff.isOld == true && (lIndexDiff.isMatched == false || lIndexDiff.isRecreateNeeded == true) )
          {
            addStmt( "drop index " + lIndexDiff.consNameOld );
          }
        }

        for( InlineCommentDiff lCommentDiff : lTableDiff.commentsDiff )
        {
          if( lCommentDiff.isOld == true && lCommentDiff.isMatched == false )
          {
            boolean lIsColumnComment = lCommentDiff.column_nameOld != null;
            if( !lIsColumnComment || columnIsNew( lTableDiff, lCommentDiff.column_nameOld ) )
            {
              stmtStart( "comment on" );
              stmtAppend( lCommentDiff.comment_objectOld.getName() );
              stmtAppend( " " );
              stmtAppend( lTableDiff.nameOld );
              if( lIsColumnComment )
              {
                stmtAppend( "." );
                stmtAppend( lCommentDiff.column_nameOld );
              }
              stmtAppend( "is" );
              stmtAppend( "''" );
              stmtDone();
            }
          }
        }

        if( lTableDiff.primary_keyDiff.isOld == true && (lTableDiff.primary_keyDiff.isMatched == false || lTableDiff.primary_keyDiff.isRecreateNeeded == true) )
        {
          dropTableConstraintByName( lTableDiff.nameOld, lTableDiff.primary_keyDiff.consNameOld );
        }
      }
    }

    for( TableDiff lTableDiff : pModelDiff.model_elementsTableDiff )
    {
      if( lTableDiff.isNew == true )
      {
        handleTable( lTableDiff );
      }
    }

    for( TableDiff lTableDiff : pModelDiff.model_elementsTableDiff )
    {
      for( ForeignKeyDiff lForeignKeyDiff : lTableDiff.foreign_keysDiff )
      {
        if( lForeignKeyDiff.isNew == true && (lForeignKeyDiff.isMatched == false || lForeignKeyDiff.isRecreateNeeded == true) )
        {
          if( lTableDiff.isOld == false && lTableDiff.tablePartitioningRefPartitionsDiff.isNew == true && lTableDiff.tablePartitioningRefPartitionsDiff.fkNameNew.equalsIgnoreCase( lForeignKeyDiff.consNameNew ) )
          {
            // fk for ref-partition created in create-table
          }
          else
          {
            createForeignKey( lTableDiff, lForeignKeyDiff );
          }
        }
      }
    }
  }

  private LobStorageDiff findLobstorage( TableDiff pTableDiff, String pColumnName )
  {
    for( LobStorageDiff lLobStorageDiff : pTableDiff.lobStoragesDiff )
    {
      if( lLobStorageDiff.column_nameNew.equals( pColumnName ) )
      {
        return lLobStorageDiff;
      }
    }

    return null;
  }

  private void handleTable( TableDiff pTableDiff )
  {
    if( pTableDiff.isMatched == true )
    {
      if( pTableDiff.tablespaceIsEqual == false && isTablemovetablespace() == true )
      {
        stmtStart( "alter table" );
        stmtAppend( pTableDiff.nameNew );
        stmtAppend( "move tablespace" );
        stmtAppend( nvl( pTableDiff.tablespaceNew, getDefaultTablespace() ) );
        stmtDone();
      }
    }

    if( pTableDiff.isMatched == false || pTableDiff.isRecreateNeeded == true )
    {
      createTable( pTableDiff );
    }
    else
    {
      for( ColumnDiff lColumnDiff : pTableDiff.columnsDiff )
      {
        if( lColumnDiff.isNew == true )
        {
          handleColumn( pTableDiff, lColumnDiff );
        }
        else
        {
          dropWithDropmodeCheck( "select 1 from " + pTableDiff.nameOld + " where " + lColumnDiff.nameOld + " != null", "alter table " + pTableDiff.nameOld + " drop column " + lColumnDiff.nameOld );
        }
      }

      if( pTableDiff.loggingIsEqual == false )
      {
        if( pTableDiff.transactionControlNew == null )
        {
          stmtStart( "alter table" );
          stmtAppend( pTableDiff.nameNew );
          if( pTableDiff.loggingNew == LoggingType.NOLOGGING )
          {
            stmtAppend( "nologging" );
          }
          else
          {
            stmtAppend( "logging" );
          }
          stmtDone();
        }
      }

      if( pTableDiff.parallelIsEqual == false || pTableDiff.parallel_degreeIsEqual == false )
      {
        stmtStart( "alter table" );
        stmtAppend( pTableDiff.nameNew );

        handleParallel( pTableDiff.parallelNew, pTableDiff.parallel_degreeNew, true );

        stmtDone();
      }

      if( pTableDiff.permanentnessNew != PermanentnessType.GLOBAL_TEMPORARY && (pTableDiff.compressionIsEqual == false || pTableDiff.compressionForIsEqual == false) )
      {
        stmtStart( "alter table" );
        stmtAppend( pTableDiff.nameNew );

        handleCompression( pTableDiff.compressionNew, pTableDiff.compressionForNew, true );

        stmtDone();
      }
    }

    if( pTableDiff.primary_keyDiff.isNew == true )
    {
      handlePrimarykey( pTableDiff );
    }

    for( ConstraintDiff lConstraintDiff : pTableDiff.constraintsDiff )
    {
      if( lConstraintDiff.isNew == true )
      {
        handleConstraint( pTableDiff.nameNew, lConstraintDiff );
      }
    }

    for( IndexDiff lIndexDiff : pTableDiff.ind_uksIndexDiff )
    {
      if( lIndexDiff.isNew == true )
      {
        handleIndex( pTableDiff.nameNew, lIndexDiff );
      }
    }

    for( UniqueKeyDiff lUniqueKeyDiff : pTableDiff.ind_uksUniqueKeyDiff )
    {
      if( lUniqueKeyDiff.isNew == true )
      {
        handleUniquekey( pTableDiff.nameNew, lUniqueKeyDiff );
      }
    }

    for( InlineCommentDiff lInlineCommentDiff : pTableDiff.commentsDiff )
    {
      handleComment( pTableDiff, lInlineCommentDiff );
    }

    if( pTableDiff.mviewLogDiff.isNew )
    {
      handleMviewlog( pTableDiff );
    }
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

      if( lColumnDiff.isNew == true )
      {
        lReturn = lReturn + " " + createColumnCreatePart( lColumnDiff );
      }
    }

    return lReturn;
  }

  private String createColumnStorageClause( List<LobStorageDiff> pLobStorageDiffList )
  {
    String lReturn = "";

    for( LobStorageDiff lLobStorageDiff : pLobStorageDiffList )
    {
      if( lLobStorageDiff.isNew == true )
      {
        lReturn = lReturn + " lob(" + lLobStorageDiff.column_nameNew + ") store as (tablespace " + lLobStorageDiff.tablespaceNew + ")";
      }
    }

    return lReturn;
  }

  private void createTable( TableDiff pTableDiff )
  {
    stmtStart( "create" );
    if( pTableDiff.permanentnessNew == PermanentnessType.GLOBAL_TEMPORARY )
    {
      stmtAppend( "global " + pTableDiff.permanentnessNew.getLiteral() );
    }
    stmtAppend( "table" );
    stmtAppend( pTableDiff.nameNew );
    stmtAppend( "(" );
    stmtAppend( createColumnClause( pTableDiff.columnsDiff ) );
    stmtAppend( createRefFkClause( pTableDiff ) );
    stmtAppend( ")" );
    if( pTableDiff.transactionControlNew != null )
    {
      stmtAppend( "on commit " );
      stmtAppend( pTableDiff.transactionControlNew.getLiteral() );
      stmtAppend( "rows nocache" );
    }
    else
    {
      stmtAppend( createColumnStorageClause( pTableDiff.lobStoragesDiff ) );
      if( pTableDiff.tablespaceNew != null )
      {
        stmtAppend( "tablespace" );
        stmtAppend( pTableDiff.tablespaceNew );
      }
      
      if( pTableDiff.permanentnessNew != PermanentnessType.GLOBAL_TEMPORARY )
      {
		  //add pctfree
		  if(pTableDiff.pctfreeNew != null) {
		      stmtAppend( "pctfree" );
		      stmtAppend( ""+pTableDiff.pctfreeNew );
		  }    	  
      }

      if( pTableDiff.permanentnessNew != PermanentnessType.GLOBAL_TEMPORARY )
      {
        if( pTableDiff.loggingNew == LoggingType.NOLOGGING )
        {
          stmtAppend( "nologging" );
        }
      }
      handleCompression( pTableDiff.compressionNew, pTableDiff.compressionForNew, false );
    }
    handleParallel( pTableDiff.parallelNew, pTableDiff.parallel_degreeNew, false );
    stmtAppend( createPartitioningClause( pTableDiff ) );

    stmtDone();
  }

  private void handleParallel( ParallelType pParallelType, Integer pParallelDegree, boolean pWithDefault )
  {
    if( pParallelType == ParallelType.PARALLEL )
    {
      stmtAppend( "parallel" );
      if( pParallelDegree != null && pParallelDegree > 1 )
      {
        stmtAppend( "" + pParallelDegree );
      }
    }
    else
    {
      if( pWithDefault || pParallelType == ParallelType.NOPARALLEL )
      {
        stmtAppend( "noparallel" );
      }
    }
  }

  private void handleCompression( CompressType pCompressionType, CompressForType pCompressForType, boolean pWithDefault )
  {
    if( pCompressionType == CompressType.COMPRESS )
    {
      stmtAppend( "compress" );

      if( pCompressForType == CompressForType.ALL )
      {
        stmtAppend( "for all operations" );
      }
      if( pCompressForType == CompressForType.DIRECT_LOAD )
      {
        stmtAppend( "for direct_load operations" );
      }
      if( pCompressForType == CompressForType.QUERY_LOW )
      {
        stmtAppend( "for query low" );
      }
      if( pCompressForType == CompressForType.QUERY_HIGH )
      {
        stmtAppend( "for query high" );
      }
      if( pCompressForType == CompressForType.ARCHIVE_LOW )
      {
        stmtAppend( "for archive low" );
      }
      if( pCompressForType == CompressForType.ARCHIVE_HIGH )
      {
        stmtAppend( "for archive high" );
      }
    }
    else
    {
      if( pWithDefault || pCompressionType == CompressType.NOCOMPRESS )
      {
        stmtAppend( "nocompress" );
      }
    }
  }

  private void createForeignKey( TableDiff pTableDiff, ForeignKeyDiff pForeignKeyDiff )
  {
    String lFkFalseDataSelect;
    String lFkFalseDataWherePart;

    if( isDropmode() == true && pTableDiff.isOld == true )
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

      if( hasRowsIgnoreExceptions( lFkFalseDataSelect ) == true )
      {
        if( pForeignKeyDiff.delete_ruleNew == FkDeleteRuleType.CASCADE )
        {
          addStmt( "delete " + pTableDiff.nameNew + " " + lFkFalseDataWherePart );
          addStmt( "commit" );
        }
        else
        {
          if( pForeignKeyDiff.delete_ruleNew == FkDeleteRuleType.SET_NULL )
          {
            addStmt( "update " + pTableDiff.nameNew + " set " + getColumnList( pForeignKeyDiff.srcColumnsDiff ) + " = null " + lFkFalseDataWherePart );
            addStmt( "commit" );
          }
          else
          {
            throw new RuntimeException( "error FK rebuild " + pForeignKeyDiff.consNameNew + " on table " + pTableDiff.nameNew + " data-cleaning not possible , missing delete rule. " + lFkFalseDataSelect );
          }
        }
      }
    }

    if( _parameters.getMultiSchema() )
    {
      if( !getSchemaName( pTableDiff.nameNew ).equals( getSchemaName( pForeignKeyDiff.destTableNew ) ) )
      {
        addStmt( "grant references on " + pForeignKeyDiff.destTableNew + " to " + getSchemaName( pTableDiff.nameNew ) );
      }
    }

    stmtStart( "alter table " + pTableDiff.nameNew );
    stmtAppend( "add" );
    stmtAppend( createForeignKeyClause( pForeignKeyDiff ) );
    stmtDone();
  }

  private String getSchemaName( String pName )
  {
    if( pName.indexOf( '.' ) < 0 )
    {
      return "";
    }
    
    return pName.substring( 0, pName.indexOf( '.' ) );
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

  private void handleComment( TableDiff pTableDiff, InlineCommentDiff pInlineCommentDiff )
  {
    if( pInlineCommentDiff.isEqual == false )
    {
      if( pInlineCommentDiff.isNew == true )
      {
        stmtStart( "comment on" );
        stmtAppend( pInlineCommentDiff.comment_objectNew.getName() );
        stmtAppend( " " );
        stmtAppend( pTableDiff.nameNew );
        if( pInlineCommentDiff.column_nameNew != null )
        {
          stmtAppend( "." );
          stmtAppend( pInlineCommentDiff.column_nameNew );
        }
        stmtAppend( "is" );
        stmtAppend( "'" + pInlineCommentDiff.commentNew.replace( "'", "''" ) + "'" );
        stmtDone();
      }
    }
  }

  private boolean columnIsNew( TableDiff pTableDiff, String pColumnName )
  {
    for( ColumnDiff lColumnDiff : pTableDiff.columnsDiff )
    {
      if( lColumnDiff.isNew )
      {
        if( lColumnDiff.nameNew.equals( pColumnName ) )
        {
          return true;
        }
      }
    }

    return false;
  }

  private void handleUniquekey( String pTablename, UniqueKeyDiff pUniqueKeyDiff )
  {
    if( pUniqueKeyDiff.isMatched == false || pUniqueKeyDiff.isRecreateNeeded == true )
    {
      stmtStart( "alter table " + pTablename + " add constraint " + pUniqueKeyDiff.consNameNew + " unique (" + getColumnList( pUniqueKeyDiff.uk_columnsDiff ) + ")" );
      if( pUniqueKeyDiff.tablespaceNew != null )
      {
        stmtAppend( "using index tablespace " + pUniqueKeyDiff.tablespaceNew );
      }
      else
      {
        if( pUniqueKeyDiff.indexnameNew != null && !pUniqueKeyDiff.indexnameNew.equals( pUniqueKeyDiff.consNameNew ) )
        {
          stmtAppend( "using index " + pUniqueKeyDiff.indexnameNew );
        }
      }
      if( pUniqueKeyDiff.statusNew != null )
      {
        stmtAppend( pUniqueKeyDiff.statusNew.getName() );
      }

      stmtDone();
    }
  }

  private void handleIndex( String pTablename, IndexDiff pIndexDiff )
  {
    if( pIndexDiff.isMatched == false || pIndexDiff.isRecreateNeeded == true )
    {
      stmtStart( "create" );
      if( pIndexDiff.uniqueNew != null )
      {
        stmtAppend( pIndexDiff.uniqueNew );
      }
      if( pIndexDiff.bitmapNew != null )
      {
        stmtAppend( "bitmap" );
      }
      stmtAppend( "index" );
      stmtAppend( pIndexDiff.consNameNew );
      stmtAppend( "on" );
      stmtAppend( pTablename );
      stmtAppend( "(" );
      if( pIndexDiff.function_based_expressionNew != null )
      {
        stmtAppend( pIndexDiff.function_based_expressionNew );
      }
      else
      {
        stmtAppend( getColumnList( pIndexDiff.index_columnsDiff ) );
      }
      stmtAppend( ")" );
      if( pIndexDiff.domain_index_expressionNew != null )
      {
        stmtAppend( pIndexDiff.domain_index_expressionNew );
      }
      else
      {
        if( pIndexDiff.loggingNew != null )
        {
          stmtAppend( pIndexDiff.loggingNew.getLiteral() );
        }
      }
      if( pIndexDiff.tablespaceNew != null )
      {
        stmtAppend( "tablespace" );
        stmtAppend( pIndexDiff.tablespaceNew );
      }
      if( pIndexDiff.globalNew != null )
      {
        stmtAppend( pIndexDiff.globalNew.getLiteral() );
      }
      if( pIndexDiff.bitmapNew == null && pIndexDiff.compressionNew == CompressType.COMPRESS )
      {
        stmtAppend( "compress" );
      }
      if( pIndexDiff.compressionNew == CompressType.NOCOMPRESS )
      {
        stmtAppend( "nocompress" );
      }

      if( pIndexDiff.parallelNew == ParallelType.PARALLEL || isIndexparallelcreate() )
      {
        stmtAppend( "parallel" );
        if( pIndexDiff.parallel_degreeNew != null && pIndexDiff.parallel_degreeNew > 1 )
        {
          stmtAppend( " " + pIndexDiff.parallel_degreeNew );
        }
      }

      stmtDone();

      if( pIndexDiff.parallelNew != ParallelType.PARALLEL && isIndexparallelcreate() )
      {
        addStmt( "alter index " + pIndexDiff.consNameNew + " noparallel" );
      }
    }
    else
    {
      if( pIndexDiff.parallelIsEqual == false || pIndexDiff.parallel_degreeIsEqual == false )
      {
        stmtStart( "alter index" );
        stmtAppend( pIndexDiff.consNameNew );
        if( pIndexDiff.parallelNew == ParallelType.PARALLEL )
        {
          stmtAppend( "parallel" );
          if( pIndexDiff.parallel_degreeNew != null && pIndexDiff.parallel_degreeNew > 1 )
          {
            stmtAppend( " " + pIndexDiff.parallel_degreeNew );
          }
        }
        else
        {
          stmtAppend( "noparallel" );
        }

        stmtDone();
      }

      if( pIndexDiff.loggingIsEqual == false )
      {
        stmtStart( "alter index" );
        stmtAppend( pIndexDiff.consNameNew );
        if( pIndexDiff.loggingNew == LoggingType.NOLOGGING )
        {
          stmtAppend( "nologging" );
        }
        else
        {
          stmtAppend( "logging" );
        }

        stmtDone();
      }

      if( pIndexDiff.tablespaceIsEqual == false && !(pIndexDiff.tablespaceOld == null && pIndexDiff.tablespaceNew == null) && isIndexmovetablespace() == true )
      {
        stmtStart( "alter index" );
        stmtAppend( pIndexDiff.consNameNew );
        stmtAppend( "rebuild tablespace" );
        stmtAppend( nvl( pIndexDiff.tablespaceNew, getDefaultTablespace() ) );
        stmtDone();
      }
    }
  }

  private String getDefaultTablespace()
  {
    return InitDiffRepository.getDefaultTablespace();
  }

  private boolean isIndexparallelcreate()
  {
    return _parameters.isIndexparallelcreate();
  }

  private void handleConstraint( String pTablename, ConstraintDiff pConstraintDiff )
  {
    if( pConstraintDiff.isMatched == false || pConstraintDiff.isRecreateNeeded == true )
    {
      stmtStart( "alter table " + pTablename + " add constraint " + pConstraintDiff.consNameNew + " check (" + pConstraintDiff.ruleNew + ")" );
      if( pConstraintDiff.deferrtypeNew != null )
      {
        stmtAppend( "deferrable initially " + pConstraintDiff.deferrtypeNew.getName() );
      }
      if( pConstraintDiff.statusNew != null )
      {
        stmtAppend( pConstraintDiff.statusNew.getName() );
      }

      stmtDone();
    }
  }

  private void handlePrimarykey( TableDiff pTableDiff )
  {
    if( pTableDiff.primary_keyDiff.isMatched == false || pTableDiff.primary_keyDiff.isRecreateNeeded == true )
    {
      stmtStart( "alter table " + pTableDiff.nameNew + " add" );
      if( pTableDiff.primary_keyDiff.consNameNew != null )
      {
        stmtAppend( "constraint " + pTableDiff.primary_keyDiff.consNameNew );
      }
      stmtAppend( "primary key (" + getColumnList( pTableDiff.primary_keyDiff.pk_columnsDiff ) + ")" );

      if( pTableDiff.primary_keyDiff.tablespaceNew != null || pTableDiff.primary_keyDiff.reverseNew != null )
      {
        stmtAppend( "using index" );

        if( pTableDiff.primary_keyDiff.reverseNew != null )
        {
          stmtAppend( "reverse" );
        }

        if( pTableDiff.primary_keyDiff.tablespaceNew != null )
        {
          stmtAppend( "tablespace " + pTableDiff.primary_keyDiff.tablespaceNew );
        }
      }

      stmtDone();
    }
  }

  private String getColumnList( List<ColumnRefDiff> pColumnRefDiffList )
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

  private String getColumnDatatype( ColumnDiff pColumnDiff )
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
        lDatatype = pColumnDiff.data_typeNew.name().toUpperCase();
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

      if( "with_time_zone".equals( pColumnDiff.with_time_zoneNew ) )
      {
        lDatatype = lDatatype + " with time zone";
      }
    }

    return lDatatype;
  }

  private String createColumnCreatePart( ColumnDiff pColumnDiff )
  {
    String lReturn = pColumnDiff.nameNew + " " + getColumnDatatype( pColumnDiff );

    if( pColumnDiff.default_valueNew != null )
    {
      lReturn = lReturn + " default " + pColumnDiff.default_valueNew;
    }

    if( pColumnDiff.identityDiff.isNew == true )
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

    if( pColumnDiff.notnullNew == true )
    {
      lReturn = lReturn + " not null";
    }

    return lReturn;
  }

  private void handleColumn( TableDiff pTableDiff, ColumnDiff pColumnDiff )
  {

    if( pColumnDiff.isMatched == false )
    {
      stmtStart( "alter table " + pTableDiff.nameNew + " add " + createColumnCreatePart( pColumnDiff ) );

      if( findLobstorage( pTableDiff, pColumnDiff.nameNew ) != null )
      {
        stmtAppend( "lob (" + pColumnDiff.nameNew + ") store as ( tablespace " + findLobstorage( pTableDiff, pColumnDiff.nameNew ).tablespaceNew + " )" );
      }

      stmtDone();
    }
    else
    {
      if( pColumnDiff.isRecreateNeeded == true )
      {
        recreateColumn( pTableDiff, pColumnDiff );
      }
      else
      {
        if( pColumnDiff.byteorcharIsEqual == false || pColumnDiff.precisionIsEqual == false || pColumnDiff.scaleIsEqual == false )
        {
          addStmt( "alter table " + pTableDiff.nameNew + " modify ( " + pColumnDiff.nameNew + " " + getColumnDatatype( pColumnDiff ) + ")" );
        }

        if( pColumnDiff.default_valueIsEqual == false )
        {
          stmtStart( "alter table " + pTableDiff.nameNew + " modify ( " + pColumnDiff.nameNew + " default" );
          if( pColumnDiff.default_valueNew == null )
          {
            stmtAppend( "null" );
          }
          else
          {
            stmtAppend( pColumnDiff.default_valueNew );
          }
          stmtAppend( ")" );
          stmtDone();
        }

        if( pColumnDiff.notnullIsEqual == false )
        {
          stmtStart( "alter table " + pTableDiff.nameNew + " modify ( " + pColumnDiff.nameNew );
          if( pColumnDiff.notnullNew == false )
          {
            stmtAppend( "null" );
          }
          else
          {
            stmtAppend( "not null" );
          }
          stmtAppend( ")" );
          stmtDone();
        }
      }
    }
  }

  private void recreateColumn( TableDiff pTableDiff, ColumnDiff pColumnDiff )
  {
    String lTmpOldColumnameNew = "DTO_" + pColumnDiff.nameNew;
    String lTmpNewColumnameNew = "DTN_" + pColumnDiff.nameNew;

    addStmt( "alter table " + pTableDiff.nameNew + " add " + lTmpNewColumnameNew + " " + getColumnDatatype( pColumnDiff ) );

    //      TODO    for cur_trigger in
    //            (
    //            select trigger_name
    //              from user_triggers
    //             where table_name = pTableDiff.nameNew
    //            )
    //          {
    //            add_stmt( "alter trigger " + cur_trigger.trigger_name + " disable" );
    //          }

    addStmt( "update " + pTableDiff.nameNew + " set " + lTmpNewColumnameNew + " = " + pColumnDiff.nameOld );
    addStmt( "commit" );

    //          for cur_trigger in
    //            (
    //            select trigger_name
    //              from user_triggers
    //             where table_name = pTableDiff.nameNew
    //            )
    //          {
    //            add_stmt( "alter trigger " + cur_trigger.trigger_name + " enable" );
    //          }

    addStmt( "alter table " + pTableDiff.nameNew + " rename column " + pColumnDiff.nameOld + " to " + lTmpOldColumnameNew );
    addStmt( "alter table " + pTableDiff.nameNew + " rename column " + lTmpNewColumnameNew + " to " + pColumnDiff.nameNew );
    addStmt( "alter table " + pTableDiff.nameNew + " drop column " + lTmpOldColumnameNew );

    if( pColumnDiff.default_valueNew != null )
    {
      stmtStart( "alter table " + pTableDiff.nameNew + " modify ( " + pColumnDiff.nameNew + " default" );
      stmtAppend( pColumnDiff.default_valueNew );
      stmtAppend( ")" );
      stmtDone();
    }

    if( pColumnDiff.notnullNew == true )
    {
      stmtStart( "alter table " + pTableDiff.nameNew + " modify ( " + pColumnDiff.nameNew );
      stmtAppend( "not null" );
      stmtAppend( ")" );
      stmtDone();
    }
  }

  private void dropWithDropmodeCheck( String pTestStatement, String pDropStatement )
  {
    if( isDropmode() != true )
    {
      if( hasRows( pTestStatement ) == true )
      {
        if( isDropmodeIgnore() != true )
        {
          throw new RuntimeException( "drop mode not active, the following statement has to be executed but would result in data-loss: " + pDropStatement );
        }
        else
        {
          addStmt( "-- dropmode-ignore: " + pDropStatement );
          return;
        }
      }
    }

    addStmt( pDropStatement );
  }

  private boolean isDropmodeIgnore()
  {
    return false;
  }

  private boolean hasRowsIgnoreExceptions( String pTestStatement )
  {
    try
    {
      return hasRows( pTestStatement );
    }
    catch( Exception e )
    {
      return false;
    }
  }

  private boolean hasRows( String pTestStatement )
  {
    return (Boolean)new WrapperReturnValueFromResultSet( pTestStatement, getCallableStatementProvider() )
    {
      @Override
      protected Object getValueFromResultSet( ResultSet pResultSet ) throws SQLException
      {
        return pResultSet.next();
      }
    }.executeForValue();
  }

  private CallableStatementProvider getCallableStatementProvider()
  {
    return _callableStatementProvider;
  }

  private boolean isDropmode()
  {
    return _parameters.isDropmode();
  }

  private boolean isTablemovetablespace()
  {
    return _parameters.isTablemovetablespace();
  }

  private <T> T nvl( T pObject, T pDefault )
  {
    return pObject == null ? pDefault : pObject;
  }

  private String adjustRefreshmethodLiteral( String pLiteral )
  {
    return pLiteral.replace( "refresh_", " refresh " ).replace( "never_", "never " );
  }

  private ForeignKeyDiff getFkForRefPartitioning( TableDiff pTableDiff )
  {
    for( ForeignKeyDiff lForeignKeyDiff : pTableDiff.foreign_keysDiff )
    {
      if( lForeignKeyDiff.consNameNew.equalsIgnoreCase( pTableDiff.tablePartitioningRefPartitionsDiff.fkNameNew ) )
      {
        return lForeignKeyDiff;
      }
    }

    throw new RuntimeException( "fk for refpartitioning not found" );
  }

  private boolean addTableList( List<TableDiff> pRemainingTableDiffList, List<String> pAddedTableNames, List<TableDiff> pOrderedList )
  {
    boolean lAddedAtLeastOne = false;

    for( TableDiff lTableDiff : new ArrayList<TableDiff>( pRemainingTableDiffList ) )
    {
      String lRequiredTableName = null;

      if( !lTableDiff.isOld && lTableDiff.tablePartitioningRefPartitionsDiff.isNew )
      {
        lRequiredTableName = getFkForRefPartitioning( lTableDiff ).destTableNew;
      }

      if( lRequiredTableName == null || pAddedTableNames.contains( lRequiredTableName ) )
      {
        pOrderedList.add( lTableDiff );
        pAddedTableNames.add( lTableDiff.nameNew );
        pRemainingTableDiffList.remove( lTableDiff );

        lAddedAtLeastOne = true;
      }
    }

    return lAddedAtLeastOne;
  }

  private void sortTablesForRefPart( ModelDiff pModelDiff )
  {
    List<TableDiff> lTableDiffList = pModelDiff.model_elementsTableDiff;

    List<TableDiff> lRemainingTableDiffList = new ArrayList<TableDiff>( lTableDiffList );
    List<String> lAddedTableNames = new ArrayList<String>();
    List<TableDiff> lOrderedList = new ArrayList<TableDiff>();

    while( addTableList( lRemainingTableDiffList, lAddedTableNames, lOrderedList ) )
    {
    }

    if( !lRemainingTableDiffList.isEmpty() )
    {
      throw new RuntimeException( "possible table order not found " + lRemainingTableDiffList.get( 0 ).nameNew );
    }

    pModelDiff.model_elementsTableDiff = lOrderedList;
  }

  private void createMview( MviewDiff pMviewDiff )
  {
    stmtStart( "create materialized view" );
    stmtAppend( pMviewDiff.mview_nameNew );

    if( pMviewDiff.buildModeNew == BuildModeType.PREBUILT )
    {
      stmtAppend( "on prebuilt table" );
    }
    else
    {
      // Physical properties nur, wenn nicht prebuilt
      if( pMviewDiff.tablespaceNew != null )
      {
        stmtAppend( "tablespace" );
        stmtAppend( pMviewDiff.tablespaceNew );
      }

      handleCompression( pMviewDiff.compressionNew, pMviewDiff.compressionForNew, false );

      handleParallel( pMviewDiff.parallelNew, pMviewDiff.parallel_degreeNew, false );

      if( pMviewDiff.buildModeNew != null )
      {
        stmtAppend( "build" );
        stmtAppend( pMviewDiff.buildModeNew.getLiteral() );
      }
    }

    if( pMviewDiff.refreshMethodNew != null )
    {
      stmtAppend( adjustRefreshmethodLiteral( pMviewDiff.refreshMethodNew.getLiteral() ) );

      if( pMviewDiff.refreshModeNew != null )
      {
        stmtAppend( "on" );
        stmtAppend( pMviewDiff.refreshModeNew.getLiteral() );
      }
    }

    if( pMviewDiff.queryRewriteNew == EnableType.ENABLE )
    {
      stmtAppend( "enable query rewrite" );
    }

    stmtAppend( "as" );
    stmtAppend( pMviewDiff.viewSelectCLOBNew );
    stmtDone();
  }

  private void handleMview( MviewDiff pMviewDiff )
  {
    if( pMviewDiff.isMatched == false || pMviewDiff.isRecreateNeeded )
    {
      createMview( pMviewDiff );
    }
    else
    {
      if( !pMviewDiff.queryRewriteIsEqual )
      {
        EnableType lQueryRewriteNew = pMviewDiff.queryRewriteNew;

        if( lQueryRewriteNew == null )
        {
          lQueryRewriteNew = EnableType.DISABLE;
        }

        addStmt( "alter materialized view " + pMviewDiff.mview_nameNew + " " + lQueryRewriteNew.getLiteral() + " query rewrite" );
      }

      if( !pMviewDiff.refreshModeIsEqual || !pMviewDiff.refreshMethodIsEqual )
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
        addStmt( "alter materialized view " + pMviewDiff.mview_nameNew + " " + adjustRefreshmethodLiteral( pMviewDiff.refreshMethodNew.getLiteral() ) + lRefreshmode );
      }

      //     Physical parameters nur, wenn nicht prebuilt
      if( pMviewDiff.buildModeNew != BuildModeType.PREBUILT )
      {
        if( pMviewDiff.parallelIsEqual == false || pMviewDiff.parallel_degreeIsEqual == false )
        {
          stmtStart( "alter materialized view" );
          stmtAppend( pMviewDiff.mview_nameNew );

          handleParallel( pMviewDiff.parallelNew, pMviewDiff.parallel_degreeNew, true );

          stmtDone();
        }

        if( pMviewDiff.compressionIsEqual == false || pMviewDiff.compressionForIsEqual == false )
        {
          stmtStart( "alter materialized view" );
          stmtAppend( pMviewDiff.mview_nameNew );

          handleCompression( pMviewDiff.compressionNew, pMviewDiff.compressionForNew, true );

          stmtDone();
        }
      }
    }
  }

  private void handleAllMviews( ModelDiff pModelDiff )
  {
    for( MviewDiff lMviewDiff : pModelDiff.model_elementsMviewDiff )
    {
      if( !lMviewDiff.isEqual )
      {
        if( lMviewDiff.isRecreateNeeded || !lMviewDiff.isNew )
        {
          addStmt( "drop materialized view " + lMviewDiff.mview_nameOld );
        }

        if( lMviewDiff.isNew )
        {
          handleMview( lMviewDiff );
        }
      }
    }
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
      ForeignKeyDiff lForeignKeyDiff = getFkForRefPartitioning( pTableDiff );

      lReturn = lReturn + ", " + createForeignKeyClause( lForeignKeyDiff );
    }
    return lReturn;
  }

  private void createMviewlog( TableDiff pTableDiff )
  {
    String c_date_format = _parameters.getDateformat();
    stmtStart( "create materialized view log on" );
    stmtAppend( pTableDiff.nameNew );

    if( pTableDiff.mviewLogDiff.tablespaceNew != null )
    {
      stmtAppend( "tablespace" );
      stmtAppend( pTableDiff.mviewLogDiff.tablespaceNew );
    }

    handleParallel( pTableDiff.mviewLogDiff.parallelNew, pTableDiff.mviewLogDiff.parallel_degreeNew, false );

    stmtAppend( "with" );

    if( nvl( pTableDiff.mviewLogDiff.primaryKeyNew, "null" ).equals( "primary" ) || !nvl( pTableDiff.mviewLogDiff.rowidNew, "null" ).equals( "rowid" ) )
    {
      stmtAppend( "primary key" );
      if( nvl( pTableDiff.mviewLogDiff.rowidNew, "null" ).equals( "rowid" ) )
      {
        stmtAppend( "," );
      }
    }

    if( nvl( pTableDiff.mviewLogDiff.rowidNew, "null" ).equals( "rowid" ) )
    {
      stmtAppend( "rowid" );
    }

    if( nvl( pTableDiff.mviewLogDiff.withSequenceNew, "null" ).equals( "sequence" ) )
    {
      stmtAppend( "," );
      stmtAppend( "sequence" );
    }

    if( pTableDiff.mviewLogDiff.columnsDiff.size() > 0 )
    {
      stmtAppend( "(" );
      stmtAppend( getColumnList( pTableDiff.mviewLogDiff.columnsDiff ) );
      stmtAppend( ")" );
    }

    if( nvl( pTableDiff.mviewLogDiff.commitScnNew, "null" ).equals( "commit_scn" ) )
    {
      stmtAppend( "," );
      stmtAppend( "commit scn" );
    }

    if( pTableDiff.mviewLogDiff.newValuesNew != null )
    {
      stmtAppend( pTableDiff.mviewLogDiff.newValuesNew.getLiteral() );
      stmtAppend( "new values" );
    }

    if( pTableDiff.mviewLogDiff.startWithNew != null || pTableDiff.mviewLogDiff.nextNew != null || (pTableDiff.mviewLogDiff.repeatIntervalNew != null && pTableDiff.mviewLogDiff.repeatIntervalNew != 0) )
    {
      stmtAppend( "purge" );
      if( pTableDiff.mviewLogDiff.startWithNew != null )
      {
        stmtAppend( "start with" );
        stmtAppend( "to_date('" + pTableDiff.mviewLogDiff.startWithNew + "','" + c_date_format + "')" );
      }
      if( pTableDiff.mviewLogDiff.nextNew != null )
      {
        stmtAppend( "next" );
        stmtAppend( "to_date('" + pTableDiff.mviewLogDiff.nextNew + "','" + c_date_format + "')" );
      }
      else
      {
        if( pTableDiff.mviewLogDiff.repeatIntervalNew != null && pTableDiff.mviewLogDiff.repeatIntervalNew != 0 )
        {
          stmtAppend( "repeat interval" );
          stmtAppend( "" + pTableDiff.mviewLogDiff.repeatIntervalNew );
        }
      }
    }
    else
    {
      if( pTableDiff.mviewLogDiff.synchronousNew == SynchronousType.ASYNCHRONOUS )
      {
        stmtAppend( "purge immediate asynchronous" );
      }
    }

    stmtDone();
  }

  private void handleMviewlog( TableDiff pTableDiff )
  {
    if( !pTableDiff.mviewLogDiff.isOld || pTableDiff.mviewLogDiff.isRecreateNeeded )
    {
      createMviewlog( pTableDiff );
    }
    else
    {
      if( pTableDiff.mviewLogDiff.parallelIsEqual == false || pTableDiff.mviewLogDiff.parallel_degreeIsEqual == false )
      {
        stmtStart( "alter materialized view log on" );
        stmtAppend( pTableDiff.nameNew );
        handleParallel( pTableDiff.mviewLogDiff.parallelNew, pTableDiff.mviewLogDiff.parallel_degreeNew, true );

        stmtDone();
      }

      if( pTableDiff.mviewLogDiff.newValuesIsEqual == false )
      {
        stmtStart( "alter materialized view log on" );
        stmtAppend( pTableDiff.nameNew );

        if( pTableDiff.mviewLogDiff.newValuesNew == NewValuesType.INCLUDING )
        {
          stmtAppend( "including" );
        }
        else
        {
          stmtAppend( "excluding" );
        }
        stmtAppend( "new values" );

        stmtDone();
      }

      if( pTableDiff.mviewLogDiff.startWithIsEqual == false || pTableDiff.mviewLogDiff.nextIsEqual == false || pTableDiff.mviewLogDiff.repeatIntervalIsEqual == false )
      {
        stmtStart( "alter materialized view log on" );
        stmtAppend( pTableDiff.nameNew );
        stmtAppend( "purge" );
        if( pTableDiff.mviewLogDiff.startWithIsEqual == false )
        {
          stmtAppend( "start with" );
          stmtAppend( "to_date('" + pTableDiff.mviewLogDiff.startWithNew + "','" + _parameters.getDateformat() + "')" );
        }
        if( pTableDiff.mviewLogDiff.nextIsEqual == false )
        {
          stmtAppend( "next" );
          stmtAppend( "to_date('" + pTableDiff.mviewLogDiff.nextNew + "','" + _parameters.getDateformat() + "')" );
        }
        else
        {
          if( pTableDiff.mviewLogDiff.repeatIntervalIsEqual == false )
          {
            stmtAppend( "repeat interval" );
            stmtAppend( pTableDiff.mviewLogDiff.repeatIntervalNew + "" );
          }
        }

        stmtDone();
      }
      else
      {
        if( pTableDiff.mviewLogDiff.synchronousIsEqual == false )
        {
          stmtAppend( "alter materialized view log on" );
          stmtAppend( pTableDiff.nameNew );
          if( pTableDiff.mviewLogDiff.synchronousNew == SynchronousType.ASYNCHRONOUS )
          {
            stmtAppend( "purge immediate asynchronous" );
          }
          else
          {
            stmtAppend( "purge immediate synchronous" );
          }

          stmtDone();
        }
      }
    }
  }
}
