package de.opitzconsulting.orcas.extensions;

import java.util.ArrayList;
import java.util.List;

import de.opitzconsulting.orcasDsl.Model;

public abstract class BaseMultiExtensions extends OrcasBaseExtensionWithParameter
{
  private List<OrcasExtension> _extensions = new ArrayList<OrcasExtension>();

  protected void addExtension( OrcasExtension pExtension )
  {
    _extensions.add( pExtension );
  }

  public Model transformModel( Model pModel )
  {
    Model lModel = pModel;

    for( OrcasExtension lOrcasExtension : _extensions )
    {
      if( lOrcasExtension instanceof OrcasExtensionWithParameter )
      {
        ((OrcasExtensionWithParameter)lOrcasExtension).setParameter( getParameterAsString() );
      }

      lModel = lOrcasExtension.transformModel( lModel );
    }

    return lModel;
  }
}
