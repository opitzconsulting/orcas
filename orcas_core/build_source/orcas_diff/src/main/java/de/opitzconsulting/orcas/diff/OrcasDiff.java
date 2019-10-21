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
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EStructuralFeature;

import de.opitzconsulting.orcas.diff.DiffAction.DiffReasonType;
import de.opitzconsulting.orcas.diff.DiffReasonKey.DiffReasonKeyRegistry;
import de.opitzconsulting.orcas.diff.RecreateNeededBuilder.Difference;
import de.opitzconsulting.orcas.diff.RecreateNeededBuilder.DifferenceImpl;
import de.opitzconsulting.orcas.diff.RecreateNeededBuilder.DifferenceImplAttributeOnly;
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
  private static Log _log = LogFactory.getLog( OrcasDiff.class );

  private Parameters _parameters;
  private RecreateNeededRegistry recreateNeededRegistry;
  private List<AbstractDiff> implicitDropList;
  private DiffReasonKeyRegistry diffReasonKeyRegistry;
  private DdlBuilder ddlBuilder;
  private List<DiffAction> diffActions = new ArrayList<DiffAction>();
  private DiffAction activeDiffAction;
  private DataHandler dataHandler;
  private AlterTableCombiner currentAlterTableCombiner;
  private Map<String, List<String>> oldContraintNames = new HashMap<>();
  private List<String> oldObjectNames = new ArrayList<>();
  private Map<String, List<String>> newContraintNames = new HashMap<>();
  private List<String> newObjectNames = new ArrayList<>();
  private DatabaseHandler databaseHandler;

  public OrcasDiff( CallableStatementProvider pCallableStatementProvider, Parameters pParameters, DatabaseHandler pDatabaseHandler )
  {
    _parameters = pParameters;

    dataHandler = new DataHandler( pCallableStatementProvider, pParameters );

    ddlBuilder = pDatabaseHandler.createDdlBuilder( pParameters );
    databaseHandler = pDatabaseHandler;
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

  private List<Difference> isRecreateColumn( ColumnDiff pColumnDiff )
  {
    List<Difference> lReturn = new ArrayList<>();

    if( pColumnDiff.data_typeNew != null && pColumnDiff.data_typeOld != null )
    {
      if( !pColumnDiff.data_typeIsEqual )
      {
        lReturn.add(new DifferenceImpl(COLUMN__DATA_TYPE, pColumnDiff));
      }

      if( isLessTahnOrNull( pColumnDiff.precisionNew, pColumnDiff.precisionOld ) )
      {
        lReturn.add(new DifferenceImpl(COLUMN__PRECISION, pColumnDiff));
      }

      if( isLessTahnOrNull( pColumnDiff.scaleNew, pColumnDiff.scaleOld ) )
      {
        lReturn.add(new DifferenceImpl(COLUMN__SCALE, pColumnDiff));
      }
    }

    if( !pColumnDiff.object_typeIsEqual )
    {
      lReturn.add(new DifferenceImpl(COLUMN__OBJECT_TYPE, pColumnDiff));
    }

    if( !pColumnDiff.unsignedIsEqual )
    {
      lReturn.add(new DifferenceImpl(COLUMN__UNSIGNED, pColumnDiff));
    }

    if( !pColumnDiff.virtualIsEqual )
    {
      lReturn.add(new DifferenceImpl(COLUMN__VIRTUAL, pColumnDiff));
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

  private void loadNames( ModelDiff pModelDiff, boolean pIsNew )
  {
    Map<String, List<String>> lContraintNames;
    List<String> lObjectNames;
    Predicate<AbstractDiff> lPredicate;

    if( pIsNew )
    {
      lPredicate = p -> p.isNew;
      lContraintNames = newContraintNames;
      lObjectNames = newObjectNames;
    }
    else
    {
      lPredicate = p -> p.isOld;
      lContraintNames = oldContraintNames;
      lObjectNames = oldObjectNames;
    }

    pModelDiff.model_elementsTableDiff//
    .stream()//
    .filter( lPredicate )//
    .forEach( lTableDiff ->
    {
      String lTableName = (String) lTableDiff.getValue( TABLE__NAME, pIsNew );
      lObjectNames.add( lTableName );

      Consumer<String> lContraintNameHandler = p ->
      {
        List<String> lContraintNameList = lContraintNames.get( lTableName );

        if( lContraintNameList == null )
        {
          lContraintNameList = new ArrayList<>();
          lContraintNames.put( lTableName, lContraintNameList );
        }

        lContraintNameList.add( p );
      };

      Optional.of( lTableDiff.primary_keyDiff )//
      .filter( lPredicate )//
      .map( getNameFunction( PRIMARY_KEY__CONS_NAME, pIsNew ) )//
      .ifPresent( lContraintNameHandler );

      lTableDiff.ind_uksIndexDiff//
      .stream()//
      .filter( lPredicate )//
      .map( getNameFunction( INDEX_OR_UNIQUE_KEY__CONS_NAME, pIsNew ) )//
      .forEach( lObjectNames::add );

      lTableDiff.ind_uksUniqueKeyDiff//
      .stream()//
      .filter( lPredicate )//
      .map( getNameFunction( INDEX_OR_UNIQUE_KEY__CONS_NAME, pIsNew ) )//
      .forEach( lContraintNameHandler );

      lTableDiff.constraintsDiff//
      .stream()//
      .filter( lPredicate )//
      .map( getNameFunction( CONSTRAINT__CONS_NAME, pIsNew ) )//
      .forEach( lContraintNameHandler );

      lTableDiff.foreign_keysDiff//
      .stream()//
      .filter( lPredicate )//
      .map( getNameFunction( FOREIGN_KEY__CONS_NAME, pIsNew ) )//
      .forEach( lContraintNameHandler );
    } );

    pModelDiff.model_elementsMviewDiff//
    .stream()//
    .filter( lPredicate )//
    .map( getNameFunction( MVIEW__MVIEW_NAME, pIsNew ) )//
    .forEach( lObjectNames::add );

    pModelDiff.model_elementsSequenceDiff//
    .stream()//
    .filter( lPredicate )//
    .map( getNameFunction( SEQUENCE__SEQUENCE_NAME, pIsNew ) )//
    .forEach( lObjectNames::add );
  }

  private Function<? super AbstractDiff, ? extends String> getNameFunction( EAttribute pAttribute, boolean pIsNew )
  {
    Function<? super AbstractDiff, ? extends String> lFunction = p -> (String) p.getValue( pAttribute, pIsNew );
    return lFunction;
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
            List<Difference> lRecreateColumn = isRecreateColumn( p.getDiff() );
            if( !lRecreateColumn.isEmpty() )
            {
              p.setRecreateNeededDifferent( lRecreateColumn );
              lRecreateColumnNames.put( lColumnDiff.nameOld, recreateNeededRegistry.getRecreateNeededReasons( lColumnDiff ) );
            }
          } )//
          .calculate();
        }

        setRecreateNeededFor( lTableDiff.primary_keyDiff )//
        .ifDifferentName( PRIMARY_KEY__CONS_NAME, oldContraintNames, lTableDiff.primary_keyDiff.consNameNew, lTableDiff.primary_keyDiff.consNameOld, databaseHandler.isRenamePrimaryKey() )//
        .ifDifferent( PRIMARY_KEY__PK_COLUMNS )//
        .ifDifferent( PRIMARY_KEY__REVERSE )//
        .ifDifferent( PRIMARY_KEY__INDEXNAME )//
        .ifDifferent( PRIMARY_KEY__TABLESPACE, _parameters.isIndexmovetablespace() )//
        .ifColumnDependentRecreate( lRecreateColumnNames, lTableDiff.primary_keyDiff.pk_columnsDiff )//
        .calculate();

        for( IndexDiff lIndexDiff : lTableDiff.ind_uksIndexDiff )
        {
          // domain index cant be compared
          boolean lNoDomainIndex = lIndexDiff.domain_index_expressionNew == null;

          // prevent recreate if column-list equals function based expression
          boolean lExpressionDifferent = !Objects.equals(ddlBuilder.getIndexExpression(lIndexDiff,true),ddlBuilder.getIndexExpression(lIndexDiff,false));

          setRecreateNeededFor( lIndexDiff )//
          .ifDifferentName( INDEX_OR_UNIQUE_KEY__CONS_NAME, oldObjectNames, lIndexDiff.consNameNew, lIndexDiff.consNameOld, databaseHandler.isRenameIndex() )//
          .ifDifferent( INDEX__INDEX_COLUMNS, lNoDomainIndex && lExpressionDifferent )//
          .ifDifferent( INDEX__FUNCTION_BASED_EXPRESSION, lNoDomainIndex && lExpressionDifferent )//
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
          .ifDifferentName( INDEX_OR_UNIQUE_KEY__CONS_NAME, oldContraintNames, lUniqueKeyDiff.consNameNew, lUniqueKeyDiff.consNameOld, databaseHandler.isRenameUniqueKey() )//
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
          .ifDifferentName( CONSTRAINT__CONS_NAME, oldContraintNames, lConstraintDiff.consNameNew, lConstraintDiff.consNameOld, databaseHandler.isRenameConstraint() )//
          .ifDifferent( CONSTRAINT__RULE, databaseHandler.isExpressionDifferent(lConstraintDiff.ruleNew,lConstraintDiff.ruleOld) )//
          .ifDifferent( CONSTRAINT__DEFERRTYPE )//
          .calculate();
        }

        for( ForeignKeyDiff lForeignKeyDiff : lTableDiff.foreign_keysDiff )
        {
          setRecreateNeededFor( lForeignKeyDiff )//
          .ifDifferentName( FOREIGN_KEY__CONS_NAME, oldContraintNames, lForeignKeyDiff.consNameNew, lForeignKeyDiff.consNameOld, databaseHandler.isRenameForeignKey() )//
          .ifDifferent( FOREIGN_KEY__DEFERRTYPE )//
          .ifDifferent( FOREIGN_KEY__DELETE_RULE )//
          .ifDifferent( FOREIGN_KEY__DEST_COLUMNS )//
          .ifDifferent( FOREIGN_KEY__DEST_TABLE )//
          .ifDifferent( FOREIGN_KEY__SRC_COLUMNS )//
          //.ifDifferent( FOREIGN_KEY__STATUS )//
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
            p.setRecreateNeededDifferentAttributes( Collections.singletonList( MVIEW_LOG__PRIMARY_KEY ) );
          }
        } )//
        .ifDifferent( MVIEW_LOG__ROWID )//
        .ifDifferent( MVIEW_LOG__WITH_SEQUENCE )//
        .ifDifferent( MVIEW_LOG__COMMIT_SCN )//
        .ifDifferent( MVIEW_LOG__TABLESPACE, _parameters.isMviewlogmovetablespace() )//
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

    for( MviewDiff lMviewDiff : pModelDiff.model_elementsMviewDiff )
    {
      setRecreateNeededFor( lMviewDiff )//
      .ifDifferentName( MVIEW__MVIEW_NAME, oldObjectNames, lMviewDiff.mview_nameNew, lMviewDiff.mview_nameOld, databaseHandler.isRenameMView() )//
      .ifDifferent( MVIEW__TABLESPACE )//
      .ifDifferent( MVIEW__BUILD_MODE )//
      .ifX( p ->
      {
        if( !replaceLinefeedBySpace( lMviewDiff.viewSelectCLOBNew ).equals( replaceLinefeedBySpace( lMviewDiff.viewSelectCLOBOld ) ) )
        {
          p.setRecreateNeededDifferentAttributes( Collections.singletonList( MVIEW__VIEW_SELECT_CLOB ) );
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

  private Optional<TableDiff> findTableDiffByOldName( ModelDiff pModelDiff, String pTableName )
  {
    return pModelDiff.model_elementsTableDiff.stream()//
    .filter( p -> p.isOld )//
    .filter( p -> p.nameOld.equals( pTableName ) )//
    .findAny();
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
    _log.debug( "build generic diff" );
    ModelDiff lModelDiff = new ModelDiff( pModelSoll );
    lModelDiff.mergeWithOldValue( pModelIst );

    _log.debug( "sort for ref part" );
    sortTablesForRefPart( lModelDiff );

    diffReasonKeyRegistry = new DiffReasonKey.DiffReasonKeyRegistry( lModelDiff );
    recreateNeededRegistry = new RecreateNeededRegistry( diffReasonKeyRegistry );

    _log.debug( "update recreate" );
    loadNames( lModelDiff, true );
    loadNames( lModelDiff, false );
    updateIsRecreateNeeded( lModelDiff );

    implicitDropList = new ArrayList<>();
    if( _parameters.isMinimizeStatementCount() )
    {
      updateImplicitDropList( lModelDiff );
    }

    _log.debug( "handle all tables" );
    handleAllTables( lModelDiff );

    _log.debug( "handle all sequences" );
    handleAllSequences( lModelDiff );

    _log.debug( "handle all mviews" );
    handleAllMviews( lModelDiff );

    if( _parameters.isAdditionsOnly() )
    {
      // diffActions = diffActions.stream().filter( p -> p.getDiffReasonType()
      // == DiffReasonType.CREATE ).collect( Collectors.toList() );
    }

    return new DiffResult( diffActions );
  }

  private void updateImplicitDropList( ModelDiff pModelDiff )
  {
    if( _parameters.isAdditionsOnly() )
    {
      return;
    }

    pModelDiff.model_elementsTableDiff.stream()//
    .filter( p -> p.isOld )//
    .forEach( pTableDiff ->
    {
      pTableDiff.constraintsDiff.stream()//
      .filter( p -> p.isOld )//
      .filter( pDiff -> ddlBuilder.isContainsOnlyDropColumns( pTableDiff, pDiff.ruleOld ) )//
      .forEach( implicitDropList::add );

      Optional.ofNullable( pTableDiff.primary_keyDiff )//
      .filter( p -> p.isOld )//
      .filter( pDiff -> ddlBuilder.isAllColumnsOnlyOld( pTableDiff, pDiff.pk_columnsDiff ) )//
      .ifPresent( implicitDropList::add );

      pTableDiff.ind_uksUniqueKeyDiff.stream()//
      .filter( p -> p.isOld )//
      .filter( pDiff -> ddlBuilder.isAllColumnsOnlyOld( pTableDiff, pDiff.uk_columnsDiff ) )//
      .forEach( implicitDropList::add );

      pTableDiff.ind_uksIndexDiff.stream()//
      .filter( p -> p.isOld )//
      .filter( p -> p.function_based_expressionOld == null )//
      .filter( pDiff -> ddlBuilder.isAllColumnsOnlyOld( pTableDiff, pDiff.index_columnsDiff ) )//
      .forEach( implicitDropList::add );

      pTableDiff.foreign_keysDiff.stream()//
      .filter( p -> p.isOld )//
      .filter( pDiff ->
      {
        if( !ddlBuilder.isAllColumnsOnlyOld( pTableDiff, pDiff.srcColumnsDiff ) )
        {
          return false;
        }

        Optional<TableDiff> lDestTableDiff = findTableDiffByOldName( pModelDiff, pDiff.destTableOld );
        if( lDestTableDiff.isPresent() )
        {
          return ddlBuilder.isAllColumnsNew( pDiff.destColumnsDiff, lDestTableDiff.get() );
        }

        return true;
      } )//
      .forEach( implicitDropList::add );
    } );
  }

  public static class DiffResult
  {
    private List<DiffAction> diffActions = new ArrayList<DiffAction>();

    public List<DiffAction> getDiffActions()
    {
      return diffActions;
    }

    public DiffResult( List<DiffAction> pDiffActions )
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
    pRunnable.accept( pAbstractStatementBuilder );
    addDiffAction( activeDiffAction );
    activeDiffAction = null;
  }

  private void addDiffAction( DiffAction pDiffAction )
  {
    if( !diffActions.contains( pDiffAction ) && !pDiffAction.hasNoStatements() )
    {
      diffActions.add( pDiffAction );
    }
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
    doInDiffAction( lDiffAction, Collections.singletonList( lDiffActionReasonDifferent ), pRunnable, new StatementBuilderAlter( lDiffActionReasonDifferent, pDiff, _parameters.isAdditionsOnly(), () -> activeDiffAction, currentAlterTableCombiner ) );
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
      if( !implicitDropList.contains( pDiff ) )
      {
        doInDiffActionDrop( pDiff, pRunnable );
      }
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
        pRunnable.accept( new StatementBuilderWithDiff<>( () -> activeDiffAction, _parameters.isAdditionsOnly(), lDiff, currentAlterTableCombiner ) );
      } );
    }
  }

  private void runInAlterTableCombiner( Runnable pRunnable )
  {
    if( _parameters.isMinimizeStatementCount() )
    {
      currentAlterTableCombiner = new AlterTableCombiner( diffReasonKeyRegistry, this::addDiffAction );
    }

    pRunnable.run();

    if( currentAlterTableCombiner != null )
    {
      currentAlterTableCombiner.finishIfNeeded();
      currentAlterTableCombiner = null;
    }
  }

  private void handleAllTables( ModelDiff pModelDiff )
  {
    for( TableDiff lTableDiff : pModelDiff.model_elementsTableDiff )
    {
      runInAlterTableCombiner( () -> dropIfNeeded( lTableDiff.foreign_keysDiff, p -> ddlBuilder.dropForeignKey( p, lTableDiff, p.diff ) ) );
    }

    for( TableDiff lTableDiff : pModelDiff.model_elementsTableDiff )
    {
      dropIfNeeded( lTableDiff, p -> ddlBuilder.dropTable( p, lTableDiff, dataHandler ), null );
    }

    pModelDiff.model_elementsTableDiff//
    .stream()//
    .filter( p -> p.isNew )//
    .filter( p -> p.isOld )//
    .filter( p -> !isRecreateNeeded( p ) )//
    .forEach( pTableDiff ->
    {
      runInAlterTableCombiner( () ->
      {
        handleTableCleanupTableDetails( pTableDiff, true );
      } );
    } );

    pModelDiff.model_elementsTableDiff//
    .stream()//
    .filter( p -> p.isNew )//
    .forEach( pTableDiff ->
    {
      if( !pTableDiff.isOld || isRecreateNeeded( pTableDiff ) )
      {
        runInAlterTableCombiner( () ->
        {
          doInDiffActionCreate( pTableDiff, p -> ddlBuilder.createTable( p, pTableDiff ) );

          handleTableDetailsNoColumns( pTableDiff );
        } );
      }
      else
      {
        runInAlterTableCombiner( () ->
        {
          doInDiffActionAlter( pTableDiff, p -> ddlBuilder.alterTableIfNeeded( p, pTableDiff, _parameters.isTablemovetablespace(), getDefaultTablespace() ) );

          handleTableCleanupTableDetails( pTableDiff, false );
        } );

        runInAlterTableCombiner( () ->
        {
          handleTableRecreateOrAlterColumns( pTableDiff );
        } );

        runInAlterTableCombiner( () ->
        {
          handleTableCreateColumns( pTableDiff );

          handleTableDetailsNoColumns( pTableDiff );
        } );
      }
    } );

    for( TableDiff lTableDiff : pModelDiff.model_elementsTableDiff )
    {
      runInAlterTableCombiner( () ->
      {
        for( ForeignKeyDiff lForeignKeyDiff : lTableDiff.foreign_keysDiff )
        {
          if( lTableDiff.isOld == false && lTableDiff.tablePartitioningRefPartitionsDiff.isNew && lTableDiff.tablePartitioningRefPartitionsDiff.fkNameNew.equalsIgnoreCase( lForeignKeyDiff.consNameNew ) )
          {
            // fk for ref-partition created in create-table
          }
          else
          {
            if( _parameters.getMultiSchema() ) {
              if( lForeignKeyDiff.isNew )
              {
                if( lForeignKeyDiff.isOld == false  )
                {
                  DiffAction lDiffAction = new DiffAction( new DiffReasonKey(lForeignKeyDiff.destTableNew), DiffReasonType.CREATE );

                  doInDiffAction( lDiffAction, Collections.singletonList( new DiffActionReasonMissing( diffReasonKeyRegistry.getDiffReasonKey(
                      lForeignKeyDiff) ) ),
                      (DiffActionRunnable) p -> ddlBuilder.createForeignKeyGrantIfNeeded(p, lTableDiff, lForeignKeyDiff), createStatementBuilder() );
                }
              }
            }

            createIfNeededOrAlter( lForeignKeyDiff, p -> ddlBuilder.createForeignKey( p, lTableDiff, lForeignKeyDiff, dataHandler ), p -> ddlBuilder.alterForeignKeyIfNeeded( p, lTableDiff, lForeignKeyDiff, dataHandler ) );
          }
        }
      } );
    }
  }

  private void handleTableCreateColumns( TableDiff pTableDiff )
  {
    List<ColumnDiff> lCreateColumnDiffList = pTableDiff.columnsDiff.stream()//
    .filter( p -> p.isNew && !p.isOld )//
    .collect( Collectors.toList() );

    if( lCreateColumnDiffList.size() > 1 && _parameters.isMinimizeStatementCount() && ddlBuilder.isMultiCreatePossible( pTableDiff, lCreateColumnDiffList ) )
    {
      DiffAction lDiffAction = new DiffAction( diffReasonKeyRegistry.getDiffReasonKey( pTableDiff ), DiffReasonType.ALTER );
      List<DiffActionReason> lDiffActionReasonList = lCreateColumnDiffList//
      .stream()//
      .map( pColumnDiff -> new DiffActionReasonMissing( diffReasonKeyRegistry.getDiffReasonKey( pColumnDiff ) ) )//
      .collect( Collectors.toList() );

      doInDiffAction( lDiffAction, lDiffActionReasonList, p -> ddlBuilder.createColumns( p, pTableDiff, lCreateColumnDiffList ), createStatementBuilder() );
    }
    else
    {
      lCreateColumnDiffList.forEach( pColumnDiff -> doInDiffActionCreate( pColumnDiff, p -> ddlBuilder.createColumn( p, pTableDiff, pColumnDiff ) ) );
    }
  }

  private void handleTableRecreateOrAlterColumns( TableDiff pTableDiff )
  {
    for( ColumnDiff lColumnDiff : pTableDiff.columnsDiff )
    {
      if( lColumnDiff.isNew && lColumnDiff.isOld )
      {
        if( isRecreateNeeded( lColumnDiff ) )
        {
          doInDiffAction( lColumnDiff, recreateNeededRegistry.getRecreateNeededReasons( lColumnDiff ), DiffReasonType.RECREATE, //
          p -> ddlBuilder.recreateColumn( p, pTableDiff, lColumnDiff ) //
          , createStatementBuilder() );
        }
        else
        {
          doInDiffActionAlter( lColumnDiff, p -> ddlBuilder.alterColumnIfNeeded( p, pTableDiff, lColumnDiff ) );
        }
      }
    }
  }

  private void handleTableDetailsNoColumns( TableDiff pTableDiff )
  {
    if ( pTableDiff.primary_keyDiff.indexnameNew != null )
    {
      pTableDiff.ind_uksIndexDiff.stream()
                                 .filter( p -> p.isNew )
                                 .filter( p -> p.consNameNew.toLowerCase().equals( pTableDiff.primary_keyDiff.indexnameNew.toLowerCase() ) )
                                 .forEach( p -> handleIndex( pTableDiff, p ) );
    }

    handlePrimarykey( pTableDiff );

    pTableDiff.constraintsDiff.forEach( p -> handleConstraint( pTableDiff, p ) );

    pTableDiff.ind_uksIndexDiff.stream().filter( p -> pTableDiff.primary_keyDiff.indexnameNew == null || !p.isNew || !p.consNameNew.toLowerCase().equals( pTableDiff.primary_keyDiff.indexnameNew.toLowerCase() ) ).forEach( p -> handleIndex( pTableDiff, p ) );

    pTableDiff.ind_uksUniqueKeyDiff.forEach( p -> handleUniquekey( pTableDiff, p ) );

    pTableDiff.commentsDiff.forEach( p -> handleComment( pTableDiff, p ) );

    handleMviewlog( pTableDiff );
  }

  private boolean isCleanupTableDetailsGlobalNeeded( TableDiff pTableDiff )
  {
    String lTableName = pTableDiff.nameNew;

    List<String> lOldConstraintNamesOtherTables = oldContraintNames.entrySet()//
    .stream()//
    .filter( p -> !p.getKey().equals( lTableName ) )//
    .map( Map.Entry::getValue )//
    .flatMap( List::stream ).collect( Collectors.toList() );

    if( newContraintNames.containsKey( lTableName ) && newContraintNames.get( lTableName )//
    .stream()//
    .filter( p -> lOldConstraintNamesOtherTables.contains( p ) )//
    .findAny()//
    .isPresent() )
    {
      return true;
    }

    return false;
  }

  private void handleTableCleanupTableDetails( TableDiff pTableDiff, boolean pGlobalMode )
  {
    if( isCleanupTableDetailsGlobalNeeded( pTableDiff ) == pGlobalMode )
    {
      if( databaseHandler.isUpdateIdentity() )
      {
        for( ColumnDiff lColumnDiff : pTableDiff.columnsDiff )
        {
          dropIfNeeded( lColumnDiff.identityDiff, p -> ddlBuilder.dropColumnIdentity( p, pTableDiff, lColumnDiff, lColumnDiff.identityDiff ) );
        }
      }

      dropIfNeeded( pTableDiff.primary_keyDiff, p -> ddlBuilder.dropPrimaryKey( p, pTableDiff, pTableDiff.primary_keyDiff ) );

      dropIfNeeded( pTableDiff.constraintsDiff, p -> ddlBuilder.dropConstraint( p, pTableDiff, p.diff ) );

      dropIfNeeded( pTableDiff.mviewLogDiff, p -> ddlBuilder.dropMaterializedViewLog( p, pTableDiff ) );

      dropIfNeeded( pTableDiff.ind_uksUniqueKeyDiff, p -> ddlBuilder.dropUniqueKey( p, pTableDiff, p.diff ) );

      dropIfNeeded( pTableDiff.ind_uksIndexDiff, p -> ddlBuilder.dropIndex( p, pTableDiff, p.diff ) );

      for( InlineCommentDiff lCommentDiff : pTableDiff.commentsDiff )
      {
        if( lCommentDiff.isOld && lCommentDiff.isMatched == false )
        {
          boolean lIsColumnComment = lCommentDiff.column_nameOld != null;
          if( !lIsColumnComment || columnIsNew( pTableDiff, lCommentDiff.column_nameOld ) )
          {
            doInDiffActionDrop( lCommentDiff, p -> ddlBuilder.dropComment( p, pTableDiff, lCommentDiff ) );
          }
        }
      }

      List<ColumnDiff> lDropColumnDiffList = pTableDiff.columnsDiff.stream()//
      .filter( p -> !p.isNew )//
      .collect( Collectors.toList() );

      if( lDropColumnDiffList.size() > 1 && _parameters.isMinimizeStatementCount() && ddlBuilder.isMultiDropPossible( pTableDiff, lDropColumnDiffList, dataHandler ) )
      {
        DiffActionRunnable lRunnable = p -> ddlBuilder.getColumnDropHandler( p, pTableDiff, lDropColumnDiffList ).run();

        DiffAction lDiffAction = new DiffAction( diffReasonKeyRegistry.getDiffReasonKey( pTableDiff ), DiffReasonType.ALTER );
        List<DiffActionReason> lDiffActionReasonList = lDropColumnDiffList//
        .stream()//
        .map( pColumnDiff -> new DiffActionReasonSurplus( diffReasonKeyRegistry.getDiffReasonKey( pColumnDiff ) ) )//
        .collect( Collectors.toList() );

        doInDiffAction( lDiffAction, lDiffActionReasonList, lRunnable, createStatementBuilder() );
      }
      else
      {
        lDropColumnDiffList.forEach( pColumnDiff -> doInDiffActionDrop( pColumnDiff, p -> ddlBuilder.dropColumn( p, pTableDiff, pColumnDiff, dataHandler ) ) );
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
            lDiffActionReasonDifferent.addDiffReasonDetail( new DifferenceImplAttributeOnly(INLINE_COMMENT__COMMENT) );
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
    createIfNeededOrAlter( pUniqueKeyDiff, //
    p -> ddlBuilder.createUniqueKey( p, pTableDiff, pUniqueKeyDiff ), //
    p -> ddlBuilder.alterUniqueKeyIfNeeded( p, pTableDiff, pUniqueKeyDiff ) );
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
    createIfNeededOrAlter( pConstraintDiff, p -> ddlBuilder.createConstraint( p, pTableDiff, pConstraintDiff ), p -> ddlBuilder.updateConstraintIfNeeded( p, pTableDiff, pConstraintDiff ) );
  }

  private void handlePrimarykey( TableDiff pTableDiff )
  {
    createIfNeededOrAlter( pTableDiff.primary_keyDiff, p -> ddlBuilder.createPrimarykey( p, pTableDiff ), p -> ddlBuilder.alterPrimarykeyIfNeeded( p, pTableDiff ) );
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

  private void createIfNeededOrAlter( AbstractDiff pDiff, DiffActionRunnable pRunnableCreate, DiffActionRunnableAlter pRunnableAlter )
  {
    if( pDiff.isNew )
    {
      if( pDiff.isOld == false || isRecreateNeeded( pDiff ) )
      {
        doInDiffActionCreate( pDiff, pRunnableCreate );
      }
      else
      {
        if( pRunnableAlter != null )
        {
          doInDiffActionAlter( pDiff, pRunnableAlter );
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

  private StatementBuilder createStatementBuilder()
  {
    return new StatementBuilder( () -> activeDiffAction, _parameters.isAdditionsOnly(), currentAlterTableCombiner );
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

    public void dropWithDropmodeCheck( StatementBuilder pStatementBuilder, String pTestStatement, Runnable pDropHandler )
    {
      if( !isDropOk( pTestStatement ) )
      {
        pStatementBuilder.fail( "dropmode not active, the following statement contains data. Execution would result in data-loss: " + pTestStatement );
      }

      pDropHandler.run();
    }

    public boolean isDropOk( String pTestStatement )
    {
      if( isCheckDropMode() )
      {
        if( hasRows( pTestStatement ) )
        {
          return false;
        }
      }

      return true;
    }

    public boolean isCheckDropMode()
    {
      return !_parameters.isAdditionsOnly() && !isDropmode();
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
