package de.opitzconsulting.orcas.diff;

public class DiffActionReasonSurplus extends DiffActionReason
{
  public DiffActionReasonSurplus( DiffReasonKey pDiffReasonKey )
  {
    super( pDiffReasonKey );
  }

  @Override
  protected String getTypeString()
  {
    return "surplus";
  }
}
