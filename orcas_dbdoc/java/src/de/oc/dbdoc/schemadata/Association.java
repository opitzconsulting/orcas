// Copyright (c) 2004 OPITZ CONSULTING GmbH
package de.oc.dbdoc.schemadata;

import java.util.HashSet;
import java.util.Set;

/**
 * DOCUMENT ME!
 * 
 * @author FSA
 */
public class Association
{
  public static int MULTIPLICITY_N = 1000;
  private String _associationName;
  private Table _tableFrom;
  private Table _tableTo;
  private boolean _directed;
  private int _multiplicityFromMin;
  private int _multiplicityFromMax;
  private int _multiplicityToMin;
  private int _multiplicityToMax;
  private Set<String> _columnsFrom = new HashSet<String>();
  private Set<String> _columnsTo = new HashSet<String>();

  public Association( String pAssociationName, Table pTableFrom, Table pTableTo, boolean pDirected, int pMultipliocityFromMin, int pMultipliocityFromMax, int pMultiplicityToMin, int pMultiplicityToMax )
  {
    _associationName = pAssociationName;
    _tableFrom = pTableFrom;
    _tableTo = pTableTo;
    _directed = pDirected;
    _multiplicityFromMin = pMultipliocityFromMin;
    _multiplicityFromMax = pMultipliocityFromMax;
    _multiplicityToMin = pMultiplicityToMin;
    _multiplicityToMax = pMultiplicityToMax;
  }

  public Association merge( Association pAssociation )
  {
    boolean lIsReversed = !_tableFrom.equals( pAssociation._tableFrom );

    Association lAssociation = new Association( _associationName + "," + pAssociation._associationName, _tableFrom, _tableTo, _directed && pAssociation._directed && !lIsReversed,
        _multiplicityFromMin + (lIsReversed ? pAssociation._multiplicityToMin : pAssociation._multiplicityFromMin), _multiplicityFromMax
                                                                                                                    + (lIsReversed ? pAssociation._multiplicityToMax
                                                                                                                        : pAssociation._multiplicityFromMax),
        _multiplicityToMin + (lIsReversed ? pAssociation._multiplicityFromMin : pAssociation._multiplicityToMin), _multiplicityToMax
                                                                                                                  + (lIsReversed ? pAssociation._multiplicityFromMax : pAssociation._multiplicityToMax) );

    lAssociation._columnsFrom.addAll( this._columnsFrom );
    lAssociation._columnsTo.addAll( this._columnsTo );
    lAssociation._columnsFrom.addAll( pAssociation._columnsFrom );
    lAssociation._columnsTo.addAll( pAssociation._columnsTo );

    return lAssociation;
  }

  public boolean isMergeable( Association pAssociation )
  {
    return (_tableFrom.equals( pAssociation._tableFrom ) && _tableTo.equals( pAssociation._tableTo )) || (_tableFrom.equals( pAssociation._tableTo ) && _tableTo.equals( pAssociation._tableFrom ));
  }

  public String getAssociationName()
  {
    return _associationName;
  }

  public Table getTableFrom()
  {
    return _tableFrom;
  }

  public Table getTableTo()
  {
    return _tableTo;
  }

  public String getMultiplicityTextFrom()
  {
    return _getMultiplicityString( _multiplicityFromMin, _multiplicityFromMax );
  }

  public String getMultiplicityTextTo()
  {
    return _getMultiplicityString( _multiplicityToMin, _multiplicityToMax );
  }

  private String _getMultiplicityString( int pMultiplicityMin, int pMultiplicityMax )
  {
    if( pMultiplicityMax >= MULTIPLICITY_N )
    {
      return (pMultiplicityMin != 0 ? pMultiplicityMin + ".." : "") + "*";
    }

    if( pMultiplicityMin == pMultiplicityMax )
    {
      if( pMultiplicityMax == 1 )
      {
        return "";
      }
      else
      {
        return "" + pMultiplicityMax;
      }
    }

    return pMultiplicityMin + ".." + pMultiplicityMax;
  }

  public boolean isDirected()
  {
    return _directed;
  }

  public void addColumnFrom( String pColumName )
  {
    _columnsFrom.add( pColumName );
  }

  public void addColumnTo( String pColumName )
  {
    _columnsTo.add( pColumName );
  }

  public boolean isFromColumn( Column pColumn )
  {
    return _columnsFrom.contains( pColumn.getColumnName() );
  }

  public boolean isToColumn( Column pColumn )
  {
    return _columnsTo.contains( pColumn.getColumnName() );
  }
}
