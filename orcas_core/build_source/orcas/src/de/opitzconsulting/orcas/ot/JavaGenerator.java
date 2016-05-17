package de.opitzconsulting.orcas.ot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

public abstract class JavaGenerator
{
  private File _directory;

  protected JavaGenerator( String[] pArgs )
  {
    if( pArgs.length > 0 )
    {
      _directory = new File( pArgs[0] );

      if( pArgs.length > 1 )
      {
        ClassDataType.setTypePrefix( pArgs[1] );
      }
    }
    else
    {
      _directory = new File( "D:/2_orcas/workspace/orcas_gen/src/" + getPackageName().replace( ".", "/" ) );
    }
  }

  protected void writeJavaFile( String pFilename, DoWithWriter pDoWithWriter )
  {
    try
    {
      FileOutputStream lFileOutputStream = new FileOutputStream( new File( _directory, pFilename ) );
      PrintStream lPrintStream = new PrintStream( lFileOutputStream );

      pDoWithWriter.write( new JavaPrettyWriter( lPrintStream ) );

      lPrintStream.close();
      lFileOutputStream.close();
    }
    catch( Exception e )
    {
      throw new RuntimeException( e );
    }
  }

  protected void writePackage( JavaPrettyWriter pOut )
  {
    pOut.println( "package " + getPackageName() + ";" );
  }

  protected abstract String getPackageName();

  protected interface DoWithWriter
  {
    void write( JavaPrettyWriter pJavaPrettyWriter );
  }

  public abstract void export();
}
