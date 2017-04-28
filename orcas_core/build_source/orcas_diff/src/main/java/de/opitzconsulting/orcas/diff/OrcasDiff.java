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
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.emf.ecore.EStructuralFeature;

import de.opitzconsulting.orcas.diff.DdlBuilder.AbstractStatementBuilder;
import de.opitzconsulting.orcas.diff.DdlBuilder.StatementBuilder;
import de.opitzconsulting.orcas.diff.DiffAction.DiffReasonType;
import de.opitzconsulting.orcas.diff.DiffReasonKey.DiffReasonKeyRegistry;
import de.opitzconsulting.orcas.orig.diff.AbstractDiff;
import de.opitzconsulting.orcas.orig.diff.ColumnDiff;
import de.opitzconsulting.orcas.orig.diff.ColumnRefDiff;
import de.opitzconsulting.orcas.orig.diff.ConstraintDiff;
import de.opitzconsulting.orcas.orig.diff.ForeignKeyDiff;
import de.opitzconsulting.orcas.orig.diff.IndexDiff;
import de.opitzconsulting.orcas.orig.diff.IndexOrUniqueKeyDiff;
import de.opitzconsulting.orcas.orig.diff.InlineCommentDiff;
import de.opitzconsulting.orcas.orig.diff.ModelDiff;
import de.opitzconsulting.orcas.orig.diff.MviewDiff;
import de.opitzconsulting.orcas.orig.diff.SequenceDiff;
import de.opitzconsulting.orcas.orig.diff.TableDiff;
import de.opitzconsulting.orcas.orig.diff.UniqueKeyDiff;
import de.opitzconsulting.orcas.sql.CallableStatementProvider;
import de.opitzconsulting.orcas.sql.WrapperReturnFirstValue;
import de.opitzconsulting.orcas.sql.WrapperReturnValueFromResultSet;
import de.opitzconsulting.origOrcasDsl.Model;

public class OrcasDiff
{
  private Parameters _parameters;
  private RecreateNeededRegistry recreateNeededRegistry;
  private DiffReasonKeyRegistry diffReasonKeyRegistry;
  private DdlBuilder ddlBuilder;
  private List<DiffAction> diffActions = new ArrayList<DiffAction>();
  private DiffAction activeDiffAction;
  private DataHandler dataHandler;

  public OrcasDiff( CallableStatementProvider pCallableStatementProvider, Parameters pParameters, DatabaseHandler pDatabaseHandler )
  {
    _parameters = pParameters;

    dataHandler = new DataHandler( pCallableStatementProvider, pParameters );

    ddlBuilder = pDatabaseHandler.createDdlBuilder();
  }

  private List<DiffActionReason> getIndexRecreate( TableDiff pTableDiff, String pIndexname )
  {
    for( IndexOrUniqueKeyDiff lIndexOrUniqueKeyDiff : pTableDiff.ind_uksIndexDiff )
    {
      if( lIndexOrUniqueKeyDiff.consNameNew.equals( pIndexname ) )
      {
        if( isRecreateNeeded( lIndexOrUniqueKeyDiff ) )
        {
          return recreateNeededRegistry.getRecreateNeededReasons( lIndexOrUniqueKeyDiff );
        }
        else
        {
          return Collections.emptyList();
        }
      }
    }

    throw new RuntimeException( "index not found: " + pIndexname + " " + pTableDiff.nameNew );
  }

