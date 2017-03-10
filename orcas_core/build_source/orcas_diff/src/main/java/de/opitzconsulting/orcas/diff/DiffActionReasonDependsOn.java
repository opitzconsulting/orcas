package de.opitzconsulting.orcas.diff;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Element;

public class DiffActionReasonDependsOn extends DiffActionReason
{
  private List<DiffActionReason> diffActionReasonDependsOnList = new ArrayList<>();

  public DiffActionReasonDependsOn( DiffReasonKey pDiffReasonKey, List<DiffActionReason> pDiffActionReasonDependsOnList )
  {
    super( pDiffReasonKey );
    diffActionReasonDependsOnList = pDiffActionReasonDependsOnList;
  }

  @Override
  public Element getJdomElement( boolean pIncludeKey )
  {
    Element lReturn = super.getJdomElement( pIncludeKey );

    for( DiffActionReason lDiffActionReason : diffActionReasonDependsOnList )
    {
      lReturn.addContent( lDiffActionReason.getJdomElement( true ) );
    }

    return lReturn;
  }

  @Override
  protected String getTypeString()
  {
    return "dependent";
  }
}
