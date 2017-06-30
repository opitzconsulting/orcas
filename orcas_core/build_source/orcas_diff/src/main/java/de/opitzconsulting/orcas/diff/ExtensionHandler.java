package de.opitzconsulting.orcas.diff;

import java.net.URL;

import org.apache.commons.logging.Log;

import de.opitzconsulting.orcas.sql.CallableStatementProvider;
import de.opitzconsulting.origOrcasDsl.Model;

public interface ExtensionHandler
{
  void setParameters( Parameters pParameters );
  
  Model loadModel();

  String convertModelToXMLString( Model pModel );

  void handleTargetplsql( CallableStatementProvider pCallableStatementProvider );

  void initOrcasDbIfNeeded( Log pLog );
  
  URL getXsltExtractFileURL();

  URL getUriResolverURLForImport( String pHref );
}
