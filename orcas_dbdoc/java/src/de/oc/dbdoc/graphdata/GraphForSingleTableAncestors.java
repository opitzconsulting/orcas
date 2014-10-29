package de.oc.dbdoc.graphdata;

import java.util.ArrayList;
import java.util.List;

import de.oc.dbdoc.ant.Styles;
import de.oc.dbdoc.schemadata.Association;
import de.oc.dbdoc.schemadata.Schema;
import de.oc.dbdoc.schemadata.Table;

public class GraphForSingleTableAncestors extends GraphForSingleTable
{
  private List<Table> _allAncestorTablesForSingleTableReturn;

  public GraphForSingleTableAncestors( Table pSingleTable, List<Graph> pParentGraphs, Styles pStyles )
  {
    super( pSingleTable, pParentGraphs, pStyles );
  }

  @Override
  public String getLabel()
  {
    return super.getLabel() + "-Ancestors";
  }

  @Override
  public boolean allAssociations()
  {
    return false;
  }

  @Override
  public boolean isAllAncestors()
  {
    return true;
  }

  @Override
  public List<Association> getVisibleAssociation( Table pTable, Schema pSchema, boolean pOutRefsOnly )
  {
    List<Table> lAllAncestorTablesForSingleTable = getAllAncestorTablesForSingleTable( pSchema );

    List<Association> lReturn = new ArrayList<Association>();

    if( lAllAncestorTablesForSingleTable.contains( pTable ) )
    {
      for( Association lAssociation : pSchema.getAssociations() )
      {
        if( (lAssociation.getTableTo().equals( pTable ) && lAllAncestorTablesForSingleTable.contains( lAssociation.getTableFrom() ))
            || (lAssociation.getTableFrom().equals( pTable ) && lAllAncestorTablesForSingleTable.contains( lAssociation.getTableTo() )) )
        {
          lReturn.add( lAssociation );
        }
      }
    }

    return lReturn;
  }

  private List<Table> getAllAncestorTablesForSingleTable( Schema pSchema )
  {
    if( _allAncestorTablesForSingleTableReturn == null )
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
                if( lAssociation.getTableTo().equals( lTable ) && lReturn.contains( lAssociation.getTableFrom() ) )
                {
                  lReturn.add( lTable );
                  lOneNewFound = true;
                  break;
                }
              }
            }
          }
        }
      }
      while( lOneNewFound );

      _allAncestorTablesForSingleTableReturn = lReturn;
    }
    return _allAncestorTablesForSingleTableReturn;
  }
}
