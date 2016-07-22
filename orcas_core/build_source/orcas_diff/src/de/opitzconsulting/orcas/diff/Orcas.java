package de.opitzconsulting.orcas.diff;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Collections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.opitzconsulting.orcas.diff.Parameters.ParameterTypeMode;

public abstract class Orcas
{
  protected static Log _log;
  private Parameters _parameters;

  public void mainRun( String[] pArgs )
  {
    try
    {
      _parameters = new Parameters( pArgs, getParameterTypeMode() );

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

  protected void addSpoolfolderScriptIfNeeded( File pScriptFile )
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

        fileAppendLine( lSpoolLognameMainFile, "@@ " + pScriptFile.getName() );

        fileCopy( pScriptFile, new File( lSpoolLognamefolder, pScriptFile.getName() ) );
      }
    }
    catch( IOException e )
    {
      throw new RuntimeException( e );
    }
  }

  private void fileCopy( File pSrcFile, File pDstFile ) throws IOException
  {
    Files.copy( pSrcFile.toPath(), pDstFile.toPath(), StandardCopyOption.REPLACE_EXISTING );
  }

  private void fileAppendLine( File pFile, String pLine ) throws IOException
  {
    Files.write( pFile.toPath(), Collections.singletonList( pLine ), StandardOpenOption.APPEND );
  }
}
