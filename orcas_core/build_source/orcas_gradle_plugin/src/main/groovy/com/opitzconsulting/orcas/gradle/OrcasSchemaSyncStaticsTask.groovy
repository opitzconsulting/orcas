package com.opitzconsulting.orcas.gradle

import de.opitzconsulting.orcas.diff.OrcasMain;
import de.opitzconsulting.orcas.diff.Parameters.JdbcConnectParameters;
import de.opitzconsulting.orcas.diff.ParametersCall
import org.gradle.api.tasks.Internal;

public class OrcasSchemaSyncStaticsTask extends BaseOrcasTask
{
  @Internal
  private String logname = "sync-statics";

  @Override
  protected String getLogname()
  {
    return logname;
  }
  
  @Override
  protected void executeOrcasTaskWithParameters( ParametersCall pParameters )
  {
    JdbcConnectParameters lSrcJdbcConnectParameters = new JdbcConnectParameters();
    
    lSrcJdbcConnectParameters.setJdbcDriver( project.orcasconfiguration.jdbcdriver );
    lSrcJdbcConnectParameters.setJdbcUrl( project.orcasconfiguration.srcjdbcurl == null ? project.orcasconfiguration.jdbcurl : project.orcasconfiguration.srcjdbcurl );
    lSrcJdbcConnectParameters.setJdbcUser( project.orcasconfiguration.srcusername );
    lSrcJdbcConnectParameters.setJdbcPassword( project.orcasconfiguration.srcpassword );
    
    pParameters.setSrcJdbcConnectParameters( lSrcJdbcConnectParameters);
    pParameters.setModelFile( null );
    pParameters.setSqlplustable( false );
    pParameters.setOrderColumnsByName( false );

    new OrcasMain().mainRun( pParameters );
  }
}
