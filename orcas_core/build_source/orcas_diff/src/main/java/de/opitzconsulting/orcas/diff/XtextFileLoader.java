package de.opitzconsulting.orcas.diff;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
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

public abstract class XtextFileLoader<T extends EObject>
{
  @SuppressWarnings( "unchecked" )
  public T loadModelXml( String pFilename, String pNamespaceUri, EPackage pEPackage )
  {
    EPackage.Registry.INSTANCE.put( pNamespaceUri, pEPackage );
    Resource.Factory.Registry lRegistry = Resource.Factory.Registry.INSTANCE;
    Map<String, Object> lMap = lRegistry.getExtensionToFactoryMap();
    lMap.put( "xml", new XMLResourceFactoryImpl() );

    ResourceSet lResourceSet = new ResourceSetImpl();
    Resource lResource = lResourceSet.createResource( URI.createFileURI( pFilename ) );

    ((XMLResource) lResource).getDefaultSaveOptions();

    try
    {
      lResource.load( Collections.EMPTY_MAP );
    }
    catch( IOException e )
    {
      throw new RuntimeException( e );
    }

    return (T) lResource.getContents().get( 0 );
  }

  public T loadModelDsl( List<File> pModelFiles, Parameters pParameters, Injector pInjector )
  {
    XtextResourceSet lResourceSet = pInjector.getInstance( XtextResourceSet.class );

    lResourceSet.addLoadOption( XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE );

    Map<Object, Object> lLoadOptions = lResourceSet.getLoadOptions();
    lLoadOptions.put( XtextResource.OPTION_ENCODING, "utf8" );

    T lReturn = createModelInstance();

    int lCounter = 0;

    for( File lFile : pModelFiles )
    {
      T lLoadModelDslFile = loadModelDslFile( lFile, pParameters, lResourceSet, lLoadOptions, lCounter++ );
      combinModelResults( lReturn, lLoadModelDslFile );
    }

    return lReturn;
  }

  protected abstract void combinModelResults( T pCombinedModel, T pModelPartFromSingleFile );

  protected abstract T createModelInstance();

  protected abstract String getXtextExpectedFileEnding();

  private T loadModelDslFile( File pFile, Parameters pParameters, XtextResourceSet pResourceSet, Map<Object, Object> pLoadOptions, int pCounter )
  {
    Resource lResource = pResourceSet.createResource( URI.createURI( "dummy:/dummy" + pCounter + "." + getXtextExpectedFileEnding() ) );
    try
    {
      FileInputStream lInputStream = new FileInputStream( pFile );
      lResource.load( lInputStream, pLoadOptions );
      lInputStream.close();
      @SuppressWarnings( "unchecked" )
      T lModel = (T) lResource.getContents().get( 0 );

      if( !lResource.getErrors().isEmpty() )
      {
        throw new RuntimeException( "parse errors" + pFile + " :" + lResource.getErrors().get( 0 ) );
      }

      return lModel;
    }
    catch( Exception e )
    {
      if( lResource != null )
      {
        for( Diagnostic lDiagnostic : lResource.getErrors() )
        {
          Orcas.logError( "Error in File: " + pFile, pParameters );
          Orcas.logError( lDiagnostic + "", pParameters );
        }
      }

      throw new RuntimeException( e );
    }
  }
}
