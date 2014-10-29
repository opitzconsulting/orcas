package de.opitzconsulting.orcas.extensions;

import de.opitzconsulting.orcasDsl.*;
import de.opitzconsulting.orcasDsl.impl.*;

public class Benutzerstempel extends TableVisitorExtension
{
  @Override
  protected void handleTable( Table pTable )
  {
    if( pTable.getBenutzerstempel() != null )
    {
      {
        Column lColumn = new ColumnImpl();

        lColumn.setName( pTable.getAlias() + "_INSERT_DATE" );
        lColumn.setData_type( DataType.DATE );
        pTable.getColumns().add( lColumn );
      }

      {
        Column lColumn = new ColumnImpl();

        lColumn.setName( pTable.getAlias() + "_INSERT_USER" );
        lColumn.setData_type( DataType.VARCHAR2 );
        lColumn.setPrecision( 100 );
        pTable.getColumns().add( lColumn );
      }

      {
        Column lColumn = new ColumnImpl();

        lColumn.setName( pTable.getAlias() + "_UPDATE_DATE" );
        lColumn.setData_type( DataType.DATE );
        pTable.getColumns().add( lColumn );
      }

      {
        Column lColumn = new ColumnImpl();

        lColumn.setName( pTable.getAlias() + "_UPDATE_USER" );
        lColumn.setData_type( DataType.VARCHAR2 );
        lColumn.setPrecision( 100 );
        pTable.getColumns().add( lColumn );
      }

      {
        Column lColumn = new ColumnImpl();

        lColumn.setName( pTable.getAlias() + "_COMMENT" );
        lColumn.setData_type( DataType.VARCHAR2 );
        lColumn.setPrecision( 2000 );
        pTable.getColumns().add( lColumn );
      }
    }
  }
}
