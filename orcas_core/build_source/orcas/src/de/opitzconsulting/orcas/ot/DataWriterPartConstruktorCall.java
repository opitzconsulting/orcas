package de.opitzconsulting.orcas.ot;

import java.util.ArrayList;
import java.util.List;

public class DataWriterPartConstruktorCall extends DataWriterPart
{
  private static final int _MAX_LENGTH_BEFORE_NEWLINE = 180;
  private List<DataWriterPart> _arguments = new ArrayList<DataWriterPart>();
  private String _typeName;
  private int _dummyValueCount;

  public DataWriterPartConstruktorCall( int pDummyValueCount, List<DataWriterPart> pArguments, String pTypeName )
  {
    _arguments = pArguments;
    _typeName = pTypeName;
    _dummyValueCount = pDummyValueCount;
  }

  @Override
  public void writeRecursive( StringBuilder pOut, int pIndentSize )
  {
    StringBuilder lStringBuilder = new StringBuilder();

    _writeInternal( lStringBuilder, false, pIndentSize );

    if( lStringBuilder.length() > _MAX_LENGTH_BEFORE_NEWLINE )
    {
      lStringBuilder = new StringBuilder();
      _writeInternal( lStringBuilder, true, pIndentSize );
    }

    pOut.append( lStringBuilder );
  }

  private void _writeIndentAndNewLine( StringBuilder pOut, boolean pIsNewlineMode, int pIndentSize )
  {
    if( pIsNewlineMode )
    {
      pOut.append( "\n" );

      for( int i = 0; i < pIndentSize; i++ )
      {
        pOut.append( " " );
      }
    }
  }

  private void _writeInternal( StringBuilder pOut, boolean pIsNewlineMode, int pIndentSize )
  {
    pOut.append( "new " + _typeName );

    _writeIndentAndNewLine( pOut, pIsNewlineMode, pIndentSize );

    pOut.append( "( " );

    boolean lIsFirst = true;

    for( int i = 0; i < _dummyValueCount; i++ )
    {
      if( lIsFirst )
      {
        lIsFirst = false;
      }
      else
      {
        pOut.append( ", " );
      }

      _writeIndentAndNewLine( pOut, pIsNewlineMode, pIndentSize + 2 );

      pOut.append( "null" );
    }

    for( DataWriterPart lDataWriterPart : _arguments )
    {
      if( lIsFirst )
      {
        lIsFirst = false;
      }
      else
      {
        pOut.append( ", " );
      }

      _writeIndentAndNewLine( pOut, pIsNewlineMode, pIndentSize + 2 );

      lDataWriterPart.writeRecursive( pOut, pIsNewlineMode ? pIndentSize + 2 : pIndentSize );
    }

    _writeIndentAndNewLine( pOut, pIsNewlineMode, pIndentSize );

    if( !pIsNewlineMode )
    {
      pOut.append( " " );
    }
    pOut.append( ")" );
  }
}
