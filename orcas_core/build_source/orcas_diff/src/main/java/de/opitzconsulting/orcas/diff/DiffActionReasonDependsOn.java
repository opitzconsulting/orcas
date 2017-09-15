package de.opitzconsulting.orcas.diff;

import java.util.ArrayList;
import java.util.List;

public class DiffActionReasonDependsOn extends DiffActionReason
{
  private List<DiffActionReason> diffActionReasonDependsOnList = new ArrayList<>();

  public List<DiffActionReason> getDiffActionReasonDependsOnList()
  {
    return diffActionReasonDependsOnList;
  }

  public DiffActionReasonDependsOn( DiffReasonKey pDiffReasonKey, List<DiffActionReason> pDiffActionReasonDependsOnList )
  {
    super( pDiffReasonKey );
    diffActionReasonDependsOnList = pDiffActionReasonDependsOnList;
  }

  @Override
  protected String getTypeString()
  {
    return "dependent";
  }

  @Override
  public boolean equals( Object pOther )
  {
    if( !super.equals( pOther ) )
    {
      return false;
    }

    DiffActionReasonDependsOn lOther = (DiffActionReasonDependsOn) pOther;

    if( diffActionReasonDependsOnList.size() != lOther.diffActionReasonDependsOnList.size() )
    {
      return false;
    }

    return !diffActionReasonDependsOnList//
    .stream()//
    .filter( p -> !lOther.diffActionReasonDependsOnList.contains( p ) )//
    .findAny()//
    .isPresent();
  }
}
