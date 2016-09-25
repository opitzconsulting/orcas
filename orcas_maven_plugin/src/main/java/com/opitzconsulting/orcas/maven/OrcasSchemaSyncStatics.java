package com.opitzconsulting.orcas.maven;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import de.opitzconsulting.orcas.diff.OrcasMain;
import de.opitzconsulting.orcas.diff.Parameters.JdbcConnectParameters;
import de.opitzconsulting.orcas.diff.ParametersCall;

@Mojo( name = "schemaSyncStatics" )
public class OrcasSchemaSyncStatics extends BaseOrcasMojo
{
  @Parameter( defaultValue = "schema-sync-statics" )
  private String logname;

  @Override
  protected String getLogname()
  {
    return logname;
  }
  
  @Parameter( defaultValue = "oracle.jdbc.OracleDriver" )
  private String srcjdbcdriver;

  @Parameter
  private String srcjdbcurl;

  @Parameter
  private String srcusername;

  @Parameter
  private String srcpassword;

  @Override
  protected void executeWithParameters( ParametersCall pParameters )
  {
    JdbcConnectParameters lSrcJdbcConnectParameters = new JdbcConnectParameters();
    
    lSrcJdbcConnectParameters.setJdbcDriver( srcjdbcdriver );
    lSrcJdbcConnectParameters.setJdbcUrl( srcjdbcurl );
    lSrcJdbcConnectParameters.setJdbcUser( srcusername );
    lSrcJdbcConnectParameters.setJdbcPassword( srcpassword );
    
    pParameters.setSrcJdbcConnectParameters( lSrcJdbcConnectParameters);
    pParameters.setModelFile( null );
    pParameters.setSqlplustable( false );
    pParameters.setOrderColumnsByName( false );

    new OrcasMain().mainRun( pParameters );
  }
}
