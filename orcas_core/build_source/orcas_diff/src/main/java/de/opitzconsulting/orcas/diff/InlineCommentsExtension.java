package de.opitzconsulting.orcas.diff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.opitzconsulting.origOrcasDsl.Comment;
import de.opitzconsulting.origOrcasDsl.InlineComment;
import de.opitzconsulting.origOrcasDsl.Model;
import de.opitzconsulting.origOrcasDsl.ModelElement;
import de.opitzconsulting.origOrcasDsl.Table;
import de.opitzconsulting.origOrcasDsl.impl.InlineCommentImpl;

public class InlineCommentsExtension
{
  public Model transformModel( Model pModel )
  {
    Map<String, Table> lCacheTableMap = new HashMap<>();

    for( ModelElement lModelElement : new ArrayList<ModelElement>( pModel.getModel_elements() ) )
    {
      if( lModelElement instanceof Comment )
      {
        Comment lComment = (Comment) lModelElement;

        InlineComment lInlineComment = new InlineCommentImpl();

        lInlineComment.setColumn_name( lComment.getColumn_name() );
        lInlineComment.setComment( lComment.getComment() );
        lInlineComment.setComment_object( lComment.getComment_object() );

        findTable( lCacheTableMap, pModel, lComment.getTable_name() ).getComments().add( lInlineComment );

        pModel.getModel_elements().remove( lComment );
      }
    }

    return pModel;
  }

  private Table findTable( Map<String, Table> pCacheTableMap, Model pModel, String pTablename )
  {
    if( pCacheTableMap.containsKey( pTablename ) )
    {
      return pCacheTableMap.get( pTablename );
    }

    for( ModelElement lModelElement : pModel.getModel_elements() )
    {
      if( lModelElement instanceof Table )
      {
        if( ((Table) lModelElement).getName().equalsIgnoreCase( pTablename ) )
        {
          pCacheTableMap.put( pTablename, (Table) lModelElement );

          return (Table) lModelElement;
        }
      }
    }

    throw new IllegalArgumentException( "Table not found: " + pTablename );
  }
}
