package de.opitzconsulting.orcas.diff;

import java.net.URL;

import org.apache.commons.logging.Log;
import org.eclipse.emf.ecore.EObject;

import de.opitzconsulting.orcas.sql.CallableStatementProvider;

public abstract class SimpleExtensionHandlerImpl<T extends EObject> extends BaseExtensionHandlerImpl<T>
{
  @Override
  public String convertModelToXMLString( de.opitzconsulting.origOrcasDsl.Model lOrigModel )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void handleTargetplsql( CallableStatementProvider pCallableStatementProvider )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void initOrcasDbIfNeeded( Log pLog )
  {
  }

  @Override
  public URL getXsltExtractFileURL()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public URL getUriResolverURLForImport( String pHref )
  {
    throw new UnsupportedOperationException();
  }
}
