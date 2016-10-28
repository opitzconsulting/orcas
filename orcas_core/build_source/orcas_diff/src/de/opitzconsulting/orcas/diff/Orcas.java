package de.opitzconsulting.orcas.diff;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
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
  }

  protected Parameters getParameters()
  {
    return _parameters;
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
        Orcas.fileCopy( pScriptFile, new File( pSpoolLognamefolder, pScriptFile.getName() ) );
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

  protected void addSpoolfolderScriptIfNeeded( final URL pScriptURL, final String pFilename )
  {
    addSpoolfolderScriptIfNeeded( new FileHandlerForLog()
    {
      public String getFilenameWithoutDirectory()
      {
        return pFilename;
      }

      public void fileCopy( File pSpoolLognamefolder ) throws IOException
      {
        Orcas.fileCopy( pScriptURL, new File( pSpoolLognamefolder, pFilename ) );
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
        OutputStreamWriter lOutputStreamWriter = new OutputStreamWriter( new FileOutputStream( new File( pSpoolLognamefolder, pFilename ) ) );

        for( String lLine : pLines )
        {
          lOutputStreamWriter.append( lLine + "\n" );
        }

        lOutputStreamWriter.close();
      }
    } );
  }

  private static void fileCopy( File pSrcFile, File pDstFile ) throws IOException
  {
    Files.copy( pSrcFile.toPath(), pDstFile.toPath(), StandardCopyOption.REPLACE_EXISTING );
  }

  private static void fileCopy( URL pSrcFile, File pDstFile ) throws IOException
  {
    Files.copy( pSrcFile.openStream(), pDstFile.toPath(), StandardCopyOption.REPLACE_EXISTING );
  }

  private void fileAppendLine( File pFile, String pLine ) throws IOException
  {
    Files.write( pFile.toPath(), Collections.singletonList( pLine ), StandardOpenOption.APPEND );
  }

  protected void setParameters( Parameters pParameters )
  {
    _parameters = pParameters;
  }
}
