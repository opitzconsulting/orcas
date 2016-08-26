package de.opitzconsulting.orcas.diff;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import de.opitzconsulting.orcas.diff.Parameters.ParameterTypeMode;
import de.opitzconsulting.orcas.sql.CallableStatementProvider;
import de.opitzconsulting.orcas.sql.WrapperExecuteUpdate;
import de.opitzconsulting.orcas.sql.WrapperIteratorResultSet;

public class OrcasScriptRunner extends Orcas
{
  public static void main( String[] pArgs )
  {
    new OrcasScriptRunner().mainRun( pArgs );
  }

  @Override
  protected ParameterTypeMode getParameterTypeMode()
  {
    return ParameterTypeMode.ORCAS_SCRIPT;
  }

  @Override
  protected void run()
  {
    _log.info( "execute script start: " + getParameters().getModelFile() );

    CallableStatementProvider lCallableStatementProvider = JdbcConnectionHandler.createCallableStatementProvider( getParameters() );

    for( File lFile : FolderHandler.getModelFiles( getParameters() ) )
    {
      try
      {
        runFile( lFile, lCallableStatementProvider, getParameters() );
        addSpoolfolderScriptIfNeeded( lFile );
      }
      catch( Exception e )
      {
        throw new RuntimeException( e );
      }
    }

    _log.debug( "execute script done" );
  }

  private String doReplace( String pValue, Parameters pParameters )
  {
    String lOrig = null;

    for( int i = 0; i < pParameters.getAdditionalParameters().size(); i++ )
    {
      String lParameter = pParameters.getAdditionalParameters().get( i );

      String lKey = "&" + (i + 1);
      if( pValue.contains( lKey ) )
      {
        lOrig = pValue;

        pValue = pValue.replace( lKey + ".", lParameter );
        pValue = pValue.replace( lKey, lParameter );
      }
    }

    if( lOrig != null )
    {
      _log.debug( "replaced: " + lOrig + " with: " + pValue );
    }

    return pValue;
  }

  private CommandHandler findCommandHandler( String pTrimedLine, List<CommandHandler> pCommandHandlerList )
  {
    for( CommandHandler lCommandHandler : pCommandHandlerList )
    {
      if( lCommandHandler.isCommand( pTrimedLine ) )
      {
        return lCommandHandler;
      }
    }

    return null;
  }

