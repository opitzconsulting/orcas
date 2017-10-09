package de.opitzconsulting.orcas.diff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.eclipse.emf.ecore.EStructuralFeature;

import de.opitzconsulting.orcas.diff.DiffReasonKey.DiffReasonKeyRegistry;
import de.opitzconsulting.orcas.orig.diff.AbstractDiff;
import de.opitzconsulting.orcas.orig.diff.ColumnRefDiff;

class RecreateNeededBuilder<T extends AbstractDiff> implements RecreateNeededBuilderHandler<T>
{
  interface RecreateNeededBuilderRunnable<T extends AbstractDiff> extends Consumer<RecreateNeededBuilderHandler<T>>
  {
  }

  private T diff;
  private List<EStructuralFeature> eAttributes = new ArrayList<>();
  private List<RecreateNeededBuilderRunnable<T>> additionalHandlers = new ArrayList<>();
  private DiffReasonKeyRegistry diffReasonKeyRegistry;
  private Map<AbstractDiff, List<DiffActionReason>> recreateDiffDiffActionReasonMap = new HashMap<>();

  @Override
  public T getDiff()
  {
    return diff;
  }

  @Override
  public void setRecreateNeededDifferent( List<EStructuralFeature> pDiffReasonDetails )
  {
    DiffActionReasonDifferent lDiffActionReasonDifferent = new DiffActionReasonDifferent( diffReasonKeyRegistry.getDiffReasonKey( diff ) );

    for( EStructuralFeature lEAttribute : pDiffReasonDetails )
    {
      if( lEAttribute != null )
      {
        lDiffActionReasonDifferent.addDiffReasonDetail( lEAttribute );
      }
    }

    addRecreateNeeded( lDiffActionReasonDifferent );
  }

  private void addRecreateNeeded( DiffActionReason pDiffActionReason )
  {
    List<DiffActionReason> lDiffActionReasonList = recreateDiffDiffActionReasonMap.get( diff );

    if( lDiffActionReasonList == null )
    {
      lDiffActionReasonList = new ArrayList<>();
      recreateDiffDiffActionReasonMap.put( diff, lDiffActionReasonList );
    }

    lDiffActionReasonList.add( pDiffActionReason );
  }

  @Override
  public void setRecreateNeededDependsOn( List<DiffActionReason> pDiffActionReasonDependsOnList )
  {
    addRecreateNeeded( new DiffActionReasonDependsOn( diffReasonKeyRegistry.getDiffReasonKey( diff ), pDiffActionReasonDependsOnList ) );
  }

  public RecreateNeededBuilder( T pDiff, DiffReasonKeyRegistry pDiffReasonKeyRegistry, Map<AbstractDiff, List<DiffActionReason>> pRecreateDiffDiffActionReasonMap )
  {
    diff = pDiff;
    diffReasonKeyRegistry = pDiffReasonKeyRegistry;
    recreateDiffDiffActionReasonMap = pRecreateDiffDiffActionReasonMap;
  }

  public RecreateNeededBuilder<T> ifX( RecreateNeededBuilderRunnable<T> pObject )
  {
    additionalHandlers.add( pObject );

    return this;
  }

  public RecreateNeededBuilder<T> ifColumnDependentRecreate( Map<String, List<DiffActionReason>> pRecreateColumnNames, List<ColumnRefDiff> pColumnRefDiff )
  {
    return ifX( p ->
    {
      handleColumnDependentRecreate( pRecreateColumnNames, pColumnRefDiff );
    } );
  }

  private void handleColumnDependentRecreate( Map<String, List<DiffActionReason>> pRecreateColumnNames, List<ColumnRefDiff> pColumnDiffList )
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
      setRecreateNeededDependsOn( lDependsOnList );
    }
  }

  public RecreateNeededBuilder<T> ifColumnDependentRecreate( Map<String, List<DiffActionReason>> pRecreateColumnNames, String pColumnName )
  {
    return ifX( p ->
    {
      handleColumnDependentRecreate( pRecreateColumnNames, pColumnName );
    } );
  }

  private void handleColumnDependentRecreate( Map<String, List<DiffActionReason>> pRecreateColumnNames, String pColumnName )
  {
    List<DiffActionReason> lDependsOnList = new ArrayList<>();

    if( pRecreateColumnNames.keySet().contains( pColumnName ) )
    {
      lDependsOnList.addAll( pRecreateColumnNames.get( pColumnName ) );
    }

    if( !lDependsOnList.isEmpty() )
    {
      setRecreateNeededDependsOn( lDependsOnList );
    }
  }

  public RecreateNeededBuilder<T> ifDifferent( EStructuralFeature pEAttribute, boolean pCheckThis )
  {
    if( pCheckThis )
    {
      eAttributes.add( pEAttribute );
    }

    return this;
  }

  public RecreateNeededBuilder<T> ifDifferent( EStructuralFeature pEAttribute )
  {
    eAttributes.add( pEAttribute );

    return this;
  }

  public RecreateNeededBuilder<T> ifDifferentName( EStructuralFeature pEAttribute, List<String> pOldNames, String pNewName, String pOldName )
  {
    if( pNewName == null || pOldName == null || pOldNames.contains( pNewName ) )
    {
      ifDifferent( pEAttribute );
    }

    return this;
  }

  public RecreateNeededBuilder<T> ifDifferentName( EStructuralFeature pEAttribute, Map<String, List<String>> pOldNames, String pNewName, String pOldName )
  {
    if( pNewName == null || pOldName == null )
    {
      ifDifferent( pEAttribute );
    }

    if( pOldNames.values().stream().filter( p -> p.contains( pNewName ) ).findAny().isPresent() )
    {
      ifDifferent( pEAttribute );
    }

    return this;
  }

  public void calculate()
  {
    if( diff.isMatched )
    {
      List<EStructuralFeature> lDifferentEAttributes = getDifferentEAttributes( diff, eAttributes );
      if( !lDifferentEAttributes.isEmpty() )
      {
        setRecreateNeededDifferent( lDifferentEAttributes );
      }

      for( RecreateNeededBuilderRunnable<T> lRunnable : additionalHandlers )
      {
        lRunnable.accept( this );
      }
    }
  }

  static List<EStructuralFeature> getDifferentEAttributes( AbstractDiff pDiff, List<EStructuralFeature> pEAttributes )
  {
    List<EStructuralFeature> lReturn = new ArrayList<>();

    for( EStructuralFeature lEAttribute : pEAttributes )
    {
      if( !pDiff.isFieldEqual( lEAttribute ) )
      {
        lReturn.add( lEAttribute );
      }
    }

    return lReturn;
  }
}
