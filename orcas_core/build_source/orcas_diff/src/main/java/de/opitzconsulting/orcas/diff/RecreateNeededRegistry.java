package de.opitzconsulting.orcas.diff;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.opitzconsulting.orcas.diff.DiffReasonKey.DiffReasonKeyRegistry;
import de.opitzconsulting.orcas.orig.diff.AbstractDiff;

public class RecreateNeededRegistry
{
  private Map<AbstractDiff, List<DiffActionReason>> recreateDiffDiffActionReasonMap = new HashMap<>();
  private DiffReasonKeyRegistry diffReasonKeyRegistry;

  public RecreateNeededRegistry( DiffReasonKeyRegistry pDiffReasonKeyRegistry )
  {
    diffReasonKeyRegistry = pDiffReasonKeyRegistry;
  }

  public boolean isRecreateNeeded( AbstractDiff pDiff )
  {
    return recreateDiffDiffActionReasonMap.containsKey( pDiff );
  }

  public List<DiffActionReason> getRecreateNeededReasons( AbstractDiff pDiff )
  {
    return recreateDiffDiffActionReasonMap.get( pDiff );
  }

  public <T extends AbstractDiff> RecreateNeededBuilder<T> createRecreateNeededBuilder( T pDiff )
  {
    return new RecreateNeededBuilder<T>( pDiff, diffReasonKeyRegistry, recreateDiffDiffActionReasonMap );
  }
}
