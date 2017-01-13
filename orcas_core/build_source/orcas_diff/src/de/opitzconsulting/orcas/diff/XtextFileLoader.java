package de.opitzconsulting.orcas.diff;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;

import com.google.inject.Injector;

import de.opitzconsulting.OrcasDslStandaloneSetup;
import de.opitzconsulting.orcasDsl.Model;
import de.opitzconsulting.orcasDsl.OrcasDslPackage;
import de.opitzconsulting.orcasDsl.impl.ModelImpl;

public class XtextFileLoader
{
  public static Model loadModelXml( String pFilename )
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

  public static Model loadModelDslFolder( Parameters pParameters )
  {
    return loadModelDsl( FolderHandler.getModelFiles( pParameters ), pParameters );
  }

  public static Model loadModelDsl( List<File> pModelFiles, Parameters pParameters )
  {
    Injector lInjector = new OrcasDslStandaloneSetup().createInjectorAndDoEMFRegistration();
    XtextResourceSet lResourceSet = lInjector.getInstance( XtextResourceSet.class );

    lResourceSet.addLoadOption( XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE );

    Map<Object,Object> lLoadOptions = lResourceSet.getLoadOptions();
    lLoadOptions.put( XtextResource.OPTION_ENCODING, "utf8" );

    Model lReturn = new ModelImpl();

    int lCounter = 0;

    for( File lFile : pModelFiles )
    {
      lReturn.getModel_elements().addAll( loadModelDslFile( lFile, pParameters, lResourceSet, lLoadOptions, lCounter++ ).getModel_elements() );
    }

    return lReturn;
  }

  private static Model loadModelDslFile( File pFile, Parameters pParameters, XtextResourceSet pResourceSet, Map<Object,Object> pLoadOptions, int pCounter )
  {
    Resource lResource = pResourceSet.createResource( URI.createURI( "dummy:/dummy" + pCounter + ".orcasdsl" ) );
    try
    {
      FileInputStream lInputStream = new FileInputStream( pFile );
      lResource.load( lInputStream, pLoadOptions );
      lInputStream.close();
      Model lModel = (Model)lResource.getContents().get( 0 );

      if( !lResource.getErrors().isEmpty() )
      {
        throw new RuntimeException( "parse errors" );
      }

      return lModel;
    }
    catch( Exception e )
    {
      for( Diagnostic lDiagnostic : lResource.getErrors() )
      {
        Orcas.logError( "Error in File: " + pFile, pParameters );
        Orcas.logError( lDiagnostic + "", pParameters );
      }

      throw new RuntimeException( e );
    }
  }
}
