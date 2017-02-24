package com.opitzconsulting.orcas.maven;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import de.opitzconsulting.orcas.diff.OrcasMain;
import de.opitzconsulting.orcas.diff.Parameters.JdbcConnectParameters;
import de.opitzconsulting.orcas.diff.ParametersCall;

/**
 * This can be used to do a schema compare and to apply the changes to the destination schema.
 *
 * The src-schema is used to read the information that is usually taken form orcas-scripts.
 */
@Mojo( name = "schemaSyncStatics" )
public class OrcasSchemaSyncStatics extends BaseOrcasMojo
{
  /**
   * Logname for spooling.
   */
  @Parameter( defaultValue = "schema-sync-statics" )
  private String logname;

  @Override
  protected String getLogname()
  {
    return logname;
  }
  
  /**
   * The JDBC-Driver of the src-schema. Always leave as default!
   */
  @Parameter( defaultValue = "oracle.jdbc.OracleDriver" )
  private String srcjdbcdriver;

  /**
   * The JDBC-URL to the src-database. Defaults to jdbcurl.
   */
  @Parameter
  private String srcjdbcurl;

  /**
   * The username of the src-schema.
   */
  @Parameter
  private String srcusername;

  /**
   * The password of the src-schema.
   */
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