  private List<EStructuralFeature> isRecreateColumn( ColumnDiff pColumnDiff )
  {
    List<EStructuralFeature> lReturn = new ArrayList<>();

    if( pColumnDiff.data_typeNew != null && pColumnDiff.data_typeOld != null )
    {
      if( !pColumnDiff.data_typeIsEqual )
      {
        lReturn.add( COLUMN__DATA_TYPE );
      }

      if( isLessTahnOrNull( pColumnDiff.precisionNew, pColumnDiff.precisionOld ) )
      {
        lReturn.add( COLUMN__PRECISION );
      }

      if( isLessTahnOrNull( pColumnDiff.scaleNew, pColumnDiff.scaleOld ) )
      {
        lReturn.add( COLUMN__SCALE );
      }
    }

    if( !pColumnDiff.object_typeIsEqual )
    {
      lReturn.add( COLUMN__OBJECT_TYPE );
    }

    return lReturn;
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
        setRecreateNeededFor( lTableDiff )//
        .ifDifferent( TABLE__PERMANENTNESS )//
        .ifDifferent( TABLE__TRANSACTION_CONTROL )//
        .calculate();

        Map<String, List<DiffActionReason>> lRecreateColumnNames = new HashMap<>();

        for( ColumnDiff lColumnDiff : lTableDiff.columnsDiff )
        {
          setRecreateNeededFor( lColumnDiff )//
          .ifX( p ->
          {
            List<EStructuralFeature> lRecreateColumn = isRecreateColumn( p.getDiff() );
            if( !lRecreateColumn.isEmpty() )
            {
              p.setRecreateNeededDifferent( lRecreateColumn );
              lRecreateColumnNames.put( lColumnDiff.nameOld, recreateNeededRegistry.getRecreateNeededReasons( lColumnDiff ) );
            }
          } )//
          .calculate();
        }

        setRecreateNeededFor( lTableDiff.primary_keyDiff )//
        .ifDifferent( PRIMARY_KEY__CONS_NAME )//
        .ifDifferent( PRIMARY_KEY__PK_COLUMNS )//
        .ifDifferent( PRIMARY_KEY__REVERSE )//
        .ifDifferent( PRIMARY_KEY__TABLESPACE, _parameters.isIndexmovetablespace() )//
        .ifColumnDependentRecreate( lRecreateColumnNames, lTableDiff.primary_keyDiff.pk_columnsDiff )//
        .calculate();

        for( IndexDiff lIndexDiff : lTableDiff.ind_uksIndexDiff )
        {
          // domain index cant be compared
          boolean lNoDomainIndex = lIndexDiff.domain_index_expressionNew == null;

          setRecreateNeededFor( lIndexDiff )//
          .ifDifferent( INDEX__INDEX_COLUMNS, lNoDomainIndex )//
          .ifDifferent( INDEX__FUNCTION_BASED_EXPRESSION, lNoDomainIndex )//
          .ifDifferent( INDEX__DOMAIN_INDEX_EXPRESSION, lNoDomainIndex )//
          .ifDifferent( INDEX__UNIQUE )//
          .ifDifferent( INDEX__BITMAP )//
          .ifDifferent( INDEX__GLOBAL )//
          .ifDifferent( INDEX__COMPRESSION )//
          .ifColumnDependentRecreate( lRecreateColumnNames, lIndexDiff.index_columnsDiff )//
          .calculate();
        }

        for( UniqueKeyDiff lUniqueKeyDiff : lTableDiff.ind_uksUniqueKeyDiff )
        {
          setRecreateNeededFor( lUniqueKeyDiff )//
          .ifDifferent( UNIQUE_KEY__UK_COLUMNS )//
          .ifDifferent( UNIQUE_KEY__INDEXNAME )//
          .ifDifferent( INDEX_OR_UNIQUE_KEY__TABLESPACE, _parameters.isIndexmovetablespace() )//
          .ifColumnDependentRecreate( lRecreateColumnNames, lUniqueKeyDiff.uk_columnsDiff )//
          .ifX( p ->
          {
            if( lUniqueKeyDiff.indexnameNew != null )
            {
              List<DiffActionReason> lIndexRecreate = getIndexRecreate( lTableDiff, lUniqueKeyDiff.indexnameNew );

              if( !lIndexRecreate.isEmpty() )
              {
                p.setRecreateNeededDependsOn( lIndexRecreate );
              }
            }
          } ).calculate();
        }

        for( ConstraintDiff lConstraintDiff : lTableDiff.constraintsDiff )
        {
          setRecreateNeededFor( lConstraintDiff )//
          .ifDifferent( CONSTRAINT__RULE )//
          .ifDifferent( CONSTRAINT__DEFERRTYPE )//
          .calculate();
        }

        for( ForeignKeyDiff lForeignKeyDiff : lTableDiff.foreign_keysDiff )
        {
          setRecreateNeededFor( lForeignKeyDiff )//
          .ifDifferent( FOREIGN_KEY__CONS_NAME )//
          .ifDifferent( FOREIGN_KEY__DEFERRTYPE )//
          .ifDifferent( FOREIGN_KEY__DELETE_RULE )//
          .ifDifferent( FOREIGN_KEY__DEST_COLUMNS )//
          .ifDifferent( FOREIGN_KEY__DEST_TABLE )//
          .ifDifferent( FOREIGN_KEY__SRC_COLUMNS )//
          .ifDifferent( FOREIGN_KEY__STATUS )//
          .ifColumnDependentRecreate( lRecreateColumnNames, lForeignKeyDiff.srcColumnsDiff )//
          .calculate();
        }

        setRecreateNeededFor( lTableDiff.mviewLogDiff )//
        .ifDifferent( MVIEW_LOG__COLUMNS )//
        .ifDifferent( MVIEW_LOG__PRIMARY_KEY, "rowid".equalsIgnoreCase( lTableDiff.mviewLogDiff.rowidNew ) )//
        .ifX( p ->
        {
          if( lTableDiff.mviewLogDiff.rowidNew == null && !"primary".equalsIgnoreCase( lTableDiff.mviewLogDiff.primaryKeyOld ) )
          {
            p.setRecreateNeededDifferent( Collections.singletonList( MVIEW_LOG__PRIMARY_KEY ) );
          }
        } )//
        .ifDifferent( MVIEW_LOG__ROWID )//
        .ifDifferent( MVIEW_LOG__WITH_SEQUENCE )//
        .ifDifferent( MVIEW_LOG__COMMIT_SCN )//
        .ifDifferent( MVIEW_LOG__TABLESPACE )//
        // these changes should by applied with alter statements, but it results
        // in ORA-27476
        .ifDifferent( MVIEW_LOG__START_WITH )//
        .ifDifferent( MVIEW_LOG__NEXT )//
        .ifDifferent( MVIEW_LOG__REPEAT_INTERVAL )//
        .calculate();

        for( InlineCommentDiff lCommentDiff : lTableDiff.commentsDiff )
        {
          if( lCommentDiff.column_nameOld != null )
          {
            setRecreateNeededFor( lCommentDiff )//
            .ifColumnDependentRecreate( lRecreateColumnNames, lCommentDiff.column_nameOld )//
            .calculate();
          }
        }
      }
    }

    for(

    MviewDiff lMviewDiff : pModelDiff.model_elementsMviewDiff )
    {
      setRecreateNeededFor( lMviewDiff )//
      .ifDifferent( MVIEW__TABLESPACE )//
      .ifDifferent( MVIEW__BUILD_MODE )//
      .ifX( p ->
      {
        if( !replaceLinefeedBySpace( lMviewDiff.viewSelectCLOBNew ).equals( replaceLinefeedBySpace( lMviewDiff.viewSelectCLOBOld ) ) )
        {
          p.setRecreateNeededDifferent( Collections.singletonList( MVIEW__VIEW_SELECT_CLOB ) );
        }
      } )//
      .calculate();
    }

    for( TableDiff lTableDiff : pModelDiff.model_elementsTableDiff )
    {
      for( ForeignKeyDiff lForeignKeyDiff : lTableDiff.foreign_keysDiff )
      {
        setRecreateNeededFor( lForeignKeyDiff )//
        .ifX( p ->
        {
          List<DiffActionReason> lRefConstraintRecreate = getRefConstraintRecreate( pModelDiff, lForeignKeyDiff.destTableOld, lForeignKeyDiff.destColumnsDiff );

          if( !lRefConstraintRecreate.isEmpty() )
          {
            p.setRecreateNeededDependsOn( lRefConstraintRecreate );
          }
        } )//
        .calculate();
      }
    }
  }

  private <T extends AbstractDiff> RecreateNeededBuilder<T> setRecreateNeededFor( T pDiff )
  {
    return recreateNeededRegistry.createRecreateNeededBuilder( pDiff );
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
          lReturn.addAll( recreateNeededRegistry.getRecreateNeededReasons( lTableDiff ) );
        }

        if( isRecreateNeeded( lTableDiff.primary_keyDiff ) )
        {
          if( isOldColumnNamesEqual( pDestColumnsDiff, lTableDiff.primary_keyDiff.pk_columnsDiff ) )
          {
            lReturn.addAll( recreateNeededRegistry.getRecreateNeededReasons( lTableDiff.primary_keyDiff ) );
          }
        }

        for( UniqueKeyDiff lUniqueKeyDiff : lTableDiff.ind_uksUniqueKeyDiff )
        {
          if( isRecreateNeeded( lUniqueKeyDiff ) )
          {
            if( isOldColumnNamesEqual( pDestColumnsDiff, lUniqueKeyDiff.uk_columnsDiff ) )
            {
              lReturn.addAll( recreateNeededRegistry.getRecreateNeededReasons( lUniqueKeyDiff ) );
            }
          }
        }
      }
    }

    return lReturn;
  }

  private boolean isRecreateNeeded( AbstractDiff pDiff )
  {
    return recreateNeededRegistry.isRecreateNeeded( pDiff );
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

  public DiffResult compare( Model pModelSoll, Model pModelIst )
  {
    ModelDiff lModelDiff = new ModelDiff( pModelSoll );
    lModelDiff.mergeWithOldValue( pModelIst );

    sortTablesForRefPart( lModelDiff );

    diffReasonKeyRegistry = new DiffReasonKey.DiffReasonKeyRegistry( lModelDiff );
    recreateNeededRegistry = new RecreateNeededRegistry( diffReasonKeyRegistry );

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

  private void handleAllSequences( ModelDiff pModelDiff )
  {
    for( SequenceDiff lSequenceDiff : pModelDiff.model_elementsSequenceDiff )
    {
      dropIfNeeded( lSequenceDiff, p -> ddlBuilder.dropSequence( p, lSequenceDiff ) );

      createIfNeededOrAlter( lSequenceDiff, //
      p1 -> ddlBuilder.createSequnece( p1, lSequenceDiff, dataHandler ), //
      p2 -> ddlBuilder.alterSequenceIfNeeded( p2, lSequenceDiff, dataHandler ) );
    }
  }

  private <T> void doInDiffAction( DiffAction pDiffAction, AbstractDiffActionRunnable<T> pRunnable, T pAbstractStatementBuilder )
  {
    activeDiffAction = pDiffAction;
    diffActions.add( pDiffAction );
    pRunnable.accept( pAbstractStatementBuilder );
    if( activeDiffAction.getStatements().isEmpty() )
    {
      diffActions.remove( activeDiffAction );
    }
    activeDiffAction = null;
  }

  private <T> void doInDiffAction( DiffAction pDiffAction, List<DiffActionReason> pDiffActionReasonList, AbstractDiffActionRunnable<T> pRunnable, T pAbstractStatementBuilder )
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
    doInDiffAction( lDiffAction, Collections.singletonList( lDiffActionReasonDifferent ), pRunnable, new StatementBuilderAlter( lDiffActionReasonDifferent, pDiff ) );
  }

  private void doInDiffActionDrop( AbstractDiff pDiff, DiffActionRunnable pRunnable )
  {
    boolean lRecreateNeeded = isRecreateNeeded( pDiff );
    DiffAction lDiffAction = new DiffAction( diffReasonKeyRegistry.getDiffReasonKey( pDiff ), lRecreateNeeded ? DiffReasonType.RECREATE_DROP : DiffReasonType.DROP );

    doInDiffAction( lDiffAction, lRecreateNeeded ? recreateNeededRegistry.getRecreateNeededReasons( pDiff ) : Collections.singletonList( new DiffActionReasonSurplus( diffReasonKeyRegistry.getDiffReasonKey( pDiff ) ) ), pRunnable, createStatementBuilder() );
  }

  private void doInDiffActionCreate( AbstractDiff pDiff, DiffActionRunnable pRunnable )
  {
    boolean lRecreateNeeded = isRecreateNeeded( pDiff );
    DiffAction lDiffAction = new DiffAction( diffReasonKeyRegistry.getDiffReasonKey( pDiff ), lRecreateNeeded ? DiffReasonType.RECREATE_CREATE : DiffReasonType.CREATE );

    doInDiffAction( lDiffAction, lRecreateNeeded ? recreateNeededRegistry.getRecreateNeededReasons( pDiff ) : Collections.singletonList( new DiffActionReasonMissing( diffReasonKeyRegistry.getDiffReasonKey( pDiff ) ) ), pRunnable, createStatementBuilder() );
  }

  private void dropIfNeeded( AbstractDiff pDiff, DiffActionRunnable pRunnable )
  {
    dropIfNeeded( pDiff, pRunnable, null );
  }

  private void dropIfNeeded( AbstractDiff pDiff, DiffActionRunnable pRunnable, Runnable pNoDropHandler )
  {
    if( pDiff.isOld && (pDiff.isMatched == false || isRecreateNeeded( pDiff )) )
    {
      doInDiffActionDrop( pDiff, pRunnable );
    }
    else
    {
      if( pNoDropHandler != null )
      {
        pNoDropHandler.run();
      }
    }
  }

  private <T extends AbstractDiff> void dropIfNeeded( List<T> pDiffList, DiffActionRunnableWithDiff<T> pRunnable )
  {
    for( T lDiff : pDiffList )
    {
      dropIfNeeded( lDiff, p ->
      {
        pRunnable.accept( new StatementBuilderWithDiff<>( () -> activeDiffAction, lDiff ) );
      } );
    }
  }

  private void handleAllTables( ModelDiff pModelDiff )
  {
    for( TableDiff lTableDiff : pModelDiff.model_elementsTableDiff )
    {
      dropIfNeeded( lTableDiff.foreign_keysDiff, p -> ddlBuilder.dropForeignKey( p, lTableDiff, p.diff ) );
    }

    for( TableDiff lTableDiff : pModelDiff.model_elementsTableDiff )
    {
      dropIfNeeded( lTableDiff, p -> ddlBuilder.dropTable( p, lTableDiff, dataHandler ), //
      () ->
      {
        dropIfNeeded( lTableDiff.constraintsDiff, p -> ddlBuilder.dropConstraint( p, lTableDiff, p.diff ) );

        dropIfNeeded( lTableDiff.mviewLogDiff, p -> ddlBuilder.dropMaterializedViewLog( p, lTableDiff ) );

        dropIfNeeded( lTableDiff.ind_uksUniqueKeyDiff, p -> ddlBuilder.dropUniqueKey( p, lTableDiff, p.diff ) );

        dropIfNeeded( lTableDiff.ind_uksIndexDiff, p -> ddlBuilder.dropIndex( p, p.diff ) );

        for( InlineCommentDiff lCommentDiff : lTableDiff.commentsDiff )
        {
          if( lCommentDiff.isOld && lCommentDiff.isMatched == false )
          {
            boolean lIsColumnComment = lCommentDiff.column_nameOld != null;
            if( !lIsColumnComment || columnIsNew( lTableDiff, lCommentDiff.column_nameOld ) )
            {
              doInDiffActionDrop( lCommentDiff, p -> ddlBuilder.dropComment( p, lTableDiff, lCommentDiff ) );
            }
          }
        }

        dropIfNeeded( lTableDiff.primary_keyDiff, p -> ddlBuilder.dropPrimaryKey( p, lTableDiff, lTableDiff.primary_keyDiff ) );
      } );
    }

    pModelDiff.model_elementsTableDiff.forEach( p ->
    {
      createIfNeededOrAlter( p, //
      p4 -> ddlBuilder.createTable( p4, p ), //
      () ->
      {
        for( ColumnDiff lColumnDiff : p.columnsDiff )
        {
          if( lColumnDiff.isNew )
          {
            handleColumn( p, lColumnDiff );
          }
          else
          {
            doInDiffActionDrop( lColumnDiff, p6 -> ddlBuilder.dropColumn( p6, p, lColumnDiff, dataHandler ) );
          }
        }
      }, //
      p1 -> ddlBuilder.alterTableIfNeeded( p1, p, _parameters.isTablemovetablespace(), getDefaultTablespace() ), //
      () ->
      {
        handlePrimarykey( p );

        p.constraintsDiff.forEach( p2 -> handleConstraint( p, p2 ) );

        p.ind_uksIndexDiff.forEach( p3 -> handleIndex( p, p3 ) );

        p.ind_uksUniqueKeyDiff.forEach( p5 -> handleUniquekey( p, p5 ) );

        p.commentsDiff.forEach( p7 -> handleComment( p, p7 ) );

        handleMviewlog( p );
      } );
    } );

    for( TableDiff lTableDiff : pModelDiff.model_elementsTableDiff )
    {
      for( ForeignKeyDiff lForeignKeyDiff : lTableDiff.foreign_keysDiff )
      {
        if( lTableDiff.isOld == false && lTableDiff.tablePartitioningRefPartitionsDiff.isNew && lTableDiff.tablePartitioningRefPartitionsDiff.fkNameNew.equalsIgnoreCase( lForeignKeyDiff.consNameNew ) )
        {
          // fk for ref-partition created in create-table
        }
        else
        {
          createIfNeeded( lForeignKeyDiff, p -> ddlBuilder.createForeignKey( p, lTableDiff, lForeignKeyDiff, _parameters.getMultiSchema(), dataHandler ) );
        }
      }
    }
  }

  private void handleComment( TableDiff pTableDiff, InlineCommentDiff pInlineCommentDiff )
  {
    if( pInlineCommentDiff.isEqual == false || isRecreateNeeded( pInlineCommentDiff ) )
    {
      if( pInlineCommentDiff.isNew )
      {
        DiffReasonType lDiffReasonType;
        List<DiffActionReason> lDiffActionReasonList;
        if( isRecreateNeeded( pInlineCommentDiff ) )
        {
          lDiffReasonType = DiffReasonType.RECREATE_CREATE;
          lDiffActionReasonList = recreateNeededRegistry.getRecreateNeededReasons( pInlineCommentDiff );
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

        doInDiffAction( pInlineCommentDiff, lDiffActionReasonList, lDiffReasonType, p -> ddlBuilder.setComment( p, pTableDiff, pInlineCommentDiff ), createStatementBuilder() );
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
    createIfNeeded( pUniqueKeyDiff, p -> ddlBuilder.createUniqueKey( p, pTableDiff, pUniqueKeyDiff ) );
  }

  private void handleIndex( TableDiff pTableDiff, IndexDiff pIndexDiff )
  {
    createIfNeededOrAlter( pIndexDiff, //
    p -> ddlBuilder.createIndex( p, pTableDiff, pIndexDiff, _parameters.isIndexparallelcreate() ), //
    p -> ddlBuilder.alterIndexIfNeeded( p, pIndexDiff, _parameters.isIndexmovetablespace(), getDefaultTablespace() ) );
  }

  private String getDefaultTablespace()
  {
    return InitDiffRepository.getDefaultTablespace();
  }

  private void handleConstraint( TableDiff pTableDiff, ConstraintDiff pConstraintDiff )
  {
    createIfNeeded( pConstraintDiff, p -> ddlBuilder.createConstraint( p, pTableDiff, pConstraintDiff ) );
  }

  private void handlePrimarykey( TableDiff pTableDiff )
  {
    createIfNeeded( pTableDiff.primary_keyDiff, p -> ddlBuilder.createPrimarykey( p, pTableDiff ) );
  }

  private void handleColumn( TableDiff pTableDiff, ColumnDiff pColumnDiff )
  {
    if( isRecreateNeeded( pColumnDiff ) )
    {
      doInDiffAction( pColumnDiff, Collections.singletonList( new DiffActionReasonDifferent( diffReasonKeyRegistry.getDiffReasonKey( pColumnDiff ) ) ), DiffReasonType.RECREATE, p ->
      {
        ddlBuilder.recreateColumn( p, pTableDiff, pColumnDiff );
      }, createStatementBuilder() );
    }
    else
    {
      createIfNeededOrAlter( pColumnDiff, //
      p -> ddlBuilder.createColumn( p, pTableDiff, pColumnDiff ), //
      p -> ddlBuilder.alterColumnIfNeeded( p, pTableDiff, pColumnDiff ) );
    }
  }

  static ForeignKeyDiff getFkForRefPartitioning( TableDiff pTableDiff )
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

  private void createIfNeeded( AbstractDiff pDiff, DiffActionRunnable pRunnableCreate )
  {
    createIfNeededOrAlter( pDiff, pRunnableCreate, null );
  }

  private void createIfNeededOrAlter( AbstractDiff pDiff, DiffActionRunnable pRunnableCreate, DiffActionRunnableAlter pRunnableAlter )
  {
    createIfNeededOrAlter( pDiff, pRunnableCreate, null, pRunnableAlter, null );
  }

  private void createIfNeededOrAlter( AbstractDiff pDiff, DiffActionRunnable pRunnableCreate, Runnable pHandleDetailsAlterOnly, DiffActionRunnableAlter pRunnableAlter, Runnable pHandleDetailsCreateOrAlter )
  {
    if( pDiff.isNew )
    {
      if( pDiff.isOld == false || isRecreateNeeded( pDiff ) )
      {
        doInDiffActionCreate( pDiff, pRunnableCreate );

        if( pHandleDetailsCreateOrAlter != null )
        {
          pHandleDetailsCreateOrAlter.run();
        }
      }
      else
      {
        if( pRunnableAlter != null )
        {
          doInDiffActionAlter( pDiff, pRunnableAlter );
        }

        if( pHandleDetailsAlterOnly != null )
        {
          pHandleDetailsAlterOnly.run();
        }

        if( pHandleDetailsCreateOrAlter != null )
        {
          pHandleDetailsCreateOrAlter.run();
        }
      }
    }
  }

  private void handleAllMviews( ModelDiff pModelDiff )
  {
    for( MviewDiff lMviewDiff : pModelDiff.model_elementsMviewDiff )
    {
      dropIfNeeded( lMviewDiff, p -> ddlBuilder.dropMview( p, lMviewDiff ) );

      createIfNeededOrAlter( lMviewDiff, //
      p2 -> ddlBuilder.createMview( p2, lMviewDiff ), //
      p1 -> ddlBuilder.alterMviewIfNeeded( p1, lMviewDiff ) );
    }
  }

  private void handleMviewlog( TableDiff pTableDiff )
  {
    createIfNeededOrAlter( pTableDiff.mviewLogDiff, //
    p -> ddlBuilder.createMviewlog( p, pTableDiff, _parameters.getDateformat() ), //
    p -> ddlBuilder.alterMviewlogIfNeeded( p, pTableDiff, _parameters.getDateformat() ) );
  }

  private interface AbstractDiffActionRunnable<T> extends Consumer<T>
  {
  }

  private interface DiffActionRunnable extends AbstractDiffActionRunnable<StatementBuilder>
  {
  }

  private interface DiffActionRunnableWithDiff<T extends AbstractDiff> extends Consumer<StatementBuilderWithDiff<T>>
  {
  }

  private interface DiffActionRunnableAlter extends AbstractDiffActionRunnable<StatementBuilderAlter>
  {
  }

  public class StatementBuilderAlter
  {
    private DiffActionReasonDifferent diffActionReasonDifferent;
    private AbstractDiff diff;

    public StatementBuilderAlter( DiffActionReasonDifferent pDiffActionReasonDifferent, AbstractDiff pDiff )
    {
      diffActionReasonDifferent = pDiffActionReasonDifferent;
      diff = pDiff;
    }

    public AlterBuilder handleAlterBuilder()
    {
      return new AlterBuilder();
    }

    public class AlterBuilder
    {
      private List<EStructuralFeature> checkDifferentEStructuralFeatureList = new ArrayList<>();
      private List<EStructuralFeature> forceDifferentEStructuralFeatureList = new ArrayList<>();

      public AlterBuilder ifDifferent( EStructuralFeature pEStructuralFeature )
      {
        checkDifferentEStructuralFeatureList.add( pEStructuralFeature );
        return this;
      }

      public AlterBuilder forceDifferent( EStructuralFeature pEStructuralFeature )
      {
        forceDifferentEStructuralFeatureList.add( pEStructuralFeature );
        return this;
      }

      public AlterBuilder ifDifferent( EStructuralFeature pEStructuralFeature, boolean pUseIt )
      {
        if( pUseIt )
        {
          checkDifferentEStructuralFeatureList.add( pEStructuralFeature );
        }

        return this;
      }

      public void handle( Consumer<StatementBuilder> pHanlder )
      {
        List<EStructuralFeature> lDifferentEAttributes = RecreateNeededBuilder.getDifferentEAttributes( diff, checkDifferentEStructuralFeatureList );
        lDifferentEAttributes.addAll( forceDifferentEStructuralFeatureList );

        if( !lDifferentEAttributes.isEmpty() )
        {
          for( EStructuralFeature lEStructuralFeature : lDifferentEAttributes )
          {
            diffActionReasonDifferent.addDiffReasonDetail( lEStructuralFeature );
          }

          pHanlder.accept( createStatementBuilder() );
        }
      }
    }
  }

  private StatementBuilder createStatementBuilder()
  {
    return new StatementBuilder( () -> activeDiffAction );
  }

  class StatementBuilderWithDiff<T extends AbstractDiff> extends StatementBuilder
  {
    T diff;

    public StatementBuilderWithDiff( Supplier<DiffAction> pDiffActionSupplier, T pDiff )
    {
      super( pDiffActionSupplier );
      diff = pDiff;
    }
  }

  public static class DataHandler
  {
    private CallableStatementProvider _callableStatementProvider;
    private Parameters _parameters;

    public DataHandler( CallableStatementProvider pCallableStatementProvider, Parameters pParameters )
    {
      _callableStatementProvider = pCallableStatementProvider;
      _parameters = pParameters;
    }

    public boolean hasRowsIgnoreExceptions( String pTestStatement )
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

    public void dropWithDropmodeCheck( String pTestStatement, Runnable pDropHandler )
    {
      if( !isDropmode() )
      {
        if( hasRows( pTestStatement ) )
        {
          throw new RuntimeException( "drop mode not active, the following statement contains data executed would result in data-loss: " + pTestStatement );
        }
      }

      pDropHandler.run();
    }

    private boolean hasRows( String pTestStatement )
    {
      return (Boolean) new WrapperReturnValueFromResultSet( pTestStatement, _callableStatementProvider )
      {
        @Override
        protected Object getValueFromResultSet( ResultSet pResultSet ) throws SQLException
        {
          return pResultSet.next();
        }
      }.executeForValue();
    }

    public boolean isDropmode()
    {
      return _parameters.isDropmode();
    }

    public BigDecimal getSequenceMaxValueSelectValue( SequenceDiff pSequenceDiff )
    {
      BigDecimal lSollStartValue = null;

      try
      {
        if( pSequenceDiff.max_value_selectNew != null )
        {
          lSollStartValue = (BigDecimal) new WrapperReturnFirstValue( pSequenceDiff.max_value_selectNew, _callableStatementProvider ).executeForValue();

          lSollStartValue = lSollStartValue.add( BigDecimal.valueOf( 1 ) );
        }
      }
      catch( Exception e )
      {
        // kann vorkommen, wenn fuer das select benoetigte Tabellen nicht
        // exisitieren. kann erst richtig korrigiert werden, wenn auch der
        // Tabellenabgleich auf dieses Package umgestellt wurde
      }
      return lSollStartValue;
    }
  }
}
