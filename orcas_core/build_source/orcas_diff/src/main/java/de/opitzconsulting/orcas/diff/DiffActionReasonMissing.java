package de.opitzconsulting.orcas.diff;

public class DiffActionReasonMissing extends DiffActionReason
{
  public DiffActionReasonMissing( DiffReasonKey pDiffReasonKey )
  {
    super( pDiffReasonKey );
  }

  @Override
  protected String getTypeString()
  {
    return "missing";
  }
}
