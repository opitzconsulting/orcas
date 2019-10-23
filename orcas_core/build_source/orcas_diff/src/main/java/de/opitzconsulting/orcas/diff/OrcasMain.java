package de.opitzconsulting.orcas.diff;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import de.opitzconsulting.orcas.diff.DiffAction.Statement;
import de.opitzconsulting.orcas.diff.DiffReasonKey.DiffReasonEntity;
import de.opitzconsulting.orcas.diff.ExecuteSqlErrorHandler.ExecuteSqlErrorHandlerCallback;
import de.opitzconsulting.orcas.diff.JdbcConnectionHandler.RunWithCallableStatementProvider;
import de.opitzconsulting.orcas.diff.OrcasDiff.DiffResult;
import de.opitzconsulting.orcas.diff.ParametersCommandline.ParameterTypeMode;
import de.opitzconsulting.orcas.orig.diff.DiffRepository;
import de.opitzconsulting.orcas.sql.CallableStatementProvider;

public class OrcasMain extends Orcas
{
  public static void main( String[] pArgs )
  {
    new OrcasMain().mainRun( pArgs );
  }

  @Override
  protected ParameterTypeMode getParameterTypeMode()
  {
    return ParameterTypeMode.ORCAS_MAIN;
  }

  @Override
  protected void run() throws Exception
  {
    if( getParameters().getSrcJdbcConnectParameters() != null )
    {
      doSchemaSync( getParameters() );
    }
    else
    {
      Consumer<CallableStatementProvider> lStaticsHandler = (CallableStatementProvider pCallableStatementProvider) -> {
        InitDiffRepository.init( pCallableStatementProvider, getDatabaseHandler(), getParameters() );

        logInfo( "starting orcas statics" );

        if( getParameters().getTargetplsql().equals( "" ) )
        {
          de.opitzconsulting.origOrcasDsl.Model lDatabaseModel;
          if( getParameters().getSchemaFiles() == null ) {
            logInfo("loading database");
            lDatabaseModel = getDatabaseHandler().createLoadIst(pCallableStatementProvider, getParameters()).loadModel(true);
          } else {
            logInfo("loading schema-files");
            ParametersCall lParametersCall = ParametersCall.createWithDefaults();
            lParametersCall.setModelFiles(getParameters().getSchemaFiles());
            lParametersCall.setModelFile("");
            getParameters().getExtensionHandler().setParameters(lParametersCall);
            lDatabaseModel = getParameters().getExtensionHandler().loadModel();
            getParameters().getExtensionHandler().setParameters(getParameters());
          }

          DiffRepository.getModelMerge().cleanupValues( lDatabaseModel );
          de.opitzconsulting.origOrcasDsl.Model lSollModel = getParameters().getExtensionHandler().loadModel();

          logInfo( "building diff" );
          lSollModel = modifyModel( lSollModel );

          _log.debug( "cleanupValues" );
          DiffRepository.getModelMerge().cleanupValues( lSollModel );

          DiffResult lDiffResult = new OrcasDiff( pCallableStatementProvider, getParameters(), getDatabaseHandler() ).compare( lSollModel, lDatabaseModel );

          handleRelevanceCheck( lDiffResult, getParameters() );

          handleDiffResult( getParameters(), pCallableStatementProvider, lDiffResult );
        }
        else
        {
          getParameters().getExtensionHandler().handleTargetplsql( pCallableStatementProvider );
        }

        logInfo( "done orcas statics" );
      };

      if( getParameters().getSchemaFiles() == null ) {
        JdbcConnectionHandler.runWithCallableStatementProvider(getParameters(), new RunWithCallableStatementProvider() {
          public void run(CallableStatementProvider pCallableStatementProvider) {
            lStaticsHandler.accept(pCallableStatementProvider);
          }
        });
      }
      else {
        lStaticsHandler.accept(null);
      }
    }
  }

