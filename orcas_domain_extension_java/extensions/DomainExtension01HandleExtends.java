package de.opitzconsulting.orcas.extensions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.EcoreUtil2;

import de.opitzconsulting.orcasDsl.ColumnDomain;
import de.opitzconsulting.orcasDsl.Domain;
import de.opitzconsulting.orcasDsl.GenNameRule;
import de.opitzconsulting.orcasDsl.GenNameRulePart;
import de.opitzconsulting.orcasDsl.Model;
import de.opitzconsulting.orcasDsl.ModelElement;

public class DomainExtension01HandleExtends extends OrcasBaseExtensionWithParameter
{
  private void mergeSuperDomain( Domain pDomain, Domain pSuperDomain )
  {
    pDomain.getColumns().addAll( 0, EcoreUtil.copyAll( pSuperDomain.getColumns() ) );

    if( pDomain.getHistoryTable() == null )
    {
      pDomain.setHistoryTable( EcoreUtil.copy( pSuperDomain.getHistoryTable() ) );
    }

    pDomain.setExtends( null );
  }

  public static Domain getDomain( String pDomainName, Model pModel )
  {
    for( ModelElement lModelElement : new ArrayList<ModelElement>( pModel.getModel_elements() ) )
    {
      if( lModelElement instanceof Domain )
      {
        if( ((Domain)lModelElement).getName().equals( pDomainName ) )
        {
          return (Domain)lModelElement;
        }
      }
    }

    throw new RuntimeException( "domain not found: " + pDomainName );
  }

  public static ColumnDomain getColumnDomain( String pColumnDomainName, Model pModel )
  {
    for( ModelElement lModelElement : new ArrayList<ModelElement>( pModel.getModel_elements() ) )
    {
      if( lModelElement instanceof ColumnDomain )
      {
        if( ((ColumnDomain)lModelElement).getName().equals( pColumnDomainName ) )
        {
          return (ColumnDomain)lModelElement;
        }
      }
    }

    throw new RuntimeException( "column domain not found: " + pColumnDomainName );
  }

  private static class TokenBuilder
  {
    private String returnValue = "";
    private boolean removeNext = false;
    private String removeToken;

    void handle_next_token( String pToken )
    {
      if( pToken != null )
      {
        if( removeNext )
        {
          removeToken = removeToken + pToken;
          removeNext = false;
        }
        else
        {
          returnValue = returnValue + pToken;

          if( removeToken != null )
          {
            if( returnValue.indexOf( removeToken ) < 0 )
            {
              throw new RuntimeException( "token not found " + returnValue + " " + removeToken );
            }

            returnValue = returnValue.replace( removeToken, "" );
            removeToken = null;
          }
        }
      }
    }
  }

  private static String getGeneratedName( List<GenNameRule> pGenNameRuleList, String pColumnName, String pColumnDomainName, String pTableName, String pAlias )
  {
    TokenBuilder lTokenBuilder = new TokenBuilder();

    for( GenNameRule lGenNameRule : pGenNameRuleList )
    {
      lTokenBuilder.handle_next_token( lGenNameRule.getConstant_name() );

      if( lGenNameRule.getConstant_part() == GenNameRulePart.COLUMN_NAME )
      {
        if( pColumnName == null )
        {
          throw new RuntimeException( "p_column_name invalid" + pAlias + pTableName );
        }
        lTokenBuilder.handle_next_token( pColumnName );
      }
      if( lGenNameRule.getConstant_part() == GenNameRulePart.COLUMN_DOMAIN_NAME )
      {
        if( pColumnDomainName == null )
        {
          throw new RuntimeException( "p_column_domain_name invalid" + pColumnName + pTableName );
        }
        lTokenBuilder.handle_next_token( pColumnDomainName );
      }
      if( lGenNameRule.getConstant_part() == GenNameRulePart.TABLE_NAME )
      {
        if( pTableName == null )
        {
          throw new RuntimeException( "p_table_name invalid" + pColumnName + pAlias );
        }
        lTokenBuilder.handle_next_token( pTableName );
      }
      if( lGenNameRule.getConstant_part() == GenNameRulePart.ALIAS_NAME )
      {
        lTokenBuilder.handle_next_token( pAlias );
      }
      if( lGenNameRule.getConstant_part() == GenNameRulePart.REMOVE_NEXT )
      {
        lTokenBuilder.removeNext = true;
      }
    }

    return lTokenBuilder.returnValue;
  }

  public static String getGeneratedNameTable( List<GenNameRule> pGenNameRuleList, String pTableName, String pAlias )
  {
    return getGeneratedName( pGenNameRuleList, null, null, pTableName, pAlias );
  }

  public static String getGeneratedNameColumn( List<GenNameRule> pGenNameRuleList, String pColumnName, String pTableName, String pAlias )
  {
    return getGeneratedName( pGenNameRuleList, pColumnName, null, pTableName, pAlias );
  }

  public static String getGeneratedNameColDomain( List<GenNameRule> pGenNameRuleList, String pColumnDomainName )
  {
    return getGeneratedName( pGenNameRuleList, null, pColumnDomainName, null, null );
  }

  private Domain getMergedDomain( String pDomainName, Model pModel )
  {
    Domain lDomain = getDomain( pDomainName, pModel );

    mergeSuperDomainIfNeeded( lDomain, pModel );

    return lDomain;
  }

  private void mergeSuperDomainIfNeeded( Domain pDomain, Model pModel )
  {
    if( pDomain.getExtends() != null )
    {
      mergeSuperDomain( pDomain, getMergedDomain( pDomain.getExtends(), pModel ) );
    }
  }

  @Override
  public Model transformModel( Model pModel )
  {
    for( ModelElement lModelElement : new ArrayList<ModelElement>( pModel.getModel_elements() ) )
    {
      if( lModelElement instanceof Domain )
      {
        mergeSuperDomainIfNeeded( (Domain)lModelElement, pModel );
      }
    }

    return pModel;
  }
}