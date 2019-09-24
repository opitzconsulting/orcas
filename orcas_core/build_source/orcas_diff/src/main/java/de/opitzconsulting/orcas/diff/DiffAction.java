package de.opitzconsulting.orcas.diff;

import java.util.ArrayList;
import java.util.List;

public class DiffAction
{
  private DiffReasonKey diffReasonKey;
  private DiffReasonType diffReasonType;
  private List<Statement> statements = new ArrayList<>();
  private List<DiffActionReason> diffActionReasons = new ArrayList<>();

  public DiffAction( DiffReasonKey pDiffReasonKey, DiffReasonType pDiffReasonType )
  {
    diffReasonKey = pDiffReasonKey;
    diffReasonType = pDiffReasonType;
  }

  public DiffReasonKey getDiffReasonKey()
  {
    return diffReasonKey;
  }

  public void addStatement( String pStatement )
  {
    statements.add( new Statement( pStatement ) );
  }

  public void addStatement( String pStatement, String pComment )
  {
    statements.add( new Statement( pStatement, pComment ) );
  }

  public void addIgnoredStatement( String pStatement, String pIgnoreReason )
  {
    statements.add( new Statement( pStatement, true, false, pIgnoreReason ) );
  }

  public void addFailureStatement( String pStatement, String pFailureReason )
  {
    statements.add( new Statement( pStatement, false, true, pFailureReason ) );
  }

  public DiffReasonType getDiffReasonType()
  {
    return diffReasonType;
  }

  public DiffReasonType getDiffReasonTypeNoCombinedRecreates()
  {
    if( diffReasonType == DiffReasonType.RECREATE_CREATE )
    {
      return DiffReasonType.CREATE;
    }
    if( diffReasonType == DiffReasonType.RECREATE_DROP )
    {
      return DiffReasonType.DROP;
    }

    return diffReasonType;
  }

  public boolean isRecreate()
  {
    return diffReasonType == DiffReasonType.RECREATE || diffReasonType == DiffReasonType.RECREATE_CREATE || diffReasonType == DiffReasonType.RECREATE_DROP;
  }

  public void addDiffActionReason( DiffActionReason pDiffActionReason )
  {
    diffActionReasons.add( pDiffActionReason );
  }

  public enum DiffReasonType
  {
    CREATE, RECREATE, RECREATE_CREATE, ALTER, DROP, RECREATE_DROP
  }

  public static DiffAction parseFromXml( String pType, boolean pIsRecreate, DiffReasonKey pDiffReasonKey )
  {
    DiffReasonType lDiffReasonType = DiffReasonType.valueOf( pType.toUpperCase() );

    if( pIsRecreate )
    {
      switch( lDiffReasonType )
      {
        case CREATE:
          lDiffReasonType = DiffReasonType.RECREATE_CREATE;
          break;
        case DROP:
          lDiffReasonType = DiffReasonType.RECREATE_DROP;
          break;
        case RECREATE:
          break;

        default:
          throw new IllegalArgumentException( "cant handle recreate for " + lDiffReasonType );
      }
    }

    return new DiffAction( pDiffReasonKey, lDiffReasonType );
  }

  public String getTextKey()
  {
    return diffReasonType.name().toLowerCase() + ":" + diffReasonKey.getTextKey();
  }

  public boolean hasNoStatements()
  {
    return getStatements().isEmpty();
  }

  public List<Statement> getStatements()
  {
    return statements;
  }

  public List<DiffActionReason> getDiffActionReasons()
  {
    return diffActionReasons;
  }

  public static class Statement
  {
    private String statement;
    private boolean isIgnore;
    private boolean isFailure;

    public boolean isFailure()
    {
      return isFailure;
    }

    private String comment;

    public String getStatement()
    {
      return statement;
    }

    public boolean isIgnore()
    {
      return isIgnore;
    }

    public String getComment()
    {
      return comment;
    }

    public Statement( String pStatement )
    {
      statement = pStatement;
    }

    public Statement( String pStatement, String pComment )
    {
      statement = pStatement;
      comment = pComment;
    }

    public Statement( String pStatement, boolean pIsIgnore, boolean pIsFailure, String pComment )
    {
      statement = pStatement;
      isIgnore = pIsIgnore;
      isFailure = pIsFailure;
      comment = pComment;
    }

    public void setIgnore( String pIgnoreReason ){
      isIgnore = true;
      comment = pIgnoreReason;
    }
  }
}
