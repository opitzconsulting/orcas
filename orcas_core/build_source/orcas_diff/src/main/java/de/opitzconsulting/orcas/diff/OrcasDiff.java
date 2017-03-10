package de.opitzconsulting.orcas.diff;

import static de.opitzconsulting.origOrcasDsl.OrigOrcasDslPackage.Literals.*;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EAttribute;

import de.opitzconsulting.orcas.diff.DiffAction.DiffReasonType;
import de.opitzconsulting.orcas.diff.DiffReasonKey.DiffReasonKeyRegistry;
import de.opitzconsulting.orcas.orig.diff.AbstractDiff;
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

public class OrcasDiff // extends AbstractStatementBuilder
{
  private CallableStatementProvider _callableStatementProvider;

  public OrcasDiff( CallableStatementProvider pCallableStatementProvider, Parameters pParameters )
  {
    _callableStatementProvider = pCallableStatementProvider;
    _parameters = pParameters;
  }

  private Parameters _parameters;
  private List<DiffAction> diffActions = new ArrayList<DiffAction>();
  private DiffAction activeDiffAction;

  private Map<AbstractDiff, List<DiffActionReason>> recreateDiffDiffActionReasonMap = new HashMap<>();
  private DiffReasonKeyRegistry diffReasonKeyRegistry;

  private boolean isRecreateNeeded( AbstractDiff pDiff )
  {
    return recreateDiffDiffActionReasonMap.containsKey( pDiff );
  }

  private void addRecreateNeeded( AbstractDiff pDiff, DiffActionReason pDiffActionReason )
  {
    List<DiffActionReason> lDiffActionReasonList = recreateDiffDiffActionReasonMap.get( pDiff );

    if( lDiffActionReasonList == null )
    {
      lDiffActionReasonList = new ArrayList<>();
      recreateDiffDiffActionReasonMap.put( pDiff, lDiffActionReasonList );
    }

    lDiffActionReasonList.add( pDiffActionReason );
  }

  private void setRecreateNeededDifferent( AbstractDiff pDiff, EAttribute... pDiffReasonDetails )
  {
    DiffActionReasonDifferent lDiffActionReasonDifferent = new DiffActionReasonDifferent( diffReasonKeyRegistry.getDiffReasonKey( pDiff ) );

    for( EAttribute lEAttribute : pDiffReasonDetails )
    {
      if( lEAttribute != null )
      {
        lDiffActionReasonDifferent.addDiffReasonDetail( lEAttribute );
      }
    }

    addRecreateNeeded( pDiff, lDiffActionReasonDifferent );
  }

  private void setRecreateNeededDependsOn( AbstractDiff pDiff, List<DiffActionReason> pDiffActionReasonDependsOnList )
  {
    addRecreateNeeded( pDiff, new DiffActionReasonDependsOn( diffReasonKeyRegistry.getDiffReasonKey( pDiff ), pDiffActionReasonDependsOnList ) );
  }

