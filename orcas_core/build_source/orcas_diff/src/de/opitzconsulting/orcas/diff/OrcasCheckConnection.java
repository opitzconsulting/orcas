package de.opitzconsulting.orcas.diff;

import de.opitzconsulting.orcas.diff.Parameters.ParameterTypeMode;

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
  protected void run( Parameters pParameters ) throws Exception
  {
    try
    {
      JdbcConnectionHandler.createCallableStatementProvider( pParameters );
    }
    catch( Exception e )
    {
      _log.error( "connection test failed: " + pParameters.getJdbcConnectParameters().getJdbcUrl() + " " + pParameters.getJdbcConnectParameters().getJdbcUser() );

      throw e;
    }
  }
}
