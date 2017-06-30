package de.opitzconsulting.orcas.diff;

import java.util.ArrayList;

import de.opitzconsulting.origOrcasDsl.Index;
import de.opitzconsulting.origOrcasDsl.IndexOrUniqueKey;
import de.opitzconsulting.origOrcasDsl.Model;
import de.opitzconsulting.origOrcasDsl.ModelElement;
import de.opitzconsulting.origOrcasDsl.Table;

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
