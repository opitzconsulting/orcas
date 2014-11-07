package de.opitzconsulting.orcas.syntax_extensions;

public class AliasSyntaxExtension extends BaseSyntaxExtension
{
  public void run()
  {
    addField( new FieldReference( "Table", "name" ), new NewFieldDataIdentifier( "alias", false ) );
  }
}
