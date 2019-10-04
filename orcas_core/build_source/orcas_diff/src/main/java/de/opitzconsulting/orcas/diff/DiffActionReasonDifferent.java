package de.opitzconsulting.orcas.diff;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EStructuralFeature;

import de.opitzconsulting.orcas.diff.RecreateNeededBuilder.Difference;

public class DiffActionReasonDifferent extends DiffActionReason
{
  private List<DiffDifference> diffReasonDetails = new ArrayList<>();

  public List<DiffDifference> getDiffReasonDetails()
  {
    return diffReasonDetails;
  }

  public DiffActionReasonDifferent( DiffReasonKey pDiffReasonKey )
  {
    super( pDiffReasonKey );
  }

  public DiffActionReasonDifferent( DiffReasonKey pDiffReasonKey, List<DiffDifference> pDiffReasonDetails )
  {
    super( pDiffReasonKey );

    diffReasonDetails = pDiffReasonDetails;
  }

  public void addDiffReasonDetail( Difference pDifference )
  {
    if(pDifference.getEAttribute()!=null) {
      diffReasonDetails.add(new DiffDifference(pDifference.getEAttribute().getName(),pDifference.getOldValue(), pDifference.getNewValue()));
    }
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

  public static class DiffDifference{
    private String difference;
    private String oldValue;
    private String newValue;

    public DiffDifference(String pDifference, Object pOldValue, Object pNewValue) {
      difference = pDifference;
      oldValue = pOldValue == null ? null : pOldValue.toString();
      newValue = pNewValue == null ? null : pNewValue.toString();
    }

    public String getDifference() {
      return difference;
    }

    public String getOldValue() {
      return oldValue;
    }

    public String getNewValue() {
      return newValue;
    }
  }
}
