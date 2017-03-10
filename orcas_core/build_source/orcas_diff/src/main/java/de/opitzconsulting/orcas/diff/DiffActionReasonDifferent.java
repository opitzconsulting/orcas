package de.opitzconsulting.orcas.diff;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EAttribute;
import org.jdom2.Element;

public class DiffActionReasonDifferent extends DiffActionReason
{
  private List<String> diffReasonDetails = new ArrayList<>();

  public DiffActionReasonDifferent( DiffReasonKey pDiffReasonKey )
  {
    super( pDiffReasonKey );
  }

  public void addDiffReasonDetail( EAttribute pDiffReasonDetail )
  {
    diffReasonDetails.add( pDiffReasonDetail.getName() );
  }

  @Override
  public Element getJdomElement( boolean pIncludeKey )
  {
    Element lReturn = super.getJdomElement( pIncludeKey );

    for( String lDiffReasonDetail : diffReasonDetails )
    {
      Element lStatementElement = new Element( "reason-detail" );
      lReturn.addContent( lStatementElement );
      lStatementElement.addContent( lDiffReasonDetail );
    }

    return lReturn;
  }

  @Override
  protected String getTypeString()
  {
    return "different";
  }
}