  private void handleRelevanceCheck(DiffResult pDiffResult, Parameters pParameters) {
    if (pParameters.getRelevantTables() != null || pParameters.getRelevantSequences() != null) {
      pDiffResult.getDiffActions().forEach(p -> {
            boolean lIsTable = p.getDiffReasonKey().getTextObjectType().equals(DiffReasonEntity.TABLE.name().toLowerCase());
            boolean lIsSequence = p.getDiffReasonKey().getTextObjectType().equals(DiffReasonEntity.SEQUENCE.name().toLowerCase());

            boolean lIgnore = false;

        String lTextObjectName = p.getDiffReasonKey().getTextObjectName();

        if( p.getDiffReasonKey().getTextSchemaName() != null ){
          lTextObjectName = p.getDiffReasonKey().getTextSchemaName() + "." + lTextObjectName;
        }

        lTextObjectName = lTextObjectName.toUpperCase();

        if (lIsTable && pParameters.getRelevantTables() != null) {
              if (!pParameters.getRelevantTables().contains(lTextObjectName)) {
                lIgnore = true;
              }
            }
            if (lIsSequence && pParameters.getRelevantSequences() != null) {
              if (!pParameters.getRelevantSequences().contains(lTextObjectName)) {
                lIgnore = true;
              }
            }

            if( lIgnore ) {
              p.getStatements().forEach(pStatement -> pStatement.setIgnore("change on object not marked as relevant"));
            }
          }
      );
    }
  }

  private de.opitzconsulting.origOrcasDsl.Model modifyModel( de.opitzconsulting.origOrcasDsl.Model pSyexModel )
  {
    _log.debug( "modifyModel" );
    pSyexModel = new InlineCommentsExtension().transformModel( pSyexModel );
    pSyexModel = new InlineIndexExtension().transformModel( pSyexModel );

    if( getParameters().isCreatemissingfkindexes() )
    {
      pSyexModel = new AddFkIndexExtension().transformModel( pSyexModel );
    }

    if( getParameters().getMultiSchema() )
    {
      pSyexModel = new MultiSchemaPrefixIndexExtension().transformModel( pSyexModel );
    }

    if ( !getParameters().getMultiSchema() )
    {
      pSyexModel = new RemoveMultiSchemaPrefixExtension().transformModel( pSyexModel );
    }

    return pSyexModel;
  }

  private void handleDiffResult( Parameters pParameters, CallableStatementProvider pCallableStatementProvider, DiffResult pOriginalDiffResult )
  {
    try {
        DiffResult lDiffResult = pOriginalDiffResult;

        if (pParameters.getXmlInputFile() != null) {
            logInfo("applying xml input file: " + pParameters.getXmlInputFile());
            lDiffResult = getXmlLogFileHandler().handleXmlInputFile(lDiffResult, pParameters.getXmlInputFile(), getParameters().getXmlLogFile());
        } else {
            if (getParameters().getXmlLogFile() != null) {
                getXmlLogFileHandler().logXml(lDiffResult, getParameters().getXmlLogFile());
            }
        }

        List<String> lScriptLines = new ArrayList<String>();

        for (DiffAction lDiffAction : lDiffResult.getDiffActions()) {
            for (Statement lStatementClass : lDiffAction.getStatements()) {
                if (lStatementClass.isFailure()) {
                    throw new RuntimeException(lStatementClass.getComment() + ":" + lStatementClass.getStatement());
                }
            }
        }

        for (DiffAction lDiffAction : lDiffResult.getDiffActions()) {
            for (Statement lStatementClass : lDiffAction.getStatements()) {
                String lStatement = lStatementClass.getStatement();

                int lMaxLineLength = getParameters().getStaticsSpoolMaxLineLength();

                lStatement = addLineBreaksIfNeeded(lStatement, lMaxLineLength);
                String lStatementToExecute = lStatement;

                if (!lStatementClass.isIgnore() || getParameters().isLogIgnoredStatements()) {
                    if (lStatementClass.isIgnore()) {
                        lStatement = "-- ignore-" + lStatementClass.getComment() + ": " + lStatement;
                    } else {
                        if (lStatementClass.getComment() != null) {
                            lStatement = lStatement + " -- " + lStatementClass.getComment();
                        }
                    }

                    if (lStatement.endsWith(";")) {
                        lScriptLines.add(lStatement);
                        lScriptLines.add("/");
                        logInfo(lStatement);
                        logInfo("/");
                    } else {
                        lScriptLines.add(lStatement + ";");
                        logInfo(lStatement + ";");
                    }
                }

                if (!pParameters.isLogonly() && !lStatementClass.isIgnore()) {
                    String lSchemaName = lDiffAction.getDiffReasonKey().getTextSchemaName();

                    if (lDiffAction.getDiffReasonKey().getTextSubSchemaName() != null) {
                        lSchemaName = lDiffAction.getDiffReasonKey().getTextSubSchemaName();
                    }

                    CallableStatementProvider lCallableStatementProviderForSchema = getParameters()
                            .getMultiSchemaConnectionManager()
                            .getCallableStatementProviderForSchema(
                                pCallableStatementProvider,
                                lSchemaName,
                                getParameters());
                    try {
                        getDatabaseHandler().executeDiffResultStatement(
                            lStatementToExecute,
                            lCallableStatementProviderForSchema);
                    } catch (RuntimeException e) {
                        getParameters().getExecuteSqlErrorHandler().handleExecutionError(
                            e,
                            lStatementToExecute,
                            lCallableStatementProviderForSchema,
                            getParameters(),
                            new ExecuteSqlErrorHandlerCallback() {
                                @Override
                                public void rethrow() {
                                    throw new RuntimeException(e);
                                }

                                @Override
                                public void logError() {
                                    _log.warn(e, e);
                                }

                                @Override
                                public void logInfo(String pMessage) {
                                    OrcasMain.this.logInfo(pMessage);
                                }
                            });
                    }
                }
            }
        }

        if (!lScriptLines.isEmpty()) {
            addSpoolfolderScriptIfNeeded(lScriptLines, pParameters.getLogname() + ".sql");
        }
    }
    finally {
        getParameters().getMultiSchemaConnectionManager().releaseAllConnections();
    }
  }

