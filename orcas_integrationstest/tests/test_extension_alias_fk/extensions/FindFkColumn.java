package de.opitzconsulting.orcas.extensions;

import de.opitzconsulting.orcasDsl.*;
import de.opitzconsulting.orcasDsl.impl.*;

public class FindFkColumn extends TableVisitorExtension
{
  @Override
  protected void handleTable( Table pTable )
  {
    for( ForeignKey lForeignKey : pTable.getForeign_keys() )
    {
      String lDestTableAlias = _findTable( lForeignKey.getDestTable() ).getAlias();

      ColumnRef lDestColumnRef = new ColumnRefImpl();
      lDestColumnRef.setColumn_name( lDestTableAlias + "_ID" );

	  if( lForeignKey.getDestColumns().isEmpty() )
      {
        lForeignKey.getDestColumns().add( lDestColumnRef );
      }

      if( lForeignKey.getSrcColumns().isEmpty() )
      {
        ColumnRef lSrcColumnRef = new ColumnRefImpl();
        lSrcColumnRef.setColumn_name( lDestTableAlias + "_ID" );
        lForeignKey.getSrcColumns().add( lSrcColumnRef );
      }
    }
  }

  private Table _findTable( String pTableName )
  {
    for( ModelElement lModelElement : getModel().getModel_elements() )
    {
      if( lModelElement instanceof Table )
      {
        Table lTable = (Table)lModelElement;

        if( lTable.getName().equals( pTableName ) )
        {
          return lTable;
        }
      }
    }

    throw new RuntimeException( "Tabelle nicht gefunden: " + pTableName );
  }
}
