// Copyright (c) 2004 OPITZ CONSULTING GmbH
package de.oc.dbdoc.schemadata;

/**
 * DOCUMENT ME!
 *
 * @author FSA
 */
public class Column
{
  private String _columnName;
  private String _columnType;

  public Column( String pColumnName, String pColumnType )
  {
    _columnName = pColumnName;
    _columnType = pColumnType;
    
    _columnName = _columnName.replaceAll("ß", "SS");
    _columnName = _columnName.replaceAll("Ä", "AE");
    _columnName = _columnName.replaceAll("ä", "AE");
    _columnName = _columnName.replaceAll("Ö", "OE");
    _columnName = _columnName.replaceAll("ö", "OE");
    _columnName = _columnName.replaceAll("Ü", "UE");
    _columnName = _columnName.replaceAll("ü", "UE");
    _columnName = _columnName.toUpperCase();
  }

  public String getColumnName()
  {
    return _columnName;
  }

  public String getColumnType()
  {
    return _columnType;
  }
}
