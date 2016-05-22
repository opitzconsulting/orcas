package de.opitzconsulting.orcas.diff;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;

import de.opitzconsulting.orcas.diff.OrcasDiff.DiffResult;
import de.opitzconsulting.orcas.diff.Parameters.ParameterTypeMode;
import de.opitzconsulting.orcas.sql.WrapperExecuteStatement;
import de.opitzconsulting.orcas.syex.trans.TransformSyexOrig;
import de.opitzconsulting.orcasDsl.Model;
import de.opitzconsulting.orcasDsl.OrcasDslPackage;

public class OrcasMain
{
  public static void main( String[] pArgs ) throws Exception
  {
    Parameters lParameters = new Parameters( pArgs, ParameterTypeMode.ORCAS_MAIN );

    JdbcConnectionHandler.initWithMainParameters( lParameters );
    InitDiffRepository.init();

    Model lSyexModel = loadModel( lParameters.getModelFile() );

    lSyexModel = new InlineCommentsExtension().transformModel( lSyexModel );
    lSyexModel = new InlineIndexExtension().transformModel( lSyexModel );

    if( lParameters.isCreatemissingfkindexes() )
    {
      lSyexModel = new AddFkIndexExtension().transformModel( lSyexModel );
    }

    DiffResult lDiffResult = new OrcasDiff().compare( TransformSyexOrig.convertModel( lSyexModel ), new LoadIst().loadModel( lParameters ), lParameters );

    PrintStream lPrintStream = new PrintStream( new FileOutputStream( lParameters.getSpoolfile() ) );
    for( String lLine : lDiffResult.getSqlStatements() )
    {
      int lMaxLineLength = 2000;

      lLine = addLineBreaksIfNeeded( lLine, lMaxLineLength );

      if( lLine.endsWith( ";" ) )
      {
        lPrintStream.println( lLine );
        lPrintStream.println( "/" );
        System.out.println( lLine );
        System.out.println( "/" );
      }
      else
      {
        lPrintStream.println( lLine + ";" );
        System.out.println( lLine + ";" );
      }

      if( !lParameters.isLogonly() )
      {
        new WrapperExecuteStatement( lLine, JdbcConnectionHandler.getCallableStatementProvider() ).execute();
      }
    }

    lPrintStream.close();
  }

  private static String addLineBreaksIfNeeded( String pLine, int pMaxLineLength )
  {
    if( pLine.length() > pMaxLineLength )
    {
      List<String> lSubstrings = new ArrayList<String>();

      while( pLine.length() > 0 )
      {
        if( pLine.length() > pMaxLineLength )
        {
          lSubstrings.add( pLine.substring( 0, pMaxLineLength ) );
          pLine = pLine.substring( pMaxLineLength );
        }
        else
        {
          lSubstrings.add( pLine );
          pLine = "";
        }
      }

      StringBuilder lStringBuilder = new StringBuilder();

      for( String lLine : lSubstrings )
      {
        if( !lLine.contains( "\n" ) )
        {
          int lSpaceIndex = lLine.indexOf( " " );

          lLine = lLine.substring( 0, lSpaceIndex ) + "\n" + lLine.substring( lSpaceIndex );
        }

        lStringBuilder.append( lLine );
      }

      return lStringBuilder.toString();
    }
    else
    {
      return pLine;
    }
  }

  public static Model loadModel( String pFilename )
  {
    EPackage.Registry.INSTANCE.put( OrcasDslPackage.eNS_URI, OrcasDslPackage.eINSTANCE );
    Resource.Factory.Registry lRegistry = Resource.Factory.Registry.INSTANCE;
    Map<String,Object> lMap = lRegistry.getExtensionToFactoryMap();
    lMap.put( "xml", new XMLResourceFactoryImpl() );

    ResourceSet lResourceSet = new ResourceSetImpl();
    Resource lResource = lResourceSet.createResource( URI.createFileURI( pFilename ) );

    ((XMLResource)lResource).getDefaultSaveOptions();

    try
    {
      lResource.load( Collections.EMPTY_MAP );
    }
    catch( IOException e )
    {
      throw new RuntimeException( e );
    }

    return (Model)lResource.getContents().get( 0 );
  }
}