  private List<DiffActionReason> getIndexRecreate( TableDiff pTableDiff, String pIndexname )
  {
    for( IndexOrUniqueKeyDiff lIndexOrUniqueKeyDiff : pTableDiff.ind_uksIndexDiff )
    {
      if( lIndexOrUniqueKeyDiff.consNameNew.equals( pIndexname ) )
      {
        if( isRecreateNeeded( lIndexOrUniqueKeyDiff ) )
        {
          return recreateDiffDiffActionReasonMap.get( lIndexOrUniqueKeyDiff );
        }
        else
        {
          return Collections.emptyList();
        }
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

  private List<EAttribute> getDifferentEAttributes( AbstractDiff pDiff, EAttribute... pEAttributes )
  {
    List<EAttribute> lReturn = new ArrayList<>();

    for( EAttribute lEAttribute : pEAttributes )
    {
      if( !pDiff.isFieldEqual( lEAttribute ) )
      {
        lReturn.add( lEAttribute );
      }
    }

    return lReturn;
  }

  private void setRecreateNeededIfDifferent( AbstractDiff pDiff, EAttribute... pEAttributes )
  {
    List<EAttribute> lDifferentEAttributes = getDifferentEAttributes( pDiff, pEAttributes );
    if( !lDifferentEAttributes.isEmpty() )
    {
      setRecreateNeededDifferent( pDiff, new ArrayList<>( lDifferentEAttributes ).toArray( new EAttribute[lDifferentEAttributes.size()] ) );
    }
  }

  private void updateIsRecreateNeeded( ModelDiff pModelDiff )
  {
    for( TableDiff lTableDiff : pModelDiff.model_elementsTableDiff )
    {
      if( lTableDiff.isMatched )
      {
        setRecreateNeededIfDifferent( lTableDiff, TABLE__PERMANENTNESS, TABLE__TRANSACTION_CONTROL );

        Map<String, List<DiffActionReason>> lRecreateColumnNames = new HashMap<>();

        for( ColumnDiff lColumnDiff : lTableDiff.columnsDiff )
        {
          if( lColumnDiff.isMatched )
          {
            if( isRecreateColumn( lColumnDiff ) )
            {
              setRecreateNeededDifferent( lColumnDiff );
              lRecreateColumnNames.put( lColumnDiff.nameOld, recreateDiffDiffActionReasonMap.get( lColumnDiff ) );
            }
          }
        }

        if( lTableDiff.primary_keyDiff.isMatched )
        {
          if( !lTableDiff.primary_keyDiff.consNameIsEqual //
              || !lTableDiff.primary_keyDiff.pk_columnsIsEqual || !lTableDiff.primary_keyDiff.reverseIsEqual || (!lTableDiff.primary_keyDiff.tablespaceIsEqual && isIndexmovetablespace()) )
          {
            setRecreateNeededDifferent( lTableDiff.primary_keyDiff );
          }

          handleColumnDependentRecreate( lRecreateColumnNames, lTableDiff.primary_keyDiff, lTableDiff.primary_keyDiff.pk_columnsDiff );
        }

        for( IndexDiff lIndexDiff : lTableDiff.ind_uksIndexDiff )
        {
          if( lIndexDiff.isMatched )
          {
            if( ((!lIndexDiff.index_columnsIsEqual //
                  || !lIndexDiff.function_based_expressionIsEqual || !lIndexDiff.domain_index_expressionIsEqual)
                 // domain index cant be compared
                 && lIndexDiff.domain_index_expressionNew == null)
                || !lIndexDiff.uniqueIsEqual || !lIndexDiff.bitmapIsEqual || !lIndexDiff.globalIsEqual || !lIndexDiff.compressionIsEqual )
            {
              setRecreateNeededDifferent( lIndexDiff );
            }

            handleColumnDependentRecreate( lRecreateColumnNames, lIndexDiff, lIndexDiff.index_columnsDiff );
          }
        }

        for( UniqueKeyDiff lUniqueKeyDiff : lTableDiff.ind_uksUniqueKeyDiff )
        {
          if( lUniqueKeyDiff.isMatched )
          {
            if( !lUniqueKeyDiff.uk_columnsIsEqual || //
                !lUniqueKeyDiff.indexnameIsEqual || (!lUniqueKeyDiff.tablespaceIsEqual && isIndexmovetablespace()) )
            {
              setRecreateNeededDifferent( lUniqueKeyDiff );
            }
            else
            {
              if( lUniqueKeyDiff.indexnameNew != null )
              {
                List<DiffActionReason> lIndexRecreate = getIndexRecreate( lTableDiff, lUniqueKeyDiff.indexnameNew );

                if( !lIndexRecreate.isEmpty() )
                {
                  setRecreateNeededDependsOn( lUniqueKeyDiff, lIndexRecreate );
                }
              }
            }

            handleColumnDependentRecreate( lRecreateColumnNames, lUniqueKeyDiff, lUniqueKeyDiff.uk_columnsDiff );
          }
        }

        for( ConstraintDiff lConstraintDiff : lTableDiff.constraintsDiff )
        {
          if( lConstraintDiff.isMatched )
          {
            if( !lConstraintDiff.ruleIsEqual //
                || !lConstraintDiff.deferrtypeIsEqual )
            {
              setRecreateNeededDifferent( lConstraintDiff );
            }
          }
        }

        for( ForeignKeyDiff lForeignKeyDiff : lTableDiff.foreign_keysDiff )
        {
          if( lForeignKeyDiff.isMatched )
          {
            if( !lForeignKeyDiff.isEqual )
            {
              setRecreateNeededDifferent( lForeignKeyDiff );
            }

            handleColumnDependentRecreate( lRecreateColumnNames, lForeignKeyDiff, lForeignKeyDiff.srcColumnsDiff );
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
          setRecreateNeededDifferent( lTableDiff.mviewLogDiff );
        }

        // these changes should by applied with alter statements, but it results
        // in ORA-27476
        if( lTableDiff.mviewLogDiff.startWithIsEqual == false || lTableDiff.mviewLogDiff.nextIsEqual == false || lTableDiff.mviewLogDiff.repeatIntervalIsEqual == false )
        {
          setRecreateNeededDifferent( lTableDiff.mviewLogDiff );
        }

        for( InlineCommentDiff lCommentDiff : lTableDiff.commentsDiff )
        {
          if( lCommentDiff.isMatched )
          {
            if( lCommentDiff.column_nameOld != null )
            {
              handleColumnDependentRecreate( lRecreateColumnNames, lCommentDiff, lCommentDiff.column_nameOld );
            }
          }
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
          setRecreateNeededDifferent( lMviewDiff );
        }
      }
    }

    for( TableDiff lTableDiff : pModelDiff.model_elementsTableDiff )
    {
      for( ForeignKeyDiff lForeignKeyDiff : lTableDiff.foreign_keysDiff )
      {
        if( lForeignKeyDiff.isMatched )
        {
          List<DiffActionReason> lRefConstraintRecreate = getRefConstraintRecreate( pModelDiff, lForeignKeyDiff.destTableOld, lForeignKeyDiff.destColumnsDiff );

          if( !lRefConstraintRecreate.isEmpty() )
          {
            setRecreateNeededDependsOn( lForeignKeyDiff, lRefConstraintRecreate );
          }
        }
      }
    }
  }

  private void handleColumnDependentRecreate( Map<String, List<DiffActionReason>> pRecreateColumnNames, AbstractDiff pDiff, List<ColumnRefDiff> pColumnDiffList )
  {
    List<DiffActionReason> lDependsOnList = new ArrayList<>();

    for( ColumnRefDiff lColumnRefDiff : pColumnDiffList )
    {
      if( lColumnRefDiff.isOld )
      {
        if( pRecreateColumnNames.keySet().contains( lColumnRefDiff.column_nameOld ) )
        {
          lDependsOnList.addAll( pRecreateColumnNames.get( lColumnRefDiff.column_nameOld ) );
        }
      }
    }

    if( !lDependsOnList.isEmpty() )
    {
      setRecreateNeededDependsOn( pDiff, lDependsOnList );
    }
  }

  private void handleColumnDependentRecreate( Map<String, List<DiffActionReason>> pRecreateColumnNames, AbstractDiff pDiff, String pColumnName )
  {
    List<DiffActionReason> lDependsOnList = new ArrayList<>();

    if( pRecreateColumnNames.keySet().contains( pColumnName ) )
    {
      lDependsOnList.addAll( pRecreateColumnNames.get( pColumnName ) );
    }

    if( !lDependsOnList.isEmpty() )
    {
      setRecreateNeededDependsOn( pDiff, lDependsOnList );
    }
  }

  private List<DiffActionReason> getRefConstraintRecreate( ModelDiff pModelDiff, String pDestTableName, List<ColumnRefDiff> pDestColumnsDiff )
  {
    List<DiffActionReason> lReturn = new ArrayList<>();

    for( TableDiff lTableDiff : pModelDiff.model_elementsTableDiff )
    {
      if( lTableDiff.isMatched && lTableDiff.nameOld.equals( pDestTableName ) )
      {
        if( isRecreateNeeded( lTableDiff ) )
        {
          lReturn.addAll( recreateDiffDiffActionReasonMap.get( lTableDiff ) );
        }

        if( isRecreateNeeded( lTableDiff.primary_keyDiff ) )
        {
          if( isOldColumnNamesEqual( pDestColumnsDiff, lTableDiff.primary_keyDiff.pk_columnsDiff ) )
          {
            lReturn.addAll( recreateDiffDiffActionReasonMap.get( lTableDiff.primary_keyDiff ) );
          }
        }

        for( UniqueKeyDiff lUniqueKeyDiff : lTableDiff.ind_uksUniqueKeyDiff )
        {
          if( isRecreateNeeded( lUniqueKeyDiff ) )
          {
            if( isOldColumnNamesEqual( pDestColumnsDiff, lUniqueKeyDiff.uk_columnsDiff ) )
            {
              lReturn.addAll( recreateDiffDiffActionReasonMap.get( lUniqueKeyDiff ) );
            }
          }
        }
      }
    }

    return lReturn;
  }

  private boolean isOldColumnNamesEqual( List<ColumnRefDiff> pColumnRefDiffList1, List<ColumnRefDiff> pColumnRefDiffList2 )
  {
    return getOldColumnNames( pColumnRefDiffList1 ).equals( getOldColumnNames( pColumnRefDiffList2 ) );
  }

  private String getOldColumnNames( List<ColumnRefDiff> pColumnRefDiffList )
  {
    String lReturn = "";

    for( ColumnRefDiff lColumnRefDiff : pColumnRefDiffList )
    {
      if( lColumnRefDiff.isOld )
      {
        lReturn += lColumnRefDiff.column_nameOld + ",";
      }
    }

    return lReturn;
  }

  private String replaceLinefeedBySpace( String pValue )
  {
    return pValue.replace( Character.toString( (char) 13 ) + Character.toString( (char) 10 ), " " ).replace( Character.toString( (char) 10 ), " " ).replace( Character.toString( (char) 13 ), " " );
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

    diffReasonKeyRegistry = new DiffReasonKey.DiffReasonKeyRegistry( lModelDiff );

    updateIsRecreateNeeded( lModelDiff );

    handleAllTables( lModelDiff );

    handleAllSequences( lModelDiff );

    handleAllMviews( lModelDiff );

    return new DiffResult( diffActions );
  }

  public class DiffResult
  {
    private List<DiffAction> diffActions = new ArrayList<DiffAction>();

    public List<DiffAction> getDiffActions()
    {
      return diffActions;
    }

    private DiffResult( List<DiffAction> pDiffActions )
    {
      diffActions.addAll( pDiffActions );
    }
  }

  private void handleSequence( SequenceDiff pSequenceDiff )
  {
    BigDecimal lSollStartValue = null;

    try
    {
      if( pSequenceDiff.max_value_selectNew != null )
      {
        lSollStartValue = (BigDecimal) new WrapperReturnFirstValue( pSequenceDiff.max_value_selectNew, getCallableStatementProvider() ).executeForValue();

        lSollStartValue = lSollStartValue.add( BigDecimal.valueOf( 1 ) );
      }
    }
    catch( Exception e )
    {
      // kann vorkommen, wenn fuer das select benoetigte Tabellen nicht
      // exisitieren. kann erst richtig korrigiert werden, wenn auch der
      // Tabellenabgleich auf dieses Package umgestellt wurde
    }

    BigDecimal lFinalSollStartValue = lSollStartValue;

    if( pSequenceDiff.isMatched == false )
    {
      doInDiffActionCreate( pSequenceDiff, p ->
      {
        p.stmtStart( "create sequence " + pSequenceDiff.sequence_nameNew );
        if( pSequenceDiff.increment_byNew != null )
        {
          p.stmtAppend( "increment by " + pSequenceDiff.increment_byNew );
        }

        if( lFinalSollStartValue != null )
        {
          p.stmtAppend( "start with " + lFinalSollStartValue );
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
      } );
    }
    else
    {
      doInDiffActionAlter( pSequenceDiff, p ->
      {

        BigDecimal lIstValue = BigDecimal.valueOf( Long.valueOf( pSequenceDiff.max_value_selectOld ) );
        if( lFinalSollStartValue != null && lIstValue != null && lFinalSollStartValue.compareTo( lIstValue ) > 0 )
        {
          p.addStmt( SEQUENCE__MAX_VALUE_SELECT, "alter sequence " + pSequenceDiff.sequence_nameNew + " increment by " + (lFinalSollStartValue.longValue() - lIstValue.longValue()) );
          p.addStmt( SEQUENCE__MAX_VALUE_SELECT, "declare\n v_dummy number;\n begin\n select " + pSequenceDiff.sequence_nameNew + ".nextval into v_dummy from dual;\n end;" );
          p.addStmt( SEQUENCE__MAX_VALUE_SELECT, "alter sequence " + pSequenceDiff.sequence_nameNew + " increment by " + nvl( pSequenceDiff.increment_byNew, 1 ) );
        }
        else
        {
          if( pSequenceDiff.increment_byIsEqual == false )
          {
            p.addStmt( SEQUENCE__INCREMENT_BY, "alter sequence " + pSequenceDiff.sequence_nameNew + " increment by " + nvl( pSequenceDiff.increment_byNew, 1 ) );
          }
        }

        if( pSequenceDiff.maxvalueIsEqual == false )
        {
          p.addStmt( SEQUENCE__MAXVALUE, "alter sequence " + pSequenceDiff.sequence_nameNew + " maxvalue " + pSequenceDiff.maxvalueNew );
        }

        if( pSequenceDiff.minvalueIsEqual == false )
        {
          p.addStmt( SEQUENCE__MINVALUE, "alter sequence " + pSequenceDiff.sequence_nameNew + " minvalue " + nvl( pSequenceDiff.minvalueNew, 1 ) );
        }

        if( pSequenceDiff.cycleIsEqual == false )
        {
          p.addStmt( SEQUENCE__CYCLE, "alter sequence " + pSequenceDiff.sequence_nameNew + " " + pSequenceDiff.cycleNew.getLiteral() );
        }

        if( pSequenceDiff.cacheIsEqual == false )
        {
          p.addStmt( SEQUENCE__CACHE, "alter sequence " + pSequenceDiff.sequence_nameNew + " cache " + nvl( pSequenceDiff.cacheNew, 20 ) );
        }

        if( pSequenceDiff.orderIsEqual == false )
        {
          p.addStmt( SEQUENCE__ORDER, "alter sequence " + pSequenceDiff.sequence_nameNew + " " + pSequenceDiff.orderNew.getLiteral() );
        }
      } );
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
          doInDiffActionDrop( lSequenceDiff, p ->
          {
            p.addStmt( "drop sequence " + lSequenceDiff.sequence_nameOld );
          } );
        }
      }
    }
  }

  private <T extends AbstractStatementBuilder> void doInDiffAction( DiffAction pDiffAction, AbstractDiffActionRunnable<T> pRunnable, T pAbstractStatementBuilder )
  {
    addDiffAction( pDiffAction );
    pRunnable.run( pAbstractStatementBuilder );
    diffActionDone();
  }

  private <T extends AbstractStatementBuilder> void doInDiffAction( DiffAction pDiffAction, List<DiffActionReason> pDiffActionReasonList, AbstractDiffActionRunnable<T> pRunnable, T pAbstractStatementBuilder )
  {
    for( DiffActionReason lDiffActionReason : pDiffActionReasonList )
    {
      pDiffAction.addDiffActionReason( lDiffActionReason );
    }
    doInDiffAction( pDiffAction, pRunnable, pAbstractStatementBuilder );
  }

  private <T extends AbstractStatementBuilder> void doInDiffAction( AbstractDiff pDiff, List<DiffActionReason> pDiffActionReasonList, DiffReasonType pDiffReasonType, AbstractDiffActionRunnable<T> pRunnable, T pAbstractStatementBuilder )
  {
    doInDiffAction( new DiffAction( diffReasonKeyRegistry.getDiffReasonKey( pDiff ), pDiffReasonType ), pDiffActionReasonList, pRunnable, pAbstractStatementBuilder );
  }

  private void doInDiffActionAlter( AbstractDiff pDiff, DiffActionRunnableAlter pRunnable )
  {
    DiffAction lDiffAction = new DiffAction( diffReasonKeyRegistry.getDiffReasonKey( pDiff ), DiffReasonType.ALTER );
    DiffActionReasonDifferent lDiffActionReasonDifferent = new DiffActionReasonDifferent( diffReasonKeyRegistry.getDiffReasonKey( pDiff ) );
    doInDiffAction( lDiffAction, Collections.singletonList( lDiffActionReasonDifferent ), pRunnable, new StatementBuilderAlter( lDiffActionReasonDifferent ) );
  }

  private void doInDiffActionDrop( AbstractDiff pDiff, DiffActionRunnable pRunnable )
  {
    boolean lRecreateNeeded = isRecreateNeeded( pDiff );
    DiffAction lDiffAction = new DiffAction( diffReasonKeyRegistry.getDiffReasonKey( pDiff ), lRecreateNeeded ? DiffReasonType.RECREATE_DROP : DiffReasonType.DROP );

    doInDiffAction( lDiffAction, lRecreateNeeded ? recreateDiffDiffActionReasonMap.get( pDiff ) : Collections.singletonList( new DiffActionReasonSurplus( diffReasonKeyRegistry.getDiffReasonKey( pDiff ) ) ), pRunnable, new StatementBuilder() );
  }

  private void doInDiffActionCreate( AbstractDiff pDiff, DiffActionRunnable pRunnable )
  {
    boolean lRecreateNeeded = isRecreateNeeded( pDiff );
    DiffAction lDiffAction = new DiffAction( diffReasonKeyRegistry.getDiffReasonKey( pDiff ), lRecreateNeeded ? DiffReasonType.RECREATE_CREATE : DiffReasonType.CREATE );

    doInDiffAction( lDiffAction, lRecreateNeeded ? recreateDiffDiffActionReasonMap.get( pDiff ) : Collections.singletonList( new DiffActionReasonMissing( diffReasonKeyRegistry.getDiffReasonKey( pDiff ) ) ), pRunnable, new StatementBuilder() );
  }

  private void diffActionDone()
  {
    if( activeDiffAction.getStatements().isEmpty() )
    {
      diffActions.remove( activeDiffAction );
    }
    activeDiffAction = null;
  }

  private void addDiffAction( DiffAction pDiffAction )
  {
    activeDiffAction = pDiffAction;
    diffActions.add( pDiffAction );
  }

  private void dropTableConstraintByName( StatementBuilder p, String pTablename, String pCconstraintName )
  {
    p.addStmt( "alter table " + pTablename + " drop constraint " + pCconstraintName );
  }

  private void handleAllTables( ModelDiff pModelDiff )
  {
    for( TableDiff lTableDiff : pModelDiff.model_elementsTableDiff )
    {
      for( ForeignKeyDiff lForeignKeyDiff : lTableDiff.foreign_keysDiff )
      {
        if( lForeignKeyDiff.isOld == true && (lForeignKeyDiff.isMatched == false || isRecreateNeeded( lForeignKeyDiff ) == true) )
        {
          doInDiffActionDrop( lForeignKeyDiff, p ->
          {
            dropTableConstraintByName( p, lTableDiff.nameOld, lForeignKeyDiff.consNameOld );
          } );
        }
      }
    }

    for(

    TableDiff lTableDiff : pModelDiff.model_elementsTableDiff )
    {
      if( lTableDiff.isOld == true && (lTableDiff.isMatched == false || isRecreateNeeded( lTableDiff ) == true) )
      {
        doInDiffActionDrop( lTableDiff, p ->
        {
          dropWithDropmodeCheck( p, "select 1 from " + lTableDiff.nameOld, "drop table " + lTableDiff.nameOld );
        } );
      }
      else
      {
        for( ConstraintDiff lConstraintDiff : lTableDiff.constraintsDiff )
        {
          if( lConstraintDiff.isOld == true && (lConstraintDiff.isMatched == false || isRecreateNeeded( lConstraintDiff )) )
          {
            doInDiffActionDrop( lConstraintDiff, p ->
            {
              dropTableConstraintByName( p, lTableDiff.nameOld, lConstraintDiff.consNameOld );
            } );
          }
        }

        if( lTableDiff.mviewLogDiff.isOld == true && (lTableDiff.mviewLogDiff.isMatched == false || isRecreateNeeded( lTableDiff.mviewLogDiff )) )
        {
          doInDiffActionDrop( lTableDiff.mviewLogDiff, p ->
          {
            p.addStmt( "drop materialized view log on " + lTableDiff.nameOld );
          } );
        }

        for( UniqueKeyDiff lUniqueKeyDiff : lTableDiff.ind_uksUniqueKeyDiff )
        {
          if( lUniqueKeyDiff.isOld == true && (lUniqueKeyDiff.isMatched == false || isRecreateNeeded( lUniqueKeyDiff ) == true) )
          {
            doInDiffActionDrop( lUniqueKeyDiff, p ->
            {
              dropTableConstraintByName( p, lTableDiff.nameOld, lUniqueKeyDiff.consNameOld );
            } );
          }
        }

        for( IndexDiff lIndexDiff : lTableDiff.ind_uksIndexDiff )
        {
          if( lIndexDiff.isOld == true && (lIndexDiff.isMatched == false || isRecreateNeeded( lIndexDiff ) == true) )
          {
            doInDiffActionDrop( lIndexDiff, p ->
            {
              p.addStmt( "drop index " + lIndexDiff.consNameOld );
            } );
          }
        }

        for( InlineCommentDiff lCommentDiff : lTableDiff.commentsDiff )
        {
          if( lCommentDiff.isOld == true && lCommentDiff.isMatched == false )
          {
            boolean lIsColumnComment = lCommentDiff.column_nameOld != null;
            if( !lIsColumnComment || columnIsNew( lTableDiff, lCommentDiff.column_nameOld ) )
            {
              doInDiffActionDrop( lCommentDiff, p ->
              {
                p.stmtStart( "comment on" );
                p.stmtAppend( lCommentDiff.comment_objectOld.getName() );
                p.stmtAppend( " " );
                p.stmtAppend( lTableDiff.nameOld );
                if( lIsColumnComment )
                {
                  p.stmtAppend( "." );
                  p.stmtAppend( lCommentDiff.column_nameOld );
                }
                p.stmtAppend( "is" );
                p.stmtAppend( "''" );
                p.stmtDone();
              } );
            }
          }
        }

        if( lTableDiff.primary_keyDiff.isOld == true && (lTableDiff.primary_keyDiff.isMatched == false || isRecreateNeeded( lTableDiff.primary_keyDiff )) )
        {
          doInDiffActionDrop( lTableDiff.primary_keyDiff, p ->
          {
            dropTableConstraintByName( p, lTableDiff.nameOld, lTableDiff.primary_keyDiff.consNameOld );
          } );
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
        if( lForeignKeyDiff.isNew == true && (lForeignKeyDiff.isMatched == false || isRecreateNeeded( lForeignKeyDiff )) )
        {
          if( lTableDiff.isOld == false && lTableDiff.tablePartitioningRefPartitionsDiff.isNew == true && lTableDiff.tablePartitioningRefPartitionsDiff.fkNameNew.equalsIgnoreCase( lForeignKeyDiff.consNameNew ) )
          {
            // fk for ref-partition created in create-table
          }
          else
          {
            doInDiffActionCreate( lForeignKeyDiff, p ->
            {
              createForeignKey( p, lTableDiff, lForeignKeyDiff );
            } );
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
    if( pTableDiff.isMatched == false || isRecreateNeeded( pTableDiff ) == true )
    {
      doInDiffActionCreate( pTableDiff, p ->
      {
        createTable( p, pTableDiff );
      } );
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
          doInDiffActionDrop( lColumnDiff, p ->
          {
            dropWithDropmodeCheck( p, "select 1 from " + pTableDiff.nameOld + " where " + lColumnDiff.nameOld + " != null", "alter table " + pTableDiff.nameOld + " drop column " + lColumnDiff.nameOld );
          } );
        }
      }

      doInDiffActionAlter( pTableDiff, p ->
      {
        if( pTableDiff.tablespaceIsEqual == false && isTablemovetablespace() == true )
        {
          p.stmtStart( "alter table" );
          p.stmtAppend( pTableDiff.nameNew );
          p.stmtAppend( "move tablespace" );
          p.stmtAppend( nvl( pTableDiff.tablespaceNew, getDefaultTablespace() ) );
          p.stmtDone( TABLE__TABLESPACE );
        }

        if( pTableDiff.loggingIsEqual == false )
        {
          if( pTableDiff.transactionControlNew == null )
          {
            p.stmtStart( "alter table" );
            p.stmtAppend( pTableDiff.nameNew );
            if( pTableDiff.loggingNew == LoggingType.NOLOGGING )
            {
              p.stmtAppend( "nologging" );
            }
            else
            {
              p.stmtAppend( "logging" );
            }
            p.stmtDone( TABLE__LOGGING );
          }
        }

        if( pTableDiff.parallelIsEqual == false || pTableDiff.parallel_degreeIsEqual == false )
        {
          p.stmtStart( "alter table" );
          p.stmtAppend( pTableDiff.nameNew );

          handleParallel( p, pTableDiff.parallelNew, pTableDiff.parallel_degreeNew, true );

          p.stmtDone( TABLE__PARALLEL );
        }

        if( pTableDiff.permanentnessNew != PermanentnessType.GLOBAL_TEMPORARY && (pTableDiff.compressionIsEqual == false || pTableDiff.compressionForIsEqual == false) )
        {
          p.stmtStart( "alter table" );
          p.stmtAppend( pTableDiff.nameNew );

          handleCompression( p, pTableDiff.compressionNew, pTableDiff.compressionForNew, true );

          p.stmtDone( TABLE__COMPRESSION );
        }
      } );
    }

    if( pTableDiff.primary_keyDiff.isNew == true )
    {
      handlePrimarykey( pTableDiff );
    }

    for( ConstraintDiff lConstraintDiff : pTableDiff.constraintsDiff )
    {
      if( lConstraintDiff.isNew == true )
      {
        handleConstraint( pTableDiff, lConstraintDiff );
      }
    }

    for( IndexDiff lIndexDiff : pTableDiff.ind_uksIndexDiff )
    {
      if( lIndexDiff.isNew == true )
      {
        handleIndex( pTableDiff, lIndexDiff );
      }
    }

    for( UniqueKeyDiff lUniqueKeyDiff : pTableDiff.ind_uksUniqueKeyDiff )
    {
      if( lUniqueKeyDiff.isNew == true )
      {
        handleUniquekey( pTableDiff, lUniqueKeyDiff );
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

  private void createTable( StatementBuilder p, TableDiff pTableDiff )
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
      p.stmtAppend( createColumnStorageClause( pTableDiff.lobStoragesDiff ) );
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
    handleParallel( p, pTableDiff.parallelNew, pTableDiff.parallel_degreeNew, false );
    p.stmtAppend( createPartitioningClause( pTableDiff ) );

    p.stmtDone();
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

  private void createForeignKey( StatementBuilder p, TableDiff pTableDiff, ForeignKeyDiff pForeignKeyDiff )
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

    if( _parameters.getMultiSchema() )
    {
      if( !getSchemaName( pTableDiff.nameNew ).equals( getSchemaName( pForeignKeyDiff.destTableNew ) ) )
      {
        p.addStmt( "grant references on " + pForeignKeyDiff.destTableNew + " to " + getSchemaName( pTableDiff.nameNew ) );
      }
    }

    p.stmtStart( "alter table " + pTableDiff.nameNew );
    p.stmtAppend( "add" );
    p.stmtAppend( createForeignKeyClause( pForeignKeyDiff ) );
    p.stmtDone();
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
    if( pInlineCommentDiff.isEqual == false || isRecreateNeeded( pInlineCommentDiff ) )
    {
      if( pInlineCommentDiff.isNew == true )
      {
        DiffReasonType lDiffReasonType;
        List<DiffActionReason> lDiffActionReasonList;
        if( isRecreateNeeded( pInlineCommentDiff ) )
        {
          lDiffReasonType = DiffReasonType.RECREATE_CREATE;
          lDiffActionReasonList = recreateDiffDiffActionReasonMap.get( pInlineCommentDiff );
        }
        else
        {
          if( pInlineCommentDiff.isMatched )
          {
            lDiffReasonType = DiffReasonType.ALTER;
            DiffActionReasonDifferent lDiffActionReasonDifferent = new DiffActionReasonDifferent( diffReasonKeyRegistry.getDiffReasonKey( pInlineCommentDiff ) );
            lDiffActionReasonDifferent.addDiffReasonDetail( INLINE_COMMENT__COMMENT );
            lDiffActionReasonList = Collections.singletonList( lDiffActionReasonDifferent );
          }
          else
          {
            lDiffReasonType = DiffReasonType.CREATE;
            lDiffActionReasonList = Collections.singletonList( new DiffActionReasonMissing( diffReasonKeyRegistry.getDiffReasonKey( pInlineCommentDiff ) ) );
          }
        }
        doInDiffAction( pInlineCommentDiff, lDiffActionReasonList, lDiffReasonType, p ->
        {
          p.stmtStart( "comment on" );
          p.stmtAppend( pInlineCommentDiff.comment_objectNew.getName() );
          p.stmtAppend( " " );
          p.stmtAppend( pTableDiff.nameNew );
          if( pInlineCommentDiff.column_nameNew != null )
          {
            p.stmtAppend( "." );
            p.stmtAppend( pInlineCommentDiff.column_nameNew );
          }
          p.stmtAppend( "is" );
          p.stmtAppend( "'" + pInlineCommentDiff.commentNew.replace( "'", "''" ) + "'" );
          p.stmtDone();
        }, new StatementBuilder() );
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

  private void handleUniquekey( TableDiff pTableDiff, UniqueKeyDiff pUniqueKeyDiff )
  {
    if( pUniqueKeyDiff.isMatched == false || isRecreateNeeded( pUniqueKeyDiff ) == true )
    {
      doInDiffActionCreate( pUniqueKeyDiff, p ->
      {
        p.stmtStart( "alter table " + pTableDiff.nameNew + " add constraint " + pUniqueKeyDiff.consNameNew + " unique (" + getColumnList( pUniqueKeyDiff.uk_columnsDiff ) + ")" );
        if( pUniqueKeyDiff.tablespaceNew != null )
        {
          p.stmtAppend( "using index tablespace " + pUniqueKeyDiff.tablespaceNew );
        }
        else
        {
          if( pUniqueKeyDiff.indexnameNew != null && !pUniqueKeyDiff.indexnameNew.equals( pUniqueKeyDiff.consNameNew ) )
          {
            p.stmtAppend( "using index " + pUniqueKeyDiff.indexnameNew );
          }
        }
        if( pUniqueKeyDiff.statusNew != null )
        {
          p.stmtAppend( pUniqueKeyDiff.statusNew.getName() );
        }

        p.stmtDone();
      } );
    }
  }

  private void handleIndex( TableDiff pTableDiff, IndexDiff pIndexDiff )
  {
    if( pIndexDiff.isMatched == false || isRecreateNeeded( pIndexDiff ) == true )
    {
      doInDiffActionCreate( pIndexDiff, p ->
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

        if( pIndexDiff.parallelNew == ParallelType.PARALLEL || isIndexparallelcreate() )
        {
          p.stmtAppend( "parallel" );
          if( pIndexDiff.parallel_degreeNew != null && pIndexDiff.parallel_degreeNew > 1 )
          {
            p.stmtAppend( " " + pIndexDiff.parallel_degreeNew );
          }
        }

        p.stmtDone();

        if( pIndexDiff.parallelNew != ParallelType.PARALLEL && isIndexparallelcreate() )
        {
          p.addStmt( "alter index " + pIndexDiff.consNameNew + " noparallel" );
        }
      } );
    }
    else
    {
      doInDiffActionAlter( pIndexDiff, p ->
      {
        if( pIndexDiff.parallelIsEqual == false || pIndexDiff.parallel_degreeIsEqual == false )
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

          p.stmtDone( INDEX__PARALLEL );
        }

        if( pIndexDiff.loggingIsEqual == false )
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

          p.stmtDone( INDEX__LOGGING );
        }

        if( pIndexDiff.tablespaceIsEqual == false && !(pIndexDiff.tablespaceOld == null && pIndexDiff.tablespaceNew == null) && isIndexmovetablespace() == true )
        {
          p.stmtStart( "alter index" );
          p.stmtAppend( pIndexDiff.consNameNew );
          p.stmtAppend( "rebuild tablespace" );
          p.stmtAppend( nvl( pIndexDiff.tablespaceNew, getDefaultTablespace() ) );
          p.stmtDone( INDEX_OR_UNIQUE_KEY__TABLESPACE );
        }
      } );
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

  private void handleConstraint( TableDiff pTableDiff, ConstraintDiff pConstraintDiff )
  {
    if( pConstraintDiff.isMatched == false || isRecreateNeeded( pConstraintDiff ) == true )
    {
      doInDiffActionCreate( pConstraintDiff, p ->
      {
        p.stmtStart( "alter table " + pTableDiff.nameNew + " add constraint " + pConstraintDiff.consNameNew + " check (" + pConstraintDiff.ruleNew + ")" );
        if( pConstraintDiff.deferrtypeNew != null )
        {
          p.stmtAppend( "deferrable initially " + pConstraintDiff.deferrtypeNew.getName() );
        }
        if( pConstraintDiff.statusNew != null )
        {
          p.stmtAppend( pConstraintDiff.statusNew.getName() );
        }

        p.stmtDone();
      } );
    }
  }

  private void handlePrimarykey( TableDiff pTableDiff )
  {
    if( pTableDiff.primary_keyDiff.isMatched == false || isRecreateNeeded( pTableDiff.primary_keyDiff ) )
    {
      doInDiffActionCreate( pTableDiff.primary_keyDiff, p ->
      {
        p.stmtStart( "alter table " + pTableDiff.nameNew + " add" );
        if( pTableDiff.primary_keyDiff.consNameNew != null )
        {
          p.stmtAppend( "constraint " + pTableDiff.primary_keyDiff.consNameNew );
        }
        p.stmtAppend( "primary key (" + getColumnList( pTableDiff.primary_keyDiff.pk_columnsDiff ) + ")" );

        if( pTableDiff.primary_keyDiff.tablespaceNew != null || pTableDiff.primary_keyDiff.reverseNew != null )
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

        p.stmtDone();
      } );
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
      doInDiffActionCreate( pColumnDiff, p ->
      {
        p.stmtStart( "alter table " + pTableDiff.nameNew + " add " + createColumnCreatePart( pColumnDiff ) );

        if( findLobstorage( pTableDiff, pColumnDiff.nameNew ) != null )
        {
          p.stmtAppend( "lob (" + pColumnDiff.nameNew + ") store as ( tablespace " + findLobstorage( pTableDiff, pColumnDiff.nameNew ).tablespaceNew + " )" );
        }

        p.stmtDone();
      } );
    }
    else
    {
      if( isRecreateNeeded( pColumnDiff ) )
      {
        doInDiffAction( pColumnDiff, Collections.singletonList( new DiffActionReasonDifferent( diffReasonKeyRegistry.getDiffReasonKey( pColumnDiff ) ) ), DiffReasonType.RECREATE, p ->
        {
          recreateColumn( p, pTableDiff, pColumnDiff );
        }, new StatementBuilder() );
      }
      else
      {
        doInDiffActionAlter( pColumnDiff, p ->
        {
          if( pColumnDiff.byteorcharIsEqual == false || pColumnDiff.precisionIsEqual == false || pColumnDiff.scaleIsEqual == false )
          {
            p.addStmt( COLUMN__BYTEORCHAR, "alter table " + pTableDiff.nameNew + " modify ( " + pColumnDiff.nameNew + " " + getColumnDatatype( pColumnDiff ) + ")" );
          }

          if( pColumnDiff.default_valueIsEqual == false )
          {
            p.stmtStart( "alter table " + pTableDiff.nameNew + " modify ( " + pColumnDiff.nameNew + " default" );
            if( pColumnDiff.default_valueNew == null )
            {
              p.stmtAppend( "null" );
            }
            else
            {
              p.stmtAppend( pColumnDiff.default_valueNew );
            }
            p.stmtAppend( ")" );
            p.stmtDone( COLUMN__DEFAULT_VALUE );
          }

          if( pColumnDiff.notnullIsEqual == false )
          {
            p.stmtStart( "alter table " + pTableDiff.nameNew + " modify ( " + pColumnDiff.nameNew );
            if( pColumnDiff.notnullNew == false )
            {
              p.stmtAppend( "null" );
            }
            else
            {
              p.stmtAppend( "not null" );
            }
            p.stmtAppend( ")" );
            p.stmtDone( COLUMN__NOTNULL );
          }
        } );
      }
    }
  }

  private void recreateColumn( StatementBuilder p, TableDiff pTableDiff, ColumnDiff pColumnDiff )
  {
    String lTmpOldColumnameNew = "DTO_" + pColumnDiff.nameNew;
    String lTmpNewColumnameNew = "DTN_" + pColumnDiff.nameNew;

    p.addStmt( "alter table " + pTableDiff.nameNew + " add " + lTmpNewColumnameNew + " " + getColumnDatatype( pColumnDiff ) );

    // TODO for cur_trigger in
    // (
    // select trigger_name
    // from user_triggers
    // where table_name = pTableDiff.nameNew
    // )
    // {
    // add_stmt( "alter trigger " + cur_trigger.trigger_name + " disable" );
    // }

    p.addStmt( "update " + pTableDiff.nameNew + " set " + lTmpNewColumnameNew + " = " + pColumnDiff.nameOld );
    p.addStmt( "commit" );

    // for cur_trigger in
    // (
    // select trigger_name
    // from user_triggers
    // where table_name = pTableDiff.nameNew
    // )
    // {
    // add_stmt( "alter trigger " + cur_trigger.trigger_name + " enable" );
    // }

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

    if( pColumnDiff.notnullNew == true )
    {
      p.stmtStart( "alter table " + pTableDiff.nameNew + " modify ( " + pColumnDiff.nameNew );
      p.stmtAppend( "not null" );
      p.stmtAppend( ")" );
      p.stmtDone();
    }
  }

  private void dropWithDropmodeCheck( StatementBuilder p, String pTestStatement, String pDropStatement )
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
          p.addStmt( "-- dropmode-ignore: " + pDropStatement );
          return;
        }
      }
    }

    p.addStmt( pDropStatement );
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
    return (Boolean) new WrapperReturnValueFromResultSet( pTestStatement, getCallableStatementProvider() )
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

  private void createMview( StatementBuilder p, MviewDiff pMviewDiff )
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

  private void handleMview( MviewDiff pMviewDiff )
  {
    if( pMviewDiff.isMatched == false || isRecreateNeeded( pMviewDiff ) )
    {
      doInDiffActionCreate( pMviewDiff, p ->
      {
        createMview( p, pMviewDiff );
      } );
    }
    else
    {
      doInDiffActionAlter( pMviewDiff, p ->
      {
        if( !pMviewDiff.queryRewriteIsEqual )
        {
          EnableType lQueryRewriteNew = pMviewDiff.queryRewriteNew;

          if( lQueryRewriteNew == null )
          {
            lQueryRewriteNew = EnableType.DISABLE;
          }

          p.addStmt( MVIEW__QUERY_REWRITE, "alter materialized view " + pMviewDiff.mview_nameNew + " " + lQueryRewriteNew.getLiteral() + " query rewrite" );
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
          p.addStmt( MVIEW__REFRESH_MODE, "alter materialized view " + pMviewDiff.mview_nameNew + " " + adjustRefreshmethodLiteral( pMviewDiff.refreshMethodNew.getLiteral() ) + lRefreshmode );
        }

        // Physical parameters nur, wenn nicht prebuilt
        if( pMviewDiff.buildModeNew != BuildModeType.PREBUILT )
        {
          if( pMviewDiff.parallelIsEqual == false || pMviewDiff.parallel_degreeIsEqual == false )
          {
            p.stmtStart( "alter materialized view" );
            p.stmtAppend( pMviewDiff.mview_nameNew );

            handleParallel( p, pMviewDiff.parallelNew, pMviewDiff.parallel_degreeNew, true );

            p.stmtDone( MVIEW__PARALLEL );
          }

          if( pMviewDiff.compressionIsEqual == false || pMviewDiff.compressionForIsEqual == false )
          {
            p.stmtStart( "alter materialized view" );
            p.stmtAppend( pMviewDiff.mview_nameNew );

            handleCompression( p, pMviewDiff.compressionNew, pMviewDiff.compressionForNew, true );

            p.stmtDone( MVIEW__COMPRESSION );
          }
        }
      } );
    }
  }

  private void handleAllMviews( ModelDiff pModelDiff )
  {
    for( MviewDiff lMviewDiff : pModelDiff.model_elementsMviewDiff )
    {
      if( !lMviewDiff.isEqual )
      {
        if( isRecreateNeeded( lMviewDiff ) || !lMviewDiff.isNew )
        {
          doInDiffActionDrop( lMviewDiff, p ->
          {
            p.addStmt( "drop materialized view " + lMviewDiff.mview_nameOld );
          } );
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

  private void createMviewlog( StatementBuilder p, TableDiff pTableDiff )
  {
    String c_date_format = _parameters.getDateformat();
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

  private void handleMviewlog( TableDiff pTableDiff )
  {
    if( !pTableDiff.mviewLogDiff.isOld || isRecreateNeeded( pTableDiff.mviewLogDiff ) )
    {
      doInDiffActionCreate( pTableDiff.mviewLogDiff, p ->
      {
        createMviewlog( p, pTableDiff );
      } );
    }
    else
    {
      doInDiffActionAlter( pTableDiff.mviewLogDiff, p ->
      {
        if( pTableDiff.mviewLogDiff.parallelIsEqual == false || pTableDiff.mviewLogDiff.parallel_degreeIsEqual == false )
        {
          p.stmtStart( "alter materialized view log on" );
          p.stmtAppend( pTableDiff.nameNew );
          handleParallel( p, pTableDiff.mviewLogDiff.parallelNew, pTableDiff.mviewLogDiff.parallel_degreeNew, true );

          p.stmtDone( MVIEW_LOG__PARALLEL );
        }

        if( pTableDiff.mviewLogDiff.newValuesIsEqual == false )
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

          p.stmtDone( MVIEW_LOG__NEW_VALUES );
        }

        if( pTableDiff.mviewLogDiff.startWithIsEqual == false || pTableDiff.mviewLogDiff.nextIsEqual == false || pTableDiff.mviewLogDiff.repeatIntervalIsEqual == false )
        {
          p.stmtStart( "alter materialized view log on" );
          p.stmtAppend( pTableDiff.nameNew );
          p.stmtAppend( "purge" );
          if( pTableDiff.mviewLogDiff.startWithIsEqual == false )
          {
            p.stmtAppend( "start with" );
            p.stmtAppend( "to_date('" + pTableDiff.mviewLogDiff.startWithNew + "','" + _parameters.getDateformat() + "')" );
          }
          if( pTableDiff.mviewLogDiff.nextIsEqual == false )
          {
            p.stmtAppend( "next" );
            p.stmtAppend( "to_date('" + pTableDiff.mviewLogDiff.nextNew + "','" + _parameters.getDateformat() + "')" );
          }
          else
          {
            if( pTableDiff.mviewLogDiff.repeatIntervalIsEqual == false )
            {
              p.stmtAppend( "repeat interval" );
              p.stmtAppend( pTableDiff.mviewLogDiff.repeatIntervalNew + "" );
            }
          }

          p.stmtDone( MVIEW_LOG__START_WITH );
        }
        else
        {
          if( pTableDiff.mviewLogDiff.synchronousIsEqual == false )
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

            p.stmtDone( MVIEW_LOG__SYNCHRONOUS );
          }
        }
      } );
    }
  }

  private abstract class AbstractStatementBuilder
  {
    String _stmt;

    protected void stmtAppend( String pString )
    {
      _stmt = _stmt + " " + pString;
    }

    protected void stmtStart( String pString )
    {
      _stmt = pString;
    }
  }

  private interface AbstractDiffActionRunnable<T extends AbstractStatementBuilder>
  {
    void run( T p );
  }

  private interface DiffActionRunnable extends AbstractDiffActionRunnable<StatementBuilder>
  {
  }

  private interface DiffActionRunnableAlter extends AbstractDiffActionRunnable<StatementBuilderAlter>
  {
  }

  private class StatementBuilderAlter extends AbstractStatementBuilder
  {
    private DiffActionReasonDifferent diffActionReasonDifferent;

    public StatementBuilderAlter( DiffActionReasonDifferent pDiffActionReasonDifferent )
    {
      diffActionReasonDifferent = pDiffActionReasonDifferent;
    }

    public void addStmt( EAttribute pAlterReason, String pString )
    {
      if( activeDiffAction != null )
      {
        activeDiffAction.addStatement( pString );
      }
      else
      {
        throw new IllegalStateException( "no active diff action: " + pString );
      }

      diffActionReasonDifferent.addDiffReasonDetail( pAlterReason );
    }

    public void stmtDone( EAttribute pAlterReason )
    {
      addStmt( pAlterReason, _stmt );
      _stmt = null;
    }
  }

  class StatementBuilder extends AbstractStatementBuilder
  {
    void addStmt( String pString )
    {
      if( activeDiffAction != null )
      {
        activeDiffAction.addStatement( pString );
      }
      else
      {
        throw new IllegalStateException( "no active diff action: " + pString );
      }
    }

    void stmtDone()
    {
      addStmt( _stmt );
      _stmt = null;
    }
  }
}
