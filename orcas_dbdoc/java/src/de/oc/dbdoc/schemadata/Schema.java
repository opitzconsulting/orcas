// Copyright (c) 2004 OPITZ CONSULTING GmbH
package de.oc.dbdoc.schemadata;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * DOCUMENT ME!
 * 
 * @author FSA
 */
public class Schema
{
  private List<Table> _tables = new ArrayList<Table>();
  private Collection<Association> _associations = new ArrayList<Association>();

  public Schema()
  {
  }

  public void addTable( Table pTable )
  {
    _tables.add( pTable );

    Collections.sort( _tables, new Comparator<Table>()
    {
      public int compare( Table pTable1, Table pTable2 )
      {
        return pTable1.getName().compareTo( pTable2.getName() );
      }
    } );
  }

  public void addAssociation( Association pAssociation )
  {
    _associations.add( pAssociation );
  }

  public Table findTable( String pTableName )
  {
    Iterator<Table> lTableIterator = _tables.iterator();
    while( lTableIterator.hasNext() )
    {
      Table lTable = lTableIterator.next();

      if( lTable.getName().equals( pTableName ) )
      {
        return lTable;
      }
    }

    return null;
  }

  public void convertAssociationTablesToAssociations()
  {
    ListIterator<Table> lTableListIterator = _tables.listIterator();
    while( lTableListIterator.hasNext() )
    {
      Table lTable = lTableListIterator.next();

      if( lTable.getName().startsWith( "ZUO_" ) )
      {
        List lAssociations = _getAssociationsFromTable( lTable );

        if( lAssociations.size() == 2 )
        {
          Association lAssociation0 = (Association)lAssociations.get( 0 );
          Association lAssociation1 = (Association)lAssociations.get( 1 );

          Association lAssociationTableAssociation = new Association( lTable.getName(), lAssociation0.getTableTo(), lAssociation1.getTableTo(), false, 0, Association.MULTIPLICITY_N, 0,
              Association.MULTIPLICITY_N );

          _associations.remove( lAssociation0 );
          _associations.remove( lAssociation1 );
          _associations.add( lAssociationTableAssociation );

          lTableListIterator.remove();
        }
      }
    }
  }

  public void mergeAssociations()
  {
    while( _mergeFirstAssociations() )
      ;
  }

  private boolean _mergeFirstAssociations()
  {
    Iterator lOuterAssociationIterator = _associations.iterator();
    while( lOuterAssociationIterator.hasNext() )
    {
      Association lOuterAssociation = (Association)lOuterAssociationIterator.next();

      Iterator lInnerAssociationIterator = _associations.iterator();
      while( lInnerAssociationIterator.hasNext() )
      {
        Association lInnerAssociation = (Association)lInnerAssociationIterator.next();

        if( lOuterAssociation != lInnerAssociation && lOuterAssociation.isMergeable( lInnerAssociation ) )
        {
          Association lMergeAssociation = lOuterAssociation.merge( lInnerAssociation );

          _associations.remove( lOuterAssociation );
          _associations.remove( lInnerAssociation );
          _associations.add( lMergeAssociation );

          return true;
        }
      }
    }

    return false;
  }

  private List<Association> _getAssociationsFromTable( Table pTable )
  {
    List<Association> lReturn = new ArrayList<Association>();

    Iterator lAssociationIterator = _associations.iterator();
    while( lAssociationIterator.hasNext() )
    {
      Association lAssociation = (Association)lAssociationIterator.next();

      if( lAssociation.getTableFrom().equals( pTable ) )
      {
        lReturn.add( lAssociation );
      }
    }

    return lReturn;
  }

  public Collection<Table> getTables()
  {
    return _tables;
  }

  public Collection<Association> getAssociations()
  {
    return _associations;
  }
}
