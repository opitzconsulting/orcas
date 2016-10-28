package de.opitzconsulting.orcas.diff;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;

import com.opitzconsulting.orcas.xslt.XsltExtractDirAccessClass;

import de.opitzconsulting.orcas.diff.JdbcConnectionHandler.RunWithCallableStatementProvider;
import de.opitzconsulting.orcas.diff.ParametersCommandline.ParameterTypeMode;
import de.opitzconsulting.orcas.orig.diff.DiffRepository;
import de.opitzconsulting.orcas.sql.CallableStatementProvider;
import de.opitzconsulting.orcas.syex.trans.TransformOrigSyex;
import de.opitzconsulting.orcas.syex.xml.XmlExport;
import de.opitzconsulting.orcasDsl.Model;

public class OrcasExtractStatics extends Orcas
{
  public static void main( String[] pArgs )
  {
    new OrcasExtractStatics().mainRun( pArgs );
  }

  @Override
  protected ParameterTypeMode getParameterTypeMode()
  {
    return ParameterTypeMode.ORCAS_EXTRACT_STATICS;
  }

  @Override
  protected void run() throws Exception
  {
    JdbcConnectionHandler.runWithCallableStatementProvider( getParameters(), new RunWithCallableStatementProvider()
    {
      public void run( CallableStatementProvider pCallableStatementProvider ) throws Exception
      {
        logInfo( "loading database" );
        de.opitzconsulting.origOrcasDsl.Model lOrigModel = new LoadIst( pCallableStatementProvider, getParameters() ).loadModel( true );

        if( getParameters().isRemoveDefaultValuesFromModel() )
        {
          InitDiffRepository.init( pCallableStatementProvider );
          logInfo( "removing default values" );
          DiffRepository.getModelMerge().cleanupValues( lOrigModel );
        }

        Model lSyexModel = TransformOrigSyex.convertModel( lOrigModel );

        if( !getParameters().getModelFile().equals( "" ) )
        {
          logInfo( "loading additional model files" );
          XtextFileLoader.initXtext();
          lSyexModel.getModel_elements().addAll( XtextFileLoader.loadModelDslFolder( getParameters() ).getModel_elements() );
        }

        if( PlSqlHandler.isPlSqlEextensionsExistst() && getParameters().isLoadExtractWithReverseExtensions() )
        {
          logInfo( "calling pl/sql reverse-extensions" );
          lSyexModel = PlSqlHandler.callPlSqlExtensions( lSyexModel, getParameters(), true );
        }

        if( getParameters().getSpoolfile().equals( "" ) )
        {
          deleteRecursive( new File( getParameters().getSpoolfolder() ) );
          logInfo( "writing files to: " + getParameters().getSpoolfolder() );
          ByteArrayOutputStream lByteArrayOutputStream = new ByteArrayOutputStream();

          OutputStreamWriter lOutputStreamWriter = new OutputStreamWriter( lByteArrayOutputStream, "utf8" );

          lOutputStreamWriter.append( new XmlExport().getModel( lSyexModel, true ) );
          lOutputStreamWriter.flush();
          lByteArrayOutputStream.close();

          DocumentBuilder lDocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
          Document lModelXmlFile = lDocumentBuilder.parse( new ByteArrayInputStream( lByteArrayOutputStream.toByteArray() ) );

          TransformerFactory lTransformerFactory = TransformerFactory.newInstance();
          lTransformerFactory.setURIResolver( new URIResolver()
          {
            public Source resolve( String pHref, String pBase ) throws TransformerException
            {
              try
              {
                return new StreamSource( XsltExtractDirAccessClass.getUriResolverURLForImport( pHref ).openStream() );
              }
              catch( IOException e )
              {
                throw new TransformerException( e );
              }
            }
          } );

          StreamSource lXsltStreamSource = new StreamSource( XsltExtractDirAccessClass.getXsltExtractFileURL().openStream() );
          Transformer lTransformer = lTransformerFactory.newTransformer( lXsltStreamSource );

          new File( getParameters().getSpoolfolder() ).mkdirs();
          File lDummyOutFile = new File( getParameters().getSpoolfolder(), "dummyout.xml" );
          lTransformer.transform( new DOMSource( lModelXmlFile ), new StreamResult( lDummyOutFile ) );

          // the xslt-file is set up to create the table-files in the same directory as the lDummyOutFile. The lDummyOutFile itself is created, but empty.
          lDummyOutFile.delete();
        }
        else
        {
          logInfo( "writing xml" );
          FileOutputStream lFileOutputStream = new FileOutputStream( getParameters().getSpoolfile() );

          OutputStreamWriter lOutputStreamWriter = new OutputStreamWriter( lFileOutputStream, "utf8" );

          lOutputStreamWriter.append( new XmlExport().getModel( lSyexModel, true ) );
          lOutputStreamWriter.flush();
          lFileOutputStream.close();
        }
      }
    } );
  }
}
