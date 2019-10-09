package de.opitzconsulting.orcas.diff;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.opitzconsulting.orcas.dbobjects.SqlplusDirAccessDbobjects;

import de.opitzconsulting.orcas.diff.JdbcConnectionHandler.RunWithCallableStatementProvider;
import de.opitzconsulting.orcas.diff.Parameters.InfoLogHandler;
import de.opitzconsulting.orcas.diff.ParametersCommandline.ParameterTypeMode;
import de.opitzconsulting.orcas.sql.CallableStatementProvider;
import de.opitzconsulting.orcas.sql.WrapperIteratorResultSet;

public class OrcasExtractReplaceables extends Orcas
{
  public static void main( String[] pArgs )
  {
    new OrcasExtractReplaceables().mainRun( pArgs );
  }

  private static void streamCopy( Reader pReader, Writer pWriter ) throws IOException
  {
    char[] lBuffer = new char[32000];

    int lCurrentCahrCount = 0;

    while( -1 != (lCurrentCahrCount = pReader.read( lBuffer )) )
    {
      pWriter.write( lBuffer, 0, lCurrentCahrCount );
    }
  }

  @Override
  protected ParameterTypeMode getParameterTypeMode()
  {
    return ParameterTypeMode.ORCAS_EXTRACT_REPLACEABLES;
  }

  protected boolean isCollectDataOnly()
  {
    return false;
  }