  private XmlLogFileHandler getXmlLogFileHandler()
  {
    return new XmlLogFileHandler( getParameters() );
  }

  private void doSchemaSync( final Parameters pParameters ) throws Exception
  {
    logInfo( "starting orcas statics schema sync" );

    logInfo( "loading source database" );
    final de.opitzconsulting.origOrcasDsl.Model[] lSrcModel = new de.opitzconsulting.origOrcasDsl.Model[1];
    JdbcConnectionHandler.runWithCallableStatementProvider( getParameters(), pParameters.getSrcJdbcConnectParameters(), new RunWithCallableStatementProvider()
    {
      public void run( CallableStatementProvider pCallableStatementProvider ) throws Exception
      {
        CallableStatementProvider lSrcCallableStatementProvider = pCallableStatementProvider;
        InitDiffRepository.init( lSrcCallableStatementProvider, getDatabaseHandler(), getParameters() );
        lSrcModel[0] = getDatabaseHandler().createLoadIst( lSrcCallableStatementProvider, getParameters() ).loadModel( false );
        DiffRepository.getModelMerge().cleanupValues( lSrcModel[0] );
        getParameters().getMultiSchemaConnectionManager().releaseAllConnections();
      }
    } );

    logInfo( "loading destination database" );
    JdbcConnectionHandler.runWithCallableStatementProvider( getParameters(), new RunWithCallableStatementProvider()
    {
      public void run( CallableStatementProvider pCallableStatementProvider ) throws Exception
      {
        CallableStatementProvider lDestCallableStatementProvider = pCallableStatementProvider;
        InitDiffRepository.init( lDestCallableStatementProvider, getDatabaseHandler(), getParameters() );
        de.opitzconsulting.origOrcasDsl.Model lDstModel = getDatabaseHandler().createLoadIst( lDestCallableStatementProvider, getParameters() ).loadModel( true );
        DiffRepository.getModelMerge().cleanupValues( lDstModel );

        logInfo( "building diff" );
        DiffResult lDiffResult = new OrcasDiff( lDestCallableStatementProvider, pParameters, getDatabaseHandler() ).compare( lSrcModel[0], lDstModel );

        handleDiffResult( pParameters, lDestCallableStatementProvider, lDiffResult );

        logInfo( "done orcas statics schema sync" );
      }
    } );
  }

  private static String addLineBreaksIfNeeded( String pLine, int pMaxLineLength )
  {
    if( pLine.length() > pMaxLineLength )
    {
      List<String> lSubstrings = new ArrayList<String>();

      while( pLine.length() > 0 )
      {
        if( pLine.length() > pMaxLineLength )
        {
          lSubstrings.add( pLine.substring( 0, pMaxLineLength ) );
          pLine = pLine.substring( pMaxLineLength );
        }
        else
        {
          lSubstrings.add( pLine );
          pLine = "";
        }
      }

      StringBuilder lStringBuilder = new StringBuilder();

      for( String lLine : lSubstrings )
      {
        if( !lLine.contains( "\n" ) && lLine.indexOf( " " ) > 0)
        {
          int lSpaceIndex = lLine.indexOf( " " );

          lLine = lLine.substring( 0, lSpaceIndex ) + "\n" + lLine.substring( lSpaceIndex );
        }

        lStringBuilder.append( lLine );
      }

      return lStringBuilder.toString();
    }
    else
    {
      return pLine;
    }
  }
}
