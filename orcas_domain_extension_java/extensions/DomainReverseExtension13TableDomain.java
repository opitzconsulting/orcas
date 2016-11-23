package de.opitzconsulting.orcas.extensions;

import java.util.ArrayList;
import java.util.List;

import de.opitzconsulting.orcas.extensions.OrcasBaseExtensionWithParameter;
import de.opitzconsulting.orcasDsl.Column;
import de.opitzconsulting.orcasDsl.ColumnDomain;
import de.opitzconsulting.orcasDsl.Domain;
import de.opitzconsulting.orcasDsl.DomainColumn;
import de.opitzconsulting.orcasDsl.Model;
import de.opitzconsulting.orcasDsl.ModelElement;
import de.opitzconsulting.orcasDsl.Sequence;
import de.opitzconsulting.orcasDsl.Table;

public class DomainReverseExtension13TableDomain extends OrcasBaseExtensionWithParameter implements OrcasReverseExtension
{
  @Override
  public Model transformModel( Model pModel )
  {
    for( ModelElement lModelElement : new ArrayList<ModelElement>( pModel.getModel_elements() ) )
    {
      if( lModelElement instanceof Table )
      {
        hanldeTable( (Table)lModelElement, pModel );
      }
    }

    return pModel;
  }

  private void hanldeTable( Table pTable, Model pModel )
  {
    Domain lDomain = findBestMatchDomain( pTable, pModel );

    if( lDomain != null )
    {
      reverseApplyDomain( pTable, lDomain, pModel );
    }
  }

  private Column getColumn( Table pTable, DomainColumn pDomainColumn )
  {
    String v_columnname = DomainExtensionHelper.getGeneratedNameColumn( pDomainColumn.getColumnNameRules(), pDomainColumn.getColumn().getName(), pTable.getName(), pTable.getAlias() );

    for( Column lColumn : pTable.getColumns() )
    {
      if( lColumn.getName().equalsIgnoreCase( v_columnname ) )
      {
        return lColumn;
      }
    }

    return null;
  }

  private Table findTable( String pTableName, Model pModel )
  {
    for( ModelElement lModelElement : pModel.getModel_elements() )
    {
      if( lModelElement instanceof Table )
      {
        Table lTable = (Table)lModelElement;

        if( lTable.getName().equalsIgnoreCase( pTableName ) )
        {
          return lTable;
        }
      }
    }

    return null;
  }

  private Integer rateDomain( Domain pDomain, Table pTable, Model pModel )
  {
    int v_return = 0;

    for( DomainColumn lDomainColumn : pDomain.getColumns() )
    {
      if( getColumn( pTable, lDomainColumn ) == null )
      {
        return null;
      }

      v_return += 1;
    }

    if( pDomain.getHistoryTable() != null )
    {
      String v_history_table_name = DomainExtensionHelper.getGeneratedNameTable( pDomain.getHistoryTable().getTableNameRules(), pTable.getName(), pTable.getAlias() );
      if( findTable( v_history_table_name, pModel ) == null )
      {
        return null;
      }

      v_return += 10;
    }

    return v_return;
  }

  private Domain findBestMatchDomain( Table pTable, Model pModel )
  {
    Integer lBestRating = null;
    Domain lReturn = null;

    for( ModelElement lModelElement : pModel.getModel_elements() )
    {
      if( lModelElement instanceof Domain )
      {
        Integer lRating = rateDomain( (Domain)lModelElement, pTable, pModel );

        if( lRating != null )
        {
          if( lBestRating == null || lRating > lBestRating )
          {
            lBestRating = lRating;
            lReturn = (Domain)lModelElement;
          }
        }
      }
    }

    return lReturn;
  }

  private void reverseApplyDomain( Table pTable, Domain pDomain, Model pModel )
  {
    pTable.setDomain( pDomain.getName() );

    if( pDomain.getHistoryTable() != null )
    {
      String lTableName = DomainExtensionHelper.getGeneratedNameTable( pDomain.getHistoryTable().getTableNameRules(), pTable.getName(), pTable.getAlias() );

      List<String> lHistoryTableSequenceNames = new ArrayList<String>();

      if( pDomain.getHistoryTable().getDomain() != null )
      {
        String lTableAlias = DomainExtensionHelper.getGeneratedNameTable( pDomain.getHistoryTable().getAliasNameRules(), pTable.getName(), pTable.getAlias() );

        for( Column lColumn : pTable.getColumns() )
        {
          if( lColumn.getDomain() != null )
          {
            ColumnDomain lColumnDomain = DomainExtensionHelper.getColumnDomain( lColumn.getDomain(), pModel );
            if( lColumnDomain.getGeneratePk() != null )
            {
              if( !lColumnDomain.getGeneratePk().getSequenceNameRules().isEmpty() )
              {
                String lSequenceName = DomainExtensionHelper.getGeneratedNameColumn( lColumnDomain.getGeneratePk().getSequenceNameRules(), lColumn.getName(), lTableName, lTableAlias );
                lHistoryTableSequenceNames.add( lSequenceName.toUpperCase() );
              }
            }
          }
        }
      }

      for( ModelElement lModelElement : new ArrayList<>( pModel.getModel_elements() ) )
      {
        if( lModelElement instanceof Table )
        {
          Table lTable = (Table)lModelElement;

          if( lTable.getName().equalsIgnoreCase( lTableName ) )
          {
            pModel.getModel_elements().remove( lTable );
          }
        }
        if( lModelElement instanceof Sequence )
        {
          Sequence lSequence = (Sequence)lModelElement;

          if( lHistoryTableSequenceNames.contains( lSequence.getSequence_name().toUpperCase() ) )
          {
            pModel.getModel_elements().remove( lSequence );
          }
        }
      }
    }

    for( DomainColumn lDomainColumn : pDomain.getColumns() )
    {
      pTable.getColumns().remove( getColumn( pTable, lDomainColumn ) );
    }
  }
}
