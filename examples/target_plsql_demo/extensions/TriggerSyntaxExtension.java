package de.opitzconsulting.orcas.syntax_extensions;

public class TriggerSyntaxExtension extends BaseSyntaxExtension
{
  public void run()
  {
    addField( new FieldReference( "Table", "name" ), new NewFieldConstant( "with_trigger", "with_trigger," ) );
  }
}
