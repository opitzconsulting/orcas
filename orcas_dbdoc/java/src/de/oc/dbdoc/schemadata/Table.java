// Copyright (c) 2004 OPITZ CONSULTING GmbH
package de.oc.dbdoc.schemadata;

import java.util.ArrayList;
import java.util.List;

/**
 * DOCUMENT ME!
 * 
 * @author FSA
 */
public class Table
{
  private String _tableName;
  private List<Column> _columns;

  public Table( String pTableName )
  {
    _tableName = pTableName;
    _tableName = _tableName.replaceAll( "ß", "SS" );
    _tableName = _tableName.replaceAll( "Ä", "AE" );
    _tableName = _tableName.replaceAll( "ä", "AE" );
    _tableName = _tableName.replaceAll( "Ö", "OE" );
    _tableName = _tableName.replaceAll( "ö", "OE" );
    _tableName = _tableName.replaceAll( "Ü", "UE" );
    _tableName = _tableName.replaceAll( "ü", "UE" );
    _tableName = _tableName.toUpperCase();
    _columns = new ArrayList<Column>();
  }

  public void addColumn( Column pColumn )
  {
    _columns.add( pColumn );
  }

  public String getName()
  {
    return _tableName;
  }

  @Override
  public int hashCode()
  {
    return _tableName.hashCode();
  }

  public boolean equals( Object pTable )
  {
    return _tableName.equals( ((Table)pTable)._tableName );
  }

  public List<Column> getColumns()
  {
    return _columns;
  }
}
