package de.oc.dbdoc.graphdata;

import java.util.ArrayList;
import java.util.List;

import de.oc.dbdoc.ant.Styles;
import de.oc.dbdoc.ant.Tableregistry;
import de.oc.dbdoc.schemadata.Association;
import de.oc.dbdoc.schemadata.Schema;
import de.oc.dbdoc.schemadata.Table;

public class GraphForSingleTableDescendants extends GraphForSingleTable
{
  private List<Table> _allDescendantTablesForSingleTableReturn;

  public GraphForSingleTableDescendants( Table pSingleTable, List<Graph> pParentGraphs, Styles pStyles, Tableregistry pTableregistry )
  {
    super( pSingleTable, pParentGraphs, pStyles, pTableregistry );
  }

  @Override
  public String getLabel()
  {
    return super.getLabel() + "-Descendants";
  }

  @Override
  public boolean allAssociations()
  {
    return false;
  }

  @Override
  public boolean isAllDescendants()
  {
    return true;
  }

  @Override
  public List<Association> getVisibleAssociation( Table pTable, Schema pSchema, boolean pOutRefsOnly )
  {
    List<Table> lAllDescendantTablesForSingleTable = getAllDescendantTablesForSingleTable( pSchema );

    List<Association> lReturn = new ArrayList<Association>();

    if( lAllDescendantTablesForSingleTable.contains( pTable ) )
    {
      for( Association lAssociation : pSchema.getAssociations() )
      {
        if( (lAssociation.getTableTo().equals( pTable ) && lAllDescendantTablesForSingleTable.contains( lAssociation.getTableFrom() )) || (lAssociation.getTableFrom().equals( pTable ) && lAllDescendantTablesForSingleTable.contains( lAssociation.getTableTo() )) )
        {
          lReturn.add( lAssociation );
        }
      }
    }

    return lReturn;
  }

  private List<Table> getAllDescendantTablesForSingleTable( Schema pSchema )
  {
    if( _allDescendantTablesForSingleTableReturn == null )
    {
      List<Table> lReturn = new ArrayList<Table>();
      boolean lOneNewFound;
      do
      {
        lOneNewFound = false;

        for( Table lTable : pSchema.getTables() )
        {
          if( !lReturn.contains( lTable ) )
          {
            if( containsTableRecursive( lTable ) )
            {
              lReturn.add( lTable );
              lOneNewFound = true;
            }
            else
            {
              for( Association lAssociation : pSchema.getAssociations() )
              {
                if( lAssociation.getTableFrom().equals( lTable ) && lReturn.contains( lAssociation.getTableTo() ) )
                {
                  lReturn.add( lTable );
                  lOneNewFound = true;
                  break;
                }
              }
            }
          }
        }
      } while( lOneNewFound );

      _allDescendantTablesForSingleTableReturn = lReturn;
    }

    return _allDescendantTablesForSingleTableReturn;
  }
}
