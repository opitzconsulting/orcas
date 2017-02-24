package de.opitzconsulting.orcas.diff;

import de.opitzconsulting.orcas.diff.JdbcConnectionHandler.RunWithCallableStatementProvider;
import de.opitzconsulting.orcas.diff.ParametersCommandline.ParameterTypeMode;
import de.opitzconsulting.orcas.sql.CallableStatementProvider;

public class OrcasCheckConnection extends Orcas
{
  public static void main( String[] pArgs )
  {
    new OrcasCheckConnection().mainRun( pArgs );
  }

  @Override
  protected ParameterTypeMode getParameterTypeMode()
  {
    return ParameterTypeMode.ORCAS_CHECK_CONNECTION;
  }

  @Override
  protected void run() throws Exception
  {
    try
    {
      JdbcConnectionHandler.runWithCallableStatementProvider( getParameters(), new RunWithCallableStatementProvider()
      {
        public void run( CallableStatementProvider pCallableStatementProvider ) throws Exception
        {
        }
      } );
    }
    catch( Exception e )
    {
      _log.error( "connection test failed: " + getParameters().getJdbcConnectParameters().getJdbcUrl() + " " + getParameters().getJdbcConnectParameters().getJdbcUser() );

      throw e;
    }
  }
}