  @Override
  protected void run() throws Exception
  {
    if( !isCollectDataOnly() )
    {
      deleteRecursive( new File( getParameters().getSpoolfolder() ) );
    }

    final boolean lFullMode = getParameters().getViewExtractMode().equals( "full" );

    JdbcConnectionHandler.runWithCallableStatementProvider( getParameters(), new RunWithCallableStatementProvider()
    {
      public void run( CallableStatementProvider pCallableStatementProvider ) throws Exception
      {
        getParameters().setRemovePromptPrefix( "LINE_BEGIN" );

        extract( pCallableStatementProvider, "triggers", null, "TRIGGER", getParameters().getExcludewheretrigger() );
        extract( pCallableStatementProvider, "types", "spec_", "TYPE", getParameters().getExcludewhereobjecttype() );
        extract( pCallableStatementProvider, "types", "body_", "TYPE_BODY", getParameters().getExcludewhereobjecttype() );
        extract( pCallableStatementProvider, "packages", "spec_", "PACKAGE", getParameters().getExcludewherepackage() );
        extract( pCallableStatementProvider, "packages", "body_", "PACKAGE_BODY", getParameters().getExcludewherepackage() );
        extract( pCallableStatementProvider, "functions", null, "FUNCTION", getParameters().getExcludewherefunction() );
        extract( pCallableStatementProvider, "procedures", null, "PROCEDURE", getParameters().getExcludewhereprocedure() );

        String lSql;
        if( lFullMode )
        {
          lSql = "select lower(view_name), dbms_metadata.get_ddl('VIEW',view_name) from user_views";
        }
        else
        {
          lSql = "select lower(view_name), text from user_views";
        }

        lSql += " where view_name in (select object_name from user_objects where object_type = 'VIEW' and not(" + getParameters().getExcludewhereview() + "))";

        final String lExistingFolderString = getExistingFolderString( "views" );
        if( !isCollectDataOnly() )
        {
          logInfo( "writing VIEWS to: " + lExistingFolderString );
        }

        new WrapperIteratorResultSet( lSql, pCallableStatementProvider )
        {
          @Override
          protected void useResultSetRow( ResultSet pResultSet ) throws SQLException
          {
            try
            {
              OutputStream lOutputStream;

              String lViewName = pResultSet.getString( 1 );
              String lFileName = lExistingFolderString + "/" + lViewName + ".sql";

              if( isCollectDataOnly() )
              {
                lOutputStream = new ByteArrayOutputStream();
              }
              else
              {
                lOutputStream = new FileOutputStream( lFileName );
              }

              OutputStreamWriter lOutputStreamWriter = new OutputStreamWriter( lOutputStream, getParameters().getEncoding() );

              if( !lFullMode )
              {
                lOutputStreamWriter.write( "create or replace force view " + lViewName + " as\n" );
              }

              streamCopy( pResultSet.getCharacterStream( 2 ), lOutputStreamWriter );

              lOutputStreamWriter.write( "\n/" );

              lOutputStreamWriter.flush();
              lOutputStream.flush();

              if( isCollectDataOnly() )
              {
                handleCollectedData( lFileName, ((ByteArrayOutputStream) lOutputStream).toByteArray() );
              }
            }
            catch( IOException e )
            {
              throw new RuntimeException( e );
            }
          }
        }.execute();
      }

      private String getExistingFolderString( String pFolderPostfix )
      {
        if( isCollectDataOnly() )
        {
          return pFolderPostfix;
        }
        else
        {
          File lFolder = new File( getParameters().getSpoolfolder() + "/" + pFolderPostfix );
          lFolder.mkdirs();
          return lFolder.toString();
        }
      }

      private void extract( CallableStatementProvider pCallableStatementProvider, String pFolderPostfix, String pFilePrefix, String pType, String pExcludeWhere ) throws Exception
      {
        String lExistingFolderString = getExistingFolderString( pFolderPostfix );

        if( !isCollectDataOnly() )
        {
          logInfo( "writing " + pType + " to: " + lExistingFolderString );
        }

        InfoLogHandler lOriginalInfoLogHandler = getParameters().getInfoLogHandler();

        getParameters().setInfoLogHandler( new InfoLogHandler()
        {
          public void logInfo( String pLogMessage )
          {
            _log.debug( pLogMessage );
          }
        } );

        final String lDummyFileName = "DUMMYFILE";

        String lFilePrefix = pFilePrefix == null ? "" : pFilePrefix;

        new OrcasScriptRunner()
        {
          byte[] _dummyFileContent;

          protected SpoolHandler createSpoolHandler( Parameters pParameters )
          {
            return new SpoolHandler( pParameters )
            {
              private ByteArrayOutputStream _out;
              private String fileNameCollectData;

              protected void openSpoolFile( String pFileName ) throws java.io.FileNotFoundException
              {
                if( pFileName.equals( lDummyFileName ) || isCollectDataOnly() )
                {
                  if( !pFileName.equals( lDummyFileName ) )
                  {
                    fileNameCollectData = pFileName;
                  }
                  _out = new ByteArrayOutputStream();
                  writer = new OutputStreamWriter( _out, pParameters.getEncoding() );
                }
                else
                {
                  super.openSpoolFile( pFileName );
                }
              }

              protected void closeSpoolFile() throws IOException
              {
                if( _out != null )
                {
                  writer.close();
                  writer = null;
                  if( fileNameCollectData != null )
                  {
                    handleCollectedData( fileNameCollectData, _out.toByteArray() );
                  }
                  else
                  {
                    _dummyFileContent = _out.toByteArray();
                  }
                  _out = null;
                }
                else
                {
                  super.closeSpoolFile();
                }
              }
            };
          }

          protected StartHandler createStartHandler( final CallableStatementProvider pCallableStatementProvider, final Parameters pParameters, final SpoolHandler pSpoolHandler )
          {
            return new StartHandler( pParameters, pCallableStatementProvider, pSpoolHandler )
            {
              @Override
              public void handleCommand( String pLine, File pCurrentFile ) throws Exception
              {
                if( pLine.equals( "@&1" ) )
                {
                  runReader( new InputStreamReader( new ByteArrayInputStream( _dummyFileContent ), pParameters.getEncoding() ), pCallableStatementProvider, pParameters, null, pSpoolHandler );
                }
                else
                {
                  super.handleCommand( pLine, pCurrentFile );
                }
              }
            };
          }
        }.runURL( SqlplusDirAccessDbobjects.getURL_extract_replaceables_sources(), pCallableStatementProvider, getParameters(), StandardCharsets.UTF_8, lDummyFileName, lExistingFolderString + "/" + lFilePrefix, pType, pExcludeWhere );

        getParameters().setInfoLogHandler( lOriginalInfoLogHandler );
      }
    } );
  }

  protected void handleCollectedData( String pFileName, byte[] pByteArray )
  {
    throw new UnsupportedOperationException();
  }
}
