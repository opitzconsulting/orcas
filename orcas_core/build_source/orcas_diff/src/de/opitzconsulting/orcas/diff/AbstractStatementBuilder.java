package de.opitzconsulting.orcas.diff;

import java.util.ArrayList;
import java.util.List;

public class AbstractStatementBuilder
{
  private String _stmt;
  private List<String> _stmtList = new ArrayList<String>();

  protected void addStmt( String pString )
  {
    _stmtList.add( pString );
  }

  protected void stmtDone()
  {
    addStmt( _stmt );
    _stmt = null;
  }

  protected void stmtAppend( String pString )
  {
    _stmt = _stmt + " " + pString;
  }

  protected void stmtStart( String pString )
  {
    _stmt = pString;
  }

  protected List<String> getStmtList()
  {
    return _stmtList;
  }
}
