package de.opitzconsulting.orcas.extensions;

import java.util.ArrayList;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.EcoreUtil2;

import de.opitzconsulting.orcasDsl.Column;
import de.opitzconsulting.orcasDsl.ColumnDomain;
import de.opitzconsulting.orcasDsl.ColumnRef;
import de.opitzconsulting.orcasDsl.Constraint;
import de.opitzconsulting.orcasDsl.Domain;
import de.opitzconsulting.orcasDsl.DomainColumn;
import de.opitzconsulting.orcasDsl.ForeignKey;
import de.opitzconsulting.orcasDsl.Model;
import de.opitzconsulting.orcasDsl.ModelElement;
import de.opitzconsulting.orcasDsl.PrimaryKey;
import de.opitzconsulting.orcasDsl.Sequence;
import de.opitzconsulting.orcasDsl.Table;
import de.opitzconsulting.orcasDsl.UniqueKey;
import de.opitzconsulting.orcasDsl.impl.ColumnRefImpl;
import de.opitzconsulting.orcasDsl.impl.ConstraintImpl;
import de.opitzconsulting.orcasDsl.impl.ForeignKeyImpl;
import de.opitzconsulting.orcasDsl.impl.PrimaryKeyImpl;
import de.opitzconsulting.orcasDsl.impl.SequenceImpl;
import de.opitzconsulting.orcasDsl.impl.TableImpl;
import de.opitzconsulting.orcasDsl.impl.UniqueKeyImpl;
import de.opitzconsulting.orcasDsl.ColumnIdentity;
import de.opitzconsulting.orcasDsl.impl.ColumnIdentityImpl;

public class DomainExtension03ApplyColumnDomains extends OrcasBaseExtensionWithParameter
{
  private ColumnIdentity copyColumnIdentity( ColumnIdentity pColumnIdentity )
  {
    ColumnIdentity lReturn = new ColumnIdentityImpl();

    lReturn.setAlways( pColumnIdentity.getAlways() );
    lReturn.setBy_default( pColumnIdentity.getBy_default() );
    lReturn.setCache( pColumnIdentity.getCache() );
    lReturn.setCycle( pColumnIdentity.getCycle() );
    lReturn.setIncrement_by( pColumnIdentity.getIncrement_by() );
    lReturn.setMaxvalue( pColumnIdentity.getMaxvalue() );
    lReturn.setMinvalue( pColumnIdentity.getMinvalue() );
    lReturn.setOn_null( pColumnIdentity.getOn_null() );
    lReturn.setOrder( pColumnIdentity.getOrder() );

    return lReturn;
  }
  
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

    for( ModelElement lModelElement : new ArrayList<ModelElement>( pModel.getModel_elements() ) )
    {
      if( lModelElement instanceof Table )
      {
        handleTableFK( (Table)lModelElement, pModel );
      }
    }

