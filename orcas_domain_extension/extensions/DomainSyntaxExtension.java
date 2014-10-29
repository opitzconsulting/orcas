package de.opitzconsulting.orcas.syntax_extensions;

import java.util.ArrayList;
import java.util.List;

import de.opitzconsulting.orcasXtextExtension.ModelElement;
import de.opitzconsulting.orcasXtextExtension.Rule;
import de.opitzconsulting.orcasXtextExtension.RuleConstnatEntry;
import de.opitzconsulting.orcasXtextExtension.RuleEntry;
import de.opitzconsulting.orcasXtextExtension.RuleNormal;
import de.opitzconsulting.orcas.syntax_extensions.BaseSyntaxExtension;

public class DomainSyntaxExtension extends BaseSyntaxExtension
{
  public void run()
  {
    {
      RuleNormal lRuleNormal = createRuleNormal( "DomainColumn" );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( "add" ) );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( "column" ) );
      addField( lRuleNormal, new NewFieldConstant( "append_last", "append_last" ) );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( "column-name" ) );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( "(" ) );
      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( "(" ) );
      lRuleNormal.getRule_entries().add( createMultiValueRuleEntry( "columnNameRules", "GenNameRule" ) );
      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( ")*" ) );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( ")" ) );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( "(" ) );
      addField( lRuleNormal, new NewFieldDataTypeWithoutKeyword( "column", "Column", true ) );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( ")" ) );
      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( "(" ) );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( "," ) );
      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( ")?" ) );
    }

    {
      RuleNormal lRuleNormal = createRuleNormal( "HistoryTable" );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( "add" ) );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( "history-table" ) );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( "(" ) );

      lRuleNormal.getRule_entries().add( createConstantRuleEntry( "table-name" ) );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( "(" ) );
      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( "(" ) );
      lRuleNormal.getRule_entries().add( createMultiValueRuleEntry( "tableNameRules", "GenNameRule" ) );
      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( ")*" ) );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( ")" ) );

      lRuleNormal.getRule_entries().add( createConstantRuleEntry( "alias-name" ) );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( "(" ) );
      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( "(" ) );
      lRuleNormal.getRule_entries().add( createMultiValueRuleEntry( "aliasNameRules", "GenNameRule" ) );
      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( ")*" ) );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( ")" ) );

      lRuleNormal.getRule_entries().add( createConstantRuleEntry( "primary-key-mode" ) );
      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( "(" ) );
      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( "(" ) );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( "append" ) );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( "(" ) );
      addField( lRuleNormal, new NewFieldDataIdentifierWithoutKeyword( "appendToPkDomain", true ) );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( ")" ) );
      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( ")" ) );
      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( "|" ) );
      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( "(" ) );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( "remove" ) );
      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( ")" ) );
      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( ")" ) );

      addField( lRuleNormal, new NewFieldDataIdentifier( "domain", false ) );

      lRuleNormal.getRule_entries().add( createConstantRuleEntry( ")" ) );
    }

    {
      RuleNormal lRuleNormal = createRuleNormal( "Domain" );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( "define" ) );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( "table" ) );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( "domain" ) );
      addField( lRuleNormal, new NewFieldDataIdentifierWithoutKeyword( "name", true ) );
      addField( lRuleNormal, new NewFieldDataIdentifier( "extends", false ) );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( "(" ) );

      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( "(" ) );
      lRuleNormal.getRule_entries().add( createMultiValueRuleEntry( "columns", "DomainColumn" ) );
      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( ")*" ) );

      addField( lRuleNormal, new NewFieldDataTypeWithoutKeyword( "historyTable", "HistoryTable", false ) );

      lRuleNormal.getRule_entries().add( createConstantRuleEntry( ")" ) );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( ";" ) );
    }

    {
      RuleNormal lRuleNormal = createRuleNormal( "GenNameRulePart" );
      lRuleNormal.setRule_enum( "enum" );

      lRuleNormal.getRule_entries().add( createRuleConstantEntry( "column_name", "column-name" ) );
      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( "|" ) );
      lRuleNormal.getRule_entries().add( createRuleConstantEntry( "table_name", "table-name" ) );
      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( "|" ) );
      lRuleNormal.getRule_entries().add( createRuleConstantEntry( "alias_name", "alias-name" ) );
      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( "|" ) );
      lRuleNormal.getRule_entries().add( createRuleConstantEntry( "column_domain_name", "column-domain-name" ) );
    }

    {
      RuleNormal lRuleNormal = createRuleNormal( "GenNameRule" );
      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( "(" ) );
      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( "(" ) );
      addField( lRuleNormal, new NewFieldDataTypeWithoutKeyword( "constant_name", "STRING", true ) );
      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( ")" ) );
      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( "|" ) );
      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( "(" ) );
      addField( lRuleNormal, new NewFieldDataTypeWithoutKeyword( "constant_part", "GenNameRulePart", true ) );
      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( ")" ) );
      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( ")" ) );
      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( "(" ) );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( "||" ) );
      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( ")?" ) );
    }

    {
      RuleNormal lRuleNormal = createRuleNormal( "GeneratePk" );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( "generate-primary-key" ) );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( "(" ) );

      lRuleNormal.getRule_entries().add( createConstantRuleEntry( "constraint-name" ) );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( "(" ) );
      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( "(" ) );
      lRuleNormal.getRule_entries().add( createMultiValueRuleEntry( "constraintNameRules", "GenNameRule" ) );
      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( ")*" ) );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( ")" ) );

      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( "(" ) );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( "sequence-name" ) );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( "(" ) );
      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( "(" ) );
      lRuleNormal.getRule_entries().add( createMultiValueRuleEntry( "sequenceNameRules", "GenNameRule" ) );
      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( ")*" ) );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( ")" ) );
      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( ")?" ) );

      lRuleNormal.getRule_entries().add( createConstantRuleEntry( ")" ) );
    }

    {
      RuleNormal lRuleNormal = createRuleNormal( "GenerateUk" );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( "generate-unique-key" ) );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( "(" ) );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( "constraint-name" ) );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( "(" ) );
      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( "(" ) );
      lRuleNormal.getRule_entries().add( createMultiValueRuleEntry( "constraintNameRules", "GenNameRule" ) );
      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( ")*" ) );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( ")" ) );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( ")" ) );
    }

    {
      RuleNormal lRuleNormal = createRuleNormal( "GenerateCc" );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( "generate-check-constraint" ) );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( "(" ) );

      lRuleNormal.getRule_entries().add( createConstantRuleEntry( "constraint-name" ) );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( "(" ) );
      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( "(" ) );
      lRuleNormal.getRule_entries().add( createMultiValueRuleEntry( "constraintNameRules", "GenNameRule" ) );
      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( ")*" ) );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( ")" ) );

      lRuleNormal.getRule_entries().add( createConstantRuleEntry( "check-rule" ) );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( "(" ) );
      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( "(" ) );
      lRuleNormal.getRule_entries().add( createMultiValueRuleEntry( "checkRuleNameRules", "GenNameRule" ) );
      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( ")*" ) );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( ")" ) );

      lRuleNormal.getRule_entries().add( createConstantRuleEntry( ")" ) );
    }

    {
      RuleNormal lRuleNormal = createRuleNormal( "GenerateFk" );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( "generate-foreign-key" ) );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( "(" ) );

      lRuleNormal.getRule_entries().add( createConstantRuleEntry( "constraint-name" ) );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( "(" ) );
      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( "(" ) );
      lRuleNormal.getRule_entries().add( createMultiValueRuleEntry( "constraintNameRules", "GenNameRule" ) );
      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( ")*" ) );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( ")" ) );

      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( "(" ) );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( "pk-column-name" ) );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( "(" ) );
      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( "(" ) );
      lRuleNormal.getRule_entries().add( createMultiValueRuleEntry( "pkColumnNameRules", "GenNameRule" ) );
      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( ")*" ) );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( ")" ) );
      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( ")" ) );

      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( "(" ) );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( "on" ) );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( "delete" ) );
      addField( lRuleNormal, new NewFieldDataTypeWithoutKeyword( "delete_rule", "FkDeleteRuleType", true ) );
      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( ")?" ) );

      lRuleNormal.getRule_entries().add( createConstantRuleEntry( ")" ) );
    }

    {
      RuleNormal lRuleNormal = createRuleNormal( "ColumnDomain" );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( "define" ) );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( "column" ) );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( "domain" ) );
      addField( lRuleNormal, new NewFieldDataIdentifierWithoutKeyword( "name", true ) );
      addField( lRuleNormal, new NewFieldDataTypeWithoutKeyword( "generatePk", "GeneratePk", false ) );
      addField( lRuleNormal, new NewFieldDataTypeWithoutKeyword( "generateUk", "GenerateUk", false ) );
      addField( lRuleNormal, new NewFieldDataTypeWithoutKeyword( "generateCc", "GenerateCc", false ) );
      addField( lRuleNormal, new NewFieldDataTypeWithoutKeyword( "generateFk", "GenerateFk", false ) );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( "(" ) );
      addField( lRuleNormal, new NewFieldDataTypeWithoutKeyword( "data_type", "DataType", true ) );
      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( "(" ) );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( "(" ) );
      addField( lRuleNormal, new NewFieldDataTypeWithoutKeyword( "precision", "INT", true ) );
      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( "(" ) );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( "," ) );
      addField( lRuleNormal, new NewFieldDataTypeWithoutKeyword( "scale", "INT", true ) );
      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( ")?" ) );
      addField( lRuleNormal, new NewFieldDataTypeWithoutKeyword( "byteorchar", "CharType", false ) );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( ")" ) );
      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( ")?" ) );
      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( "(" ) );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( "default" ) );
      addField( lRuleNormal, new NewFieldDataTypeWithoutKeyword( "default_value", "STRING", true ) );
      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( ")?" ) );
      addField( lRuleNormal, new NewFieldConstant( "notnull", "not" ) );
      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( "(" ) );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( "null" ) );
      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( ")*" ) );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( ")" ) );
      lRuleNormal.getRule_entries().add( createConstantRuleEntry( ";" ) );
    }

    {
      RuleNormal lRuleNormal = findRuleNormal( "ModelElement" );
      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( "|" ) );
      lRuleNormal.getRule_entries().add( createRuleName( "Domain" ) );
      lRuleNormal.getRule_entries().add( createMarkerRuleEntry( "|" ) );
      lRuleNormal.getRule_entries().add( createRuleName( "ColumnDomain" ) );
    }

    addField( new FieldReference( "Table", "name" ), new NewFieldDataIdentifier( "domain", false ) );
    addField( new FieldReference( "Table", "name" ), new NewFieldDataIdentifier( "alias", false ) );

    addField( new FieldReference( "Column", "name" ), new NewFieldDataIdentifier( "domain", false ) );
    makeOptional( new FieldReference( "Column", "data_type" ) );
  }
}
