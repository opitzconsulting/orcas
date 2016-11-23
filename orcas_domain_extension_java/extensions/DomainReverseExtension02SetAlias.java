package de.opitzconsulting.orcas.extensions;

import java.util.ArrayList;

import de.opitzconsulting.orcasDsl.Model;
import de.opitzconsulting.orcasDsl.ModelElement;
import de.opitzconsulting.orcasDsl.Table;

public class DomainReverseExtension02SetAlias extends OrcasBaseExtensionWithParameter implements OrcasReverseExtension
{
  @Override
  public Model transformModel( Model pModel )
  {
    for( ModelElement lModelElement : new ArrayList<ModelElement>( pModel.getModel_elements() ) )
    {
      if( lModelElement instanceof Table )
      {
        Table lTable = (Table)lModelElement;
        if( lTable.getPrimary_key() != null )
        {
          lTable.setAlias( lTable.getPrimary_key().getConsName().substring( 0, lTable.getPrimary_key().getConsName().length() - 3 ) );
        }
      }
    }

    return pModel;
  }
}
