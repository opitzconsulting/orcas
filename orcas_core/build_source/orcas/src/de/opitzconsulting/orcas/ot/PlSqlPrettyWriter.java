package de.opitzconsulting.orcas.ot;

import java.io.PrintStream;

public class PlSqlPrettyWriter
{
  private PrintStream _out;
  private int indentLevel = 0;
  private String indentString = "";
  private int newlineCount = 0;

  public PlSqlPrettyWriter( PrintStream pOut )
  {
    _out = pOut;
  }

  public void print( String pString )
  {
    if( pString.length() == 0 )
    {
      return;
    }

    if( newlineCount != 0 )
    {
      pString = pString.trim();
    }
    if( pString.startsWith( "end " ) )
    {
      decIndentLevel();
    }
    if( pString.equals( "end;" ) )
    {
      decIndentLevel();
    }
    if( pString.equals( "else" ) )
    {
      decIndentLevel();
    }
    if( pString.equals( "begin" ) )
    {
      decIndentLevel();
    }

    _out.print( indentString );
    _out.print( pString );

    if( pString.equals( "then" ) )
    {
      incIndentLevel();
    }
    if( pString.equals( "else" ) )
    {
      incIndentLevel();
    }
    if( pString.equals( "loop" ) )
    {
      incIndentLevel();
    }
    if( pString.equals( "begin" ) )
    {
      incIndentLevel();
    }
    if( pString.equals( "declare" ) )
    {
      incIndentLevel();
    }
    if( pString.equals( "is" ) )
    {
      incIndentLevel();
    }
    if( pString.endsWith( " is" ) )
    {
      incIndentLevel();
    }

    newlineCount = 0;
  }

  private void incIndentLevel()
  {
    indentLevel++;

    updateIndentString();
  }

  private void decIndentLevel()
  {
    indentLevel--;

    updateIndentString();
  }

  private void updateIndentString()
  {
    indentString = "";
    for( int i = 0; i < indentLevel; i++ )
    {
      indentString += "  ";
    }
  }

  public void println( String pString )
  {
    print( pString );
    println();
  }

  public void println()
  {
    if( newlineCount < 2 )
    {
      _out.println();
      newlineCount++;
    }
  }
}
