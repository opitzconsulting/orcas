package de.opitzconsulting.orcas.ot;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Struct;

import de.opitzconsulting.orcas.diff.JdbcConnectionHandler;
import de.opitzconsulting.orcas.diff.OrcasMain;
import de.opitzconsulting.orcas.diff.Parameters;
import de.opitzconsulting.orcas.diff.Parameters.ParameterTypeMode;
import de.opitzconsulting.orcas.sql.WrapperCallableStatement;
import de.opitzconsulting.orcas.syex.load.DataReader;
import de.opitzconsulting.orcasDsl.Model;
import de.opitzconsulting.orcasDsl.impl.ModelImpl;

public class OrcasRunPlSqlExtensions
{
  public static void main( String[] pArgs )
  {
    Parameters lParameters = new Parameters( pArgs, ParameterTypeMode.ORCAS_RUN_PL_SQL_EXTENSIONS );

    JdbcConnectionHandler.initWithMainParameters( lParameters );

    Model lInputModel = OrcasMain.loadModel( lParameters.getModelFile() );

    new DataWriter().writeToDatabase( lInputModel );

    String lCallExtensions = "" + //
                             " { " + //
                             "   ? = call pa_orcas_extensions.call_extensions( pa_orcas_model_holder.get_model() ) " + //
                             " } " + //
                             "";

    final Model lOutputModel = new ModelImpl();

    new WrapperCallableStatement( lCallExtensions, JdbcConnectionHandler.getCallableStatementProvider() )
    {
      @Override
      protected void useCallableStatement( CallableStatement pCallableStatement ) throws SQLException
      {
        pCallableStatement.registerOutParameter( 1, java.sql.Types.STRUCT, (lParameters.getOrcasDbUser() + ".ot_syex_model").toUpperCase() );

        pCallableStatement.execute();

        DataReader.setIntNullValue( -1 );
        DataReader.loadIntoModel( lOutputModel, (Struct)pCallableStatement.getObject( 1 ) );
      }
    }.execute();

    try
    {
      FileOutputStream lFileOutputStream = new FileOutputStream( lParameters.getModelFile() );

      OutputStreamWriter lOutputStreamWriter = new OutputStreamWriter( lFileOutputStream );

      lOutputStreamWriter.append( ModelWriter.getSkriptXml( lOutputModel ) );

      lOutputStreamWriter.close();
      lFileOutputStream.close();
    }
    catch( IOException e )
    {
      throw new RuntimeException();
    }
  }
}
