package de.opitzconsulting.orcas.diff;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import de.opitzconsulting.orcas.diff.JdbcConnectionHandler.RunWithCallableStatementProvider;
import de.opitzconsulting.orcas.diff.OrcasDiff.DiffResult;
import de.opitzconsulting.orcas.diff.ParametersCommandline.ParameterTypeMode;
import de.opitzconsulting.orcas.orig.diff.DiffRepository;
import de.opitzconsulting.orcas.sql.CallableStatementProvider;
import de.opitzconsulting.orcas.sql.WrapperExecuteStatement;

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
      JdbcConnectionHandler.runWithCallableStatementProvider( getParameters(), new RunWithCallableStatementProvider()
      {
        public void run( CallableStatementProvider pCallableStatementProvider ) throws Exception
        {
          InitDiffRepository.init( pCallableStatementProvider, getDatabaseHandler() );

          logInfo( "starting orcas statics" );

          if( getParameters().getTargetplsql().equals( "" ) )
          {
            logInfo( "loading database" );
            de.opitzconsulting.origOrcasDsl.Model lDatabaseModel = getDatabaseHandler().createLoadIst( pCallableStatementProvider, getParameters() ).loadModel( true );

            DiffRepository.getModelMerge().cleanupValues( lDatabaseModel );
            de.opitzconsulting.origOrcasDsl.Model lSollModel = getParameters().getExtensionHandler().loadModel();

            logInfo( "building diff" );
            lSollModel = modifyModel( lSollModel );

            DiffRepository.getModelMerge().cleanupValues( lSollModel );
            DiffResult lDiffResult = new OrcasDiff( pCallableStatementProvider, getParameters(), getDatabaseHandler() ).compare( lSollModel, lDatabaseModel );

            handleDiffResult( getParameters(), pCallableStatementProvider, lDiffResult );
          }
          else
          {
            getParameters().getExtensionHandler().handleTargetplsql( pCallableStatementProvider );
          }

          logInfo( "done orcas statics" );
        }
      } );
    }
  }

  private de.opitzconsulting.origOrcasDsl.Model modifyModel( de.opitzconsulting.origOrcasDsl.Model pSyexModel )
  {
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
    return pSyexModel;
  }

  private void handleDiffResult( Parameters pParameters, CallableStatementProvider pCallableStatementProvider, DiffResult pDiffResult ) throws FileNotFoundException
  {
    List<String> lScriptLines = new ArrayList<String>();

    logXml( pDiffResult );

    for( DiffAction lDiffAction : pDiffResult.getDiffActions() )
    {
      // lScriptLines.add( "-- " + lDiffAction.getTextKey() );
      // logInfo( lDiffAction.getTextKey() );

      for( String lStatement : lDiffAction.getStatements() )
      {
        int lMaxLineLength = 2000;

        lStatement = addLineBreaksIfNeeded( lStatement, lMaxLineLength );

        if( lStatement.endsWith( ";" ) )
        {
          lScriptLines.add( lStatement );
          lScriptLines.add( "/" );
          logInfo( lStatement );
          logInfo( "/" );
        }
        else
        {
          lScriptLines.add( lStatement + ";" );
          logInfo( lStatement + ";" );
        }

        if( !pParameters.isLogonly() )
        {
          new WrapperExecuteStatement( lStatement, pCallableStatementProvider ).execute();
        }
      }
    }

    if( !lScriptLines.isEmpty() )
    {
      addSpoolfolderScriptIfNeeded( lScriptLines, pParameters.getLogname() + ".sql" );
    }
  }

  private void logXml( DiffResult pDiffResult )
  {
    Element lDiffActionsElement = new Element( "diff-actions" );
    for( DiffAction lDiffAction : pDiffResult.getDiffActions() )
    {
      Element lDiffActionElement = new Element( "diff-action" );
      lDiffActionsElement.addContent( lDiffActionElement );
      lDiffActionElement.setAttribute( "key", lDiffAction.getTextKey() );

      for( DiffActionReason lDiffActionReason : lDiffAction.getDiffActionReasons() )
      {
        lDiffActionElement.addContent( lDiffActionReason.getJdomElement( false ) );
      }

      for( String lStatement : lDiffAction.getStatements() )
      {
        Element lStatementElement = new Element( "statement" );
        lDiffActionElement.addContent( lStatementElement );
        lStatementElement.addContent( lStatement );
      }
    }

    logInfo( new XMLOutputter( Format.getPrettyFormat() ).outputString( lDiffActionsElement ) );
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
        InitDiffRepository.init( lSrcCallableStatementProvider, getDatabaseHandler() );
        lSrcModel[0] = getDatabaseHandler().createLoadIst( lSrcCallableStatementProvider, getParameters() ).loadModel( false );
        DiffRepository.getModelMerge().cleanupValues( lSrcModel[0] );
      }
    } );

    logInfo( "loading destination database" );
    JdbcConnectionHandler.runWithCallableStatementProvider( getParameters(), new RunWithCallableStatementProvider()
    {
      public void run( CallableStatementProvider pCallableStatementProvider ) throws Exception
      {
        CallableStatementProvider lDestCallableStatementProvider = pCallableStatementProvider;
        InitDiffRepository.init( lDestCallableStatementProvider, getDatabaseHandler() );
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
        if( !lLine.contains( "\n" ) )
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
