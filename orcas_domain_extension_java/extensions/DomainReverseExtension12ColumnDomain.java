package de.opitzconsulting.orcas.extensions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.opitzconsulting.orcas.extensions.OrcasBaseExtensionWithParameter;
import de.opitzconsulting.orcasDsl.Column;
import de.opitzconsulting.orcasDsl.ColumnDomain;
import de.opitzconsulting.orcasDsl.Constraint;
import de.opitzconsulting.orcasDsl.ForeignKey;
import de.opitzconsulting.orcasDsl.IndexOrUniqueKey;
import de.opitzconsulting.orcasDsl.Model;
import de.opitzconsulting.orcasDsl.ModelElement;
import de.opitzconsulting.orcasDsl.PrimaryKey;
import de.opitzconsulting.orcasDsl.Sequence;
import de.opitzconsulting.orcasDsl.Table;
import de.opitzconsulting.orcasDsl.UniqueKey;

public class DomainReverseExtension12ColumnDomain extends OrcasBaseExtensionWithParameter implements OrcasReverseExtension
{
  @Override
  public Model transformModel( Model pModel )
  {
    List<Object[]> lReverseColumnDomainsList = new ArrayList<Object[]>();

    for( ModelElement lModelElement : new ArrayList<ModelElement>( pModel.getModel_elements() ) )
    {
      if( lModelElement instanceof Table )
      {
        Table pTable = (Table)lModelElement;
        for( final Column lColumn : pTable.getColumns() )
        {
          final ColumnDomain lColumnDomain = findBestMatchColumnDomain( lColumn, pTable, pModel );

          if( lColumnDomain != null )
          {
            lReverseColumnDomainsList.add( new Object[] { lColumnDomain, lColumn, pTable } );
          }
        }
      }
    }

    for( Object[] lEntry : lReverseColumnDomainsList )
    {
      reverseApplyColumnDomain( (ColumnDomain)lEntry[0], (Column)lEntry[1], (Table)lEntry[2], pModel );
    }

    return pModel;
  }

  private UniqueKey findUk( Table pTable, String pUkName )
  {
    for( IndexOrUniqueKey lIndexOrUniqueKey : pTable.getInd_uks() )
    {
      if( lIndexOrUniqueKey.getConsName().equalsIgnoreCase( pUkName ) )
      {
        return (UniqueKey)lIndexOrUniqueKey;
      }
    }

    return null;
  }

  private Constraint findCc( Table pTable, String pCcName )
  {
    for( Constraint lConstraint : pTable.getConstraints() )
    {
      if( lConstraint.getConsName().equalsIgnoreCase( pCcName ) )
      {
        return lConstraint;
      }
    }

    return null;
  }

  private ForeignKey findFk( Table pTable, String pFkName )
  {
    for( ForeignKey lForeignKey : pTable.getForeign_keys() )
    {
      if( lForeignKey.getConsName().equalsIgnoreCase( pFkName ) )
      {
        return lForeignKey;
      }
    }

    return null;
  }

