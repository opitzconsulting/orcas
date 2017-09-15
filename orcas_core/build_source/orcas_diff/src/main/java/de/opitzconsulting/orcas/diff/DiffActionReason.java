package de.opitzconsulting.orcas.diff;

public abstract class DiffActionReason
{
  private DiffReasonKey diffReasonKey;

  public DiffReasonKey getDiffReasonKey()
  {
    return diffReasonKey;
  }

  public DiffActionReason( DiffReasonKey pDiffReasonKey )
  {
    diffReasonKey = pDiffReasonKey;
  }

  @Override
  public int hashCode()
  {
    return 1;
  }

  @Override
  public boolean equals( Object pOther )
  {
    if( getClass() != pOther.getClass() )
    {
      return false;
    }
    DiffActionReason lOther = (DiffActionReason) pOther;

    if( diffReasonKey == null )
    {
      return lOther.diffReasonKey == null;
    }

    if( lOther.diffReasonKey == null )
    {
      return false;
    }

    return diffReasonKey.getTextKey().equals( lOther.diffReasonKey.getTextKey() );
  }

  protected abstract String getTypeString();
}
