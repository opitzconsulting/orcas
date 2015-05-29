package de.opitzconsulting.orcas.syntax_extensions;

import java.util.ArrayList;
import java.util.List;

import de.opitzconsulting.orcasXtextExtension.Model;
import de.opitzconsulting.orcasXtextExtension.Rule;
import de.opitzconsulting.orcasXtextExtension.RuleConstnatEntry;
import de.opitzconsulting.orcasXtextExtension.RuleEntry;
import de.opitzconsulting.orcasXtextExtension.RuleNormal;
import de.opitzconsulting.orcasXtextExtension.RuleValueEntry;
import de.opitzconsulting.orcasXtextExtension.impl.RuleConstnatEntryImpl;
import de.opitzconsulting.orcasXtextExtension.impl.RuleEntryImpl;
import de.opitzconsulting.orcasXtextExtension.impl.RuleNormalImpl;
import de.opitzconsulting.orcasXtextExtension.impl.RuleValueEntryImpl;

public abstract class BaseSyntaxExtension implements OrcasSyntaxExtensionWithParameter, Runnable
{
  private Model _model;
  private String _parameter;

  public void setParameter( String pParameter )
  {
    _parameter = pParameter;
  }

  public String getParameterAsString()
  {
    return _parameter;
  }

  protected Model getModel()
  {
    return _model;
  }

  public final Model transformSyntaxModel( Model pModel )
  {
    _model = pModel;

    run();

    return pModel;
  }

  protected RuleNormal findRuleNormal( FieldReference pFieldReference )
  {
    return findRuleNormal( pFieldReference._ruleName );
  }

  protected RuleNormal findRuleNormal( String pRuleName )
  {
    for( Rule lRule : _model.getModel_elements().get( 0 ).getRules() )
    {
      if( lRule.getName().equals( pRuleName ) )
      {
        return (RuleNormal)lRule;
      }
    }

    throw new RuntimeException( "rule not found: " + pRuleName );
  }

  public static class FieldReference
  {
    private String _ruleName;
    private String _fieldName;

    public FieldReference( String pRuleName, String pFieldName )
    {
      _ruleName = pRuleName;
      _fieldName = pFieldName;
    }
  }

  public abstract static class NewFieldData
  {
    private String _fieldName;
    private boolean _optional;

    protected String getFieldName()
    {
      return _fieldName;
    }

    public abstract List<RuleEntry> getRuleEntries( BaseSyntaxExtension pBaseSyntaxExtension );

    protected boolean isOptional()
    {
      return _optional;
    }

    private NewFieldData( String pFieldName, boolean pMandatory )
    {
      _fieldName = pFieldName;
      _optional = !pMandatory;
    }

    abstract void addFieldAfter( FieldReference pFieldReference, BaseSyntaxExtension pBaseSyntaxExtension );

    abstract void addField( RuleNormal pRuleNormal, BaseSyntaxExtension pBaseSyntaxExtension );
  }

  private static class _NewFieldDataSimpleField extends NewFieldData
  {
	private String _typeName;
    private boolean _useConstantKeyword;

    protected String getTypeName()
    {
      return _typeName;
    }

    protected void setTypeName( String pTypeName )
    {
      _typeName = pTypeName;
    }

    public _NewFieldDataSimpleField( String pFieldName, boolean pMandatory, String pTypeName, boolean pUseConstantKeyword )
    {
      super( pFieldName, pMandatory );

      _typeName = pTypeName;
      _useConstantKeyword = pUseConstantKeyword;
    }

    @Override
    void addFieldAfter( FieldReference pFieldReference, BaseSyntaxExtension pBaseSyntaxExtension )
    {
      pBaseSyntaxExtension.addRuleEntries( pFieldReference, getRuleEntries( pBaseSyntaxExtension ) );
    }

    @Override
    public List<RuleEntry> getRuleEntries( BaseSyntaxExtension pBaseSyntaxExtension )
    {
      List<RuleEntry> lRuleEntries = new ArrayList<RuleEntry>();

      if( isOptional() )
      {
        lRuleEntries.add( pBaseSyntaxExtension.createMarkerRuleEntry( "(" ) );
      }

      if( _useConstantKeyword )
      {
        lRuleEntries.add( pBaseSyntaxExtension.createConstantRuleEntry( getFieldName() ) );
      }

      lRuleEntries.add( createValueRuleEntry(pBaseSyntaxExtension) );

      if( isOptional() )
      {
        lRuleEntries.add( pBaseSyntaxExtension.createMarkerRuleEntry( ")?" ) );
      }
      return lRuleEntries;
    }

	protected RuleEntry createValueRuleEntry(BaseSyntaxExtension pBaseSyntaxExtension) 
	{
	  return pBaseSyntaxExtension.createValueRuleEntry( getFieldName(), _typeName );
	}

    @Override
    void addField( RuleNormal pRuleNormal, BaseSyntaxExtension pBaseSyntaxExtension )
    {
      pBaseSyntaxExtension.addRuleEntries( pRuleNormal, getRuleEntries( pBaseSyntaxExtension ) );
    }
  }

