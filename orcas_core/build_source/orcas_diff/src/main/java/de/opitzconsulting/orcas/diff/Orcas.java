package de.opitzconsulting.orcas.diff;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.opitzconsulting.orcas.diff.ParametersCommandline.ParameterTypeMode;

public abstract class Orcas
{
  protected static Log _log;
  private Parameters _parameters;

  protected void logInfo( String pLogMessage )
  {
    if( getParameters().getInfoLogHandler() != null )
    {
      getParameters().getInfoLogHandler().logInfo( pLogMessage );
    }
    else
    {
      _log.info( pLogMessage );
    }
  }

  public static void logError( String pLogMessage, Parameters pParameters )
  {
    if( pParameters.getInfoLogHandler() != null )
    {
      pParameters.getInfoLogHandler().logInfo( "error: " + pLogMessage );
    }
    else
    {
      if( _log != null )
      {
        _log.error( pLogMessage );
      }
      else
      {
        System.err.println( pLogMessage );
      }
    }
  }

  void mainRun( String[] pArgs )
  {
    mainRun( ParametersCommandline.parseFromCommandLine( pArgs, getParameterTypeMode() ) );
  }

  public void mainRun( Parameters pParameters )
  {
    try
    {
      _parameters = pParameters;

      _log = LogFactory.getLog( getLogName() );

      run();
    }
    catch( Exception e )
    {
      if( pParameters.isAbortJvmOnExit() )
      {
        if( _log != null )
        {
          _log.error( e, e );
        }
        else
        {
          e.printStackTrace();
        }
        System.exit( -1 );
      }
      else
      {
        throw new RuntimeException( e );
      }
    }
  }

  protected Parameters getParameters()
  {
    return _parameters;
  }

  protected DatabaseHandler getDatabaseHandler()
  {
    if( getParameters().getJdbcConnectParameters().getJdbcUrl().startsWith( "jdbc:mysql" ) || getParameters().getJdbcConnectParameters().getJdbcUrl().startsWith( "jdbc:mariadb" ) )
    {
      return new DatabaseHandlerMySql();
    }

    if( getParameters().getJdbcConnectParameters().getJdbcUrl().startsWith( "jdbc:hsqldb" ) )
    {
      return new DatabaseHandlerHsqlDb();
    }

    if( getParameters().getJdbcConnectParameters().getJdbcUrl().startsWith( "jdbc:h2" ) )
    {
      return new DatabaseHandlerH2();
    }

    if( getParameters().getJdbcConnectParameters().getJdbcUrl().startsWith( "jdbc:postgresql" ) )
    {
      return new DatabaseHandlerPostgres();
    }

    return new DatabaseHandlerOracle();
  }

  protected String getLogName()
  {
    return getParameters().isLognameSet() ? getParameters().getLogname() : getClass().getSimpleName();
  }

  protected abstract void run() throws Exception;

  protected abstract ParameterTypeMode getParameterTypeMode();

  public interface FileHandlerForLog
  {
    String getFilenameWithoutDirectory();

    void fileCopy( File pSpoolLognamefolder ) throws IOException;
  }

  protected void addSpoolfolderScriptIfNeeded( final File pScriptFile )
  {
    addSpoolfolderScriptIfNeeded( new FileHandlerForLog()
    {
      public String getFilenameWithoutDirectory()
      {
        return pScriptFile.getName();
      }

      public void fileCopy( File pSpoolLognamefolder ) throws IOException
      {
        Orcas.fileCopy( pScriptFile, getParameters().getEncoding(), new File( pSpoolLognamefolder, pScriptFile.getName() ), getParameters().getEncodingForSqlLog() );
      }
    } );
  }

  public static void deleteRecursive( File pFile )
  {
    if( pFile.isDirectory() )
    {
      for( String temp : pFile.list() )
      {
        deleteRecursive( new File( pFile, temp ) );
      }
    }

    pFile.delete();
  }

