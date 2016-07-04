package de.opitzconsulting.orcas.diff;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.opitzconsulting.orcas.diff.Parameters.ParameterTypeMode;

public abstract class Orcas
{
  protected static Log _log;

  public void mainRun( String[] pArgs )
  {
    try
    {
      Parameters lParameters = new Parameters( pArgs, getParameterTypeMode() );

      _log = LogFactory.getLog( Orcas.class );

      run( lParameters );
    }
    catch( Exception e )
    {
      if( _log != null )
      {
        _log.error( e, e );
      }
      else
      {
        e.printStackTrace();
      }
      System.exit( -1 );
    }
  }

  protected abstract void run( Parameters pParameters ) throws Exception;

  protected abstract ParameterTypeMode getParameterTypeMode();
}
