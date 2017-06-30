package de.opitzconsulting.orcas.diff;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.common.util.EList;

import de.opitzconsulting.origOrcasDsl.ColumnRef;
import de.opitzconsulting.origOrcasDsl.ForeignKey;
import de.opitzconsulting.origOrcasDsl.Index;
import de.opitzconsulting.origOrcasDsl.IndexExTable;
import de.opitzconsulting.origOrcasDsl.IndexOrUniqueKey;
import de.opitzconsulting.origOrcasDsl.Model;
import de.opitzconsulting.origOrcasDsl.ModelElement;
import de.opitzconsulting.origOrcasDsl.Table;
import de.opitzconsulting.origOrcasDsl.UniqueKey;
import de.opitzconsulting.origOrcasDsl.impl.IndexImpl;

public class AddFkIndexExtension
{
  public Model transformModel( Model pModel )
  {
    String lIndexPostfix = "_GEN_IX";
    Set<String> lIndexSet = new HashSet<String>();

    for( ModelElement lModelElement : pModel.getModel_elements() )
    {
      if( lModelElement instanceof Table )
      {
        Table lTable = (Table)lModelElement;

        if( lTable.getPrimary_key() != null )
        {
          lIndexSet.addAll( toIndexKey( lTable.getName(), lTable.getPrimary_key().getPk_columns() ) );
        }

        for( IndexOrUniqueKey lIndexOrUniqueKey : lTable.getInd_uks() )
        {
          if( lIndexOrUniqueKey instanceof Index )
          {
            lIndexSet.addAll( toIndexKey( lTable.getName(), ((Index)lIndexOrUniqueKey).getIndex_columns() ) );
          }
          if( lIndexOrUniqueKey instanceof UniqueKey )
          {
            lIndexSet.addAll( toIndexKey( lTable.getName(), ((UniqueKey)lIndexOrUniqueKey).getUk_columns() ) );
          }
        }
      }
      if( lModelElement instanceof IndexExTable )
      {
        lIndexSet.addAll( toIndexKey( ((IndexExTable)lModelElement).getTable_name(), ((IndexExTable)lModelElement).getIndex_columns() ) );
      }
    }

    for( ModelElement lModelElement : pModel.getModel_elements() )
    {
      if( lModelElement instanceof Table )
      {
        Table lTable = (Table)lModelElement;

        for( ForeignKey lForeignKey : ((Table)lModelElement).getForeign_keys() )
        {
          List<String> lIndexKeys = toIndexKey( lTable.getName(), lForeignKey.getSrcColumns() );

          if( !lIndexSet.containsAll( lIndexKeys ) )
          {
            Index lIndex = new IndexImpl();

            if( lForeignKey.getConsName().length() +
                lIndexPostfix.length() > 30 )
            {
              lIndex.setConsName( lForeignKey.getConsName().substring( 0, 30 -
                                                                          lIndexPostfix.length() ) +
                                  lIndexPostfix );
            }
            else
            {
              lIndex.setConsName( lForeignKey.getConsName() +
                                  lIndexPostfix );
            }

            lIndex.getIndex_columns().addAll( ModelUtil.copyColumnRefs( lForeignKey.getSrcColumns() ) );

            lTable.getInd_uks().add( lIndex );

            lIndexSet.addAll( toIndexKey( lTable.getName(), lIndex.getIndex_columns() ) );
          }
        }
      }
    }

    return pModel;
  }

  private List<String> toIndexKey( String pTablename, EList<ColumnRef> pIndexColumns )
  {
    List<String> lReturn = new ArrayList<String>();

    String lIndexKey = pTablename +
                       " cols ";

    for( ColumnRef lColumnRef : pIndexColumns )
    {
      lIndexKey += lColumnRef.getColumn_name() +
                   " ";

      lReturn.add( lIndexKey );
    }

    return lReturn;
  }
}
