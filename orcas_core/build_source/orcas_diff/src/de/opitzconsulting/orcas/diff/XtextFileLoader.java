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
  private static Map<Object,Object> loadOptions;
  private static XtextResourceSet resourceSet;
  private static int counter;

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
    XtextFileLoader.initXtext();
    Model lReturn = new ModelImpl();

    for( File lFile : pModelFiles )
    {
      lReturn.getModel_elements().addAll( loadModelDslFile( lFile, pParameters ).getModel_elements() );
    }

    return lReturn;
  }

  private static Model loadModelDslFile( File pFile, Parameters pParameters )
  {
    Resource lResource = resourceSet.createResource( URI.createURI( "dummy:/dummy" + counter++ + ".orcasdsl" ) );
    try
    {
      FileInputStream lInputStream = new FileInputStream( pFile );
      lResource.load( lInputStream, loadOptions );
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

  public static void initXtext()
  {
    Injector lInjector = new OrcasDslStandaloneSetup().createInjectorAndDoEMFRegistration();
    resourceSet = lInjector.getInstance( XtextResourceSet.class );

    resourceSet.addLoadOption( XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE );

    loadOptions = resourceSet.getLoadOptions();
    loadOptions.put( XtextResource.OPTION_ENCODING, "utf8" );
  }
}
