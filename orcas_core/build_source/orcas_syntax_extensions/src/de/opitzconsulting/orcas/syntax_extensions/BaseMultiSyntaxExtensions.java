package de.opitzconsulting.orcas.syntax_extensions;

import java.util.ArrayList;
import java.util.List;

import de.opitzconsulting.orcasXtextExtension.Model;
import de.opitzconsulting.orcasXtextExtension.ModelElement;
import de.opitzconsulting.orcasXtextExtension.Rule;
import de.opitzconsulting.orcasXtextExtension.RuleEntry;
import de.opitzconsulting.orcasXtextExtension.RuleNormal;
import de.opitzconsulting.orcasXtextExtension.impl.RuleEntryImpl;

public abstract class BaseMultiSyntaxExtensions implements OrcasSyntaxExtensionWithParameter
{
  private List<OrcasSyntaxExtension> _extensions = new ArrayList<OrcasSyntaxExtension>();
  private String _parameter;

  public void setParameter( String pParameter )
  {
    _parameter = pParameter;
  }

  protected void addExtension( OrcasSyntaxExtension pExtension )
  {
    _extensions.add( pExtension );
  }

  public Model transformSyntaxModel( Model pModel )
  {
    Model lModel = pModel;

    for( OrcasSyntaxExtension lOrcasSyntaxExtension : _extensions )
    {
      if( lOrcasSyntaxExtension instanceof OrcasSyntaxExtensionWithParameter )
      {
        ((OrcasSyntaxExtensionWithParameter)lOrcasSyntaxExtension).setParameter( _parameter );
      }

      lModel = lOrcasSyntaxExtension.transformSyntaxModel( lModel );
    }

    lModel = new OrcasSyntaxExtension()
    {
      public Model transformSyntaxModel( Model pModel )
      {
        for( ModelElement lModelElement : pModel.getModel_elements() )
        {
          for( Rule lRule : lModelElement.getRules() )
          {
            if( lRule instanceof RuleNormal )
            {
              RuleNormal lRuleNormal = (RuleNormal)lRule;

              if( lRuleNormal.getRule_enum() != null )
              {
                RuleEntry lRuleEntrySeparator = new RuleEntryImpl()
                {
                };
                lRuleEntrySeparator.setRule_marker( "|" );

                lRuleNormal.getRule_entries().add( 0, lRuleEntrySeparator );
                lRuleNormal.getRule_entries().add( 0, BaseSyntaxExtension.getNullDummyValue( lRuleNormal ) );
              }
            }
          }
        }

        return pModel;
      }
    }.transformSyntaxModel( lModel );

    return lModel;
  }
}