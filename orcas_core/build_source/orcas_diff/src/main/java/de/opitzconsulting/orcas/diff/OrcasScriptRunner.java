package de.opitzconsulting.orcas.diff;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import de.opitzconsulting.orcas.diff.JdbcConnectionHandler.RunWithCallableStatementProvider;
import de.opitzconsulting.orcas.diff.ParametersCommandline.ParameterTypeMode;
import de.opitzconsulting.orcas.sql.CallableStatementProvider;
import de.opitzconsulting.orcas.sql.WrapperCallableStatement;
import de.opitzconsulting.orcas.sql.WrapperExecuteUpdate;
import de.opitzconsulting.orcas.sql.WrapperIteratorResultSet;

public class OrcasScriptRunner extends Orcas
{
  public static final String ORCAS_UPDATES_TABLE = "orcas_updates";

  public static void main( String[] pArgs )
  {
    new OrcasScriptRunner().mainRun( pArgs );
  }

  private boolean isInMemorySpoolFile( String pFileName )
  {
    return pFileName.startsWith( "inmemory:" );
  }

  @Override
  protected ParameterTypeMode getParameterTypeMode()
  {
    return ParameterTypeMode.ORCAS_SCRIPT;
  }

  @Override
  protected void run() throws Exception
  {
    if( getParameters().isOneTimeScriptMode() )
    {
      logInfo( "execute one-time-script start" + getLogMessageFileDetail() );
    }
    else
    {
      if( getParameters().getScriptUrl() == null )
      {
        logInfo( "execute script start" + getLogMessageFileDetail() );
      }
    }

    JdbcConnectionHandler.runWithCallableStatementProvider( getParameters(), new RunWithCallableStatementProvider()
    {
      public void run( final CallableStatementProvider pCallableStatementProvider ) throws Exception
      {
        RunWithCallableStatementProvider lRunWithCallableStatementProvider = new RunWithCallableStatementProvider()
        {
          public void run( final CallableStatementProvider pOrcasCallableStatementProvider ) throws Exception
          {
            final Map<String, Date> lExecutedFilesMap = new HashMap<String, Date>();

            if( getParameters().isOneTimeScriptMode() )
            {
              new WrapperIteratorResultSet( "select scup_script_name, scup_date from " + ORCAS_UPDATES_TABLE + " where scup_logname = ?", pOrcasCallableStatementProvider, Collections.singletonList( getParameters().getLogname() ) )
              {
                protected void useResultSetRow( ResultSet pResultSet ) throws SQLException
                {
                  lExecutedFilesMap.put( pResultSet.getString( "scup_script_name" ), new Date( pResultSet.getTimestamp( "scup_date" ).getTime() ) );
                }

                protected boolean handleSQLException( SQLException pSQLException )
                {
                  getDatabaseHandler().createOrcasUpdatesTable( ORCAS_UPDATES_TABLE, pOrcasCallableStatementProvider );

                  return true;
                }
              }.execute();
            }

            try
            {
              if( getParameters().getScriptUrl() != null )
              {
                runURL( getParameters().getScriptUrl(), pCallableStatementProvider, getParameters(), getParameters().getScriptUrlCharset() );
                addSpoolfolderScriptIfNeeded( getParameters().getScriptUrl(), getParameters().getScriptUrlFilename(), getParameters().getScriptUrlCharset() );
              }
              else
              {

                for( File lFile : FolderHandler.getModelFiles( getParameters() ) )
                {
                  if( getParameters().isOneTimeScriptMode() )
                  {
                    String lScriptfolderAbsolutePath = new File( getParameters().getModelFile() ).getAbsolutePath();
                    String lFileAbsolutePath = lFile.getAbsolutePath();

                    String lFilePart = lFileAbsolutePath.substring( lScriptfolderAbsolutePath.length() );
                    lFilePart = lFilePart.replace( "\\", "/" );
                    lFilePart = lFilePart.substring( 1 );

                    if( !lExecutedFilesMap.containsKey( lFilePart ) )
                    {
                      if( !getParameters().isOneTimeScriptLogonlyMode() )
                      {
                        logInfo( "execute one-time-script " + lFilePart );
                        runFile( lFile, pCallableStatementProvider, getParameters(), null );
                        addSpoolfolderScriptIfNeeded( lFile );
                      }

                      getDatabaseHandler().insertIntoOrcasUpdatesTable( ORCAS_UPDATES_TABLE, pOrcasCallableStatementProvider, lFilePart, getParameters().getLogname() );
                    }
                    else
                    {
                      _log.debug( "Script already executed: " + lFilePart + " on: " + lExecutedFilesMap.get( lFilePart ) );
                    }
                  }
                  else
                  {
                    runFile( lFile, pCallableStatementProvider, getParameters(), null );
                    addSpoolfolderScriptIfNeeded( lFile );
                  }
                }
              }
            }
            catch( Exception e )
            {
              throw new RuntimeException( e );
            }

            _log.debug( "execute script done" );
          }
        };

        if( !getParameters().isOneTimeScriptMode() )
        {
          lRunWithCallableStatementProvider.run( null );
        }
        else
        {
          JdbcConnectionHandler.runWithCallableStatementProvider( getParameters(), getParameters().getOrcasJdbcConnectParameters(), lRunWithCallableStatementProvider );
        }
      }
    } );
  }

