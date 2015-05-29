package de.opitzconsulting.orcas.extensions;

import de.opitzconsulting.orcasDsl.*;
import de.opitzconsulting.orcasDsl.impl.*;

public class AddSequenceFromAlias extends TableVisitorExtension
{
  @Override
  protected void handleTable( Table pTable )
  {
    if( pTable.getAlias() != null )
    {
      Sequence lSequence = new SequenceImpl();
      lSequence.setSequence_name( pTable.getAlias() + "_SEQ" );
      lSequence.setMax_value_select( "select max(" + pTable.getAlias() + "_ID) from " + pTable.getName() );
      getModel().getModel_elements().add( lSequence );
    }
  }
}