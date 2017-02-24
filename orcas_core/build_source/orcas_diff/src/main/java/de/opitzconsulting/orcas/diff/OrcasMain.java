package de.opitzconsulting.orcas.diff;

import java.io.FileNotFoundException;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.List;

import de.opitzconsulting.orcas.diff.JdbcConnectionHandler.RunWithCallableStatementProvider;
import de.opitzconsulting.orcas.diff.OrcasDiff.DiffResult;
import de.opitzconsulting.orcas.diff.ParametersCommandline.ParameterTypeMode;
import de.opitzconsulting.orcas.extensions.AllExtensions;
import de.opitzconsulting.orcas.extensions.OrcasExtension;
import de.opitzconsulting.orcas.orig.diff.DiffRepository;
import de.opitzconsulting.orcas.sql.CallableStatementProvider;
import de.opitzconsulting.orcas.sql.WrapperCallableStatement;
import de.opitzconsulting.orcas.sql.WrapperExecuteStatement;
import de.opitzconsulting.orcas.syex.load.DataReader;
import de.opitzconsulting.orcas.syex.trans.TransformSyexOrig;
import de.opitzconsulting.orcasDsl.Model;
import de.opitzconsulting.orcasDsl.impl.ModelImpl;

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
          InitDiffRepository.init( pCallableStatementProvider );

          logInfo( "starting orcas statics" );

          Model lSyexModel;

          if( getParameters().getModelFile().endsWith( "xml" ) )
          {
            lSyexModel = XtextFileLoader.loadModelXml( getParameters().getModelFile() );
          }
          else
          {
            if( getParameters().getSqlplustable() )
            {
              logInfo( "loading sqlplus data" );
              lSyexModel = loadModelFromSqlplusTable( getParameters() );
            }
            else
            {
              logInfo( "loading files" );
              lSyexModel = getParameters().getModelLoader().loadModel( getParameters() );
            }

            AllExtensions lAllExtensions = new AllExtensions();
            lAllExtensions.setUseReverseExtension( false );
            if( lAllExtensions.hasExtension() )
            {
              logInfo( "calling java extensions" );
              lSyexModel = callJavaExtensions( lSyexModel, lAllExtensions, getParameters() );
            }

            if( !getParameters().getAdditionalOrcasExtensions().isEmpty() )
            {
              logInfo( "calling additional extensions" );
              for( OrcasExtension lOrcasExtension : getParameters().getAdditionalOrcasExtensions() )
              {
                lSyexModel = lOrcasExtension.transformModel( lSyexModel );
              }
            }

            if( PlSqlHandler.isPlSqlEextensionsExistst() )
            {
              logInfo( "calling pl/sql extensions" );
              lSyexModel = PlSqlHandler.callPlSqlExtensions( lSyexModel, getParameters(), false );
            }
          }

          lSyexModel = new InlineCommentsExtension().transformModel( lSyexModel );
          lSyexModel = new InlineIndexExtension().transformModel( lSyexModel );

          if( getParameters().isCreatemissingfkindexes() )
          {
            lSyexModel = new AddFkIndexExtension().transformModel( lSyexModel );
          }

          if( getParameters().getMultiSchema() )
          {
            lSyexModel = new MultiSchemaPrefixIndexExtension().transformModel( lSyexModel );
          }

          if( getParameters().getTargetplsql().equals( "" ) )
          {
            logInfo( "loading database" );
            de.opitzconsulting.origOrcasDsl.Model lDatabaseModel = new LoadIst( pCallableStatementProvider, getParameters() ).loadModel( true );

            logInfo( "building diff" );
            DiffRepository.getModelMerge().cleanupValues( lDatabaseModel );
            de.opitzconsulting.origOrcasDsl.Model lSollModel = TransformSyexOrig.convertModel( lSyexModel );
            DiffRepository.getModelMerge().cleanupValues( lSollModel );
            DiffResult lDiffResult = new OrcasDiff( pCallableStatementProvider, getParameters() ).compare( lSollModel, lDatabaseModel );

            handleDiffResult( getParameters(), pCallableStatementProvider, lDiffResult );
          }
          else
          {
            logInfo( "executing " + getParameters().getTargetplsql() );
            PlSqlHandler.callTargetPlSql( lSyexModel, getParameters(), pCallableStatementProvider );
          }

          logInfo( "done orcas statics" );
        }
      } );
    }
  }

  private static Model loadModelFromSqlplusTable( final Parameters pParameters ) throws Exception
  {
    final Model lOutputModel = new ModelImpl();

    JdbcConnectionHandler.runWithCallableStatementProvider( pParameters, pParameters.getOrcasJdbcConnectParameters(), new RunWithCallableStatementProvider()
    {
      public void run( CallableStatementProvider pCallableStatementProvider ) throws Exception
      {
        String lCallExtensions = "" + //
                                 " declare" +
                                 " v_model " +
                                 pParameters.getOrcasDbUser() +
                                 ".ot_syex_model;" +
                                 " v_anydata SYS.ANYDATA;" +
                                 " begin " + //
                                 "   select model into v_anydata from " +
                                 pParameters.getOrcasDbUser() +
                                 ".orcas_sqlplus_model;" +
                                 " if( v_anydata.getObject( v_model ) = DBMS_TYPES.SUCCESS )" +
                                 " then " +
                                 "    null; " +
                                 " end if;" + //
                                 " ? := v_model;" + //
                                 " end; " + //
                                 "";

        new WrapperCallableStatement( lCallExtensions, pCallableStatementProvider )
        {
          @Override
          protected void useCallableStatement( CallableStatement pCallableStatement ) throws SQLException
          {
            pCallableStatement.registerOutParameter( 1, java.sql.Types.STRUCT, (pParameters.getOrcasDbUser() + ".ot_syex_model").toUpperCase() );

            pCallableStatement.execute();

            DataReader.setIntNullValue( DiffRepository.getNullIntValue() );
            DataReader.loadIntoModel( lOutputModel, (Struct)pCallableStatement.getObject( 1 ) );
          }
        }.execute();
      }
    } );

    return lOutputModel;
  }

  private void handleDiffResult( Parameters pParameters, CallableStatementProvider pCallableStatementProvider, DiffResult pDiffResult ) throws FileNotFoundException
  {
    List<String> lScriptLines = new ArrayList<String>();

    for( String lLine : pDiffResult.getSqlStatements() )
    {
      int lMaxLineLength = 2000;

      lLine = addLineBreaksIfNeeded( lLine, lMaxLineLength );

      if( lLine.endsWith( ";" ) )
      {
        lScriptLines.add( lLine );
        lScriptLines.add( "/" );
        logInfo( lLine );
        logInfo( "/" );
      }
      else
      {
        lScriptLines.add( lLine + ";" );
        logInfo( lLine + ";" );
      }

      if( !pParameters.isLogonly() )
      {
        new WrapperExecuteStatement( lLine, pCallableStatementProvider ).execute();
      }
    }

    if( !lScriptLines.isEmpty() )
    {
      addSpoolfolderScriptIfNeeded( lScriptLines, pParameters.getLogname() + ".sql" );
    }
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
        InitDiffRepository.init( lSrcCallableStatementProvider );
        lSrcModel[0] = new LoadIst( lSrcCallableStatementProvider, pParameters ).loadModel( false );
        DiffRepository.getModelMerge().cleanupValues( lSrcModel[0] );
      }
    } );

    logInfo( "loading destination database" );
    JdbcConnectionHandler.runWithCallableStatementProvider( getParameters(), new RunWithCallableStatementProvider()
    {
      public void run( CallableStatementProvider pCallableStatementProvider ) throws Exception
      {
        CallableStatementProvider lDestCallableStatementProvider = pCallableStatementProvider;
        InitDiffRepository.init( lDestCallableStatementProvider );
        de.opitzconsulting.origOrcasDsl.Model lDstModel = new LoadIst( lDestCallableStatementProvider, pParameters ).loadModel( true );
        DiffRepository.getModelMerge().cleanupValues( lDstModel );

        logInfo( "building diff" );
        DiffResult lDiffResult = new OrcasDiff( lDestCallableStatementProvider, pParameters ).compare( lSrcModel[0], lDstModel );

        handleDiffResult( pParameters, lDestCallableStatementProvider, lDiffResult );

        logInfo( "done orcas statics schema sync" );
      }
    } );
  }

  public static Model callJavaExtensions( Model lSyexModel, AllExtensions pAllExtensions, Parameters pParameters )
  {
    if( pParameters.getExtensionParameter().length() != 0 )
    {
      pAllExtensions.setParameter( pParameters.getExtensionParameter() );
    }
    lSyexModel = pAllExtensions.transformModel( lSyexModel );

    return lSyexModel;
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