  public static class NewFieldDataIdentifier extends _NewFieldDataSimpleField
  {
    public NewFieldDataIdentifier( String pFieldName, boolean pMandatory )
    {
      super( pFieldName, pMandatory, "DBNAME", true );
    }
  }

  public static class NewFieldDataIdentifierWithoutKeyword extends _NewFieldDataSimpleField
  {
    public NewFieldDataIdentifierWithoutKeyword( String pFieldName, boolean pMandatory )
    {
      super( pFieldName, pMandatory, "DBNAME", false );
    }
  }

  public static class NewFieldDataType extends _NewFieldDataSimpleField
  {
    public NewFieldDataType( String pFieldName, String pTypeName, boolean pMandatory )
    {
      super( pFieldName, pMandatory, pTypeName, true );
    }
  }

  public static class NewFieldDataTypeWithoutKeyword extends _NewFieldDataSimpleField
  {
    public NewFieldDataTypeWithoutKeyword( String pFieldName, String pTypeName, boolean pMandatory )
    {
      super( pFieldName, pMandatory, pTypeName, false );
    }
  }

  public static class NewFieldDataString extends _NewFieldDataSimpleField
  {
    public NewFieldDataString( String pFieldName, boolean pMandatory )
    {
      super( pFieldName, pMandatory, "String", true );
    }
  }

  public static class NewFieldConstant extends _NewFieldDataSimpleField
  {
	private String _constantValue; 
	  
    public NewFieldConstant( String pFieldName, String pConstantValue )
    {
      super( pFieldName, false, "\"" + pConstantValue + "\"", false );
      
      _constantValue = pConstantValue;
    }

	@Override
	protected RuleEntry createValueRuleEntry( BaseSyntaxExtension pBaseSyntaxExtension ) 
	{
		return pBaseSyntaxExtension.createRuleConstantFlagEntry( getFieldName(), _constantValue );
	}
  }

  public static class NewFieldDataEnumeration extends _NewFieldDataSimpleField
  {
    private String[] _allowedValues;

    public NewFieldDataEnumeration( String pFieldName, boolean pMandatory, String... pAllowedValues )
    {
      super( pFieldName, pMandatory, null, true );

      _allowedValues = pAllowedValues;
    }

    @Override
    void addField( RuleNormal pRuleNormal, BaseSyntaxExtension pBaseSyntaxExtension )
    {
      throw new UnsupportedOperationException();
    }

    @Override
    void addFieldAfter( FieldReference pFieldReference, BaseSyntaxExtension pBaseSyntaxExtension )
    {
      String lEnumName = "Enum" + pFieldReference._ruleName + getFieldName().substring( 0, 1 ).toUpperCase() + getFieldName().substring( 1 );

      setTypeName( lEnumName );

      super.addFieldAfter( pFieldReference, pBaseSyntaxExtension );

      RuleNormal lRuleNormal = pBaseSyntaxExtension.createRuleNormal( lEnumName );

      lRuleNormal.setRule_enum( "enum" );

      boolean lIsFirst = true;

      List<String> lAllowedValues = new ArrayList<String>();

      for( String lAllowedValue : _allowedValues )
      {
        lAllowedValues.add( lAllowedValue );
      }

      for( String lAllowedValue : lAllowedValues )
      {
        if( lIsFirst )
        {
          lIsFirst = false;
        }
        else
        {
          lRuleNormal.getRule_entries().add( pBaseSyntaxExtension.createMarkerRuleEntry( "|" ) );
        }

        if( lAllowedValue == null )
        {
          lRuleNormal.getRule_entries().add( getNullDummyValue( lRuleNormal ) );
        }
        else
        {
          lRuleNormal.getRule_entries().add( pBaseSyntaxExtension.createRuleConstantEntry( lAllowedValue, lAllowedValue ) );
        }
      }
    }
  }

  protected void addField( FieldReference pFieldReference, NewFieldData pNewFieldData )
  {
    pNewFieldData.addFieldAfter( pFieldReference, this );
  }

  protected void addField( RuleNormal pRuleNormal, NewFieldData pNewFieldData )
  {
    pNewFieldData.addField( pRuleNormal, this );
  }

  protected RuleValueEntry createValueRuleEntry( String pFieldName, String pType )
  {
    RuleValueEntry lRuleEntry = new RuleValueEntryImpl()
    {
    };

    lRuleEntry.setValue_name( pFieldName );
    lRuleEntry.setAssignmnet_type( "=" );
    lRuleEntry.setRule_name( pType );
    return lRuleEntry;
  }  

  protected RuleValueEntry createMultiValueRuleEntry( String pFieldName, String pType )
  {
    RuleValueEntry lRuleEntry = new RuleValueEntryImpl()
    {
    };

    lRuleEntry.setValue_name( pFieldName );
    lRuleEntry.setAssignmnet_type( "+=" );
    lRuleEntry.setRule_name( pType );
    return lRuleEntry;
  }  

