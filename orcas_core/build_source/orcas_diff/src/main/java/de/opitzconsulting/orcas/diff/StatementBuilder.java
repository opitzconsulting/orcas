package de.opitzconsulting.orcas.diff;

import java.util.function.Supplier;

public class StatementBuilder extends AbstractStatementBuilder
{
  public static final String ADDITIONSONLY_COMMENT = "additionsonly";
  public static final String ADDITIONSONLY_ALTERNATIVE_COMMENT = ADDITIONSONLY_COMMENT + "-alternative";

  private Supplier<DiffAction> diffActionSupplier;
  private boolean isAdditionsOnlyMode;
  private boolean ignoreEverythingIfAdditionsOnly;
  private String failMessage;

  public StatementBuilder( Supplier<DiffAction> pDiffActionSupplier, boolean pIsAdditionsOnlyMode, AlterTableCombiner pAlterTableCombiner )
  {
    super( pAlterTableCombiner );

    diffActionSupplier = pDiffActionSupplier;

    isAdditionsOnlyMode = pIsAdditionsOnlyMode;
  }

  void addStmt( String pString, boolean pIsgnoreIfAdditionsOnly )
  {
    addStmt( pString, pIsgnoreIfAdditionsOnly, null );
  }

  void addStmt( String pString, boolean pIsgnoreIfAdditionsOnly, String pComment )
  {
    DiffAction lDiffAction = diffActionSupplier.get();

    if( alterTableCombiner != null )
    {
      lDiffAction = alterTableCombiner.getDiffActionForAddStatement( lDiffAction, pString );
    }

    if( lDiffAction != null )
    {
      if( !isAdditionsOnlyMode || !(pIsgnoreIfAdditionsOnly || ignoreEverythingIfAdditionsOnly) )
      {
        if( failMessage != null )
        {
          lDiffAction.addFailureStatement( pString, failMessage );
        }
        else
        {
          if( pComment != null )
          {
            lDiffAction.addStatement( pString, pComment );
          }
          else
          {
            lDiffAction.addStatement( pString );
          }
        }
      }
      else
      {
        lDiffAction.addIgnoredStatement( pString, ADDITIONSONLY_COMMENT );
      }
    }
    else
    {
      throw new IllegalStateException( "no active diff action: " + pString );
    }
  }

  void addStmt( String pString )
  {
    addStmt( pString, false );
  }

  void addStmt( String pString, Runnable pAdditionsOnlyAlternativeHandler )
  {
    if( isAdditionsOnlyMode )
    {
      addStmt( pString, true );
      pAdditionsOnlyAlternativeHandler.run();
    }
    else
    {
      addStmt( pString );
    }
  }

  void stmtDone()
  {
    stmtDone( false );
  }

  void stmtDone( Runnable pAdditionsOnlyAlternativeHandler )
  {
    stmtDone( true );

    if( isAdditionsOnlyMode )
    {
      pAdditionsOnlyAlternativeHandler.run();
    }
  }

  void stmtDone( boolean pIsgnoreIfAdditionsOnly )
  {
    stmtDone( pIsgnoreIfAdditionsOnly, null );
  }

  void stmtDone( String pComment )
  {
    stmtDone( false, pComment );
  }

  void stmtDone( boolean pIsgnoreIfAdditionsOnly, String pComment )
  {
    addStmt( _stmt, pIsgnoreIfAdditionsOnly, pComment );
    _stmt = null;
  }

  public void failIfAdditionsOnly( String pMessage )
  {
    failIfAdditionsOnly( true, pMessage );
  }

  public void failIfAdditionsOnly( boolean pFailIfAdditionsOnly, String pMessage )
  {
    if( isAdditionsOnlyMode && pFailIfAdditionsOnly && !ignoreEverythingIfAdditionsOnly )
    {
      fail( ADDITIONSONLY_COMMENT + ":" + pMessage );
    }
  }

  public void fail( String pMessage )
  {
    failMessage = pMessage;
  }

  public void ignoreEverythingIfAdditionsOnly( boolean pIgnoreIfAdditionsOnly )
  {
    ignoreEverythingIfAdditionsOnly = pIgnoreIfAdditionsOnly;
  }
}
