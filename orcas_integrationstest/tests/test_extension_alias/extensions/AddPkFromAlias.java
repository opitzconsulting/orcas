package de.opitzconsulting.orcas.extensions;

import de.opitzconsulting.orcasDsl.*;
import de.opitzconsulting.orcasDsl.impl.*;

public class AddPkFromAlias extends TableVisitorExtension
{
  @Override
  protected void handleTable( Table pTable )
  {
    if( pTable.getAlias() != null )
    {
      Column lColumn = new ColumnImpl();
      lColumn.setName( pTable.getAlias() + "_ID" );
      lColumn.setData_type( DataType.NUMBER );
      lColumn.setPrecision( 22 );
      lColumn.setNotnull( "not" );
      pTable.getColumns().add( 0, lColumn );
      _addPKColumns( pTable );
    }
  }

  private void _addPKColumns( Table pTable )
  {
    PrimaryKey lPrimaryKey = new PrimaryKeyImpl();
    ColumnRef lColumnRef = new ColumnRefImpl();
    lColumnRef.setColumn_name( pTable.getAlias() + "_ID" );

    lPrimaryKey.getPk_columns().add( lColumnRef );
    lPrimaryKey.setConsName(  pTable.getAlias() + "_PK" );
    pTable.setPrimary_key( lPrimaryKey );
  }
}
