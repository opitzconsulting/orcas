package de.opitzconsulting.orcas.ot;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;

import de.opitzconsulting.orcasDsl.Model;

public class ModelWriter
{
  public static String getSkriptXml( Model pModel )
  {
    Resource.Factory.Registry lRegistry = Resource.Factory.Registry.INSTANCE;
    Map<String,Object> lMap = lRegistry.getExtensionToFactoryMap();
    lMap.put( "xml", new XMLResourceFactoryImpl() );

    ResourceSet lResourceSet = new ResourceSetImpl();
    Resource lResource = lResourceSet.createResource( URI.createFileURI( "*.xml" ) );

    ((XMLResource)lResource).getDefaultSaveOptions();

    lResource.getContents().add( pModel );
    try
    {
      ByteArrayOutputStream lByteArrayOutputStream = new ByteArrayOutputStream();
      lResource.save( lByteArrayOutputStream, Collections.EMPTY_MAP );

      return new String( lByteArrayOutputStream.toByteArray() );
    }
    catch( IOException e )
    {
      throw new RuntimeException( e );
    }
  }
}
