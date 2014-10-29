package de.opitzconsulting.orcas.extensions;

import de.opitzconsulting.orcasDsl.*;

public class CharColumn extends TableVisitorExtension
{
  @Override
  protected void handleTable( Table pTable )
  {
    for( Column lColumn : pTable.getColumns() )
    {
      if( lColumn.getData_type() == DataType.VARCHAR2 )
      {
        lColumn.setByteorchar( CharType.CHAR );
      }
    }
  }
}