  protected RuleValueEntry createRuleName( String pType )
  {
    RuleValueEntry lRuleEntry = new RuleValueEntryImpl()
    {
    };

    lRuleEntry.setRule_name( pType );
    return lRuleEntry;
  }

  protected RuleNormal createRuleNormal( String pRuleName )
  {
    RuleNormal lRuleNormal = new RuleNormalImpl()
    {
    };
    lRuleNormal.setName( pRuleName );

    _model.getModel_elements().get( 0 ).getRules().add( lRuleNormal );

    return lRuleNormal;
  }

  protected RuleEntry createConstantRuleEntry( String pFieldName )
  {
    RuleEntry lRuleEntry = new RuleEntryImpl()
    {
    };
    lRuleEntry.setConstant_string( pFieldName );
    return lRuleEntry;
  }

  protected RuleEntry createRuleConstantEntry( String pValueName, String pConstant )
  {
    RuleConstnatEntry lRuleConstnatEntry = new RuleConstnatEntryImpl()
    {
    };
    lRuleConstnatEntry.setValue_name( pValueName );
    lRuleConstnatEntry.setAssignmnet_type( "=" );
    lRuleConstnatEntry.setRule_constant( pConstant );
    return lRuleConstnatEntry;
  }  

  protected RuleEntry createRuleConstantFlagEntry( String pValueName, String pConstant )
  {
    RuleConstnatEntry lRuleConstnatEntry = new RuleConstnatEntryImpl()
    {
    };
    lRuleConstnatEntry.setValue_name( pValueName );
    lRuleConstnatEntry.setAssignmnet_type( "?=" );
    lRuleConstnatEntry.setRule_constant( pConstant );
    return lRuleConstnatEntry;
  }  

  private RuleValueEntry findRuleValueEntry( FieldReference pFieldReference )
  {
    for( RuleEntry lRuleEntry : findRuleNormal( pFieldReference ).getRule_entries() )
    {
      if( lRuleEntry instanceof RuleValueEntry )
      {
        if( ((RuleValueEntry)lRuleEntry).getValue_name().equals( pFieldReference._fieldName ) )
        {
          return (RuleValueEntry)lRuleEntry;
        }
      }
    }

    throw new RuntimeException( "RuleValueEntry not found: " + pFieldReference._ruleName + " " + pFieldReference._fieldName );
  }

  protected RuleEntry createMarkerRuleEntry( String pMarker )
  {
    RuleEntry lRuleEntry = new RuleEntryImpl()
    {
    };
    lRuleEntry.setRule_marker( pMarker );
    return lRuleEntry;
  }

  protected void addRuleEntries( RuleNormal pRuleNormal, List<RuleEntry> pRuleEntries )
  {
    pRuleNormal.getRule_entries().addAll( pRuleEntries );
  }

  protected void addRuleEntries( FieldReference pFieldReference, List<RuleEntry> pRuleEntries )
  {
    RuleEntry[] lRuleEntries = new RuleEntry[pRuleEntries.size()];

    for( int i = 0; i < lRuleEntries.length; i++ )
    {
      lRuleEntries[i] = pRuleEntries.get( i );
    }

    addRuleEntries( pFieldReference, lRuleEntries );
  }

  protected void addRuleEntries( FieldReference pFieldReference, RuleEntry... pRuleEntries )
  {
    RuleNormal lRuleNormal = findRuleNormal( pFieldReference );
    RuleValueEntry lRuleEntryAfter = findRuleValueEntry( pFieldReference );

    int lIndex = lRuleNormal.getRule_entries().indexOf( lRuleEntryAfter ) + 1;

    while( true )
    {
      RuleEntry lRuleEntry = lRuleNormal.getRule_entries().get( lIndex );

      if( lRuleEntry.getRule_marker() == null || !lRuleEntry.getRule_marker().contains( ")" ) )
      {
        break;
      }
      else
      {
        lIndex++;
      }
    }

    for( int i = pRuleEntries.length - 1; i >= 0; i-- )
    {
      lRuleNormal.getRule_entries().add( lIndex, pRuleEntries[i] );
    }
  }

  protected void makeOptional( FieldReference pFieldReference )
  {
    RuleNormal lRuleNormal = findRuleNormal( pFieldReference );
    RuleValueEntry lRuleValueEntry = findRuleValueEntry( pFieldReference );

    addRuleEntries( pFieldReference, createMarkerRuleEntry( ")?" ) );

    int lIndex = lRuleNormal.getRule_entries().indexOf( lRuleValueEntry );

    lRuleNormal.getRule_entries().add( lIndex, createMarkerRuleEntry( "(" ) );
  }

  protected static RuleConstnatEntry getNullDummyValue( RuleNormal pRuleNormal )
  {
    RuleConstnatEntry lRuleConstnatEntry = new RuleConstnatEntryImpl()
    {
    };

    lRuleConstnatEntry.setValue_name( "null" );
    lRuleConstnatEntry.setAssignmnet_type( "=" );
    lRuleConstnatEntry.setRule_constant( "null_" + pRuleNormal.getName() );
    return lRuleConstnatEntry;
  }
}