    return pModel;
  }

  private void handleTable( Table pTable, Model pModel )
  {
    for( Column lColumn : pTable.getColumns() )
    {
      if( lColumn.getDomain() != null )
      {
        ColumnDomain lColumnDomain = DomainExtensionHelper.getColumnDomain( lColumn.getDomain(), pModel );

        lColumn.setData_type( lColumnDomain.getData_type() );
        lColumn.setPrecision( lColumnDomain.getPrecision() );
        lColumn.setScale( lColumnDomain.getScale() );
        lColumn.setByteorchar( lColumnDomain.getByteorchar() );
        lColumn.setUnsigned( lColumnDomain.isUnsigned() );    

        if( lColumnDomain.getDefault_value() != null )
        {
          lColumn.setDefault_value( lColumnDomain.getDefault_value() );
        }

        if( lColumnDomain.getIdentity() != null )
        {
          lColumn.setIdentity( copyColumnIdentity( lColumnDomain.getIdentity() ) );
        }

        if( lColumnDomain.isNotnull() )
        {
          lColumn.setNotnull( true );
        }

        if( lColumnDomain.getGeneratePk() != null )
        {

          ColumnRef lColumnRef = new ColumnRefImpl();
          lColumnRef.setColumn_name( lColumn.getName() );

          String lPkName = DomainExtensionHelper.getGeneratedNameColumn( lColumnDomain.getGeneratePk().getConstraintNameRules(), lColumn.getName(), pTable.getName(), pTable.getAlias() );

          if( pTable.getPrimary_key() == null )
          {
            PrimaryKey lPrimaryKey = new PrimaryKeyImpl();
            lPrimaryKey.getPk_columns().add( lColumnRef );            
            lPrimaryKey.setConsName( lPkName.length() == 0 ? null : lPkName );

            pTable.setPrimary_key( lPrimaryKey );
          }
          else
          {
            if( !lPkName.equalsIgnoreCase( pTable.getPrimary_key().getConsName() ) && (!( lPkName.length() == 0 && pTable.getPrimary_key().getConsName() == null )) )
            {
              throw new RuntimeException( "pk setup invalid: " + pTable.getPrimary_key().getConsName() + " " + lPkName );
            }

            pTable.getPrimary_key().getPk_columns().add( lColumnRef );
          }

          if( !lColumnDomain.getGeneratePk().getSequenceNameRules().isEmpty() )
          {
            Sequence lSequence = new SequenceImpl();

            lSequence.setSequence_name( DomainExtensionHelper.getGeneratedNameColumn( lColumnDomain.getGeneratePk().getSequenceNameRules(), lColumn.getName(), pTable.getName(), pTable.getAlias() ) );
            lSequence.setMax_value_select( "select max(" + lColumn.getName() + ") from " + pTable.getName() );

            pModel.getModel_elements().add( lSequence );
          }
        }

        if( lColumnDomain.getGenerateUk() != null )
        {
          UniqueKey lUniqueKey = new UniqueKeyImpl();

          ColumnRef lColumnRef = new ColumnRefImpl();
          lUniqueKey.getUk_columns().add( lColumnRef );
          lColumnRef.setColumn_name( lColumn.getName() );
          lUniqueKey.setConsName( DomainExtensionHelper.getGeneratedNameColumn( lColumnDomain.getGenerateUk().getConstraintNameRules(), lColumn.getName(), pTable.getName(), pTable.getAlias() ) );

          pTable.getInd_uks().add( lUniqueKey );
        }

        if( lColumnDomain.getGenerateCc() != null )
        {
          Constraint lConstraint = new ConstraintImpl();

          lConstraint.setConsName( DomainExtensionHelper.getGeneratedNameColumn( lColumnDomain.getGenerateCc().getConstraintNameRules(), lColumn.getName(), pTable.getName(), pTable.getAlias() ) );
          lConstraint.setRule( DomainExtensionHelper.getGeneratedNameColumn( lColumnDomain.getGenerateCc().getCheckRuleNameRules(), lColumn.getName(), pTable.getName(), pTable.getAlias() ) );

          pTable.getConstraints().add( lConstraint );
        }
      }
    }
  }

  private void setupFkTable( ColumnDomain pColumnDomain, Column pColumn, Table pTable, ForeignKey pForeignKey, Model pModel )
  {
    if( pColumnDomain.getGenerateFk().getPkColumnNameRules() != null && !pColumnDomain.getGenerateFk().getPkColumnNameRules().isEmpty() )
    {
      for( ModelElement lModelElement : pModel.getModel_elements() )
      {
        if( lModelElement instanceof Table )
        {
          Table lTable = (Table)lModelElement;
  
          if( lTable.getPrimary_key() != null )
          {
  
            String lExpectedPkColumnName = DomainExtensionHelper.getGeneratedNameColumn( pColumnDomain.getGenerateFk().getPkColumnNameRules(), pColumn.getName(), pTable.getName(), pTable.getAlias() );
  
            for( ColumnRef lColumnRef : lTable.getPrimary_key().getPk_columns() )
            {
              if( lExpectedPkColumnName.equalsIgnoreCase( lColumnRef.getColumn_name() ) )
              {
                ColumnRef lDestColumnRef = new ColumnRefImpl();
                lDestColumnRef.setColumn_name( lColumnRef.getColumn_name() );
                pForeignKey.getDestColumns().add( lDestColumnRef );
                pForeignKey.setDestTable( lTable.getName() );
  
                return;
              }
            }
          }
        }
      }
    }

    if( pColumnDomain.getGenerateFk().getAliasNameRules() != null && !pColumnDomain.getGenerateFk().getAliasNameRules().isEmpty() )
    {
      for( ModelElement lModelElement : pModel.getModel_elements() )
      {
        if( lModelElement instanceof Table )
        {
          Table lTable = (Table)lModelElement;
  
          if( lTable.getPrimary_key() != null && lTable.getPrimary_key().getPk_columns().size() == 1 )
          {
            String lExpectedAliasName = DomainExtensionHelper.getGeneratedNameColumn( pColumnDomain.getGenerateFk().getAliasNameRules(), pColumn.getName(), pTable.getName(), pTable.getAlias() );
  
            for( ColumnRef lColumnRef : lTable.getPrimary_key().getPk_columns() )
            {
              if( lExpectedAliasName.equalsIgnoreCase( lTable.getAlias() ) )
              {
                ColumnRef lDestColumnRef = new ColumnRefImpl();
                lDestColumnRef.setColumn_name( lColumnRef.getColumn_name() );
                pForeignKey.getDestColumns().add( lDestColumnRef );
                pForeignKey.setDestTable( lTable.getName() );
  
                return;
              }
            }
          }
        }
      }
    }

    if( pColumnDomain.getGenerateFk().getTableNameRules() != null && !pColumnDomain.getGenerateFk().getTableNameRules().isEmpty() )
    {
      for( ModelElement lModelElement : pModel.getModel_elements() )
      {
        if( lModelElement instanceof Table )
        {
          Table lTable = (Table)lModelElement;
  
          if( lTable.getPrimary_key() != null && lTable.getPrimary_key().getPk_columns().size() == 1 )
          {
            String lExpectedTableName = DomainExtensionHelper.getGeneratedNameColumn( pColumnDomain.getGenerateFk().getTableNameRules(), pColumn.getName(), pTable.getName(), pTable.getAlias() );
  
            for( ColumnRef lColumnRef : lTable.getPrimary_key().getPk_columns() )
            {
              if( lExpectedTableName.equalsIgnoreCase( lTable.getName() ) )
              {
                ColumnRef lDestColumnRef = new ColumnRefImpl();
                lDestColumnRef.setColumn_name( lColumnRef.getColumn_name() );
                pForeignKey.getDestColumns().add( lDestColumnRef );
                pForeignKey.setDestTable( lTable.getName() );
  
                return;
              }
            }
          }
        }
      }
    }

    throw new RuntimeException( "fk not found: " + pTable.getName() + "." + pColumn.getName() );
  }

  private void handleTableFK( Table pTable, Model pModel )
  {
    for( Column lColumn : pTable.getColumns() )
    {
      if( lColumn.getDomain() != null )
      {
        ColumnDomain lColumnDomain = DomainExtensionHelper.getColumnDomain( lColumn.getDomain(), pModel );

        if( lColumnDomain.getGenerateFk() != null )
        {
          ForeignKey lForeignKey = new ForeignKeyImpl();

          ColumnRef lSrcColumnRef = new ColumnRefImpl();
          lForeignKey.getSrcColumns().add( lSrcColumnRef );
          lSrcColumnRef.setColumn_name( lColumn.getName() );
          lForeignKey.setConsName( DomainExtensionHelper.getGeneratedNameColumn( lColumnDomain.getGenerateFk().getConstraintNameRules(), lColumn.getName(), pTable.getName(), pTable.getAlias() ) );
          lForeignKey.setDelete_rule( lColumnDomain.getGenerateFk().getDelete_rule() );
          setupFkTable( lColumnDomain, lColumn, pTable, lForeignKey, pModel );

          pTable.getForeign_keys().add( lForeignKey );
        }
      }
    }
  }

}
