package de.opitzconsulting.orcas.diff;

import org.jdom2.Element;

public abstract class DiffActionReason
{
  private DiffReasonKey diffReasonKey;

  public DiffActionReason( DiffReasonKey pDiffReasonKey )
  {
    diffReasonKey = pDiffReasonKey;
  }

  public Element getJdomElement( boolean pIncludeKey )
  {
    Element lReturn = new Element( "diff-action-reason" );

    lReturn.setAttribute( "type", getTypeString() );
    if( pIncludeKey )
    {
      lReturn.setAttribute( "key", diffReasonKey.getTextKey() );
    }

    return lReturn;
  }

  protected abstract String getTypeString();
}
