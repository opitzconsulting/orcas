package de.opitzconsulting.orcas.diff;

import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.SQLException;
import java.sql.Struct;

import de.opitzconsulting.orcas.diff.JdbcConnectionHandler.RunWithCallableStatementProvider;
import de.opitzconsulting.orcas.extensions.PlSqlExtensionInfo;
import de.opitzconsulting.orcas.orig.diff.DiffRepository;
import de.opitzconsulting.orcas.sql.CallableStatementProvider;
import de.opitzconsulting.orcas.sql.WrapperCallableStatement;
import de.opitzconsulting.orcas.syex.load.DataReader;
import de.opitzconsulting.orcasDsl.Model;
import de.opitzconsulting.orcasDsl.impl.ModelImpl;

public class PlSqlHandler
{
  public static Model callPlSqlExtensions( final Model pModel, final Parameters pParameters, final boolean pReverse ) throws Exception
  {
    final Model lOutputModel = new ModelImpl();

    JdbcConnectionHandler.runWithCallableStatementProvider( pParameters, pParameters.getOrcasJdbcConnectParameters(), new RunWithCallableStatementProvider()
    {
      public void run( final CallableStatementProvider pCallableStatementProvider ) throws Exception
      {
        String lMethodName = pReverse ? "call_reverse_extensions" : "call_extensions";
        String lCallExtensions = "" + //
                                 " { " + //
                                 "   ? = call " +
                                 pParameters.getOrcasDbUser() +
                                 ".pa_orcas_extensions." +
                                 lMethodName +
                                 "( ?, ? ) " + //
                                 " } " + //
                                 "";
        new WrapperCallableStatement( lCallExtensions, pCallableStatementProvider )
        {
          @Override
          protected void useCallableStatement( CallableStatement pCallableStatement ) throws SQLException
          {
            pCallableStatement.registerOutParameter( 1, java.sql.Types.STRUCT, (pParameters.getOrcasDbUser() + ".ot_syex_model").toUpperCase() );
            pCallableStatement.setObject( 2, createDataWriter( pCallableStatementProvider ).getStructModel( pModel ) );
            pCallableStatement.setString( 3, pParameters.getExtensionParameter() );

            pCallableStatement.execute();

            DataReader.setIntNullValue( DiffRepository.getNullIntValue() );
            DataReader.loadIntoModel( lOutputModel, (Struct)pCallableStatement.getObject( 1 ) );
          }
        }.execute();
      }
    } );

    return lOutputModel;
  }

  private static de.opitzconsulting.orcas.syex.load.DataWriter createDataWriter( final CallableStatementProvider pCallableStatementProvider )
  {
    de.opitzconsulting.orcas.syex.load.DataWriter lReturn = new de.opitzconsulting.orcas.syex.load.DataWriter()
    {
      @Override
      protected Struct createStruct( String pTypeName, Object[] pAttributes )
      {
        return JdbcConnectionHandler.createStruct( pTypeName, pAttributes, pCallableStatementProvider );
      }

      @Override
      protected Array createArrayOf( String pTypeName, Object[] pElements )
      {
        return JdbcConnectionHandler.createArrayOf( pTypeName, pElements, pCallableStatementProvider );
      }

      @Override
      protected Clob createClob( String pValue )
      {
        return JdbcConnectionHandler.createClob( pValue, pCallableStatementProvider );
      }
    };

    lReturn.setIntNullValue( DiffRepository.getNullIntValue() );

    return lReturn;
  }

  public static void callTargetPlSql( final Model pModel, final Parameters pParameters, final CallableStatementProvider pCallableStatementProvider )
  {
    String lCallExtensions = "" + //
                             " begin " + //
                             pParameters.getOrcasDbUser() +
                             "." +
                             pParameters.getTargetplsql() +
                             ".run( ? ); " + //
                             " end; " + //
                             "";

    new WrapperCallableStatement( lCallExtensions, pCallableStatementProvider )
    {
      @Override
      protected void useCallableStatement( CallableStatement pCallableStatement ) throws SQLException
      {
        pCallableStatement.setObject( 1, createDataWriter( pCallableStatementProvider ).getStructModel( pModel ) );

        pCallableStatement.execute();
      }
    }.execute();
  }

  public static boolean isPlSqlEextensionsExistst()
  {
    return PlSqlExtensionInfo.hasExtensions();
  }
}
