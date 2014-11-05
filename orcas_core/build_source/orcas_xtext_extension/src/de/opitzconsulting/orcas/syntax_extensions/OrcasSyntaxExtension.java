package de.opitzconsulting.orcas.syntax_extensions;

import de.opitzconsulting.orcasXtextExtension.Model;

public interface OrcasSyntaxExtension
{
  /**
   * In dieser Methode kann das Syntax Model veraendert werden, es kann auch ein komplett neuese Mdoel zurueckgegeben werden.
   */
  Model transformSyntaxModel( Model pModel );
}
