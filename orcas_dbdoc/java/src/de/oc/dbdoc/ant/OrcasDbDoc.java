package de.oc.dbdoc.ant;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import oracle.jdbc.OracleDriver;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.velocity.app.Velocity;

import de.oc.dbdoc.Main;
import de.oc.dbdoc.load.DbLoader;
import de.oc.dbdoc.schemadata.Schema;

public class OrcasDbDoc extends Task
{
  private String _velocityTemplatePath;
  private String _jdbcurl;
  private String _user;
  private String _password;
  private String _outfolder;
  private String _tmpfolder;
  private Tableregistry _tableregistry;
  private Styles _styles;
  private Diagram _diagram;

  public void setJdbcurl( String pJdbcurl )
  {
    _jdbcurl = pJdbcurl;
  }

  public void setOutfolder( String pOutfolder )
  {
    _outfolder = pOutfolder;
  }

  public void setPassword( String pPassword )
  {
    _password = pPassword;
  }

  public void setTmpfolder( String pTmpfolder )
  {
    _tmpfolder = pTmpfolder;
  }

  public void setUser( String pUser )
  {
    _user = pUser;
  }

  public Tableregistry createTableregistry()
  {
    if( _tableregistry != null )
    {
      throw new RuntimeException( "tableregistry darf nicht mehrfach vorkommen" );
    }

    _tableregistry = new Tableregistry( this );

    return _tableregistry;
  }

  public Styles createStyles()
  {
    if( _styles != null )
    {
      throw new RuntimeException( "styles darf nicht mehrfach vorkommen" );
    }

    _styles = new Styles( this );

    return _styles;
  }

  public Diagram createDiagram()
  {
    if( _diagram != null )
    {
      throw new RuntimeException( "diagram darf nicht mehrfach vorkommen" );
    }

    _diagram = new Diagram( this );

    return _diagram;
  }

  Tableregistry getTableregistry()
  {
    return _tableregistry;
  }

  @Override
  public void execute() throws BuildException
  {
    try
    {
      Properties lProperties = new Properties();
      // TODO
      lProperties.setProperty( "file.resource.loader.path", _velocityTemplatePath );

      Velocity.init( lProperties );

      Class.forName( "oracle.jdbc.OracleDriver" );
      if( OracleDriver.class != null )
        ;

      Connection lConnection = DriverManager.getConnection( _jdbcurl, _user, _password );

      Schema lSchema = new DbLoader().loadSchema( lConnection, _diagram );
      lSchema.mergeAssociations();

      Main.writeDiagramsRecursive( _diagram, _styles, lSchema, _outfolder, _tmpfolder + "/dotfiles", _tableregistry.getTablesrcfolder() );

      lConnection.close();
    }
    catch( Exception e )
    {
      e.printStackTrace();
    }
  }

  Styles getStyles()
  {
    return _styles;
  }

  public void setVelocitytemplatepath( String pVelocityTemplatePath )
  {
    _velocityTemplatePath = pVelocityTemplatePath;
  }
}
