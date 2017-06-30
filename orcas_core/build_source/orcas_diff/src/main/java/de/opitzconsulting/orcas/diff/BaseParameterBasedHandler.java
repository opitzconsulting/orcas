package de.opitzconsulting.orcas.diff;

public abstract class BaseParameterBasedHandler
{
  private Parameters _parameters;
  
  protected void setParametersInternal( Parameters pParameters )
  {
    _parameters = pParameters;
  }

  protected void logInfo( String pString )
  {
    if( getParameters().getInfoLogHandler() != null )
    {
      _parameters.getInfoLogHandler().logInfo( pString );
    }
  }

  protected Parameters getParameters()
  {
    return _parameters;
  }
}