  private String getLogMessageFileDetail()
  {
    return getParameters().getModelFiles() == null ? (": " + getParameters().getModelFile()) : "";
  }

  public void runURL( URL pURL, CallableStatementProvider pCallableStatementProvider, Parameters pParameters, Charset pCharset ) throws Exception
  {
    runReader( new InputStreamReader( pURL.openStream(), pCharset ), pCallableStatementProvider, pParameters, null, null );
  }

  public void runURL( URL pURL, CallableStatementProvider pCallableStatementProvider, Parameters pParameters, Charset pCharset, String... pAdditionalParameters ) throws Exception
  {
    setParameters( pParameters );

    List<String> lAdditionalParameters = new ArrayList<String>();

    for( String lAdditionalParameter : pAdditionalParameters )
    {
      lAdditionalParameters.add( lAdditionalParameter );
    }

    List<String> lOriginalAdditionalParameters = pParameters._additionalParameters;

    pParameters._additionalParameters = lAdditionalParameters;

    try
    {
      runURL( pURL, pCallableStatementProvider, pParameters, pCharset );
    }
    finally
    {
      pParameters._additionalParameters = lOriginalAdditionalParameters;
    }
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

  private void runFile( File pFile, final CallableStatementProvider pCallableStatementProvider, final Parameters pParameters, SpoolHandler pSpoolHandler ) throws Exception
  {
    if( pParameters.getAdditionalParameters().isEmpty() )
    {
      _log.debug( "execute script: " + pFile );
    }
    else
    {
      _log.debug( "execute script: " + pFile + " " + pParameters.getAdditionalParameters() );
    }

    runReader( new InputStreamReader( new FileInputStream( pFile ), pParameters.getEncoding() ), pCallableStatementProvider, pParameters, pFile, pSpoolHandler );
  }

  static List<String> parseReaderToLines( Reader pReader ) throws IOException
  {
    BufferedReader lBufferedReader = new BufferedReader( pReader );

    List<String> lLines = new ArrayList<String>();

    String lFileLine;
    while( (lFileLine = lBufferedReader.readLine()) != null )
    {
      lLines.add( lFileLine );
    }
    lBufferedReader.close();

    return lLines;
  }

  private boolean _serveroutput = false;
  private Map<String, byte[]> inMemorySpoolFileMap = new HashMap<>();

  protected void runReader( Reader pReader, final CallableStatementProvider pCallableStatementProvider, final Parameters pParameters, File pFile, SpoolHandler pSpoolHandler ) throws Exception
  {
    runLines( parseReaderToLines( pReader ), pCallableStatementProvider, pParameters, pFile, pSpoolHandler );
  }

  void runLines( List<String> pLines, final CallableStatementProvider pCallableStatementProvider, final Parameters pParameters, File pFile ) throws Exception
  {
    runLines( pLines, pCallableStatementProvider, pParameters, pFile, null );
  }

  void runLines( List<String> pLines, final CallableStatementProvider pCallableStatementProvider, final Parameters pParameters, File pFile, SpoolHandler pSpoolHandler ) throws Exception
  {
    inMemorySpoolFileMap.clear();

    boolean lHasPlSqlModeTerminator = false;

    for( String lFileLine : pLines )
    {
      if( lFileLine.trim().toLowerCase().equals( "/" ) )
      {
        lHasPlSqlModeTerminator = true;
      }
    }

    boolean lPlSqlMode = false;
    boolean lNonPlSqlMultilineMode = false;

    final SpoolHandler lSpoolHandler = pSpoolHandler == null ? createSpoolHandler( pParameters ) : pSpoolHandler;

    List<CommandHandler> lCommandHandlerList = new ArrayList<OrcasScriptRunner.CommandHandler>();

    lCommandHandlerList.add( lSpoolHandler );
    lCommandHandlerList.add( new PromptHandler( lSpoolHandler ) );
    lCommandHandlerList.add( new CommandHandler()
    {
      public boolean isCommand( String pTrimedLine )
      {
        if( pTrimedLine.startsWith( "set" ) )
        {
          List<String> lCommands = new ArrayList<String>();

          StringTokenizer lStringTokenizer = new StringTokenizer( pTrimedLine, " \t" );
          while( lStringTokenizer.hasMoreTokens() )
          {
            lCommands.add( lStringTokenizer.nextToken() );
          }

          if( lCommands.size() >= 3 )
          {
            if( lCommands.get( 1 ).equalsIgnoreCase( "serveroutput" ) )
            {
              return true;
            }
          }
        }

        return false;
      }

      public void handleCommand( String pLine, File pCurrentFile ) throws Exception
      {
        List<String> lCommands = new ArrayList<String>();

        StringTokenizer lStringTokenizer = new StringTokenizer( pLine.trim(), " \t" );
        while( lStringTokenizer.hasMoreTokens() )
        {
          lCommands.add( lStringTokenizer.nextToken() );
        }

        _serveroutput = lCommands.get( 2 ).equalsIgnoreCase( "on" );
      }
    } );
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
    lCommandHandlerList.add( createStartHandler( pCallableStatementProvider, pParameters, lSpoolHandler ) );

    StringBuffer lCurrent = null;
    for( String lLine : pLines )
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
          lCommandHandler.handleCommand( lLine, pFile );
        }
        else
        {
          if( lTrimedLine.startsWith( "--" ) )
          {
        	  if (lCurrent != null) {
        		  lAppend = lLine;
        	  }
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
          executeSql( lCurrent.toString(), pCallableStatementProvider, pParameters );
        }

        if( _serveroutput )
        {
          final boolean[] keepRunning = new boolean[1];
          do
          {
            new WrapperCallableStatement( "begin dbms_output.get_line( ?, ?); end;", pCallableStatementProvider )
            {
              @Override
              protected void useCallableStatement( CallableStatement pCallableStatement ) throws SQLException
              {
                pCallableStatement.registerOutParameter( 1, java.sql.Types.VARCHAR );
                pCallableStatement.registerOutParameter( 2, java.sql.Types.NUMERIC );

                pCallableStatement.executeUpdate();

                BigDecimal lStatus = pCallableStatement.getBigDecimal( 2 );

                _log.debug( "serveroutput status: " + lStatus );
                keepRunning[0] = lStatus != null && lStatus.intValue() == 0;

                String lLine = pCallableStatement.getString( 1 );

                if( lLine != null )
                {
                  lSpoolHandler.spoolIfActive( lLine );

                  logInfo( "serveroutput: " + lLine );
                }
              }
            }.execute();
          } while( keepRunning[0] );
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

  protected StartHandler createStartHandler( final CallableStatementProvider pCallableStatementProvider, final Parameters pParameters, SpoolHandler pSpoolHandler )
  {
    return new StartHandler( pParameters, pCallableStatementProvider, pSpoolHandler );
  }

  protected SpoolHandler createSpoolHandler( Parameters pParameters )
  {
    return new SpoolHandler( pParameters );
  }

  private void executeSql( String pSql, CallableStatementProvider pCallableStatementProvider, Parameters pParameters )
  {
    try
    {
      new WrapperExecuteUpdate( pSql, pCallableStatementProvider ).execute();
    }
    catch( RuntimeException e )
    {
      switch( pParameters.getFailOnErrorMode() )
      {
        case NEVER:
          _log.warn( e, e );
          return;
        case ALWAYS:
          throw e;
        case IGNORE_DROP:
          if( pSql.toLowerCase().trim().startsWith( "drop " ) )
          {
            logInfo( "ignoring: " + e.getMessage().trim() + " [" + pSql.trim() + "]" );
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

        logInfo( "select [" + lRowIndex[0] + "]: " + lLine );
      }
    }.execute();

    if( lRowIndex[0] != 0 )
    {
      logInfo( "select rowcount: " + lRowIndex[0] );
    }
    else
    {
      _log.debug( "select rowcount: " + lRowIndex[0] );
    }
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

  class SpoolHandler implements CommandHandler
  {
    private OutputStream spoolFile;
    protected Writer writer;
    private Parameters parameters;
    private String inMemorySpoolFileName;

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

    protected void openSpoolFile( String pFileName ) throws FileNotFoundException
    {
      logInfo( "start spooling: " + pFileName );

      if( isInMemorySpoolFile( pFileName ) )
      {
        spoolFile = new ByteArrayOutputStream();
        writer = new OutputStreamWriter( spoolFile, getParameters().getEncodingForSqlLog() );
        inMemorySpoolFileName = pFileName;
      }
      else
      {
        File lFile = new File( pFileName );

        if( lFile.getParentFile() != null )
        {
          lFile.getParentFile().mkdirs();
        }

        spoolFile = new FileOutputStream( lFile );
        writer = new OutputStreamWriter( spoolFile, getParameters().getEncodingForSqlLog() );
      }
    }

    private boolean isSpoolActive()
    {
      return writer != null;
    }

    protected void closeSpoolFile() throws IOException
    {
      logInfo( "stop spooling" );
      writer.close();
      writer = null;
      spoolFile.close();
      if( inMemorySpoolFileName != null )
      {
        inMemorySpoolFileMap.put( inMemorySpoolFileName, ((ByteArrayOutputStream) spoolFile).toByteArray() );
        inMemorySpoolFileName = null;
      }
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

      if( getParameters().getRemovePromptPrefix() != null && lTrimedLine.startsWith( getParameters().getRemovePromptPrefix() ) )
      {
        lTrimedLine = lTrimedLine.substring( getParameters().getRemovePromptPrefix().length() );
      }

      logInfo( lTrimedLine );
      spoolHandler.spoolIfActive( lTrimedLine );
    }
  }

  class StartHandler implements CommandHandler
  {
    private Parameters parameters;
    private CallableStatementProvider callableStatementProvider;
    private SpoolHandler spoolHandler;

    public StartHandler( Parameters pParameters, CallableStatementProvider pCallableStatementProvider, SpoolHandler pSpoolHandler )
    {
      parameters = pParameters;
      callableStatementProvider = pCallableStatementProvider;
      spoolHandler = pSpoolHandler;
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
        String lFilename = doReplace( lTrimLine.substring( 1 ).trim(), parameters );

        if( isInMemorySpoolFile( lFilename ) && inMemorySpoolFileMap.containsKey( lFilename ) )
        {
          runReader( new InputStreamReader( new ByteArrayInputStream( inMemorySpoolFileMap.get( lFilename ) ), parameters.getEncodingForSqlLog() ), callableStatementProvider, parameters, null, spoolHandler );
          return;
        }
        else
        {
          lFile = new File( lFilename );
        }
      }

      runFile( lFile, callableStatementProvider, parameters, spoolHandler );
    }
  }
}
