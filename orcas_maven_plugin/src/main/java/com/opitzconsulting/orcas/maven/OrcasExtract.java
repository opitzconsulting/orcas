package com.opitzconsulting.orcas.maven;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.w3c.dom.Document;

import de.opitzconsulting.orcas.diff.OrcasLoadExtract;
import de.opitzconsulting.orcas.diff.ParametersCall;

@Mojo( name = "extract" )
public class OrcasExtract extends BaseOrcasMojo
{
  @Parameter( defaultValue = "extract" )
  private String logname;

  @Parameter( defaultValue = "true" )
  private Boolean removeDefaultValuesFromMode;

  @Override
  protected String getLogname()
  {
    return logname;
  }

  @Parameter
  protected File staticsoutfolder;

  @Override
  protected void executeWithParameters( ParametersCall pParameters )
  {
    pParameters.setOrderColumnsByName( false );
    pParameters.setRemoveDefaultValuesFromModel( removeDefaultValuesFromMode );
    pParameters.setModelFile( null );
    pParameters.setSpoolfile( "target/extract_tables.xml" );

    new OrcasLoadExtract().mainRun( pParameters );

    try
    {
      if( staticsoutfolder == null )
      {
        staticsoutfolder = staticsfolder;
      }

      if( staticsoutfolder.exists() )
      {
        deleteRecursive( staticsoutfolder );
      }

      DocumentBuilder lDocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document lModelXmlFile = lDocumentBuilder.parse( new File( pParameters.getSpoolfile() ) );

      StreamSource lXsltStreamSource = new StreamSource( getClass().getResourceAsStream( "/xslt_extract/orcas_extract.xsl" ) );
      Transformer lTransformer = TransformerFactory.newInstance().newTransformer( lXsltStreamSource );

      File lDummyOutFile = new File( staticsoutfolder, "dummyout.xml" );
      lTransformer.transform( new DOMSource( lModelXmlFile ), new StreamResult( lDummyOutFile ) );

      // the xslt-file is set up to create the table-files in the same directory as the lDummyOutFile. The lDummyOutFile itself is created, but empty.
      lDummyOutFile.delete();
    }
    catch( Exception e )
    {
      throw new RuntimeException( e );
    }
  }

  public static void deleteRecursive( File pFile )
  {
    if( pFile.isDirectory() )
    {
      for( String temp : pFile.list() )
      {
        deleteRecursive( new File( pFile, temp ) );
      }
    }

    pFile.delete();
  }
}
