package de.oc.dbdoc.ant;

import java.util.Properties;

import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import de.oc.dbdoc.Main;
import de.oc.dbdoc.load.DbLoader;
import de.oc.dbdoc.schemadata.Schema;
import de.opitzconsulting.orcas.diff.ExtensionHandlerImpl;
import de.opitzconsulting.orcas.diff.Parameters;

public class OrcasDbDoc
{
  private String _outfolder;
  private String _tmpfolder;
  private Tableregistry _tableregistry;
  private Styles _styles;
  private Diagram _diagram;
  private boolean _svg = true;

  public boolean isSvg()
  {
    return _svg;
  }

  public void setSvg( boolean pSvg )
  {
    _svg = pSvg;
  }

  public Diagram getDiagram()
  {
    return _diagram;
  }

  public void setTableregistry( Tableregistry pTableregistry )
  {
    if( _tableregistry != null )
    {
      throw new RuntimeException( "tableregistry darf nicht mehrfach vorkommen" );
    }

    _tableregistry = pTableregistry;
  }

  public void setStyles( Styles pStyles )
  {
    if( _styles != null )
    {
      throw new RuntimeException( "styles darf nicht mehrfach vorkommen" );
    }

    _styles = pStyles;
  }

  public void setDiagram( Diagram pDiagram )
  {
    if( _diagram != null )
    {
      throw new RuntimeException( "diagram darf nicht mehrfach vorkommen" );
    }

    _diagram = pDiagram;
  }

  public void setOutfolder( String pOutfolder )
  {
    _outfolder = pOutfolder;
  }

  public void setTmpfolder( String pTmpfolder )
  {
    _tmpfolder = pTmpfolder;
  }

  public Tableregistry createTableregistry()
  {
    setTableregistry( new Tableregistry() );

    return _tableregistry;
  }

  public Styles createStyles()
  {
    setStyles( new Styles() );

    return _styles;
  }

  public Diagram createDiagram()
  {
    setDiagram( new Diagram() );

    return _diagram;
  }

  public Tableregistry getTableregistry()
  {
    return _tableregistry;
  }

  public void execute( Parameters pParameters )
  {
    try
    {
      ExtensionHandlerImpl lExtensionHandlerImpl = (ExtensionHandlerImpl) pParameters.getExtensionHandler();

      Properties lProperties = new Properties();
      lProperties.setProperty( RuntimeConstants.RESOURCE_LOADER, "classpath" );
      lProperties.setProperty( "classpath.resource.loader.class", ClasspathResourceLoader.class.getName() );

      Velocity.init( lProperties );

      Schema lSchema = new DbLoader().loadSchema( _diagram, lExtensionHandlerImpl.loadSyexModel(), _tableregistry );
      lSchema.mergeAssociations();

      Main.writeDiagramsRecursive( _diagram, _styles, lSchema, _outfolder, _tmpfolder, pParameters.getModelFile() + "/tables", _tableregistry, _svg );
    }
    catch( Exception e )
    {
      throw new RuntimeException( e );
    }
  }

  public Styles getStyles()
  {
    return _styles;
  }
}