  private Integer rateColumnDomain( ColumnDomain pColumnDomain, Column pColumn, Table pTable, Model pModel )
  {
    if( pColumnDomain.getData_type() == pColumn.getData_type() && DomainExtensionHelper.isIntEqual( pColumnDomain.getPrecision(), pColumn.getPrecision() ) && DomainExtensionHelper.isIntEqual( pColumnDomain.getScale(), pColumn.getScale() ) && pColumnDomain.getByteorchar() == pColumn.getByteorchar() )
    {
      int lReturn = 0;

      if( pColumnDomain.isNotnull() )
      {
        if( pColumn.isNotnull() )
        {
          lReturn += 1;
        }
        else
        {
          return null;
        }
      }

      if( pColumnDomain.getDefault_value() != null )
      {
        if( pColumnDomain.getDefault_value().equalsIgnoreCase( pColumn.getDefault_value() ) )
        {
          lReturn += 3;
        }
        else
        {
          return null;
        }
      }

      if( pColumnDomain.getGeneratePk() != null )
      {
        PrimaryKey lPrimaryKey = pTable.getPrimary_key();
        if( lPrimaryKey == null )
        {
          return null;
        }

        if( !lPrimaryKey.getConsName().equalsIgnoreCase( DomainExtensionHelper.getGeneratedNameColumn( pColumnDomain.getGeneratePk().getConstraintNameRules(), pColumn.getName(), pTable.getName(), pTable.getAlias() ) ) )
        {
          return null;
        }

        if( lPrimaryKey.getPk_columns().size() != 1 )
        {
          return null;
        }

        if( !lPrimaryKey.getPk_columns().get( 0 ).getColumn_name().equalsIgnoreCase( pColumn.getName() ) )
        {
          return null;
        }

        if( !pColumnDomain.getGeneratePk().getSequenceNameRules().isEmpty() )
        {
          lReturn += 1;
        }

        lReturn += 100;
      }

      if( pColumnDomain.getGenerateUk() != null )
      {
        UniqueKey lUniqueKey = findUk( pTable, DomainExtensionHelper.getGeneratedNameColumn( pColumnDomain.getGenerateUk().getConstraintNameRules(), pColumn.getName(), pTable.getName(), pTable.getAlias() ) );

        if( lUniqueKey == null )
        {
          return null;
        }

        if( lUniqueKey.getUk_columns().size() != 1 )
        {
          return null;
        }

        if( !lUniqueKey.getUk_columns().get( 0 ).getColumn_name().equalsIgnoreCase( pColumn.getName() ) )
        {
          return null;
        }

        lReturn += 50;
      }

      if( pColumnDomain.getGenerateCc() != null )
      {
        Constraint lConstraint = findCc( pTable, DomainExtensionHelper.getGeneratedNameColumn( pColumnDomain.getGenerateCc().getConstraintNameRules(), pColumn.getName(), pTable.getName(), pTable.getAlias() ) );

        if( lConstraint == null )
        {
          return null;
        }

        if( !lConstraint.getRule().equalsIgnoreCase( DomainExtensionHelper.getGeneratedNameColumn( pColumnDomain.getGenerateCc().getCheckRuleNameRules(), pColumn.getName(), pTable.getName(), pTable.getAlias() ) ) )
        {
          return null;
        }

        lReturn += 40;
      }

      if( pColumnDomain.getGenerateFk() != null )
      {
        ForeignKey lForeignKey = findFk( pTable, DomainExtensionHelper.getGeneratedNameColumn( pColumnDomain.getGenerateFk().getConstraintNameRules(), pColumn.getName(), pTable.getName(), pTable.getAlias() ) );

        if( lForeignKey == null )
        {
          return null;
        }

        if( lForeignKey.getSrcColumns().size() != 1 )
        {
          return null;
        }

        if( !lForeignKey.getSrcColumns().get( 0 ).getColumn_name().equalsIgnoreCase( pColumn.getName() ) )
        {
          return null;
        }

        String lExpectedPkColumnName;
        try
        {
          lExpectedPkColumnName = DomainExtensionHelper.getGeneratedNameColumn( pColumnDomain.getGenerateFk().getPkColumnNameRules(), pColumn.getName(), pTable.getName(), pTable.getAlias() );
        }
        catch( Exception e )
        {
          return null;
        }

        Table lDestTable = findTableByName( lForeignKey.getDestTable(), pModel );

        if( lDestTable == null )
        {
          return null;
        }

        if( lDestTable.getPrimary_key() == null )
        {
          return null;
        }

        if( lDestTable.getPrimary_key().getPk_columns().size() != 1 || !lDestTable.getPrimary_key().getPk_columns().get( 0 ).getColumn_name().equalsIgnoreCase( lExpectedPkColumnName ) )
        {
          return null;
        }

        if( lForeignKey.getDestColumns().size() != 1 || !lExpectedPkColumnName.equalsIgnoreCase( lForeignKey.getDestColumns().get( 0 ).getColumn_name() ) )
        {
          return null;
        }

        lReturn += 80;
      }

      return lReturn;
    }
    else
    {
      return null;
    }
  }

