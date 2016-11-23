package de.opitzconsulting.orcas.extensions;

import java.util.ArrayList;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.EcoreUtil2;

import de.opitzconsulting.orcasDsl.Column;
import de.opitzconsulting.orcasDsl.ColumnDomain;
import de.opitzconsulting.orcasDsl.ColumnRef;
import de.opitzconsulting.orcasDsl.Domain;
import de.opitzconsulting.orcasDsl.DomainColumn;
import de.opitzconsulting.orcasDsl.Model;
import de.opitzconsulting.orcasDsl.ModelElement;
import de.opitzconsulting.orcasDsl.Table;
import de.opitzconsulting.orcasDsl.impl.TableImpl;

public class DomainExtension02ApplyTableDomains extends OrcasBaseExtensionWithParameter
{
  @Override
  public Model transformModel( Model pModel )
  {
    for( ModelElement lModelElement : new ArrayList<ModelElement>( pModel.getModel_elements() ) )
    {
      if( lModelElement instanceof Table )
      {
        handleTable( (Table)lModelElement, pModel );
      }
    }

    return pModel;
  }

  private void applyDomain( Table pTable, Domain pDomain )
  {
    int lAddFirstColumnsIndex = 0;

    for( DomainColumn lDomainColumn : pDomain.getColumns() )
    {
      Column lColumn = EcoreUtil.copy( lDomainColumn.getColumn() );

      lColumn.setName( DomainExtensionHelper.getGeneratedNameColumn( lDomainColumn.getColumnNameRules(), lDomainColumn.getColumn().getName(), pTable.getName(), pTable.getAlias() ) );

      if( lDomainColumn.isAppend_last() )
      {
        pTable.getColumns().add( lColumn );
      }
      else
      {
        pTable.getColumns().add( lAddFirstColumnsIndex++, lColumn );
      }
    }
  }

  private boolean isPkColumn( Table pTable, String pColumName )
  {
    if( pTable.getPrimary_key() != null )
    {
      for( ColumnRef lColumnRef : pTable.getPrimary_key().getPk_columns() )
      {
        if( lColumnRef.getColumn_name().equalsIgnoreCase( pColumName ) )
        {
          return true;
        }
      }
    }

    return false;
  }

  private void handleTable( Table pTable, Model pModel )
  {
    if( pTable.getDomain() != null )
    {
      Domain pDomain = DomainExtensionHelper.getDomain( pTable.getDomain(), pModel );

      applyDomain( pTable, pDomain );

      if( pDomain.getHistoryTable() != null )
      {
        Table lNewHistoryTable = new TableImpl();

        lNewHistoryTable.setName( DomainExtensionHelper.getGeneratedNameTable( pDomain.getHistoryTable().getTableNameRules(), pTable.getName(), pTable.getAlias() ) );
        lNewHistoryTable.setAlias( DomainExtensionHelper.getGeneratedNameTable( pDomain.getHistoryTable().getAliasNameRules(), pTable.getName(), pTable.getAlias() ) );
        lNewHistoryTable.getColumns().addAll( EcoreUtil.copyAll( pTable.getColumns() ) );

        for( Column lColumn : lNewHistoryTable.getColumns() )
        {
          if( !isPkColumn( pTable, lColumn.getName() ) )
          {
            lColumn.setNotnull( false );
          }
          lColumn.setDefault_value( null );

          if( lColumn.getDomain() != null )
          {
            ColumnDomain lColumnDomain = DomainExtensionHelper.getColumnDomain( lColumn.getDomain(), pModel );

            lColumn.setData_type( lColumnDomain.getData_type() );
            lColumn.setPrecision( lColumnDomain.getPrecision() );
            lColumn.setScale( lColumnDomain.getScale() );
            lColumn.setByteorchar( lColumnDomain.getByteorchar() );

            if( lColumnDomain.getGeneratePk() == null )
            {
              lColumn.setDomain( null );
            }
            else
            {
              if( pDomain.getHistoryTable().getAppendToPkDomain() == null )
              {
                lColumn.setDomain( null );
              }
              else
              {
                lColumn.setDomain( pDomain.getHistoryTable().getAppendToPkDomain() );
              }
            }
          }
        }

        if( pDomain.getHistoryTable().getDomain() != null )
        {
          applyDomain( lNewHistoryTable, DomainExtensionHelper.getDomain( pDomain.getHistoryTable().getDomain(), pModel ) );
        }

        pModel.getModel_elements().add( lNewHistoryTable );
      }
    }
  }
}
