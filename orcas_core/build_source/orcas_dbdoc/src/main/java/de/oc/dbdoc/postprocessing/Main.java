package de.oc.dbdoc.postprocessing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main
{

  public Main()
  {
  }

  /**
   * @param args
   */
  public static void main( String[] args )
  {
    // args[0] = Pfad zu den HTML-Dateien
    // args[1] = Pfad zu den Maps
    /* File.list() liefert Strings zurück mit Dateinamen in dem Verzeichnis */
    try
    {
      String[] htmlDirectoryFiles = new File( args[0] ).list();

      // Pfade bearbeiten für einfacheres Konkatenieren
      Pattern pDirectorySeperator = Pattern.compile( "(/)" );
      Matcher mDirectorySeperator = null;
      for( int i = 0; i < args.length; i++ )
      {
        mDirectorySeperator = pDirectorySeperator.matcher( args[i] );
        boolean checkDirectory = false;
        while( mDirectorySeperator.find() )
        {
          checkDirectory = true;
          continue;
        }
        if( checkDirectory )
        {
          if( !(args[i].charAt( args[i].length() - 1 ) == ("/".toCharArray())[0]) )
          {
            args[i] = args[i] + "/";
          }
        }
        else
        {
          if( !(args[i].charAt( args[i].length() - 1 ) == ("\\".toCharArray())[0]) )
          {
            args[i] = args[i] + "\\";
          }
        }
      }

      for( int i = 0; i < htmlDirectoryFiles.length; i++ )
      {
        // Ordner nach HTML-Dateien durchsuchen
        if( htmlDirectoryFiles[i].substring( htmlDirectoryFiles[i].length() - 5 ).equals( ".html" ) )
        {

          String fMap = htmlDirectoryFiles[i].substring( 0, htmlDirectoryFiles[i].length() - 5 ) + ".map";

          String lMapFileName = args[1] + fMap;

          if( new File( lMapFileName ).exists() )
          {
            BufferedReader bufferedMapReader = new BufferedReader( new FileReader( lMapFileName ) );

            String line = "";
            String map = "";

            while( (line = bufferedMapReader.readLine()) != null )
            {
              map += line;
            }

            bufferedMapReader.close();

            BufferedReader bufferedHTMLReader = new BufferedReader( new FileReader( args[0] + htmlDirectoryFiles[i] ) );

            ArrayList<String> html = new ArrayList<String>();
            Pattern pMap = Pattern.compile( "(MAPCODE)" );

            while( (line = bufferedHTMLReader.readLine()) != null )
            {
              Matcher mMap = pMap.matcher( line );
              boolean checkVar = false;
              while( mMap.find() )
              {
                checkVar = true;
                continue;
              }
              if( checkVar )
              {
                html.add( map );
              }
              else
              {
                html.add( line );
              }
            }

            bufferedHTMLReader.close();

            FileWriter HTMLFileWriter = new FileWriter( args[0] + htmlDirectoryFiles[i] );
            PrintWriter HTMLPrintWriter = new PrintWriter( HTMLFileWriter );

            Iterator<String> htmlIterator = html.iterator();
            while( htmlIterator.hasNext() )
            {
              HTMLPrintWriter.println( htmlIterator.next() );
            }

            HTMLPrintWriter.close();
            HTMLFileWriter.close();
          }
        }
      }

      _postprocessSvgs( args[0] );

    }
    catch( Exception e )
    {
      e.printStackTrace();
    }
  }

  private static void _postprocessSvgs( String pSvgPath ) throws Exception
  {
    File lSvgDir = new File( pSvgPath );

    for( File lSvgFile : lSvgDir.listFiles( new FilenameFilter()
    {
      public boolean accept( File pDir, String pName )
      {
        return pName.endsWith( ".svg" );
      }
    } ) )
    {
      String lContent = de.oc.dbdoc.Main.readFile( lSvgFile );

      // the style attribute is split in to font-family and font-size attributes, because it is ignored by firefox otherwise
      // if more style attributes are used, this needs to be extended
      lContent = lContent.replaceAll( "style=\"font-family:(.*);font-size:(.*);\"", "font-family=\"$1\" font-size=\"$2\"" );

      FileWriter lFileWriter = new FileWriter( lSvgFile );

      lFileWriter.write( lContent );

      lFileWriter.close();
    }
  }
}
