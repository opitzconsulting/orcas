package de.opitzconsulting.orcas.syntax_extensions;

import de.opitzconsulting.orcasXtextExtension.Model;

public interface OrcasSyntaxExtensionWithParameter extends OrcasSyntaxExtension
{
  /**
   * Setzt den Parameter (der ueber extensionparameter aus dem task orcas_initialize uebergeben wird).
   */
  void setParameter( String pParameter );
}