  private Table findTableByName( String pDestTable, Model pModel )
  {
    for( ModelElement lModelElement : pModel.getModel_elements() )
    {
      if( lModelElement instanceof Table )
      {
        Table lTable = (Table)lModelElement;

        if( lTable.getName().equalsIgnoreCase( pDestTable ) )
        {
          return lTable;
        }
      }
    }

    return null;
  }

  private ColumnDomain findBestMatchColumnDomain( Column pColumn, Table pTable, Model pModel )
  {
    Integer lBestRating = null;
    ColumnDomain lReturn = null;

    for( ModelElement lModelElement : pModel.getModel_elements() )
    {
      if( lModelElement instanceof ColumnDomain )
      {
        Integer lRating = rateColumnDomain( (ColumnDomain)lModelElement, pColumn, pTable, pModel );

        if( lRating != null )
        {
          if( lBestRating == null || lRating > lBestRating )
          {
            lBestRating = lRating;
            lReturn = (ColumnDomain)lModelElement;
          }
        }
      }
    }

    return lReturn;
  }

  private void reverseApplyColumnDomain( ColumnDomain pColumnDomain, Column pColumn, Table pTable, Model pModel )
  {
    pColumn.setDomain( pColumnDomain.getName() );
    pColumn.setData_type( null );
    pColumn.setPrecision( DomainExtensionHelper.getIntNullValue() );
    pColumn.setScale( DomainExtensionHelper.getIntNullValue() );
    pColumn.setByteorchar( null );

    if( pColumnDomain.isNotnull() )
    {
      pColumn.setNotnull( false );
    }

    if( pColumnDomain.getDefault_value() != null )
    {
      pColumn.setDefault_value( null );
    }

    if( pColumnDomain.getGeneratePk() != null )
    {
      pTable.setPrimary_key( null );

      if( !pColumnDomain.getGeneratePk().getSequenceNameRules().isEmpty() )
      {
        String lSequenceName = DomainExtensionHelper.getGeneratedNameColumn( pColumnDomain.getGeneratePk().getSequenceNameRules(), pColumn.getName(), pTable.getName(), pTable.getAlias() );

        for( ModelElement lModelElement : new ArrayList<>( pModel.getModel_elements() ) )
        {
          if( lModelElement instanceof Sequence )
          {
            Sequence lSequence = (Sequence)lModelElement;

            if( lSequence.getSequence_name().equalsIgnoreCase( lSequenceName ) )
            {
              pModel.getModel_elements().remove( lSequence );
            }
          }
        }
      }
    }

    if( pColumnDomain.getGenerateUk() != null )
    {
      pTable.getInd_uks().remove( findUk( pTable, DomainExtensionHelper.getGeneratedNameColumn( pColumnDomain.getGenerateUk().getConstraintNameRules(), pColumn.getName(), pTable.getName(), pTable.getAlias() ) ) );
    }
    if( pColumnDomain.getGenerateCc() != null )
    {
      pTable.getConstraints().remove( findCc( pTable, DomainExtensionHelper.getGeneratedNameColumn( pColumnDomain.getGenerateCc().getConstraintNameRules(), pColumn.getName(), pTable.getName(), pTable.getAlias() ) ) );
    }
    if( pColumnDomain.getGenerateFk() != null )
    {
      pTable.getForeign_keys().remove( findFk( pTable, DomainExtensionHelper.getGeneratedNameColumn( pColumnDomain.getGenerateFk().getConstraintNameRules(), pColumn.getName(), pTable.getName(), pTable.getAlias() ) ) );
    }
  }
}
