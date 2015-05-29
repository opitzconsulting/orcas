package de.opitzconsulting.orcas.extensions;

import java.util.ArrayList;

import de.opitzconsulting.orcasDsl.*;
import de.opitzconsulting.orcasDsl.impl.*;

public class ColumnIdentityExtension extends TableVisitorExtension
{
  @Override
  protected void handleTable( Table pTable )
  {
    int count = 0;

    for( Column lColumn : pTable.getColumns() )
    {
      if( lColumn.getIdentity() != null )
                     {
        if( count > 0 )
        {
          throw new RuntimeException( "Mehr als 1 Spalte pro Tabelle mit ColumnIdentity gefunden: " + pTable.getName() + "_" + lColumn.getName() );
        }

        lColumn.setDefault_value( null );

        _addSequence(pTable, lColumn);

        count = count + 1;
      }
    }
  }

  private void _addSequence( Table pTable, Column pColumn )
  {
    Sequence lSequence = new SequenceImpl();
    lSequence.setSequence_name( pTable.getAlias() + "_IDENTITY_SEQ" );
    lSequence.setMax_value_select("select max(" + pColumn.getName() + ") from " + pTable.getName());
    lSequence.setIncrement_by(pColumn.getIdentity().getIncrement_by());
    lSequence.setMaxvalue(pColumn.getIdentity().getMaxvalue());
    lSequence.setMinvalue(pColumn.getIdentity().getMinvalue());
    lSequence.setCycle(pColumn.getIdentity().getCycle());
    lSequence.setCache(pColumn.getIdentity().getCache());
    lSequence.setOrder(pColumn.getIdentity().getOrder());
    getModel().getModel_elements().add( lSequence );
  }

}
