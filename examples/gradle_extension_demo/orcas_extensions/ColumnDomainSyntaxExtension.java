package de.opitzconsulting.orcas.syntax_extensions;

public class ColumnDomainSyntaxExtension extends BaseSyntaxExtension
{
  public void run()
  {
    addField( new FieldReference( "Column", "name" ), new NewFieldDataEnumeration( "domain", false, "pk_column", "fk_column" ) );
    makeOptional( new FieldReference( "Column", "data_type" ) );
  }
}
