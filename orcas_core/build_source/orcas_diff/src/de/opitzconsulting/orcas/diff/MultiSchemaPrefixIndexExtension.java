package de.opitzconsulting.orcas.diff;

import java.util.ArrayList;

import de.opitzconsulting.orcasDsl.Index;
import de.opitzconsulting.orcasDsl.IndexOrUniqueKey;
import de.opitzconsulting.orcasDsl.Model;
import de.opitzconsulting.orcasDsl.ModelElement;
import de.opitzconsulting.orcasDsl.Table;

public class MultiSchemaPrefixIndexExtension
{
  public Model transformModel( Model pModel )
  {
    for( ModelElement lModelElement : new ArrayList<ModelElement>( pModel.getModel_elements() ) )
    {
      if( lModelElement instanceof Table )
      {
        Table lTable = (Table)lModelElement;

        for( IndexOrUniqueKey lIndexExTable : lTable.getInd_uks() )
        {
          if( lIndexExTable instanceof Index )
          {
            Index lIndex = (Index)lIndexExTable;

            if( lIndex.getConsName().indexOf( '.' ) == -1 )
            {
              lIndex.setConsName( lTable.getName().substring( 0, lTable.getName().indexOf( '.' ) ) + "." + lIndex.getConsName() );
            }
          }
        }
      }
    }

    return pModel;
  }
}
