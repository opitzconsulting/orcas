package de.opitzconsulting.orcas.extensions;

import de.opitzconsulting.orcasDsl.*;

public class TablespaceRemapper extends OrcasBaseExtensionWithParameter
{
  public Model transformModel( Model pModel )
  {
    for( ModelElement lModelElement : pModel.getModel_elements() )
    {
      if( lModelElement instanceof Table )
      {
        PrimaryKey lPrimaryKey = ((Table)lModelElement).getPrimary_key();
        if( lPrimaryKey != null )
        {
          lPrimaryKey.setTablespace( _replaceTablespace( lPrimaryKey.getTablespace() ) );
        }

        for( IndexOrUniqueKey lIndexOrUniqueKey : ((Table)lModelElement).getInd_uks() )
        {
          if( lIndexOrUniqueKey instanceof Index )
          {
            ((Index)lIndexOrUniqueKey).setTablespace( _replaceTablespace( ((Index)lIndexOrUniqueKey).getTablespace() ) );
          }
          if( lIndexOrUniqueKey instanceof UniqueKey )
          {
            ((UniqueKey)lIndexOrUniqueKey).setTablespace( _replaceTablespace( ((UniqueKey)lIndexOrUniqueKey).getTablespace() ) );
          }
        }

        for( LobStorage lLobStorage : ((Table)lModelElement).getLobStorages() )
        {
          ((LobStorage)lLobStorage).setTablespace( _replaceTablespace( ((LobStorage)lLobStorage).getTablespace() ) );
        }        

        if( ((Table)lModelElement).getTablePartitioning() instanceof HashPartitions )
        {
          for( HashPartition lPartition : ((HashPartitions)((Table)lModelElement).getTablePartitioning()).getPartitionList() )
          {
            lPartition.setTablespace( _replaceTablespace( lPartition.getTablespace() ) );
          }                  
        }
        if( ((Table)lModelElement).getTablePartitioning() instanceof RangePartitions )
        {
          for( RangePartition lPartition : ((RangePartitions)((Table)lModelElement).getTablePartitioning()).getPartitionList() )
          {
            lPartition.setTablespace( _replaceTablespace( lPartition.getTablespace() ) );
          }                  
        }
        if( ((Table)lModelElement).getTablePartitioning() instanceof ListPartitions )
        {
          for( ListPartition lPartition : ((ListPartitions)((Table)lModelElement).getTablePartitioning()).getPartitionList() )
          {
            lPartition.setTablespace( _replaceTablespace( lPartition.getTablespace() ) );
          }                  
        }        
        if( ((Table)lModelElement).getTablePartitioning() instanceof RefPartitions )
        {
          for( RefPartition lPartition : ((RefPartitions)((Table)lModelElement).getTablePartitioning()).getPartitionList() )
          {
            lPartition.setTablespace( _replaceTablespace( lPartition.getTablespace() ) );
          }                  
        }                
        ((Table)lModelElement).setTablespace( _replaceTablespace( ((Table)lModelElement).getTablespace() ) );
      }
      if( lModelElement instanceof IndexExTable )
      {
        ((IndexExTable)lModelElement).setTablespace( _replaceTablespace( ((IndexExTable)lModelElement).getTablespace() ) );
      }      
    }

    return pModel;
  }

  private String _replaceTablespace( String pTablespace )
  {
    if( "replaceme1".equalsIgnoreCase( pTablespace ) )
    {
      return getParameterAsMap( "tablespace1" );
    }
    else
    {
      if( "replaceme2".equalsIgnoreCase( pTablespace ) )
      {
        return getParameterAsMap( "tablespace2" );
      }    
      else
      {
        return pTablespace;
      }
    } 
  }
}
