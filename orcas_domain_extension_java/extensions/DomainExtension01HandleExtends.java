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

  private Domain getMergedDomain( String pDomainName, Model pModel )
  {
    Domain lDomain = DomainExtensionHelper.getDomain( pDomainName, pModel );

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