package de.opitzconsulting.orcas.diff;

import java.util.ArrayList;

import de.opitzconsulting.orcasDsl.Index;
import de.opitzconsulting.orcasDsl.IndexExTable;
import de.opitzconsulting.orcasDsl.Model;
import de.opitzconsulting.orcasDsl.ModelElement;
import de.opitzconsulting.orcasDsl.Table;
import de.opitzconsulting.orcasDsl.impl.IndexImpl;

public class InlineIndexExtension
{
  public Model transformModel( Model pModel )
  {
    for( ModelElement lModelElement : new ArrayList<ModelElement>( pModel.getModel_elements() ) )
    {
      if( lModelElement instanceof IndexExTable )
      {
        IndexExTable lIndexExTable = (IndexExTable)lModelElement;

        Index lIndex = new IndexImpl();

        lIndex.setBitmap( lIndexExTable.getBitmap() );
        lIndex.setCompression( lIndexExTable.getCompression() );
        lIndex.setConsName( lIndexExTable.getIndex_name() );
        lIndex.setDomain_index_expression( lIndexExTable.getDomain_index_expression() );
        lIndex.setFunction_based_expression( lIndexExTable.getFunction_based_expression() );
        lIndex.setGlobal( lIndexExTable.getGlobal() );
        lIndex.setLogging( lIndexExTable.getLogging() );
        lIndex.setParallel( lIndexExTable.getParallel() );
        lIndex.setParallel_degree( lIndexExTable.getParallel_degree() );
        lIndex.setTablespace( lIndexExTable.getTablespace() );
        lIndex.setUnique( lIndexExTable.getUniqueness() );
        lIndex.getIndex_columns().addAll( ModelUtil.copyColumnRefs( lIndexExTable.getIndex_columns() ) );

        findTable( pModel, lIndexExTable.getTable_name() ).getInd_uks().add( lIndex );

        pModel.getModel_elements().remove( lIndexExTable );
      }
    }

    return pModel;
  }

  private Table findTable( Model pModel, String pTablename )
  {
    for( ModelElement lModelElement : pModel.getModel_elements() )
    {
      if( lModelElement instanceof Table )
      {
        if( ((Table)lModelElement).getName().equalsIgnoreCase( pTablename ) )
        {
          return (Table)lModelElement;
        }
      }
    }

    throw new IllegalArgumentException( "Table not found: " +
                                        pTablename );
  }
}
