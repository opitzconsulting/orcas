package de.opitzconsulting.orcas.extensions;

import java.util.ArrayList;

import de.opitzconsulting.orcasDsl.*;
import de.opitzconsulting.orcasDsl.impl.*;

public class ColumnDomainExtension extends TableVisitorExtension
{
  @Override
  protected void handleTable( Table pTable )
  {
    for( Column lColumn : pTable.getColumns() )
    { 
      if( lColumn.getDomain() == EnumColumnDomain.PK_COLUMN || lColumn.getDomain() == EnumColumnDomain.FK_COLUMN )
      {    
        lColumn.setData_type( DataType.NUMBER );
        lColumn.setPrecision( 15 );

        if( lColumn.getDomain() == EnumColumnDomain.PK_COLUMN )
        {
          lColumn.setNotnull( "not null" ); 
          _addPKColumns( pTable, lColumn );          
        }
	else
	{
          _addFK( pTable, lColumn );          
	}
      }
    }
  }

  private void _addPKColumns( Table pTable, Column pColumn )
  {
    PrimaryKey lPrimaryKey = new PrimaryKeyImpl();
    ColumnRef lColumnRef = new ColumnRefImpl();
    lColumnRef.setColumn_name( pColumn.getName() );

    lPrimaryKey.getPk_columns().add( lColumnRef );
    lPrimaryKey.setConsName( pColumn.getName() + "_PK" );
    pTable.setPrimary_key( lPrimaryKey );
  }

  private void _addFK( Table pTable, Column pColumn )
  {
    for( ModelElement lModelElement : new ArrayList<ModelElement>( getModel().getModel_elements() ) )
    {
      if( lModelElement instanceof Table )
      {
        Table lTable = (Table)lModelElement;

	for( Column lColumn : lTable.getColumns() )
        {		
          if( lColumn.getName().equals( pColumn.getName() ) && lColumn.getDomain() == EnumColumnDomain.PK_COLUMN )
          {
            ForeignKey lForeignKey = new ForeignKeyImpl();

            ColumnRef lDestColumnRef = new ColumnRefImpl();
            lDestColumnRef.setColumn_name( lColumn.getName() );
            lForeignKey.getDestColumns().add( lDestColumnRef );
            ColumnRef lSrcColumnRef = new ColumnRefImpl();
            lSrcColumnRef.setColumn_name( pColumn.getName() );
            lForeignKey.getSrcColumns().add( lSrcColumnRef );	    
            lForeignKey.setConsName(  "FK_" + pTable.getName() + "_" + pColumn.getName() );	    
            lForeignKey.setDestTable( lTable.getName() );	    

	    pTable.getForeign_keys().add( lForeignKey );

	    return;
          }
	}
      }
    }

    throw new RuntimeException( "FK nicht gefunden: " + pTable.getName() + "_" + pColumn.getName() );
  }
}
