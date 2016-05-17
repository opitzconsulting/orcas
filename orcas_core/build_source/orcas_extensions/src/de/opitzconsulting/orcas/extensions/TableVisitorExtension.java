package de.opitzconsulting.orcas.extensions;

import java.util.ArrayList;

import de.opitzconsulting.orcasDsl.Model;
import de.opitzconsulting.orcasDsl.ModelElement;
import de.opitzconsulting.orcasDsl.Table;

public abstract class TableVisitorExtension extends OrcasBaseExtensionWithParameter
{
  private Model _model;

  protected Model getModel()
  {
    return _model;
  }

  public Model transformModel( Model pModel )
  {
    _model = pModel;

    for( ModelElement lModelElement : new ArrayList<ModelElement>( pModel.getModel_elements() ) )
    {
      if( lModelElement instanceof Table )
      {
        handleTable( (Table)lModelElement );
      }
    }

    _model = null;

    return pModel;
  }

  protected abstract void handleTable( Table pTable );
}
