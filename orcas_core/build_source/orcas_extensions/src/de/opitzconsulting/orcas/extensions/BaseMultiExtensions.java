package de.opitzconsulting.orcas.extensions;

import java.util.ArrayList;
import java.util.List;

import de.opitzconsulting.orcasDsl.Model;

public abstract class BaseMultiExtensions extends OrcasBaseExtensionWithParameter
{
  private List<OrcasExtension> _extensions = new ArrayList<OrcasExtension>();
  private boolean useReverseExtension = false;

  protected void addExtension( OrcasExtension pExtension )
  {
    _extensions.add( pExtension );
  }

  public Model transformModel( Model pModel )
  {
    Model lModel = pModel;

    for( OrcasExtension lOrcasExtension : getExtensions() )
    {
      if( lOrcasExtension instanceof OrcasExtensionWithParameter )
      {
        ((OrcasExtensionWithParameter)lOrcasExtension).setParameter( getParameterAsString() );
      }

      lModel = lOrcasExtension.transformModel( lModel );
    }

    return lModel;
  }

  private List<OrcasExtension> getExtensions()
  {
    List<OrcasExtension> lReturn = new ArrayList<OrcasExtension>();

    for( OrcasExtension lOrcasExtension : _extensions )
    {
      if( isReverseExtension( lOrcasExtension ) == useReverseExtension )
      {
        lReturn.add( lOrcasExtension );
      }
    }

    return lReturn;
  }

  private boolean isReverseExtension( OrcasExtension pOrcasExtension )
  {
    return pOrcasExtension instanceof OrcasReverseExtension;
  }

  public boolean hasExtension()
  {
    return !getExtensions().isEmpty();
  }

  public void setUseReverseExtension( boolean pUseReverseExtension )
  {
    useReverseExtension = pUseReverseExtension;
  }
}
