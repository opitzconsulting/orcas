package de.opitzconsulting.orcas.diff;

import de.opitzconsulting.origOrcasDsl.ForeignKey;
import de.opitzconsulting.origOrcasDsl.Index;
import de.opitzconsulting.origOrcasDsl.IndexExTable;
import de.opitzconsulting.origOrcasDsl.IndexOrUniqueKey;
import de.opitzconsulting.origOrcasDsl.Model;
import de.opitzconsulting.origOrcasDsl.ModelElement;
import de.opitzconsulting.origOrcasDsl.Mview;
import de.opitzconsulting.origOrcasDsl.Sequence;
import de.opitzconsulting.origOrcasDsl.Table;
import de.opitzconsulting.origOrcasDsl.UniqueKey;

import java.util.ArrayList;

public class RemoveMultiSchemaPrefixExtension
{
  public Model transformModel( Model pModel )
  {
    for( ModelElement lModelElement : new ArrayList<>( pModel.getModel_elements() ) )
    {
      if( lModelElement instanceof Table )
      {
        Table lTable = (Table) lModelElement;

        int i = lTable.getName().indexOf( '.' );
        if ( i >= 0 )
        {
          String lOwner = lTable.getName().substring( 0, i + 1 );
          lTable.setName( lTable.getName().substring( i + 1 ) );

          if ( lTable.getPrimary_key() != null && lTable.getPrimary_key().getIndexname() != null )
          {
            lTable.getPrimary_key().setIndexname( lTable.getPrimary_key().getIndexname().replace( lOwner, "" ) );
          }

          for ( ForeignKey lForeign_key : lTable.getForeign_keys() )
          {
            lForeign_key.setDestTable( lForeign_key.getDestTable().replace( lOwner, "" ) );
          }

          for ( IndexOrUniqueKey lIndex : lTable.getInd_uks() )
          {
            if ( lIndex instanceof Index )
            {
              lIndex.setConsName( lIndex.getConsName().replace( lOwner, "" ) );
            }

            if ( lIndex instanceof UniqueKey )
            {
              UniqueKey lUniqueKey = (UniqueKey) lIndex;
              if ( lUniqueKey.getIndexname() != null )
              {
                lUniqueKey.setIndexname( lUniqueKey.getIndexname().replace( lOwner, "" ) );
              }
            }
          }
        }
      }

      if ( lModelElement instanceof Sequence )
      {
        Sequence lSequence = (Sequence) lModelElement;
        int i = lSequence.getSequence_name().indexOf( '.' );
        if ( i >= 0 )
        {
          lSequence.setSequence_name( lSequence.getSequence_name().substring( i + 1 ) );
        }
      }

      if ( lModelElement instanceof IndexExTable )
      {
        IndexExTable lIndexExTable = (IndexExTable) lModelElement;
        int i = lIndexExTable.getIndex_name().indexOf( '.' );
        if ( i >= 0 )
        {
          lIndexExTable.setIndex_name( lIndexExTable.getIndex_name().substring( i + 1 ) );
        }
      }

      if ( lModelElement instanceof Mview )
      {
        Mview lMview = (Mview) lModelElement;
        int i = lMview.getMview_name().indexOf( '.' );
        if ( i >= 0 )
        {
          lMview.setMview_name( lMview.getMview_name().substring( i + 1 ) );
        }
      }

    }

    return pModel;
  }
}
