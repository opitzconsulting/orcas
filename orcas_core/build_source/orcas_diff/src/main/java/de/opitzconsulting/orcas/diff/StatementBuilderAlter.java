package de.opitzconsulting.orcas.diff;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EStructuralFeature;

import de.opitzconsulting.orcas.diff.RecreateNeededBuilder.Difference;
import de.opitzconsulting.orcas.diff.RecreateNeededBuilder.DifferenceImplAttributeOnly;
import de.opitzconsulting.orcas.orig.diff.AbstractDiff;

public class StatementBuilderAlter
{
  private DiffActionReasonDifferent diffActionReasonDifferent;
  private AbstractDiff diff;
  private boolean isAdditionsOnlyMode;
  private Supplier<DiffAction> diffActionSupplier;
  private AlterTableCombiner alterTableCombiner;

  public StatementBuilderAlter( DiffActionReasonDifferent pDiffActionReasonDifferent, AbstractDiff pDiff, boolean pIsAdditionsOnlyMode, Supplier<DiffAction> pDiffActionSupplier, AlterTableCombiner pAlterTableCombiner )
  {
    diffActionReasonDifferent = pDiffActionReasonDifferent;
    diff = pDiff;
    isAdditionsOnlyMode = pIsAdditionsOnlyMode;
    diffActionSupplier = pDiffActionSupplier;
    alterTableCombiner = pAlterTableCombiner;
  }

  public AlterBuilder handleAlterBuilder()
  {
    return new AlterBuilder();
  }

  public class AlterBuilder
  {
    private List<EStructuralFeature> checkDifferentEStructuralFeatureList = new ArrayList<>();
    private List<EStructuralFeature> forceDifferentEStructuralFeatureList = new ArrayList<>();
    private String additionsOnlyFailMessage;
    private boolean ignoreIfAdditionsOnly;

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

    public AlterBuilder failIfAdditionsOnly()
    {
      return failIfAdditionsOnly( "" );
    }

    public AlterBuilder ignoreIfAdditionsOnly( boolean pIgnore )
    {
      if( pIgnore )
      {
        ignoreIfAdditionsOnly();
      }
      return this;
    }

    public AlterBuilder ignoreIfAdditionsOnly()
    {
      ignoreIfAdditionsOnly = true;
      return this;
    }

    public AlterBuilder failIfAdditionsOnly( boolean pFail, String pFailMessage )
    {
      if( pFail )
      {
        failIfAdditionsOnly( pFailMessage );
      }

      return this;
    }

    public AlterBuilder failIfAdditionsOnly( String pFailMessage )
    {
      additionsOnlyFailMessage = pFailMessage;
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
      List<Difference> lDifferentEAttributes = RecreateNeededBuilder.getDifferentEAttributes( diff, checkDifferentEStructuralFeatureList );
      lDifferentEAttributes.addAll( forceDifferentEStructuralFeatureList.stream().map(p->new DifferenceImplAttributeOnly(p)).collect(Collectors.toList()) );

      if( !lDifferentEAttributes.isEmpty() )
      {
        for( Difference lDifference : lDifferentEAttributes )
        {
          diffActionReasonDifferent.addDiffReasonDetail( lDifference );
        }

        StatementBuilder lStatementBuilder = new StatementBuilder( diffActionSupplier, isAdditionsOnlyMode, alterTableCombiner );
        lStatementBuilder.ignoreEverythingIfAdditionsOnly( ignoreIfAdditionsOnly );
        lStatementBuilder.failIfAdditionsOnly( additionsOnlyFailMessage != null, additionsOnlyFailMessage );
        pHanlder.accept( lStatementBuilder );
      }
    }
  }

  public void failIfAdditionsOnly( String pMessage )
  {
    failIfAdditionsOnly( true, pMessage );
  }

  public void failIfAdditionsOnly( boolean pFailIfAdditionsOnly, String pMessage )
  {
    if( isAdditionsOnlyMode && pFailIfAdditionsOnly )
    {
      throw new RuntimeException( "AdditionsOnly: " + pMessage );
    }
  }
}