  private void runFile( File pFile, final CallableStatementProvider pCallableStatementProvider, final Parameters pParameters ) throws Exception
  {
    _log.debug( "execute script: " + pFile + " " + pParameters.getAdditionalParameters() );

    BufferedReader lBufferedReader = new BufferedReader( new InputStreamReader( new FileInputStream( pFile ) ) );

    List<String> lLines = new ArrayList<String>();

    boolean lHasPlSqlModeTerminator = false;

    String lFileLine;
    while( (lFileLine = lBufferedReader.readLine()) != null )
    {
      lLines.add( lFileLine );

      if( lFileLine.trim().toLowerCase().equals( "/" ) )
      {
        lHasPlSqlModeTerminator = true;
      }
    }
    lBufferedReader.close();

    boolean lPlSqlMode = false;
    boolean lNonPlSqlMultilineMode = false;

    SpoolHandler lSpoolHandler = new SpoolHandler( pParameters );

    List<CommandHandler> lCommandHandlerList = new ArrayList<OrcasScriptRunner.CommandHandler>();

    lCommandHandlerList.add( lSpoolHandler );
    lCommandHandlerList.add( new PromptHandler( lSpoolHandler ) );
    lCommandHandlerList.add( new CommandHandler()
    {
      public boolean isCommand( String pTrimedLine )
      {
        return pTrimedLine.startsWith( "set " ) || pTrimedLine.startsWith( "quit" );
      }

      public void handleCommand( String pLine, File pCurrentFile ) throws Exception
      {
        _log.debug( "ignoring: " + pLine );
      }
    } );
    lCommandHandlerList.add( new StartHandler( pParameters, pCallableStatementProvider ) );

    StringBuffer lCurrent = new StringBuffer();
    for( String lLine : lLines )
    {
      boolean lCurrentEnd = false;
      String lAppend = null;
      String lTrimedLine = lLine.trim().toLowerCase();

      if( lPlSqlMode )
      {
        if( lTrimedLine.equals( "/" ) )
        {
          lCurrentEnd = true;
          lPlSqlMode = false;
        }
        else
        {
          lAppend = lLine;
        }
      }
      else
      {
        CommandHandler lCommandHandler = findCommandHandler( lTrimedLine, lCommandHandlerList );

        if( !lNonPlSqlMultilineMode && lCommandHandler != null )
        {
          lCommandHandler.handleCommand( lTrimedLine, pFile );
        }
        else
        {
          if( lTrimedLine.startsWith( "--" ) )
          {
          }
          else
          {
            if( lTrimedLine.endsWith( ";" ) )
            {
              int lIndex = lLine.lastIndexOf( ';' );

              lAppend = lLine.substring( 0, lIndex );

              lCurrentEnd = true;
              lNonPlSqlMultilineMode = false;
            }
            else
            {
              if( lHasPlSqlModeTerminator && (lTrimedLine.startsWith( "create " ) || lTrimedLine.startsWith( "replace " ) || lTrimedLine.startsWith( "begin" ) || lTrimedLine.startsWith( "declare" )) )
              {
                lPlSqlMode = true;
                lAppend = lLine;
              }
              else
              {
                boolean lEmptyLine = lTrimedLine.equals( "" );

                if( lNonPlSqlMultilineMode )
                {
                  lAppend = lLine;
                }
                else
                {
                  if( !lEmptyLine )
                  {
                    lNonPlSqlMultilineMode = true;
                    lAppend = lLine;
                  }
                }
              }
            }
          }
        }
      }

      if( lAppend != null )
      {
        if( lCurrent == null )
        {
          lCurrent = new StringBuffer();
        }
        else
        {
          lCurrent.append( "\n" );
        }

        lCurrent.append( doReplace( lAppend, pParameters ) );
      }

      if( lCurrentEnd )
      {
        if( isSelect( lCurrent.toString() ) )
        {
          executeSelect( lCurrent.toString(), lSpoolHandler, pCallableStatementProvider );
        }
        else
        {
          executeSql( lCurrent.toString(), pCallableStatementProvider );
        }

        lCurrent = null;
      }
    }

    if( lCurrent != null && !lCurrent.toString().trim().equals( "" ) )
    {
      _log.error( "statemmet not terminated correctly: " + lCurrent.toString() );
    }

    lSpoolHandler.spoolHandleFileEnd();
  }

  private void executeSql( String pSql, CallableStatementProvider pCallableStatementProvider )
  {
    try
    {
      new WrapperExecuteUpdate( pSql, pCallableStatementProvider ).execute();
    }
    catch( RuntimeException e )
    {
      switch( getParameters().getFailOnErrorMode() )
      {
        case NEVER:
          _log.warn( e, e );
          return;
        case ALWAYS:
          throw e;
        case IGNORE_DROP:
          if( pSql.toLowerCase().trim().startsWith( "drop " ) )
          {
            _log.info( "ignoring: " + e.getMessage().trim() + " [" + pSql.trim() + "]" );
            return;
          }
          else
          {
            throw e;
          }
      }
    }
  }

  private void executeSelect( String pSql, final SpoolHandler pSpoolHandler, CallableStatementProvider pCallableStatementProvider )
  {
    final int[] lRowIndex = new int[] { 0 };

    new WrapperIteratorResultSet( pSql, pCallableStatementProvider )
    {
      @Override
      protected void useResultSetRow( ResultSet pResultSet ) throws SQLException
      {
        int lColumnCount = pResultSet.getMetaData().getColumnCount();

        StringBuilder lLine = new StringBuilder();

        for( int i = 0; i < lColumnCount; i++ )
        {
          if( i > 0 )
          {
            lLine.append( ", " );
          }

          Object lObject = pResultSet.getObject( i + 1 );

          if( lObject == null )
          {
            lLine.append( "" );
          }
          else
          {
            lLine.append( lObject );
          }
        }

        pSpoolHandler.spoolIfActive( lLine.toString() );

        lRowIndex[0]++;

        _log.info( "select [" + lRowIndex[0] + "]: " + lLine );
      }
    }.execute();

    _log.info( "select rowcount: " + lRowIndex[0] );
  }

  private boolean isSelect( String pSql )
  {
    return pSql.toLowerCase().trim().startsWith( "select " );
  }

