package de.opitzconsulting.orcas.diff;

import de.opitzconsulting.orcas.orig.diff.TableDiff;

public class AbstractStatementBuilder
{
  protected String _stmt;
  protected AlterTableCombiner alterTableCombiner;

  public AbstractStatementBuilder( AlterTableCombiner pAlterTableCombiner )
  {
    alterTableCombiner = pAlterTableCombiner;
  }

  protected void stmtAppend( String pString )
  {
    _stmt = _stmt + " " + pString;
  }

  protected void stmtStart( String pString )
  {
    _stmt = pString;
  }

  protected void stmtStartAlterTable( String pTablename )
  {
    stmtStart( "alter table " + pTablename );
  }

  protected void stmtStartAlterTable( TableDiff pTableDiff )
  {
    stmtStartAlterTable( pTableDiff, true );
  }

  protected void stmtStartAlterTableNoCombine( TableDiff pTableDiff )
  {
    stmtStartAlterTable( pTableDiff, false );
  }

  private void stmtStartAlterTable( TableDiff pTableDiff, boolean pIsUseAlterTableCombiner )
  {
    String lStatementStart = "alter table ";

    if( pTableDiff.isNew )
    {
      lStatementStart += pTableDiff.nameNew;
    }
    else
    {
      lStatementStart += pTableDiff.nameOld;
    }

    if( alterTableCombiner != null )
    {
      if( pIsUseAlterTableCombiner )
      {
        alterTableCombiner.handleStartAlterTable( pTableDiff, lStatementStart );
      }
      else
      {
        alterTableCombiner.finishIfNeeded();
      }
    }

    stmtStart( lStatementStart );
  }
}
