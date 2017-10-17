package de.opitzconsulting.orcas.diff;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.opitzconsulting.orcas.diff.JdbcConnectionHandler.RunWithCallableStatementProvider;
import de.opitzconsulting.orcas.diff.ParametersCommandline.ParameterTypeMode;
import de.opitzconsulting.orcas.sql.CallableStatementProvider;

public class OrcasUpdateReplaceables extends Orcas
{
  public static void main( String[] pArgs )
  {
    new OrcasUpdateReplaceables().mainRun( pArgs );
  }

  @Override
  protected ParameterTypeMode getParameterTypeMode()
  {
    return ParameterTypeMode.ORCAS_UPDATE_REPLACEABLES;
  }

  @Override
  protected void run() throws Exception
  {
    final Map<String,List<String>> lDatabaseMap = new HashMap<String,List<String>>();
    final Map<String,String> lDatabaseDropMap = new HashMap<String,String>();

    new OrcasExtractReplaceables()
    {
      @Override
      protected boolean isCollectDataOnly()
      {
        return true;
      }

      @Override
      protected void handleCollectedData( String pFileName, byte[] pByteArray )
      {
        try
        {
          lDatabaseMap.put( pFileName, OrcasScriptRunner.parseReaderToLines( new InputStreamReader( new ByteArrayInputStream( pByteArray ), getParameters().getEncoding() ) ) );
          lDatabaseDropMap.put( pFileName, getDropStatementForFile( pFileName ) );
        }
        catch( IOException e )
        {
          throw new RuntimeException( e );
        }
      }

      private String getDropStatementForFile( String pFileName )
      {
        String lObjectName = pFileName.substring( pFileName.lastIndexOf( '/' ) + 1 );

        if( lObjectName.startsWith( "spec_" ) )
        {
          lObjectName = lObjectName.substring( "spec_".length() );
        }
        if( lObjectName.startsWith( "body_" ) )
        {
          lObjectName = lObjectName.substring( "body_".length() );
        }

        return "drop " + getType( pFileName ) + " " + lObjectName + ";";
      }

      private String getType( String pFileName )
      {
        String lFolder = pFileName.substring( 0, pFileName.indexOf( '/' ) );

        if( lFolder.equals( "views" ) )
        {
          return "view";
        }
        if( lFolder.equals( "functions" ) )
        {
          return "function";
        }
        if( lFolder.equals( "packages" ) )
        {
          return "package";
        }
        if( lFolder.equals( "procedures" ) )
        {
          return "procedure";
        }
        if( lFolder.equals( "types" ) )
        {
          return "type";
        }
        if( lFolder.equals( "triggers" ) )
        {
          return "trigger";
        }

        throw new IllegalArgumentException( "type unknwon: " + lFolder );
      }
    }.mainRun( getParameters() );

    final Map<String,List<String>> lFileMap = new HashMap<String,List<String>>();

    String lFilePrefix = new File( getParameters().getModelFile() ).toString();

    for( File lFile : FolderHandler.getModelFiles( getParameters() ) )
    {
      String lFilename = lFile.toString();

      lFilename = lFilename.substring( lFilePrefix.length() + 1 );
      lFilename = lFilename.replace( "\\", "/" );

      lFileMap.put( lFilename, OrcasScriptRunner.parseReaderToLines( new InputStreamReader( new FileInputStream( lFile ), getParameters().getEncoding() ) ) );
    }

    JdbcConnectionHandler.runWithCallableStatementProvider( getParameters(), new RunWithCallableStatementProvider()
    {
      public void run( CallableStatementProvider pCallableStatementProvider ) throws Exception
      {
        for( String lFilename : getSortedFileList( lFileMap ) )
        {
          List<String> lFileLines = lFileMap.get( lFilename );
          boolean lMissingInDb = !lDatabaseMap.containsKey( lFilename );
          if( lMissingInDb || !lDatabaseMap.get( lFilename ).equals( lFileLines ) )
          {
            if( lMissingInDb )
            {
              logInfo( "installing new: " + lFilename );
            }
            else
            {
              logInfo( "updating: " + lFilename );
            }

            new OrcasScriptRunner().runLines( lFileLines, pCallableStatementProvider, getParameters(), null );
            addSpoolfolderScriptIfNeeded( lFileLines, lFilename.replace( "/", "_" ) );
          }
          else
          {
            logInfo( "up to date: " + lFilename );
          }
        }

        for( String lFilename : getSortedFileList( lDatabaseMap ) )
        {
          if( !lFileMap.containsKey( lFilename ) )
          {
            logInfo( "dropping: " + lFilename );

            List<String> lFileLines = new ArrayList<String>();

            lFileLines.add( lDatabaseDropMap.get( lFilename ) );

            new OrcasScriptRunner().runLines( lFileLines, pCallableStatementProvider, getParameters(), null );
            addSpoolfolderScriptIfNeeded( lFileLines, "drop_" + lFilename.replace( "/", "_" ) );
          }
        }
      }

      private List<String> getSortedFileList( final Map<String,List<String>> lFileMap )
      {
        List<String> lReturn = new ArrayList<String>( lFileMap.keySet() );

        Collections.sort( lReturn );

        return lReturn;
      }
    } );
  }
}
