package de.opitzconsulting.orcas.diff;

import org.eclipse.emf.ecore.EObject;

import com.google.inject.Injector;

public abstract class BaseExtensionHandlerImpl<T extends EObject> extends BaseParameterBasedHandler implements ExtensionHandler
{
  @Override
  public void setParameters( Parameters pParameters )
  {
    setParametersInternal( pParameters );
  }

  protected abstract XtextFileLoader<T> createlXtextFileLoader();

  protected T loadSyexModelFromFiles( Injector pInjector )
  {
    return createlXtextFileLoader().loadModelDsl( FolderHandler.getModelFiles( getParameters() ), getParameters(), pInjector );
  }
}
