package de.opitzconsulting.orcas.syntax_extensions;

public class BenutzerstempelSyntaxExtension extends BaseSyntaxExtension
{
  public void run()
  {
    addField( new FieldReference( "Table", "columns" ), new NewFieldConstant( "benutzerstempel", "benutzerstempel," ) );
  }
}