  private interface CommandHandler
  {
    public boolean isCommand( String pTrimedLine );

    public void handleCommand( String pLine, File pCurrentFile ) throws Exception;
  }

  private class SpoolHandler implements CommandHandler
  {
    private FileOutputStream spoolFile;
    private Writer writer;
    private Parameters parameters;

    public SpoolHandler( Parameters pParameters )
    {
      parameters = pParameters;
    }

    public boolean isCommand( String pTrimedLine )
    {
      return pTrimedLine.startsWith( "spool " );
    }

    public void spoolIfActive( String pLine )
    {
      if( isSpoolActive() )
      {
        try
        {
          writer.append( pLine );
          writer.append( "\n" );
        }
        catch( IOException e )
        {
          throw new RuntimeException( e );
        }
      }
    }

    public void spoolHandleFileEnd() throws Exception
    {
      if( isSpoolActive() )
      {
        _log.error( "spooling still active" );

        closeSpoolFile();
      }
    }

    public void handleCommand( String pLine, File pCurrentFile ) throws Exception
    {
      String lTrimedLine = pLine.toLowerCase().trim();
      String lFileName = doReplace( lTrimedLine.substring( "spool ".length() ), parameters ).trim();

      lFileName = lFileName.replace( ";", "" );

      if( lFileName.equals( "off" ) )
      {
        if( !isSpoolActive() )
        {
          _log.error( "spooling not active: " + lTrimedLine );
        }
        else
        {
          closeSpoolFile();
        }
      }
      else
      {
        if( isSpoolActive() )
        {
          _log.warn( "spooling already active: " + lTrimedLine );
          closeSpoolFile();
        }

        openSpoolFile( lFileName );
      }
    }

    private void openSpoolFile( String pFileName ) throws FileNotFoundException
    {
      _log.info( "start spooling: " + pFileName );
      File lFile = new File( pFileName );

      if( lFile.getParentFile() != null )
      {
        lFile.getParentFile().mkdirs();
      }

      spoolFile = new FileOutputStream( lFile );
      writer = new OutputStreamWriter( spoolFile );
    }

    private boolean isSpoolActive()
    {
      return spoolFile != null;
    }

    private void closeSpoolFile() throws IOException
    {
      _log.info( "stop spooling" );
      writer.close();
      writer = null;
      spoolFile.close();
      spoolFile = null;
    }
  }

  private class PromptHandler implements CommandHandler
  {
    private static final String PROMPT = "prompt ";
    private SpoolHandler spoolHandler;

    public PromptHandler( SpoolHandler pSpoolHandler )
    {
      spoolHandler = pSpoolHandler;
    }

    public boolean isCommand( String pTrimedLine )
    {
      return pTrimedLine.startsWith( PROMPT );
    }

    public void handleCommand( String pLine, File pCurrentFile ) throws Exception
    {
      String lTrimedLine = pLine.trim();

      lTrimedLine = lTrimedLine.substring( PROMPT.length(), lTrimedLine.length() );

      if( lTrimedLine.endsWith( ";" ) )
      {
        lTrimedLine = lTrimedLine.substring( 0, lTrimedLine.length() - 1 );
      }

      _log.info( lTrimedLine );
      spoolHandler.spoolIfActive( lTrimedLine );
    }
  }

  private class StartHandler implements CommandHandler
  {
    private Parameters parameters;
    private CallableStatementProvider callableStatementProvider;

    public StartHandler( Parameters pParameters, CallableStatementProvider pCallableStatementProvider )
    {
      parameters = pParameters;
      callableStatementProvider = pCallableStatementProvider;
    }

    public boolean isCommand( String pTrimedLine )
    {
      return pTrimedLine.startsWith( "@" );
    }

    public void handleCommand( String pLine, File pCurrentFile ) throws Exception
    {
      File lFile;

      String lTrimLine = pLine.trim();

      if( lTrimLine.startsWith( "@@" ) )
      {
        lFile = new File( pCurrentFile.getParent(), doReplace( lTrimLine.substring( 2 ).trim(), parameters ) );
      }
      else
      {
        lFile = new File( doReplace( lTrimLine.substring( 1 ).trim(), parameters ) );
      }

      runFile( lFile, callableStatementProvider, parameters );
    }
  }
}