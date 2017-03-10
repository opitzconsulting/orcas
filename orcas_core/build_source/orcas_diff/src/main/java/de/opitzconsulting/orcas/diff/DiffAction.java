package de.opitzconsulting.orcas.diff;

import java.util.ArrayList;
import java.util.List;

public class DiffAction
{
  private DiffReasonKey diffReasonKey;
  private DiffReasonType diffReasonType;
  private List<String> statements = new ArrayList<>();
  private List<DiffActionReason> diffActionReasons = new ArrayList<>();

  public DiffAction( DiffReasonKey pDiffReasonKey, DiffReasonType pDiffReasonType )
  {
    diffReasonKey = pDiffReasonKey;
    diffReasonType = pDiffReasonType;
  }

  public void addStatement( String pStatement )
  {
    statements.add( pStatement );
  }

  public void addDiffActionReason( DiffActionReason pDiffActionReason )
  {
    diffActionReasons.add( pDiffActionReason );
  }

  public enum DiffReasonType
  {
    CREATE, RECREATE, RECREATE_CREATE, ALTER, DROP, RECREATE_DROP
  }

  public String getTextKey()
  {
    return diffReasonType.name().toLowerCase() + ":" + diffReasonKey.getTextKey();
  }

  public List<String> getStatements()
  {
    return statements;
  }

  public List<DiffActionReason> getDiffActionReasons()
  {
    return diffActionReasons;
  }
}
