package com.opitzconsulting.orcas.diff;

import de.opitzconsulting.orcas.extensions.OrcasExtension;
import de.opitzconsulting.orcasDsl.HashPartition;
import de.opitzconsulting.orcasDsl.HashPartitions;
import de.opitzconsulting.orcasDsl.Index;
import de.opitzconsulting.orcasDsl.IndexExTable;
import de.opitzconsulting.orcasDsl.IndexOrUniqueKey;
import de.opitzconsulting.orcasDsl.ListPartition;
import de.opitzconsulting.orcasDsl.ListPartitions;
import de.opitzconsulting.orcasDsl.LobStorage;
import de.opitzconsulting.orcasDsl.Model;
import de.opitzconsulting.orcasDsl.ModelElement;
import de.opitzconsulting.orcasDsl.PrimaryKey;
import de.opitzconsulting.orcasDsl.RangePartition;
import de.opitzconsulting.orcasDsl.RangePartitions;
import de.opitzconsulting.orcasDsl.RefPartition;
import de.opitzconsulting.orcasDsl.RefPartitions;
import de.opitzconsulting.orcasDsl.Table;
import de.opitzconsulting.orcasDsl.UniqueKey;
import de.opitzconsulting.orcasDsl.VarrayStorage;

public class TablespaceRemapperExtension implements OrcasExtension
{
  private String tablespace1;

  public TablespaceRemapperExtension( String pTablespace1, String pTablespace2 )
  {
    tablespace1 = pTablespace1;
    tablespace2 = pTablespace2;
  }

  private String tablespace2;

  public Model transformModel( Model pModel )
  {
    for( ModelElement lModelElement : pModel.getModel_elements() )
    {
      if( lModelElement instanceof Table )
      {
        PrimaryKey lPrimaryKey = ((Table) lModelElement).getPrimary_key();
        if( lPrimaryKey != null )
        {
          lPrimaryKey.setTablespace( _replaceTablespace( lPrimaryKey.getTablespace() ) );
        }

        for( IndexOrUniqueKey lIndexOrUniqueKey : ((Table) lModelElement).getInd_uks() )
        {
          if( lIndexOrUniqueKey instanceof Index )
          {
            ((Index) lIndexOrUniqueKey).setTablespace( _replaceTablespace( ((Index) lIndexOrUniqueKey).getTablespace() ) );
          }
          if( lIndexOrUniqueKey instanceof UniqueKey )
          {
            ((UniqueKey) lIndexOrUniqueKey).setTablespace( _replaceTablespace( ((UniqueKey) lIndexOrUniqueKey).getTablespace() ) );
          }
        }

        for( LobStorage lLobStorage : ((Table) lModelElement).getLobStorages() )
        {
          if( lLobStorage.getLobStorageParameters() != null )
          {
            lLobStorage.getLobStorageParameters().setTablespace( _replaceTablespace( lLobStorage.getLobStorageParameters().getTablespace() ) );
          }
        }

        for( VarrayStorage lVarrayStorage : ((Table) lModelElement).getVarrayStorages() )
        {
          if( lVarrayStorage.getLobStorageParameters() != null )
          {
            lVarrayStorage.getLobStorageParameters().setTablespace( _replaceTablespace( lVarrayStorage.getLobStorageParameters().getTablespace() ) );
          }
        }

        if( ((Table) lModelElement).getTablePartitioning() instanceof HashPartitions )
        {
          for( HashPartition lPartition : ((HashPartitions) ((Table) lModelElement).getTablePartitioning()).getPartitionList() )
          {
            lPartition.setTablespace( _replaceTablespace( lPartition.getTablespace() ) );
          }
        }
        if( ((Table) lModelElement).getTablePartitioning() instanceof RangePartitions )
        {
          for( RangePartition lPartition : ((RangePartitions) ((Table) lModelElement).getTablePartitioning()).getPartitionList() )
          {
            lPartition.setTablespace( _replaceTablespace( lPartition.getTablespace() ) );
          }
        }
        if( ((Table) lModelElement).getTablePartitioning() instanceof ListPartitions )
        {
          for( ListPartition lPartition : ((ListPartitions) ((Table) lModelElement).getTablePartitioning()).getPartitionList() )
          {
            lPartition.setTablespace( _replaceTablespace( lPartition.getTablespace() ) );
          }
        }
        if( ((Table) lModelElement).getTablePartitioning() instanceof RefPartitions )
        {
          for( RefPartition lPartition : ((RefPartitions) ((Table) lModelElement).getTablePartitioning()).getPartitionList() )
          {
            lPartition.setTablespace( _replaceTablespace( lPartition.getTablespace() ) );
          }
        }
        ((Table) lModelElement).setTablespace( _replaceTablespace( ((Table) lModelElement).getTablespace() ) );
      }
      if( lModelElement instanceof IndexExTable )
      {
        ((IndexExTable) lModelElement).setTablespace( _replaceTablespace( ((IndexExTable) lModelElement).getTablespace() ) );
      }
    }

    return pModel;
  }

  private String _replaceTablespace( String pTablespace )
  {
    if( "replaceme1".equalsIgnoreCase( pTablespace ) )
    {
      return tablespace1;
    }
    else
    {
      if( "replaceme2".equalsIgnoreCase( pTablespace ) )
      {
        return tablespace2;
      }
      else
      {
        return pTablespace;
      }
    }
  }
}
