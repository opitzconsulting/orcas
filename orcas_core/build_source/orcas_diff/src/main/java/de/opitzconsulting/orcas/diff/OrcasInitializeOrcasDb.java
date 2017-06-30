package de.opitzconsulting.orcas.diff;

import de.opitzconsulting.orcas.diff.ParametersCommandline.ParameterTypeMode;

public class OrcasInitializeOrcasDb extends Orcas
{
  public static void main( String[] pArgs )
  {
    new OrcasInitializeOrcasDb().mainRun( pArgs );
  }

  @Override
  protected void run() throws Exception
  {
    getParameters().getExtensionHandler().initOrcasDbIfNeeded( _log );
  }

  @Override
  protected ParameterTypeMode getParameterTypeMode()
  {
    return ParameterTypeMode.ORCAS_INITIALIZE_ORCAS_DB;
  }
}
