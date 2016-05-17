package de.opitzconsulting.orcas.diff;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;

import de.opitzconsulting.orcas.diff.OrcasDiff.DiffResult;
import de.opitzconsulting.orcas.sql.WrapperExecuteStatement;
import de.opitzconsulting.orcas.syex.trans.TransformSyexOrig;
import de.opitzconsulting.orcasDsl.Model;
import de.opitzconsulting.orcasDsl.OrcasDslPackage;

public class OrcasMain
{
  public static void main( String[] pArgs ) throws Exception
  {
    if( pArgs.length == 0 )
    {
      pArgs = new String[] { "oracle.jdbc.OracleDriver", "jdbc:oracle:thin:@localhost:1522:XE", "orcas_orderentry", "orcas_orderentry", "D:\\2_orcas\\orcas\\examples\\orderentry\\distribution/../../../../bin_orderentry/tmp/orcas/statics/all.xml", "D:/2_orcas/sql.sql", "true" };
    }

    JdbcConnectionHandler.initWithMainParameters( pArgs );
    InitDiffRepository.init();

    Model lSyexModel = loadModel( pArgs[4] );

    DiffResult lDiffResult = new OrcasDiff().compare( TransformSyexOrig.convertModel( lSyexModel ), new LoadIst().loadModel() );

    PrintStream lPrintStream = new PrintStream( new FileOutputStream( pArgs[5] ) );
    for( String lLine : lDiffResult.getSqlStatements() )
    {
      lPrintStream.println( lLine +
                            ";" );

      System.out.println( lLine );

      if( pArgs[6].equals( "false" ) )
      {
        new WrapperExecuteStatement( lLine, JdbcConnectionHandler.getCallableStatementProvider() ).execute();
      }
    }

    lPrintStream.close();
  }

  private static Model loadModel( String pFilename ) throws Exception
  {
    EPackage.Registry.INSTANCE.put( OrcasDslPackage.eNS_URI, OrcasDslPackage.eINSTANCE );
    Resource.Factory.Registry lRegistry = Resource.Factory.Registry.INSTANCE;
    Map<String,Object> lMap = lRegistry.getExtensionToFactoryMap();
    lMap.put( "xml", new XMLResourceFactoryImpl() );

    ResourceSet lResourceSet = new ResourceSetImpl();
    Resource lResource = lResourceSet.createResource( URI.createFileURI( pFilename ) );

    ((XMLResource)lResource).getDefaultSaveOptions();

    lResource.load( Collections.EMPTY_MAP );

    return (Model)lResource.getContents().get( 0 );
  }
}
