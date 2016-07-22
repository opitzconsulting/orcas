package de.opitzconsulting.orcas.diff;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FolderHandler
{
  private static Log _log = LogFactory.getLog( FolderHandler.class );

  public static List<File> getModelFiles( Parameters pParameters )
  {
    return getModelFilesRecursive( new File( pParameters.getModelFile() ), pParameters );
  }

  private static List<File> getModelFilesRecursive( File pFolder, Parameters pParameters )
  {
    if( !pFolder.isDirectory() )
    {
      return Collections.singletonList( pFolder );
    }

    List<File> lReturn = new ArrayList<File>();

    for( File lFile : pFolder.listFiles() )
    {
      if( lFile.isDirectory() )
      {
        if( pParameters.getScriptfolderrecursive() )
        {
          _log.debug( "subfolder: " + lFile );
          lReturn.addAll( getModelFilesRecursive( lFile, pParameters ) );
        }
        else
        {
          _log.debug( "skipping folder: " + lFile );
        }
      }
      else
      {
        if( lFile.getName().startsWith( pParameters.getScriptprefix() ) && lFile.getName().endsWith( pParameters.getScriptpostfix() ) )
        {
          _log.debug( "using file: " + lFile );
          lReturn.add( lFile );
        }
        else
        {
          _log.debug( "skipping file: " + lFile );
        }
      }
    }

    return lReturn;
  }
}