  protected void addSpoolfolderScriptIfNeeded( FileHandlerForLog pFileHandlerForLog )
  {
    try
    {
      if( getParameters().isSpoolfolderSet() )
      {
        if( !getParameters().isLognameSet() )
        {
          throw new RuntimeException( "cant use spoolfolder without logname" );
        }

        File lSpoolfolder = new File( getParameters().getSpoolfolder() );
        File lSpoolfolderMainFile = new File( lSpoolfolder, "master_install.sql" );

        if( !lSpoolfolder.exists() )
        {
          lSpoolfolder.mkdirs();
        }

        if( !lSpoolfolderMainFile.exists() )
        {
          lSpoolfolderMainFile.createNewFile();
        }

        File lSpoolLognamefolder = new File( lSpoolfolder, getParameters().getLogname() );
        String lSpoolLognameMainFileName = "install_" + getParameters().getLogname() + ".sql";
        File lSpoolLognameMainFile = new File( lSpoolLognamefolder, lSpoolLognameMainFileName );

        if( !lSpoolLognamefolder.exists() )
        {
          lSpoolLognamefolder.mkdir();
          lSpoolLognameMainFile.createNewFile();

          fileAppendLine( lSpoolfolderMainFile, "@@ " + getParameters().getLogname() + "/" + lSpoolLognameMainFile.getName() );
        }

        fileAppendLine( lSpoolLognameMainFile, "@@ " + pFileHandlerForLog.getFilenameWithoutDirectory() );

        pFileHandlerForLog.fileCopy( lSpoolLognamefolder );
      }
    }
    catch( IOException e )
    {
      throw new RuntimeException( e );
    }
  }

  protected void addSpoolfolderScriptIfNeeded( final URL pScriptURL, final String pFilename, Charset pCharset )
  {
    addSpoolfolderScriptIfNeeded( new FileHandlerForLog()
    {
      public String getFilenameWithoutDirectory()
      {
        return pFilename;
      }

      public void fileCopy( File pSpoolLognamefolder ) throws IOException
      {
        Orcas.fileCopy( pScriptURL, pCharset, new File( pSpoolLognamefolder, pFilename ), getParameters().getEncodingForSqlLog() );
      }
    } );
  }

  protected void addSpoolfolderScriptIfNeeded( final List<String> pLines, final String pFilename )
  {
    addSpoolfolderScriptIfNeeded( new FileHandlerForLog()
    {
      public String getFilenameWithoutDirectory()
      {
        return pFilename;
      }

      public void fileCopy( File pSpoolLognamefolder ) throws IOException
      {
        OutputStreamWriter lOutputStreamWriter = new OutputStreamWriter( new FileOutputStream( new File( pSpoolLognamefolder, pFilename ) ), getParameters().getEncodingForSqlLog() );

        for( String lLine : pLines )
        {
          lOutputStreamWriter.append( lLine + "\n" );
        }

        lOutputStreamWriter.close();
      }
    } );
  }

  private static void fileCopy( File pSrcFile, Charset pSrcCharset, File pDstFile, Charset pDstCharset ) throws IOException
  {
    writeLinesToFile( pDstFile, pDstCharset, OrcasScriptRunner.parseReaderToLines( new InputStreamReader( new FileInputStream( pSrcFile ), pSrcCharset ) ) );
  }

  private static void fileCopy( URL pSrcFile, Charset pSrcCharset, File pDstFile, Charset pDstCharset ) throws IOException
  {
    writeLinesToFile( pDstFile, pDstCharset, OrcasScriptRunner.parseReaderToLines( new InputStreamReader( pSrcFile.openStream(), pSrcCharset ) ) );
  }

  @SuppressWarnings( "resource" )
  private static void writeLinesToFile( File pDstFile, Charset pDstCharset, List<String> pLines ) throws FileNotFoundException, IOException
  {
    OutputStreamWriter lOutputStreamWriter = new OutputStreamWriter( new FileOutputStream( pDstFile ), pDstCharset );

    pLines.forEach( p ->
    {
      try
      {
        lOutputStreamWriter.append( p );
        lOutputStreamWriter.append( "\n" );
      }
      catch( IOException e )
      {
        throw new RuntimeException( e );
      }
    } );

    lOutputStreamWriter.close();
  }

  private void fileAppendLine( File pFile, String pLine ) throws IOException
  {
    List<String> lLines;
    if( pFile.exists() )
    {
      lLines = OrcasScriptRunner.parseReaderToLines( new InputStreamReader( new FileInputStream( pFile ), getParameters().getEncodingForSqlLog() ) );
      lLines.add( pLine );
    }
    else
    {
      lLines = Collections.singletonList( pLine );
    }

    writeLinesToFile( pFile, getParameters().getEncodingForSqlLog(), lLines );
  }

  protected void setParameters( Parameters pParameters )
  {
    _parameters = pParameters;
  }
}
