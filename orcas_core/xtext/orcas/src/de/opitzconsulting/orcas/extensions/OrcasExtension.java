package de.opitzconsulting.orcas.extensions;

import de.opitzconsulting.orcasDsl.Model;

public interface OrcasExtension
{
  /**
   * In dieser Methode kann das Model veraendert werden, es kann uch ein komplett neuese Mdoel zurueckgegeben werden.
   */
  Model transformModel( Model pModel );
}
