package de.opitzconsulting.orcas.diff;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EStructuralFeature;

public class DiffActionReasonDifferent extends DiffActionReason
{
  private List<String> diffReasonDetails = new ArrayList<>();

  public List<String> getDiffReasonDetails()
  {
    return diffReasonDetails;
  }

  public DiffActionReasonDifferent( DiffReasonKey pDiffReasonKey )
  {
    super( pDiffReasonKey );
  }

  public DiffActionReasonDifferent( DiffReasonKey pDiffReasonKey, List<String> pDiffReasonDetails )
  {
    super( pDiffReasonKey );

    diffReasonDetails = pDiffReasonDetails;
  }

  public void addDiffReasonDetail( EStructuralFeature pDiffReasonDetail )
  {
    diffReasonDetails.add( pDiffReasonDetail.getName() );
  }

  @Override
  protected String getTypeString()
  {
    return "different";
  }

  @Override
  public boolean equals( Object pOther )
  {
    if( !super.equals( pOther ) )
    {
      return false;
    }

    DiffActionReasonDifferent lOther = (DiffActionReasonDifferent) pOther;

    return diffReasonDetails.equals( lOther.diffReasonDetails );
  }
}
