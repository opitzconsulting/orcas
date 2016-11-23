package de.opitzconsulting.orcas.extensions;

import java.util.ArrayList;

import de.opitzconsulting.orcas.extensions.OrcasBaseExtensionWithParameter;
import de.opitzconsulting.orcasDsl.Index;
import de.opitzconsulting.orcasDsl.IndexOrUniqueKey;
import de.opitzconsulting.orcasDsl.Model;
import de.opitzconsulting.orcasDsl.ModelElement;
import de.opitzconsulting.orcasDsl.Table;

public class ReverseExtension21FkGenIdx extends OrcasBaseExtensionWithParameter implements OrcasReverseExtension
{
  @Override
  public Model transformModel( Model pModel )
  {
    for( ModelElement lModelElement : new ArrayList<ModelElement>( pModel.getModel_elements() ) )
    {
      if( lModelElement instanceof Table )
      {
        Table lTable = (Table)lModelElement;
        for( IndexOrUniqueKey lIndexOrUniqueKey : new ArrayList<IndexOrUniqueKey>( lTable.getInd_uks() ) )
        {
          if( lIndexOrUniqueKey instanceof Index )
          {
            Index lIndex = (Index)lIndexOrUniqueKey;

            if( lIndex.getConsName().endsWith( "_GEN_IX" ) )
            {
              lTable.getInd_uks().remove( lIndex );
            }
          }
        }
      }
    }

    return pModel;
  }

}
