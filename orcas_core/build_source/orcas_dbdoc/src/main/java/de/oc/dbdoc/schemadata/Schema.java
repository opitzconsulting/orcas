// Copyright (c) 2004 OPITZ CONSULTING GmbH
package de.oc.dbdoc.schemadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

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

      if( lTable.getName().equalsIgnoreCase( pTableName ) )
      {
        return lTable;
      }
    }

    return null;
  }

  public void mergeAssociations()
  {
    while( _mergeFirstAssociations() )
      ;
  }

  private boolean _mergeFirstAssociations()
  {
    for( Association lOuterAssociation : _associations )
    {
      for( Association lInnerAssociation : _associations )
      {
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

  public Collection<Table> getTables()
  {
    return _tables;
  }

  public Collection<Association> getAssociations()
  {
    return _associations;
  }
}
