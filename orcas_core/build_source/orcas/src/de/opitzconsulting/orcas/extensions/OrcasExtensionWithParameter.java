package de.opitzconsulting.orcas.extensions;

public interface OrcasExtensionWithParameter extends OrcasExtension
{
  /**
   * Setzt den Parameter (der ueber extensionparameter aus dem task orcas_initialize uebergeben wird).
   */
  void setParameter( String pParameter );
}
